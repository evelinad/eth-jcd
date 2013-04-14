package ch.se.inf.ethz.jcd.batman.controller.remote;

import java.net.URI;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import ch.se.inf.ethz.jcd.batman.controller.DirectoryEntriesService;
import ch.se.inf.ethz.jcd.batman.controller.WorkerController;
import ch.se.inf.ethz.jcd.batman.model.Path;
import ch.se.inf.ethz.jcd.batman.server.IRemoteVirtualDisk;
import ch.se.inf.ethz.jcd.batman.server.VirtualDiskServer;
import ch.se.inf.ethz.jcd.batman.vdisk.VirtualDiskException;

public class RemoteWorkerController implements WorkerController {

	private static final String SERVICE_NAME = VirtualDiskServer.SERVICE_NAME;
	
	private URI uri;
	private Path diskPath;
	private Integer diskId;
	private IRemoteVirtualDisk remoteDisk;
	
	public RemoteWorkerController(URI uri) {
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
	public boolean connect(boolean createNewIfNecessary) {
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
				return true;
			} else {
				if (createNewIfNecessary) {
					diskId = remoteDisk.createDisk(diskPath);
					return true;
				}
			}
			return false;
		} catch (RemoteException | NotBoundException | VirtualDiskException e) {
			return false;
		}
	}

	@Override
	public boolean connected() {
		return diskId != null;
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
	public DirectoryEntriesService getDirectoryEntrysService() {
		return new RemoteDirectoryEntriesService(this);
	}

	@Override
	public Service<Long> getFreeSpaceService() {
		if (connected()) {
			return new Service<Long>() {

				@Override
				protected Task<Long> createTask() {
					return new Task<Long>() {

						@Override
						protected Long call() throws Exception {
							return remoteDisk.getFreeSpace(diskId);
						}
						
					};
				}
				
			};
		} else {
			return null;
		}
	}

	@Override
	public Service<Long> getOccupiedSpaceService() {
		if (connected()) {
			return new Service<Long>() {

				@Override
				protected Task<Long> createTask() {
					return new Task<Long>() {

						@Override
						protected Long call() throws Exception {
							return remoteDisk.getOccupiedSpace(diskId);
						}
						
					};
				}
				
			};
		} else {
			return null;
		}
	}

	@Override
	public Service<Long> getUsedSpaceService() {
		if (connected()) {
			return new Service<Long>() {

				@Override
				protected Task<Long> createTask() {
					return new Task<Long>() {

						@Override
						protected Long call() throws Exception {
							return remoteDisk.getUsedSpace(diskId);
						}
						
					};
				}
				
			};
		} else {
			return null;
		}
	}

}
