package ch.se.inf.ethz.jcd.batman.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

import ch.se.inf.ethz.jcd.batman.model.Directory;
import ch.se.inf.ethz.jcd.batman.model.Entry;
import ch.se.inf.ethz.jcd.batman.model.File;
import ch.se.inf.ethz.jcd.batman.model.Path;
import ch.se.inf.ethz.jcd.batman.vdisk.VirtualDiskException;

public interface IRemoteVirtualDisk extends Remote {

	/**
	 * Unloads the virtual disk represented by the given ID.
	 * 
	 * A disk unload will also close the disk on the remote host.
	 * 
	 * @param id
	 *            the ID representing the loaded virtual disk that should be
	 *            unloaded
	 * @throws RemoteException
	 * @throws VirtualDiskException
	 */
	void unloadDisk(int id) throws RemoteException, VirtualDiskException;

	/**
	 * Returns the amount of free space inside the virtual disk.
	 * 
	 * This value does not indicate how much data the disk can hold in the
	 * future as the virtual disk may be elastic and would resize itself to fit
	 * more data inside.
	 * 
	 * @param id
	 *            the ID representing a loaded virtual disk
	 * @return the amount of free space in bytes
	 * @throws RemoteException
	 * @throws VirtualDiskException
	 */
	long getFreeSpace(int id) throws RemoteException, VirtualDiskException;

	/**
	 * Returns the amount of occupied space inside the virtual disk.
	 * 
	 * This represents the amount of stored bytes inside the virtual disk.
	 * 
	 * @param id
	 *            the ID representing a loaded virtual disk
	 * @return the amount in bytes
	 * @throws RemoteException
	 * @throws VirtualDiskException
	 */
	long getOccupiedSpace(int id) throws RemoteException, VirtualDiskException;

	/**
	 * Returns the amount of bytes the virtual disk takes on the disk of the
	 * host system.
	 * 
	 * @param id
	 *            the ID representing a loaded virtual disk
	 * @return the amount in bytes
	 * @throws RemoteException
	 * @throws VirtualDiskException
	 */
	long getUsedSpace(int id) throws RemoteException, VirtualDiskException;

	/**
	 * Creates an empty file on the remote virtual disk.
	 * 
	 * @param id
	 *            the ID representing a loaded virtual disk
	 * @param file
	 *            the file that should be created
	 * @throws RemoteException
	 * @throws VirtualDiskException
	 */
	void createFile(int id, File file) throws RemoteException,
			VirtualDiskException;

	/**
	 * Creates a directory on the remote virtual disk.
	 * 
	 * @param id
	 *            the ID representing a loaded virtual disk
	 * @param directory
	 *            the directory to create
	 * @throws RemoteException
	 * @throws VirtualDiskException
	 */
	void createDirectory(int id, Directory directory) throws RemoteException,
			VirtualDiskException;

	/**
	 * Deletes a virtual disk entry (file or directory).
	 * 
	 * @param id
	 *            the ID representing a loaded virtual disk
	 * @param entry
	 *            the entry to delete
	 * @throws RemoteException
	 * @throws VirtualDiskException
	 */
	void deleteEntry(int id, Entry entry) throws RemoteException,
			VirtualDiskException;

	/**
	 * Writes the given data into the given file.
	 * 
	 * @param id
	 *            the ID representing a loaded virtual disk
	 * @param file
	 *            the file to write into
	 * @param fileOffset
	 *            the offset inside the file from where to start writing. The
	 *            offset is in bytes and inclusive
	 * @param data
	 *            the data to write
	 * @throws RemoteException
	 * @throws VirtualDiskException
	 */
	void write(int id, File file, long fileOffset, byte[] data)
			throws RemoteException, VirtualDiskException;

	/**
	 * Reads from a file
	 * 
	 * @param id
	 *            the ID representing a loaded virtual disk
	 * @param file
	 *            the file to read from
	 * @param fileOffset
	 *            the offset inside the file from where to start reading. The
	 *            offset is in bytes and inclusive
	 * @param length
	 *            the amount of bytes to read
	 * @return the read data
	 * @throws RemoteException
	 * @throws VirtualDiskException
	 */
	byte[] read(int id, File file, long fileOffset, int length)
			throws RemoteException, VirtualDiskException;

