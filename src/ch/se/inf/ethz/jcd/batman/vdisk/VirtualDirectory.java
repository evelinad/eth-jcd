package ch.se.inf.ethz.jcd.batman.vdisk;

import java.io.IOException;



/* 
 * DirectoryEntry 64 bytes
 * 0x00 1  Entry Type
 * 0x01 8  time stamp
 * 0x09 8  BlockNr of next File/Directory in this Directory
 * 0x11 8  Starting Directory/File of this directory
 * 0x19 n  Directory name
 */
public class VirtualDirectory extends VirtualDiskEntry implements IVirtualDirectory {
	
	public static IVirtualDirectory load (IVirtualDisk disk, long position) throws IOException {
		VirtualDirectory virtualDirectory = new VirtualDirectory(disk);
		virtualDirectory.load(VirtualDiskSpace.load(disk, position));
		return virtualDirectory;
	}
	
	protected static IVirtualDirectory load (IVirtualDisk disk, IVirtualDiskSpace space) throws IOException {
		VirtualDirectory virtualDirectory = new VirtualDirectory(disk);
		virtualDirectory.load(space);
		return virtualDirectory;
	}
	
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
		return VirtualDiskEntry.load(getDisk(), nextEntry);
	}
	
	protected void loadFirstMember() throws IOException {
		space.seek(FIRST_MEMBER_POS);
		long firstMember = space.readLong();
		this.firstMember = VirtualDiskEntry.load(getDisk(), firstMember);
		this.firstMemberLoaded = true;
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
		space.write(getTimestamp());
	}
	
	protected void updateNextEntry() throws IOException {
		space.seek(NEXT_ENTRY_POS);
		IVirtualDiskEntry next = getNextEntry();
		space.write((next == null) ? 0 : next.getPosition());
	}
	
	protected void updateFirstMember() throws IOException {
		space.seek(FIRST_MEMBER_POS);
		IVirtualDiskEntry first = getFirstMember();
		space.write((first == null) ? 0 : first.getPosition());
	}
	
	protected void updateName() throws IOException {
		space.changeSize(calculateSize(getName()));
		saveString(space, NAME_POS, getName());
	}
	
	@Override
	public void delete() {
		super.delete();
		//TODO
		//Delete all files which are part of this directory and afterwards free space
	}
	
	@Override
	public void addMember(IVirtualDiskEntry member) throws IOException {
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
				member.getNextEntry().setPreviousEntry(member.getPreviousEntry());
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

	@Override
	public long getPosition() {
		return space.getVirtualDiskPosition();
	}
	
}
