package ch.se.inf.ethz.jcd.batman.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

import ch.se.inf.ethz.jcd.batman.model.Directory;
import ch.se.inf.ethz.jcd.batman.model.Entry;
import ch.se.inf.ethz.jcd.batman.model.File;
import ch.se.inf.ethz.jcd.batman.model.Path;

public interface IRemoteVirtualDisk extends Remote {

	int createDisk(Path path) throws RemoteException;
	
	int loadDisk(Path path) throws RemoteException;
	
	void unloadDisk(int id) throws RemoteException;
	
	long getFreeSpace(int id) throws RemoteException;
	
	long getOccupiedSpace(int id) throws RemoteException;
	
	long getUsedSpace(int id) throws RemoteException;
	
	File createFile(int id, Path path, long size) throws RemoteException;
	
	Directory createDirectory(int id, Path path) throws RemoteException;
	
	void deleteEntry(int id, Path path) throws RemoteException;
	
	int write(int id, File file, long fileOffset, byte[] data) throws RemoteException;
	
	byte[] read(int id, File file, long fileOffset) throws RemoteException; 
	
	Entry[] getEntrys(int id, Directory directory) throws RemoteException;
	
}
