package ch.se.inf.ethz.jcd.batman.controller.remote;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Comparator;

import ch.se.inf.ethz.jcd.batman.model.Directory;
import ch.se.inf.ethz.jcd.batman.model.Entry;
import ch.se.inf.ethz.jcd.batman.model.File;
import ch.se.inf.ethz.jcd.batman.model.util.EntryNameComperator;
import ch.se.inf.ethz.jcd.batman.vdisk.VirtualDiskException;

public class RemoteConnectionUtil {
	
	public static final int BUFFER_SIZE = 32 * 1024;
	private static final Comparator<Entry> NAME_COMPARATOR = new EntryNameComperator();
	
	public static void copySingleEntry(Entry entry, RemoteConnection sourceConnection, RemoteConnection destinationConnection) throws RemoteException, VirtualDiskException {
		if (entry instanceof Directory) {
			destinationConnection.getDisk().createDirectory(destinationConnection.getDiskId(), (Directory) entry);
		} else if (entry instanceof File) {
			File file = (File) entry;
			destinationConnection.getDisk().createFile(destinationConnection.getDiskId(), file);
			long bytesToRead = file.getSize();
			long bytesRead = 0;
			while (bytesToRead > 0) {
				byte[] buffer = sourceConnection.getDisk().read(sourceConnection.getDiskId(), file,
						bytesRead,
						(int) Math.min(bytesToRead, BUFFER_SIZE));
				destinationConnection.getDisk().write(destinationConnection.getDiskId(), file, bytesRead, buffer);
				bytesToRead -= buffer.length;
				bytesRead += buffer.length;
			}
		}
	}
		
	public static AdditionalLocalDiskInformation getDiskInformation (RemoteConnection connection) throws RemoteException, VirtualDiskException {
		byte[] additionalDiskInformation = connection.getDisk().getAdditionalDiskInformation(connection.getDiskId());
		if (additionalDiskInformation == null || additionalDiskInformation.length == 0) {
			return null;
		}
		try {
			return AdditionalLocalDiskInformation.readFromByteArray(additionalDiskInformation);
		} catch (IOException | ClassNotFoundException e) {
			throw new VirtualDiskException("Invalid disk information", e);
		}
	}
	
	public static Entry[] getChildrenSorted (RemoteConnection connection, Entry entry) throws RemoteException, VirtualDiskException {
		Entry[] children = connection.getDisk().getChildren(connection.getDiskId(), entry);
		Arrays.sort(children, NAME_COMPARATOR);
		return children;
	}
	
}
