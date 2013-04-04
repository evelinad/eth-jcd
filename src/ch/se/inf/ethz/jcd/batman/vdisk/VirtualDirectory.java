package ch.se.inf.ethz.jcd.batman.vdisk;

import java.io.IOException;



/* 
 * DirectoryEntry 64 bytes
 * 0x00 1  Entry Type
 * 0x01 8  time stamp
 * 0x09 8  BlockNr of next File/Directory in this Directory
 * 0x11 8  Starting Directory/File of this directory
 * 0x19 n  Directory name
 * 
 */
public class VirtualDirectory extends VirtualDiskEntry implements IVirtualDirectory {
	
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
	
	protected VirtualDirectory(IVirtualDisk disk, String name) throws IOException {
		super(disk, name);
		space = new VirtualDiskSpace(disk, calculateSize());
		updateAll();
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
		space.changeSize(calculateSize());
		space.seek(NAME_POS);
		space.write(getName().getBytes());
		space.write(String.valueOf('\0').getBytes());
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
		member.setNextEntry(firstMember);
		member.setPreviousEntry(null);
		firstMember.setPreviousEntry(member);
		firstMember = member;
		updateFirstMember();	
	}
	
	@Override
	public void removeMember(IVirtualDiskEntry member) throws IOException {
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
	
}
