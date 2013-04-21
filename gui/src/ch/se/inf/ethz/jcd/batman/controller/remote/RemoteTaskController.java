package ch.se.inf.ethz.jcd.batman.controller.remote;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javafx.application.Platform;
import javafx.concurrent.Task;
import ch.se.inf.ethz.jcd.batman.browser.DiskEntryListener;
import ch.se.inf.ethz.jcd.batman.controller.ConnectionException;
import ch.se.inf.ethz.jcd.batman.controller.TaskController;
import ch.se.inf.ethz.jcd.batman.model.Directory;
import ch.se.inf.ethz.jcd.batman.model.Entry;
import ch.se.inf.ethz.jcd.batman.model.File;
import ch.se.inf.ethz.jcd.batman.model.Path;
import ch.se.inf.ethz.jcd.batman.server.IRemoteVirtualDisk;
import ch.se.inf.ethz.jcd.batman.server.VirtualDiskServer;
import ch.se.inf.ethz.jcd.batman.vdisk.VirtualDiskException;

/**
 * Controller for the view (as in MVC-Pattern) that provides methods to deal
 * with a remote virtual disk server.
 * 
 * This controller uses an RMI Server on the host system to invoke commands on
 * the virtual disk.
 * 
 */
public class RemoteTaskController implements TaskController {

	/**
	 * Sorts entries in the following way: Files > Entries > Directories
	 */
	private static final class FileBeforeDirectoryComparator implements
			Comparator<Entry> {

		@Override
		public int compare(Entry entry1, Entry entry2) {
			if (entry1 instanceof File) {
				if (entry2 instanceof File) {
					return entry2.getPath().getPath()
							.compareTo(entry1.getPath().getPath());
				} else {
					return -1;
				}
			} else if (entry1 instanceof Directory) {
				if (entry2 instanceof Directory) {
					return entry2.getPath().getPath()
							.compareTo(entry1.getPath().getPath());
				} else {
					return 1;
				}
			} else {
				return entry2.getPath().getPath()
						.compareTo(entry1.getPath().getPath());
			}
		}

	}

	private static final String SERVICE_NAME = VirtualDiskServer.SERVICE_NAME;
	private static final int BUFFER_SIZE = 32 * 1024;

	private final Comparator<Entry> fileBeforeDirectoryComp;
	private final List<DiskEntryListener> diskEntryListener;

	private final URI uri;
	private final Path diskPath;
	private Integer diskId;
	private IRemoteVirtualDisk remoteDisk;

	public RemoteTaskController(URI uri) {
		fileBeforeDirectoryComp = new FileBeforeDirectoryComparator();
		diskEntryListener = new LinkedList<>();

		this.uri = uri;
		this.diskPath = new Path(uri.getQuery());
	}

	/**
	 * Returns the virtual disk ID given by the remote host system for the disk
	 * on which this controller operates.
	 * 
	 * @return the virtual disk ID for the disk on which this controller
	 *         instance operates
	 */
	public Integer getDiskId() {
		return diskId;
	}

