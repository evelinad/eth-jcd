package ch.se.inf.ethz.jcd.batman.vdisk;

import java.io.IOException;

import ch.se.inf.ethz.jcd.batman.vdisk.util.VirtualDiskUtil;


/**
 * Represents a directory on the virtual disk.
 * 
 * A directory contains members ({@link IVirtualDiskEntry}) which belong to the
 * directory. This includes files and subdirectories inside the directory. As
 * defined by the {@link IVirtualDiskEntry} the member list is a doubly linked
 * list.
 * 
 * The data stored in the directory is handled by a {@link IVirtualDiskSpace}. The structure of the data
 * is structured as follows:
 * 
 * 0x00 1  Entry Type
 * 0x01 8  Time stamp
 * 0x09 8  Offset position of the next directory/file which is stored in the same directory as this directory
 * 0x11 8  Offset position of the first member of this directory
 * 0x19 n  Directory name
 */
public final class VirtualDirectory extends VirtualDiskEntry implements IVirtualDirectory {
	
	/**
	 * Loads a directory located at the offset position given by position.
	 * 
	 * @param disk the disk on which the file is stored
	 * @param position the offset position in bytes of the directory
	 * @return the loaded directory
	 * @throws IOException if an I/O error occurs
	 */
	public static IVirtualDirectory load (IVirtualDisk disk, long position) throws IOException {
		VirtualDirectory virtualDirectory = new VirtualDirectory(disk);
		virtualDirectory.load(VirtualDiskSpace.load(disk, position));
		return virtualDirectory;
	}
	
	/**
	 * Loads a directory from the virtual disk, the data is located in the {@link IVirtualDiskSpace} given
	 * by the parameter space.
	 * 
	 * @param disk the disk on which the directory is stored
	 * @param space contains the data of the directory
	 * @return the loaded directory
	 * @throws IOException if an I/O error occurs
	 */
	protected static IVirtualDirectory load (IVirtualDisk disk, IVirtualDiskSpace space) throws IOException {
		VirtualDirectory virtualDirectory = new VirtualDirectory(disk);
		virtualDirectory.load(space);
		return virtualDirectory;
	}
	
	/**
	 * Creates a directory with the given name on the {@link IVirtualDisk}.
	 * 
	 * @param disk the disk on which the directory should be stored
	 * @param name the name of the newly created directory
	 * @return the newly created directory
	 * @throws IOException if an I/O error occurs
	 */
	public static IVirtualDirectory create (IVirtualDisk disk, String name) throws IOException {
		VirtualDirectory virtualDirectory = new VirtualDirectory(disk);
		virtualDirectory.create(name);
		return virtualDirectory;
	}
	
	protected static boolean isDirectory (IVirtualDiskSpace space) throws IOException {
		return space.read(ENTRY_TYPE_POS) == DIRECTORY_ENTRY;
	}
	
	private static final int ENTRY_TYP_SIZE = 1;
	private static final int TIMESTAMP_SIZE = 8;
	private static final int NEXT_ENTRY_SIZE = 8;
	private static final int FIRST_MEMBER_SIZE = 8;
	
	private static final int ENTRY_TYPE_POS = 0;
	private static final int TIMESTAMP_POS = ENTRY_TYP_SIZE;
	private static final int NEXT_ENTRY_POS = TIMESTAMP_POS + TIMESTAMP_SIZE;
	private static final int FIRST_MEMBER_POS = NEXT_ENTRY_POS + NEXT_ENTRY_SIZE;
	private static final int NAME_POS = FIRST_MEMBER_POS + FIRST_MEMBER_SIZE;
	
	private static final int DEFAULT_SIZE = NAME_POS;
	
	private static final byte DIRECTORY_ENTRY = 1;
	
	private IVirtualDiskSpace space;
	private IVirtualDiskEntry firstMember;
	private boolean firstMemberLoaded = false;
	
	private VirtualDirectory(IVirtualDisk disk) throws IOException {
		super(disk);
	}
	
	protected void create (String name) throws IOException {
		space = VirtualDiskSpace.create(getDisk(), calculateSize(name));
		super.create(name);
		firstMemberLoaded = true;
		updateAll();
	}
	
	protected void load (IVirtualDiskSpace space) throws IOException {
		this.space = space;
		checkEntryType();
		super.load();
		loadTimestamp();
		loadName();
		firstMemberLoaded = false;
	}
	
