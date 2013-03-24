package ch.se.inf.ethz.jcd.batman.cli.vdisk.directory;

import java.util.ArrayList;
import java.util.List;

import ch.se.inf.ethz.jcd.batman.cli.vdisk.IVirtualDiskMember;

public abstract class Directory implements IDirectory {

	private String name;
	private long timestamp;
	private IDirectory parent;
	private IVirtualDiskMember nextMember;
	private IVirtualDiskMember firstChild;
	
	public Directory() {
		this("");
	}
	
	public Directory(String name) {
		this.name = name;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void addMember(IVirtualDiskMember member) {
		if (firstChild == null) {
			firstChild = member;
		} else {
			IVirtualDiskMember lastChild = firstChild;
			while (lastChild.getNextMember() != null) {
				lastChild = lastChild.getNextMember();
			}
			lastChild.setNextMember(member);
		}
		
	}

	@Override
	public List<IVirtualDiskMember> getMembers() {
		List<IVirtualDiskMember> members = new ArrayList<IVirtualDiskMember>();
		IVirtualDiskMember child = firstChild;
		while (child != null) {
			members.add(child);
			child = child.getNextMember();
		}
		return members;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public IDirectory getParent() {
		return parent;
	}

	@Override
	public long getTimeStamp() {
		return timestamp;
	}

	@Override
	public IVirtualDiskMember getNextMember() {
		return nextMember;
	}

	@Override
	public void setNextMember(IVirtualDiskMember next) {
		this.nextMember = next;
	}

}
