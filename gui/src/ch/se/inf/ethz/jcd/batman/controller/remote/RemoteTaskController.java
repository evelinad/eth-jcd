package ch.se.inf.ethz.jcd.batman.controller.remote;

import java.beans.DesignMode;
import java.net.URI;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.sun.javafx.collections.transformation.SortedList;

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
	
	/**
	 * Sort order from smallest to biggest is: File -> Entry -> Directory
	 */
	private static final class FileBeforeDirectoryComparator implements Comparator<Entry> {

		@Override
		public int compare(Entry entry1, Entry entry2) {
			if (entry1 instanceof File) {
				if (entry2 instanceof File) {
					return 0;
				} else {
					return -1;
				}
			} else if (entry1 instanceof Directory) {
				if (entry2 instanceof Directory) {
					return 0;
				} else {
					return 1;
				}
			} else {
				return 0;
			}
		}
		
	}
	
	/**
	 * Sort order from smallest to biggest is: Directory -> File
	 */
	private static final class DirectoryBeforeFileComparator implements Comparator<java.io.File> {

		@Override
		public int compare(java.io.File file1, java.io.File file2) {
			if (file1.isDirectory()) {
				if (file2.isDirectory()) {
					return 0;
				} else {
					return -1;
				}
			} else if (file1.isFile()) {
				if (file2.isFile()) {
					return 0;
				} else {
					return 1;
				}
			} else {
				if (file2.isDirectory()) {
					return 1;
				} else if (file2.isFile()) {
					return -1;
				} else {
					return 0;
				}
			}
		}
		
	}
	
	private static final String SERVICE_NAME = VirtualDiskServer.SERVICE_NAME;
	
	private final Comparator<Entry> fileBeforeDirectoryComp = new FileBeforeDirectoryComparator();
	private final Comparator<java.io.File> directoryBeforeFileComp = new DirectoryBeforeFileComparator();
	
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
	public Task<Void> createConnectTask(final boolean createNewIfNecessary) {
		if (isConnected()) {
			throw new IllegalStateException("Already connected.");
		}
		return new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				if (isConnected()) {
					throw new IllegalStateException("Already connected.");
				}
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
				return null;
			}
			
		};
	}

	@Override
	public boolean isConnected() {
		return diskId != null;
	}
	
	private void checkIsConnected() {
		if (!isConnected()) {
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
	public Task<Entry[]> createDirectoryEntriesTask(final Directory directory) {
		checkIsConnected();
		return new Task<Entry[]>() {

			@Override
			protected Entry[] call() throws Exception {
				checkIsConnected();
				return remoteDisk.getEntries(diskId, directory);
			}
			
		};
	}

	@Override
	public Task<Long> createFreeSpaceTask() {
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
	public Task<Long> createOccupiedSpaceTask() {
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
	public Task<Long> createUsedSpaceTask() {
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
	public Task<Void> createFileTask(final File file) {
		checkIsConnected();
		return new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				checkIsConnected();
				remoteDisk.createFile(diskId, file.getPath(), file.getSize());
				return null;
			}
			
		};
	}

	@Override
	public Task<Void> createDirectoryTask(final Directory directory) {
		checkIsConnected();
		return new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				checkIsConnected();
				remoteDisk.createDirectory(diskId, directory.getPath());
				return null;
			}
			
		};
	}

	
	
	@Override
	public Task<Void> createDeleteEntriesTask(final Entry[] entries) {
		checkIsConnected();
		return new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				checkIsConnected();
				updateTitle("Deleting entries");
				
				updateMessage("Discovering items");
				SortedSet<Entry> subEntries = new TreeSet<Entry>(fileBeforeDirectoryComp);
				for (int i = 0; i < entries.length; i++) {
					subEntries.addAll(Arrays.asList(remoteDisk.getAllSubEntries(diskId, entries[i])));
				}
				
				int totalEntries = subEntries.size();
				int currentEntryNumber = 1;
				updateProgress(0, totalEntries);
				for (Entry entry : subEntries) {
					updateMessage("Deleting entry " + currentEntryNumber + " of " + totalEntries);
					remoteDisk.deleteEntry(diskId, entry.getPath());
					currentEntryNumber++;
					updateProgress(currentEntryNumber, totalEntries);
				}
				return null;
			}
			
		};
	}

	private void checkEntriesAlreadyExist (final Path[] entries) throws VirtualDiskException, RemoteException {
		boolean[] entriesExist = remoteDisk.entriesExist(diskId, entries);
		for (int i = 0; i < entries.length; i++) {
			if (entriesExist[i]) {
				throw new VirtualDiskException("Destination entry " + entries[i].getPath() + " already exists");
			}
		}
	}
	
	@Override
	public Task<Void> createMoveTask(final Entry[] sourceEntries, final Path[] destinationPaths) {
		checkIsConnected();
		if (sourceEntries.length != destinationPaths.length) {
			throw new IllegalArgumentException("Source and destination arrays have to be the same size");
		}
		return new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				checkIsConnected();
				updateTitle("Moving entries");
				//check if destination paths not already exist
				checkEntriesAlreadyExist(destinationPaths);
				//move entries
				int totalEntriesToMove = sourceEntries.length;
				for (int i = 0; i < totalEntriesToMove; i++) {
					updateProgress(i, totalEntriesToMove);
					updateMessage("Moving entry " + sourceEntries[i].getPath() + " to " + destinationPaths[i]);
					remoteDisk.renameEntry(diskId, sourceEntries[i], destinationPaths[i]);
				}
				updateProgress(totalEntriesToMove, totalEntriesToMove);
				return null;
			}
			
		};
	}
	
	private void getAllSubEntries(java.io.File file, List<java.io.File> subEntries) {
		if (file.exists()) {
			subEntries.add(file);
			if (file.isDirectory()) {
				for (java.io.File subFile : file.listFiles()) {
					getAllSubEntries(subFile, subEntries);
				}
			}
		}
	}
	
	@Override
	public Task<Void> createImportTask(final String[] sourcePaths, final Path[] destinationPaths) {
		checkIsConnected();
		if (sourcePaths.length != destinationPaths.length) {
			throw new IllegalArgumentException("Source and destination arrays have to be the same size");
		}
		return new Task<Void>() {
			
			private String getFilePath (java.io.File file) {
				return file.getPath().replaceAll("\\\\", "/");
			}
			
			private void importFile (java.io.File file, String destination) throws RemoteException, VirtualDiskException {
				updateMessage("Importing entry " + file.toString() + " to " + destination);
				if (file.isDirectory()) {
					remoteDisk.createDirectory(diskId, new Path(destination));
				} else if (file.isFile()) {
					remoteDisk.createFile(diskId, new Path(destination), file.length());
					//TODO import data.....
				}
			}
			
			@Override
			protected Void call() throws Exception {
				checkIsConnected();
				updateTitle("Import entries");
				//check if destination paths not already exist
				checkEntriesAlreadyExist(destinationPaths);
				//check how many and which files need to be imported
				@SuppressWarnings("unchecked")
				List<java.io.File>[] importFiles = new List[sourcePaths.length];
				long totalEntriesToImport = 0;
				for (int i = 0; i < sourcePaths.length; i++) {
					importFiles[i] = new LinkedList<java.io.File>();
					getAllSubEntries(new java.io.File(sourcePaths[i]), importFiles[i]);
					totalEntriesToImport += importFiles[i].size();
				}
				//import all entries
				long entriesImported = 0;
				for (int i = 0; i < sourcePaths.length; i++) {
					java.io.File baseFile = importFiles[i].get(0);
					String baseFilePath = getFilePath(baseFile);
					for (java.io.File file : importFiles[i]) {
						String entryPath = getFilePath(file);
						String destination = destinationPaths[i] + entryPath.substring(baseFilePath.length(), entryPath.length());
						updateProgress(entriesImported, totalEntriesToImport);
						importFile(file, destination);
						entriesImported++;
					}
				}
				updateProgress(entriesImported, totalEntriesToImport);
				return null;
			}
			
		};
	}

	@Override
	public Task<Void> createExportTask(final Entry[] sourceEntries, final String[] destinationPaths) {
		checkIsConnected();
		return new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				checkIsConnected();
				//TODO
				throw new UnsupportedOperationException();
			}
			
		};
	}

	@Override
	public Task<Void> createCopyTask(final Entry[] sourceEntries, final Path[] destinationPaths) {
		checkIsConnected();
		return new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				checkIsConnected();
				//TODO
				throw new UnsupportedOperationException();
			}
			
		};
	}

}
