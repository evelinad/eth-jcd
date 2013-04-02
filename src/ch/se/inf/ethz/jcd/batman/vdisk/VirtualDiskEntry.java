package ch.se.inf.ethz.jcd.batman.vdisk;

public abstract class VirtualDiskEntry implements IVirtualDiskEntry {

	private IVirtualDisk disk;
	private IVirtualDirectory parent;
	private IVirtualDiskEntry previous;
	private IVirtualDiskEntry next;
	private String name;
	private long timestamp;
	private FileState state;
	
	public VirtualDiskEntry (IVirtualDisk disk, IVirtualDirectory parent, String name) {
		this.disk = disk;
		this.parent = parent;
		this.name = name;
		state = FileState.CREATED;
	}
	
	@Override
	public void setName(String name) {
		this.name = name;
		updateName();
	}
	
	protected abstract void updateName ();
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public IVirtualDirectory getParent() {
		return parent;
	}

	@Override
	public void setParent(IVirtualDirectory parent) {
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
	public void setNextEntry(IVirtualDiskEntry next) {
		this.next = next;
		updateNextEntry();
	}
	
	protected abstract void updateNextEntry();
	
	@Override
	public long getTimestamp() {
		return timestamp;
	}

	@Override
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
		updateTimestamp();
	}
	
	protected abstract void updateTimestamp ();
	
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
