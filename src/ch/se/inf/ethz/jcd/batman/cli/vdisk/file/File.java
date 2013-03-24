package ch.se.inf.ethz.jcd.batman.cli.vdisk.file;

import ch.se.inf.ethz.jcd.batman.cli.vdisk.IVirtualDiskMember;
import ch.se.inf.ethz.jcd.batman.cli.vdisk.directory.IDirectory;

public abstract class File implements IFile {

	private String name;
	private long size;
	private long timestamp;
	private IDirectory parent;
	private IVirtualDiskMember next;
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public long getSize() {
		return size;
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
		return next;
	}

	@Override
	public void setNextMember(IVirtualDiskMember next) {
		this.next = next;
	}

}