	/**
	 * Returns the remote virtual disk on which this controller operates.
	 * 
	 * @return the virtual disk remote interface
	 */
	public IRemoteVirtualDisk getRemoteDisk() {
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
					updateTitle("Connecting");
					updateMessage("Connecting to virtual disk...");
					Registry registry;
					if (uri.getPort() == -1) {
						registry = LocateRegistry.getRegistry(uri.getHost());
					} else {
						registry = LocateRegistry.getRegistry(uri.getHost(),
								uri.getPort());
					}
					remoteDisk = (IRemoteVirtualDisk) registry
							.lookup(SERVICE_NAME);
					if (remoteDisk.diskExists(diskPath)) {
						diskId = remoteDisk.loadDisk(diskPath);
					} else {
						if (createNewIfNecessary) {
							diskId = remoteDisk.createDisk(diskPath);
						} else {
							throw new ConnectionException(
									"Disk does not exist.");
						}
					}
				} catch (RemoteException | NotBoundException
						| VirtualDiskException e) {
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

	@Override
	public Task<Entry[]> createDirectoryEntriesTask(final Directory directory) {
		checkIsConnected();
		return new Task<Entry[]>() {

			@Override
			protected Entry[] call() throws Exception {
				checkIsConnected();
				updateTitle("Retrieve directory entries");
				updateMessage("Retrieving directory entries...");
				return remoteDisk.getChildren(diskId, directory);
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
				updateTitle("Calculate free space");
				updateMessage("Calculating free space...");
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
				updateTitle("Calculate occupied space");
				updateMessage("Calculating occupied space...");
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
				updateTitle("Calculate used space");
				updateMessage("Calculating used space...");
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
				updateTitle("Create file");
				updateMessage("Creating file...");
				remoteDisk.createFile(diskId, file);
				entryAdded(file);
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
				updateTitle("Create directory");
				updateMessage("Creating directory...");
				remoteDisk.createDirectory(diskId, directory);
				entryAdded(directory);
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
				SortedSet<Entry> subEntries = new TreeSet<Entry>(
						fileBeforeDirectoryComp);
				for (int i = 0; i < entries.length; i++) {
					subEntries.addAll(Arrays.asList(remoteDisk
							.getAllChildrenBelow(diskId, entries[i])));
					subEntries.add(entries[i]);
					if (isCancelled()) {
						return null;
					}
				}

				int totalEntries = subEntries.size();
				int currentEntryNumber = 0;
				updateProgress(0, totalEntries);
				for (Entry entry : subEntries) {
					updateMessage("Deleting " + entry.getPath() + " ("
							+ (currentEntryNumber + 1) + " of " + totalEntries
							+ ")");
					remoteDisk.deleteEntry(diskId, entry.getPath());
					entryDeleted(entry);
					currentEntryNumber++;
					updateProgress(currentEntryNumber, totalEntries);
					if (isCancelled()) {
						return null;
					}
				}
				return null;
			}

		};
	}

	@Override
	public Task<Void> createMoveTask(final Entry[] sourceEntries,
			final Path[] destinationPaths) {
		checkIsConnected();
		if (sourceEntries.length != destinationPaths.length) {
			throw new IllegalArgumentException(
					"Source and destination arrays have to be the same size");
		}
		return new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				checkIsConnected();
				updateTitle("Moving entries");
				// check if destination paths not already exist
				checkEntriesAlreadyExistOnDisk(destinationPaths);
				// move entries
				int totalEntriesToMove = sourceEntries.length;
				for (int i = 0; i < totalEntriesToMove; i++) {
					updateProgress(i, totalEntriesToMove);
					Entry oldEntry = sourceEntries[i];
					Path newPath = destinationPaths[i];
					updateMessage("Moving entry " + oldEntry.getPath() + " to "
							+ newPath);
					remoteDisk.moveEntry(diskId, oldEntry, newPath);
					Entry newEntry;
					if (sourceEntries[i] instanceof File) {
						newEntry = new File(newPath, oldEntry.getTimestamp(),
								((File) oldEntry).getSize());
					} else if (sourceEntries[i] instanceof Directory) {
						newEntry = new Directory(newPath,
								oldEntry.getTimestamp());
					} else {
						newEntry = new Entry(newPath, oldEntry.getTimestamp());
					}
					entryChanged(oldEntry, newEntry);
					if (isCancelled()) {
						return null;
					}
				}
				updateProgress(totalEntriesToMove, totalEntriesToMove);
				return null;
			}

		};
	}

	private String getFilePathAsDiskPath(java.io.File file) {
		return file.getPath().replaceAll("\\\\", "/");
	}
	
	@Override
	public Task<Void> createImportTask(final String[] sourcePaths,
			final Path[] destinationPaths) {
		checkIsConnected();
		if (sourcePaths.length != destinationPaths.length) {
			throw new IllegalArgumentException(
					"Source and destination arrays have to be the same size");
		}
		return new Task<Void>() {

			private void importFile(java.io.File file, String destination)
					throws RemoteException, VirtualDiskException, IOException {
				updateMessage("Importing entry " + file.toString() + " to "
						+ destination);
				if (file.isDirectory()) {
					Directory newDirectory = new Directory(
							new Path(destination), file.lastModified());
					remoteDisk.createDirectory(diskId, newDirectory);
					entryAdded(newDirectory);
				} else if (file.isFile()) {
					remoteDisk.createFile(diskId,
							new File(new Path(destination),
									file.lastModified(), file.length()));
					// Import data
					File diskFile = new File(new Path(destination));
					FileInputStream inputStream = null;
					try {
						inputStream = new FileInputStream(file);
						long bytesToRead = file.length();
						long bytesRead = 0;
						byte[] buffer = new byte[BUFFER_SIZE];
						while (bytesToRead > 0) {
							int currentBytesRead = inputStream.read(buffer);
							if (currentBytesRead < buffer.length) {
								remoteDisk
										.write(diskId, diskFile, bytesRead,
												Arrays.copyOf(buffer,
														currentBytesRead));
							} else {
								remoteDisk.write(diskId, diskFile, bytesRead,
										buffer);
							}
							bytesToRead -= currentBytesRead;
							bytesRead += currentBytesRead;
						}
						entryAdded(diskFile);
					} finally {
						if (inputStream != null) {
							inputStream.close();
						}
					}
				}
			}

			@Override
			protected Void call() throws Exception {
				checkIsConnected();
				updateTitle("Import entries");
				// check if destination paths not already exist
				checkEntriesAlreadyExistOnDisk(destinationPaths);
				// check how many and which files need to be imported
				updateMessage("Discovering items");
				@SuppressWarnings("unchecked")
				List<java.io.File>[] importFiles = new List[sourcePaths.length];
				long totalEntriesToImport = 0;
				for (int i = 0; i < sourcePaths.length; i++) {
					importFiles[i] = new LinkedList<java.io.File>();
					getAllSubEntries(new java.io.File(sourcePaths[i]),
							importFiles[i]);
					totalEntriesToImport += importFiles[i].size();
					if (isCancelled()) {
						return null;
					}
				}
				
				// import all entries
				long entriesImported = 0;
				for (int i = 0; i < sourcePaths.length; i++) {
					java.io.File baseFile = importFiles[i].get(0);
					String baseFilePath = getFilePathAsDiskPath(baseFile);
					for (java.io.File file : importFiles[i]) {
						String entryPath = getFilePathAsDiskPath(file);
						String destination = destinationPaths[i]
								+ entryPath.substring(baseFilePath.length(),
										entryPath.length());
						updateProgress(entriesImported, totalEntriesToImport);
						importFile(file, destination);
						entriesImported++;
						if (isCancelled()) {
							return null;
						}
					}
				}
				updateProgress(entriesImported, totalEntriesToImport);
				return null;
			}

		};
	}
	
	@Override
	public Task<Void> createExportTask(final Entry[] sourceEntries,
			final String[] destinationPaths) {
		checkIsConnected();
		return new Task<Void>() {
			
			private void exportFile(Entry entry, String destination) throws RemoteException, IOException {
				updateMessage("Exporting entry " + entry.getPath() + " to "
						+ destination);
				if (entry instanceof Directory) {
					java.io.File directory = new java.io.File(destination);
					directory.mkdir();
				} else if (entry instanceof File) {
					java.io.File hostFile = new java.io.File(destination);
					FileOutputStream outputStream = null;
					try {
						hostFile.createNewFile();
						outputStream = new FileOutputStream(hostFile);
						File diskFile = (File) entry;
						long bytesToRead = diskFile.getSize();
						long bytesRead = 0;
						while (bytesToRead > 0) {
							byte[] buffer = remoteDisk.read(diskId, diskFile, bytesRead, (int) Math.min(bytesToRead, BUFFER_SIZE));
							outputStream.write(buffer);
							bytesToRead -= buffer.length;
							bytesRead += buffer.length;
						}
					} finally {
						if (outputStream != null) {
							outputStream.close();
						}
					}
			
				}
			}
			
			@Override
			protected Void call() throws Exception {
				checkIsConnected();
				updateTitle("Export entries");
				// check if destination paths not already exist
				checkEntriesAlreadyExistOnHost(destinationPaths);
				// check how many and which files need to be exported
				updateMessage("Discovering items");
				@SuppressWarnings("unchecked")
				List<Entry>[] exportFiles = new List[sourceEntries.length];
				long totalEntriesToImport = 0;
				for (int i = 0; i < sourceEntries.length; i++) {
					exportFiles[i] = new LinkedList<Entry>();
					exportFiles[i].add(sourceEntries[i]);
					exportFiles[i].addAll(Arrays.asList(remoteDisk.getAllChildrenBelow(diskId, sourceEntries[i])));
					totalEntriesToImport += exportFiles[i].size();
					if (isCancelled()) {
						return null;
					}
				}
				
				// export all entries
				long entriesExported = 0;
				for (int i = 0; i < sourceEntries.length; i++) {
					Entry baseEntry = exportFiles[i].get(0);
					System.out.println("BaseEntry: " + baseEntry);
					System.out.println("Destination: " + destinationPaths[i]);
					for (Entry entry : exportFiles[i]) {
						String destination = destinationPaths[i]
								+ entry.getPath().getRelativePath(baseEntry.getPath()).getPath();
						updateProgress(entriesExported, totalEntriesToImport);
						exportFile(entry, destination);
						entriesExported++;
						if (isCancelled()) {
							return null;
						}
					}
				}
				updateProgress(entriesExported, totalEntriesToImport);
				return null;
			}

		};
	}

	@Override
	public Task<Void> createCopyTask(final Entry[] sourceEntries,
			final Path[] destinationPaths) {
		checkIsConnected();
		return new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				checkIsConnected();
				// TODO
				throw new UnsupportedOperationException();
			}

		};
	}

	@Override
	public Task<Void> createDisconnectTask() {
		return new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				close();
				return null;
			}

		};
	}

	@Override
	public Task<Entry[]> createSearchTask(final String term,
			final boolean isRegex, final boolean checkFiles, final boolean checkFolders,
			final boolean isCaseSensitive, final boolean checkChildren,
			final Entry... parents) {
		return new Task<Entry[]>() {
			@Override
			protected Entry[] call() throws Exception {
				checkIsConnected();

				updateTitle(String.format("Searching for '%s'", term));
				updateMessage("searching...");

				return remoteDisk.search(diskId, term, isRegex, checkFiles,
						checkFolders, isCaseSensitive, checkChildren, parents);

			}
		};
	}

	public void addDiskEntryListener(DiskEntryListener listener) {
		if (!diskEntryListener.contains(listener)) {
			diskEntryListener.add(listener);
		}
	}

	public void removeDiskEntryListener(DiskEntryListener listener) {
		diskEntryListener.remove(listener);
	}

	public void close() {
		if (diskId != null) {
			try {
				remoteDisk.unloadDisk(diskId);
				diskId = null;
				remoteDisk = null;
			} catch (RemoteException | VirtualDiskException e) {
			}
		}
	}

	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}

	private void entryAdded(final Entry entry) {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				for (DiskEntryListener listener : diskEntryListener) {
					listener.entryAdded(entry);
				}
			}
		});
	}

	private void entryDeleted(final Entry entry) {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				for (DiskEntryListener listener : diskEntryListener) {
					listener.entryDeleted(entry);
				}
			}
		});
	}

	private void entryChanged(final Entry oldEntry, final Entry newEntry) {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				for (DiskEntryListener listener : diskEntryListener) {
					listener.entryChanged(oldEntry, newEntry);
				}
			}
		});
	}

	private void checkIsConnected() {
		if (!isConnected()) {
			throw new IllegalStateException("Controller is not connected");
		}
	}

	private void checkEntriesAlreadyExistOnHost(final String[] entries)
			throws VirtualDiskException, RemoteException {
		for (String entry : entries) {
			java.io.File file = new java.io.File(entry);
			if (file.exists()) {
				throw new VirtualDiskException("File "
						+ entry + " already exists");
			}
		}
	}
	
	private void checkEntriesAlreadyExistOnDisk(final Path[] entries)
			throws VirtualDiskException, RemoteException {
		boolean[] entriesExist = remoteDisk.entriesExist(diskId, entries);
		for (int i = 0; i < entries.length; i++) {
			if (entriesExist[i]) {
				throw new VirtualDiskException("Destination entry "
						+ entries[i].getPath() + " already exists");
			}
		}
	}

	private void getAllSubEntries(java.io.File file,
			List<java.io.File> subEntries) {
		if (file.exists()) {
			subEntries.add(file);
			if (file.isDirectory()) {
				for (java.io.File subFile : file.listFiles()) {
					getAllSubEntries(subFile, subEntries);
				}
			}
		}
	}

}
