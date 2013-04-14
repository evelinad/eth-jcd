package ch.se.inf.ethz.jcd.batman.controller.remote;

import java.net.URI;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javafx.concurrent.Task;

import ch.se.inf.ethz.jcd.batman.controller.ConnectionException;
import ch.se.inf.ethz.jcd.batman.controller.TaskController;
import ch.se.inf.ethz.jcd.batman.model.Directory;
import ch.se.inf.ethz.jcd.batman.model.Entry;
import ch.se.inf.ethz.jcd.batman.model.File;
import ch.se.inf.ethz.jcd.batman.model.Path;
import ch.se.inf.ethz.jcd.batman.server.IRemoteVirtualDisk;
import ch.se.inf.ethz.jcd.batman.server.VirtualDiskServer;
import ch.se.inf.ethz.jcd.batman.vdisk.VirtualDiskException;

public class RemoteTaskController implements TaskController {

	private static final String SERVICE_NAME = VirtualDiskServer.SERVICE_NAME;
	
	private URI uri;
	private Path diskPath;
	private Integer diskId;
	private IRemoteVirtualDisk remoteDisk;
	
	public RemoteTaskController(URI uri) {
		this.uri = uri;
		this.diskPath = new Path(uri.getQuery());
	}

	public Integer getDiskId () {
		return diskId;
	}
	
	public IRemoteVirtualDisk getRemoteDisk () {
		return remoteDisk;
	}
	
	@Override
	public void connect(boolean createNewIfNecessary) throws ConnectionException {
		try {
			Registry registry;
			if (uri.getPort() == -1) {
				registry = LocateRegistry.getRegistry(uri.getHost());
			} else {
				registry = LocateRegistry.getRegistry(uri.getHost(), uri.getPort());
			}
			remoteDisk = (IRemoteVirtualDisk) registry.lookup(SERVICE_NAME);
			if (remoteDisk.diskExists(diskPath)) {
				diskId = remoteDisk.loadDisk(diskPath);
			} else {
				if (createNewIfNecessary) {
					diskId = remoteDisk.createDisk(diskPath);
				} else {
					throw new ConnectionException("Disk does not exist.");
				}
			}
		} catch (RemoteException | NotBoundException | VirtualDiskException e) {
			throw new ConnectionException(e);
		}
	}

	@Override
	public boolean connected() {
		return diskId != null;
	}
	
	private void checkIsConnected() {
		if (!connected()) {
			throw new IllegalStateException("Controller is not connected");
		}
	}
	
	@Override
	public void close() {
		if (diskId != null) {
			try {
				remoteDisk.unloadDisk(diskId);
				diskId = null;
				remoteDisk = null;
			} catch (RemoteException | VirtualDiskException e) { }
		}
	}

	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}
	
	@Override
	public Task<Entry[]> getDirectoryEntrysTask(final Directory directory) {
		checkIsConnected();
		return new Task<Entry[]>() {

			@Override
			protected Entry[] call() throws Exception {
				checkIsConnected();
				return remoteDisk.getEntrys(diskId, directory);
			}
			
		};
	}

	@Override
	public Task<Long> getFreeSpaceTask() {
		checkIsConnected();
		return new Task<Long>() {

			@Override
			protected Long call() throws Exception {
				checkIsConnected();
				return remoteDisk.getFreeSpace(diskId);
			}
			
		};
	}

	@Override
	public Task<Long> getOccupiedSpaceTask() {
		checkIsConnected();
		return new Task<Long>() {

			@Override
			protected Long call() throws Exception {
				checkIsConnected();
				return remoteDisk.getOccupiedSpace(diskId);
			}
			
		};
	}

	@Override
	public Task<Long> getUsedSpaceTask() {
		checkIsConnected();
		return new Task<Long>() {

			@Override
			protected Long call() throws Exception {
				checkIsConnected();
				return remoteDisk.getUsedSpace(diskId);
			}
			
		};
	}

	@Override
	public Task<File> createFileTask(final File file) {
		checkIsConnected();
		return new Task<File>() {

			@Override
			protected File call() throws Exception {
				checkIsConnected();
				return remoteDisk.createFile(diskId, file.getPath(), file.getSize());
			}
			
		};
	}

	@Override
	public Task<Directory> createDirectoryTask(final Directory directory) {
		checkIsConnected();
		return new Task<Directory>() {

			@Override
			protected Directory call() throws Exception {
				checkIsConnected();
				return remoteDisk.createDirectory(diskId, directory.getPath());
			}
			
		};
	}

}