	protected void checkEntryType() throws IOException {
		space.seek(ENTRY_TYPE_POS);
		byte directoryEntry = space.read();
		if (directoryEntry != DIRECTORY_ENTRY) {
			throw new VirtualDiskException("Can't load directory, invalid entry type");
		}
	}
	
	protected void loadTimestamp() throws IOException {
		space.seek(TIMESTAMP_POS);
		setTimestamp(space.readLong());
	}
	
	protected IVirtualDiskEntry loadNextEntry() throws IOException {
		space.seek(NEXT_ENTRY_POS);
		long nextEntry = space.readLong();
		if (nextEntry == 0) {
			return null;
		} else  {
			return VirtualDiskEntry.load(getDisk(), nextEntry);
		}
	}
	
	protected void loadFirstMember() throws IOException {
		space.seek(FIRST_MEMBER_POS);
		long firstMemberPos = space.readLong();
		if (firstMemberPos != 0) {
			firstMember = VirtualDiskEntry.load(getDisk(), firstMemberPos);
			firstMember.setParent(this);
			firstMemberLoaded = true;
		}
	}
	
	protected String loadName() throws IOException {
		return loadString(space, NAME_POS);
	}
	
	private void updateAll() throws IOException {
		updateEntryType();
		updateTimestamp();
		updateNextEntry();
		updateFirstMember();
		updateName();
	}
	
	protected void updateEntryType() throws IOException {
		space.seek(ENTRY_TYPE_POS);
		space.write(DIRECTORY_ENTRY);
	}
	
	protected void updateTimestamp() throws IOException {
		space.seek(TIMESTAMP_POS);
		space.writeLong(getTimestamp());
	}
	
	protected void updateNextEntry() throws IOException {
		space.seek(NEXT_ENTRY_POS);
		IVirtualDiskEntry next = getNextEntry();
		space.writeLong((next == null) ? 0 : next.getPosition());
	}
	
	protected void updateFirstMember() throws IOException {
		space.seek(FIRST_MEMBER_POS);
		IVirtualDiskEntry first = getFirstMember();
		space.writeLong((first == null) ? 0 : first.getPosition());
	}
	
	protected void updateName() throws IOException {
		space.changeSize(calculateSize(getName()));
		saveString(space, NAME_POS, getName());
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void delete() throws IOException {
		super.delete();
		//Delete all files which are part of this directory and afterwards free directory space
		for (IVirtualDiskEntry entry : VirtualDiskUtil.getDirectoryMembers(this)) {
			entry.delete();
		}
		if (getParent() != null) {
			getParent().removeMember(this);
		}
		space.free();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addMember(IVirtualDiskEntry member) throws IOException {
		checkNameFree(this, member.getName());
		if (member.getParent() != null) {
			member.getParent().removeMember(member);
		}
		member.setParent(this);
		member.setNextEntry(getFirstMember());
		member.setPreviousEntry(null);
		if (getFirstMember() != null) {
			getFirstMember().setPreviousEntry(member);
		}
		setFirstMember(member);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeMember(IVirtualDiskEntry member) throws IOException {
		if (member.getParent() == this) {
			if (member.getPreviousEntry() == null) {
				setFirstMember(member.getNextEntry());
				if (getFirstMember() != null) {
					getFirstMember().setPreviousEntry(null);	
				}
			} else {
				member.getPreviousEntry().setNextEntry(member.getNextEntry());
				if (member.getNextEntry() != null) {
					member.getNextEntry().setPreviousEntry(member.getPreviousEntry());
				}
			}
			member.setParent(null);
			member.setNextEntry(null);
			member.setPreviousEntry(null);
		} else {
			throw new VirtualDiskException(
				"Unable to remove " + member.getName() + " from " + this.getName() + ". " + member.getName() + " is not a member of " + this.getName()
			);
		}
	}
	
	private void setFirstMember(IVirtualDiskEntry firstMember) throws IOException {
		this.firstMember = firstMember;
		this.firstMemberLoaded = true;
		updateFirstMember();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IVirtualDiskEntry getFirstMember() throws IOException {
		if (!firstMemberLoaded) {
			loadFirstMember();
		}
		return firstMember;
	}

	private long calculateSize (String name) throws IOException {
		return DEFAULT_SIZE + calculateStringSpace(name);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getPosition() {
		return space.getVirtualDiskPosition();
	}

	/**
	 * {@inheritDoc}
	 */
    @Override
    public long getTotalSize() throws IOException {
        return space.getDiskSize();
    }
	
}
