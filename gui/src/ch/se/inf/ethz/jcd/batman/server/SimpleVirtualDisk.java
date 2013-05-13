package ch.se.inf.ethz.jcd.batman.server;

import java.io.IOException;
import java.rmi.RemoteException;

import ch.se.inf.ethz.jcd.batman.vdisk.VirtualDiskException;

/**
 * Implementation of the IRemoteVirtualDisk. An instance of this class is used
 * on the host server.
 * 
 * This class tracks all open virtual disks and assigns them IDs. This IDs are
 * later used to identify the disk on which an operation is executed.
 * 
 */
public class SimpleVirtualDisk extends RemoteVirtualDisk implements ISimpleVirtualDisk {

	@Override
	public int createDisk(String path) throws RemoteException,
			VirtualDiskException {
		try {
			return createDiskImpl(path);
		} catch (IOException e) {
			throw new VirtualDiskException("Could not create disk at "
					+ path, e);
		}
	}

	@Override
	public int loadDisk(String path) throws RemoteException, VirtualDiskException {
		try {
			return loadDiskImpl(path);
		} catch (IOException e) {
			throw new VirtualDiskException("Could not load disk at "
					+ path, e);
		}
	}

	@Override
	public void deleteDisk(String path) throws RemoteException,
			VirtualDiskException {
		LoadedDisk disk = getPathToDiskMap().get(new java.io.File(path).toURI());
		if (disk != null && !disk.hasNoIds()) {
			throw new VirtualDiskException("Could not delete disk, disk still in use");
		}
		try {
			java.io.File diskFile = new java.io.File(path);
			if (isVirtualDisk(diskFile)) {
				if (!diskFile.delete()) {
					throw new VirtualDiskException(
							"Could not delete virtual disk at " + path);
				}
			} else {
				throw new IllegalArgumentException(path
						+ "  is not a virtual disk. File not deleted.");
			}
		} catch (IOException e) {
			throw new VirtualDiskException("Could not delete virtual disk at "
					+ path, e);
		}
	}

	@Override
	public boolean diskExists(String path) throws RemoteException,
			VirtualDiskException {
		return isVirtualDisk(new java.io.File(path));
	}
}
