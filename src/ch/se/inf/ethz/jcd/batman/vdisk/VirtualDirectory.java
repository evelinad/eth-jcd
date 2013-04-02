package ch.se.inf.ethz.jcd.batman.vdisk;



/* 
 * DirectoryEntry 64 bytes
 * 0x00 1  Entry Type
 * 0x01 8  Timestamp
 * 0x09 8  BlockNr of next File/Directory in this Directory
 * 0x11 8  Starting Directory/File of this directory
 * 0x19 n  Directory name
 * 
 */
public class VirtualDirectory extends VirtualDiskEntry implements IVirtualDirectory {
	
	private static final int DEFAULT_SIZE = 25;
	
	private static final int ENTRY_TYPE_POS = 0;
	private static final int TIMESTAMP_POS = 1;
	private static final int NEXT_ENTRY_POS = 9;
	private static final int FIRST_MEMBER_POS = 17;
	private static final int NAME_POS = 25;
	
	private static final byte DIRECTORY_ENTRY = 1;
	
	private IVirtualDiskSpace space;
	private IVirtualDiskEntry firstMember;
	
	protected VirtualDirectory(IVirtualDisk disk, IVirtualDirectory parent, String name) {
		super(disk, parent, name);
		space = disk.getFreeSpace(calculateSize());
		updateAll();
	}
	
	private void updateAll() {
		updateEntryType();
		updateTimestamp();
		updateNextEntry();
		updateFirstMember();
		updateName();
	}
	
	protected void updateEntryType() {
		space.seek(ENTRY_TYPE_POS);
		space.write(DIRECTORY_ENTRY);
	}
	
	protected void updateTimestamp() {
		space.seek(TIMESTAMP_POS);
		space.write(getTimestamp());
	}
	
	protected void updateNextEntry() {
		space.seek(NEXT_ENTRY_POS);
		IVirtualDiskEntry next = getNextEntry();
		space.write((next == null) ? 0 : next.getPosition());
	}
	
	protected void updateFirstMember() {
		space.seek(FIRST_MEMBER_POS);
		IVirtualDiskEntry first = getFirstMember();
		space.write((first == null) ? 0 : first.getPosition());
	
	}
	
	protected void updateName() {
		space.changeSize(calculateSize());
		space.seek(NAME_POS);
		space.write(getName().getBytes());
		space.write('\0');
	}

	@Override
	public void delete() {
		super.delete();
		//TODO
		//Delete all files which are part of this directory and afterwards free space
	}
	
	@Override
	public void addMember(IVirtualDiskEntry member) {
		member.setParent(this);
		member.setNextEntry(firstMember);
		member.setPreviousEntry(null);
		firstMember.setPreviousEntry(member);
		firstMember = member;
		updateFirstMember();
	}

	@Override
	public IVirtualDiskEntry getFirstMember() {
		return firstMember;
	}

	private long calculateSize () {
		return DEFAULT_SIZE + getName().getBytes().length + 1;
	}

	@Override
	public long getPosition() {
		return space.getVirtualDiskPosition();
	}

	@Override
	public void removeMember(IVirtualDiskEntry member) throws VirtualDiskException {
		if (member.getParent() == this) {
			if (member.getPreviousEntry() == null) {
				firstMember = member.getNextEntry();
				firstMember.setPreviousEntry(null);
				updateFirstMember();
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
	
}
