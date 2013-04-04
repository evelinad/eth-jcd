package ch.se.inf.ethz.jcd.batman.vdisk;

import java.io.IOException;

public abstract class VirtualDiskEntry implements IVirtualDiskEntry {

	private IVirtualDisk disk;
	private IVirtualDirectory parent;
	private IVirtualDiskEntry previous;
	private IVirtualDiskEntry next;
	private String name;
	private long timestamp;
	private FileState state;
	
	public VirtualDiskEntry (IVirtualDisk disk, String name) throws IOException {
		this.disk = disk;
		this.name = name;
		state = FileState.CREATED;
	}
	
	/**
	 * Checks if the name is already in use in the given directory. If so an exception is thrown.
	 * 
	 * @param directory the directory to check
	 * @param name the name to check
	 * @throws VirtualDiskException if the name is already in use
	 */
	protected void checkNameFree (IVirtualDirectory parent, String name) throws VirtualDiskException {
		IVirtualDiskEntry[] directoryEntrys = VirtualDiskUtil.getDirectoryEntrys(parent);
		for (IVirtualDiskEntry entry : directoryEntrys) {
			if (entry.getName().equals(name)) {
				throw new VirtualDiskException("Name already in use");
			}
		}
	}
	
	@Override
	public void setName(String name) throws IOException {
		checkNameFree(parent, name);
		this.name = name;
		updateName();	
		
	}
	
	protected abstract void updateName () throws IOException;
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public IVirtualDirectory getParent() {
		return parent;
	}

	@Override
	public void setParent(IVirtualDirectory parent) throws IOException {
		this.parent = parent;
	}
	
	@Override
	public IVirtualDiskEntry getPreviousEntry() {
		return previous;
	}

	@Override
	public void setPreviousEntry(IVirtualDiskEntry previous) {
		this.previous = previous;
	}

	@Override
	public IVirtualDiskEntry getNextEntry() {
		return next;
	}
	
	@Override
	public void setNextEntry(IVirtualDiskEntry next) throws IOException {
		this.next = next;
		updateNextEntry();
	}
	
	protected abstract void updateNextEntry() throws IOException;
	
	@Override
	public long getTimestamp() {
		return timestamp;
	}

	@Override
	public void setTimestamp(long timestamp) throws IOException {
		this.timestamp = timestamp;
		updateTimestamp();
	}
	
	protected abstract void updateTimestamp () throws IOException;
	
	public IVirtualDisk getDisk () {
		return disk;
	}
	
	@Override
	public void delete () {
		state = FileState.DELETED;
	}

	@Override
	public boolean exists () {
		return state == FileState.CREATED;
	}
}
