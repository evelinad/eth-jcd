package ch.se.inf.ethz.jcd.batman.controller.remote;

import ch.se.inf.ethz.jcd.batman.server.IRemoteVirtualDisk;

public class RemoteConnection {
	
	private Integer diskId;
	private IRemoteVirtualDisk disk;
	
	public RemoteConnection() {
		this(null, null);
	}
	
	public RemoteConnection(Integer diskId, IRemoteVirtualDisk disk) {
		this.diskId = diskId;
		this.disk = disk;
	}
	
	public Integer getDiskId () {
		return diskId;
	}
	
	public void setDiskId (Integer diskId) {
		this.diskId = diskId;
	}
	
	public IRemoteVirtualDisk getDisk() {
		return disk;
	}
	
	public void setDisk (IRemoteVirtualDisk disk) {
		this.disk = disk;
	}
}
