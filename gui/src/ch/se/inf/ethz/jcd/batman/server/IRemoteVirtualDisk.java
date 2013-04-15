package ch.se.inf.ethz.jcd.batman.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

import ch.se.inf.ethz.jcd.batman.model.Directory;
import ch.se.inf.ethz.jcd.batman.model.Entry;
import ch.se.inf.ethz.jcd.batman.model.File;
import ch.se.inf.ethz.jcd.batman.model.Path;
import ch.se.inf.ethz.jcd.batman.vdisk.VirtualDiskException;

public interface IRemoteVirtualDisk extends Remote {

	int createDisk(Path path) throws RemoteException, VirtualDiskException;

	void deleteDisk(Path path) throws RemoteException, VirtualDiskException;
	
	int loadDisk(Path path) throws RemoteException, VirtualDiskException;
	
	boolean diskExists (Path path) throws RemoteException, VirtualDiskException;
	
	void unloadDisk(int id) throws RemoteException, VirtualDiskException;
	
	long getFreeSpace(int id) throws RemoteException, VirtualDiskException;
	
	long getOccupiedSpace(int id) throws RemoteException, VirtualDiskException;
	
	long getUsedSpace(int id) throws RemoteException, VirtualDiskException;
	
	File createFile(int id, Path path, long size) throws RemoteException, VirtualDiskException;
	
	Directory createDirectory(int id, Path path) throws RemoteException, VirtualDiskException;
	
	void deleteEntry(int id, Path path) throws RemoteException, VirtualDiskException;
	
	void write(int id, File file, long fileOffset, byte[] data) throws RemoteException, VirtualDiskException;
	
	byte[] read(int id, File file, long fileOffset, int length) throws RemoteException, VirtualDiskException; 
	
	Entry[] getEntries(int id, Entry entry) throws RemoteException, VirtualDiskException;
	
	Entry[] getAllSubEntries(int id, Entry entry) throws RemoteException, VirtualDiskException;
	
	void renameEntry(int id, Entry entry, Path newPath) throws RemoteException, VirtualDiskException;
	
}