	/**
	 * Returns all child entries for the given entry.
	 * 
	 * This will (for now) only return child entries if the given entry is a
	 * directory.
	 * 
	 * @param id
	 *            the ID representing a loaded virtual disk
	 * @param entry
	 *            the parent entry of the returned children
	 * @return array of children of the given parent entry
	 * @throws RemoteException
	 * @throws VirtualDiskException
	 */
	Entry[] getChildren(int id, Entry entry) throws RemoteException,
			VirtualDiskException;

	/**
	 * Returns all entries below the given entry in the virtual disk entries
	 * tree.
	 * 
	 * @param id
	 *            the ID representing a loaded virtual disk
	 * @param entry
	 *            the parent entry
	 * @return array of all children below the given parent entry in the tree
	 * @throws RemoteException
	 * @throws VirtualDiskException
	 */
	Entry[] getAllChildrenBelow(int id, Entry entry) throws RemoteException,
			VirtualDiskException;

	/**
	 * Moves the given entry to the new location.
	 * 
	 * This can be used to move entries around or/and rename them.
	 * 
	 * @param id
	 *            the ID representing a loaded virtual disk
	 * @param entry
	 *            the entry to move
	 * @param newPath
	 *            the destination of the move
	 * @throws RemoteException
	 * @throws VirtualDiskException
	 */
	void moveEntry(int id, Entry entry, Entry newEntry) throws RemoteException,
			VirtualDiskException;

	/**
	 * Checks if the given paths exist.
	 * 
	 * @param id
	 *            the ID representing a loaded virtual disk
	 * @param entryPaths
	 *            paths to check if they exist
	 * @return indicates if the path with the same index exists. True if it
	 *         exists, otherwise false
	 * @throws RemoteException
	 * @throws VirtualDiskException
	 */
	boolean[] entriesExist(int id, Path[] entryPaths) throws RemoteException,
			VirtualDiskException;

	/**
	 * Returns the entries representing the given paths.
	 * 
	 * @param id
	 *            the ID representing a loaded virtual disk
	 * @param entryPaths
	 *            an array of paths to get the entries for
	 * @return array of entries representing the path with the same array index
	 * @throws RemoteException
	 * @throws VirtualDiskException
	 */
	Entry[] getEntries(int id, Path[] entryPaths) throws RemoteException,
			VirtualDiskException;

	/**
	 * Copies the specified entry to the new destination.
	 * 
	 * @param id
	 *            the ID representing a loaded virtual disk
	 * @param source
	 *            the source entry
	 * @param destination
	 *            the destination path, to which the source entry should be
	 *            copied
	 * @throws RemoteException
	 * @throws VirtualDiskException
	 */
	void copyEntry(int id, Entry source, Entry destination)
			throws RemoteException, VirtualDiskException;

	/**
	 * Searches for the given term inside the given parents.
	 * 
	 * @param id
	 *            the ID representing a loaded virtual disk
	 * @param term
	 *            the search term
	 * @param isRegex
	 *            true if term is a regex, otherwise false
	 * @param checkFiles
	 *            true if files should be checked
	 * @param checkFolders
	 *            true if folders should be checked
	 * @param isCaseSensitive
	 *            true if term is case sensitive
	 * @param checkChildren
	 *            true if children and subfolders should be checked
	 * @param parents
	 *            list of parents to search in
	 * @return array of found entries
	 */
	Entry[] search(int id, String term, boolean isRegex, boolean checkFiles,
			boolean checkFolders, boolean isCaseSensitive,
			boolean checkChildren, Entry[] parents) throws RemoteException,
			VirtualDiskException;

	byte[] getAdditionalDiskInformation(int id) throws RemoteException,
			VirtualDiskException;

	void saveAdditionalDiskInformation(int id, byte[] information)
			throws RemoteException, VirtualDiskException;

	Entry updateLastModified(int id, Entry entry, long newTimestamp)
			throws RemoteException, VirtualDiskException;

	void registerClient(int id, IRemoteDiskClient client)
			throws RemoteException;

	void unregisterClient(int id, IRemoteDiskClient client)
			throws RemoteException;

	void acquireLock(int id) throws RemoteException, InterruptedException;

	void releaseLock(int id) throws RemoteException;
}
