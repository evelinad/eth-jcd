package ch.se.inf.ethz.jcd.batman.server;

import java.rmi.RemoteException;

import ch.se.inf.ethz.jcd.batman.model.Path;
import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualDisk;
import ch.se.inf.ethz.jcd.batman.vdisk.VirtualDiskException;
import ch.se.inf.ethz.jcd.batman.vdisk.impl.VirtualDisk;

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
	public int createDisk(Path path) throws RemoteException,
			VirtualDiskException {
		try {
			IVirtualDisk newDisk = VirtualDisk.create(path.getPath());
			int id = getNextId();
			getDiskMap().put(id, newDisk);
			return id;
		} catch (Exception e) {
			throw new VirtualDiskException("Could not create disk at "
					+ path.getPath(), e);
		}
	}

	@Override
	public int loadDisk(Path path) throws RemoteException, VirtualDiskException {
		try {
			IVirtualDisk loadedDisk = VirtualDisk.load(path.getPath());
			int id = getNextId();
			getDiskMap().put(id, loadedDisk);
			return id;
		} catch (Exception e) {
			throw new VirtualDiskException("Could not load disk at "
					+ path.getPath(), e);
		}
	}

	@Override
	public void deleteDisk(Path path) throws RemoteException,
			VirtualDiskException {
		try {
			java.io.File diskFile = new java.io.File(path.getPath());
			if (isVirtualDisk(diskFile)) {
				if (!diskFile.delete()) {
					throw new VirtualDiskException(
							"Could not delete virtual disk at " + path);
				}
			} else {
				throw new IllegalArgumentException(path
						+ "  is not a virtual disk. File not deleted.");
			}
		} catch (Exception e) {
			throw new VirtualDiskException("Could not delete virtual disk at "
					+ path, e);
		}
	}

	@Override
	public boolean diskExists(Path path) throws RemoteException,
			VirtualDiskException {
		return isVirtualDisk(new java.io.File(path.getPath()));
	}
}
