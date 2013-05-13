package ch.se.inf.ethz.jcd.batman.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

import ch.se.inf.ethz.jcd.batman.model.Entry;
import ch.se.inf.ethz.jcd.batman.model.File;
import ch.se.inf.ethz.jcd.batman.vdisk.VirtualDiskException;

public interface IRemoteDiskClient extends Remote {

	void entryAdded(Entry entry) throws RemoteException, VirtualDiskException;

	void entryDeleted(Entry entry) throws RemoteException, VirtualDiskException;

	void entryChanged(Entry oldEntry, Entry newEntry) throws RemoteException,
			VirtualDiskException;

	void entryCopied(Entry sourceEntry, Entry destinationEntry)
			throws RemoteException, VirtualDiskException;

	void writeToEntry(File file, long fileOffset, byte[] data)
			throws RemoteException, VirtualDiskException;

}
