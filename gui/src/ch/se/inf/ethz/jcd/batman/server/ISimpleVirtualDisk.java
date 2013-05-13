package ch.se.inf.ethz.jcd.batman.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

import ch.se.inf.ethz.jcd.batman.vdisk.VirtualDiskException;

/**
 * Interface for a remote virtual disk represented by a path on the remote host.
 * 
 * This interface is used to communicate with a remote virtual disk by means of
 * RMI.
 * 
 * This interface is a facade (as in the Facade Design Pattern) for the
 * underlying subsystems used on the remote host.
 * 
 * Most of the methods expect an ID or return one. This ID represents a specific
 * open virtual disk on the remote host.
 * 
 */
public interface ISimpleVirtualDisk extends IRemoteVirtualDisk, Remote {

	/**
	 * Creates a virtual disk on the remote host.
	 * 
	 * @param path
	 *            location where the virtual disk should be created on the
	 *            remote host
	 * @return ID of the virtual disk that was created and loaded
	 * @throws RemoteException
	 * @throws VirtualDiskException
	 */
	int createDisk(String path) throws RemoteException, VirtualDiskException;

	/**
	 * Deletes a virtual disk on the remote host.
	 * 
	 * @param path
	 *            Path to the virtual disk that should be deleted.
	 * @throws RemoteException
	 * @throws VirtualDiskException
	 */
	void deleteDisk(String path) throws RemoteException, VirtualDiskException;

	/**
	 * Loads the given path as a virtual disk.
	 * 
	 * @param path
	 *            location of the virtual disk to load on the host system
	 * @return ID of the loaded virtual disk
	 * @throws RemoteException
	 * @throws VirtualDiskException
	 */
	int loadDisk(String path) throws RemoteException, VirtualDiskException;

	/**
	 * Indicates if the given path represents a virtual disk.
	 * 
	 * @param path
	 *            location of the virtual disk to check on the host system
	 * @return true if the virtual disk exists, otherwise false
	 * @throws RemoteException
	 * @throws VirtualDiskException
	 */
	boolean diskExists(String path) throws RemoteException,
			VirtualDiskException;

}
