package ch.se.inf.ethz.jcd.batman.controller.remote;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javafx.application.Platform;
import ch.se.inf.ethz.jcd.batman.browser.DiskEntryListener;
import ch.se.inf.ethz.jcd.batman.controller.ConnectionException;
import ch.se.inf.ethz.jcd.batman.controller.TaskController;
import ch.se.inf.ethz.jcd.batman.controller.UpdateableTask;
import ch.se.inf.ethz.jcd.batman.model.Directory;
import ch.se.inf.ethz.jcd.batman.model.Entry;
import ch.se.inf.ethz.jcd.batman.model.File;
import ch.se.inf.ethz.jcd.batman.model.Path;
import ch.se.inf.ethz.jcd.batman.model.util.FileBeforeDirectoryComparator;
import ch.se.inf.ethz.jcd.batman.server.AuthenticationException;
import ch.se.inf.ethz.jcd.batman.server.IRemoteDiskClient;
import ch.se.inf.ethz.jcd.batman.server.IRemoteVirtualDisk;
import ch.se.inf.ethz.jcd.batman.server.ISimpleVirtualDisk;
import ch.se.inf.ethz.jcd.batman.server.ISynchronizeServer;
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

	protected class RemoteDiskClient implements IRemoteDiskClient {
		public RemoteDiskClient() throws RemoteException {
			UnicastRemoteObject.exportObject(this, 0);
		}

		@Override
		public void entryAdded(Entry entry) throws RemoteException, VirtualDiskException {
			RemoteTaskController.this.entryAdded(entry);
		}

		@Override
		public void entryDeleted(Entry entry) throws RemoteException, VirtualDiskException {
			RemoteTaskController.this.entryDeleted(entry);
		}

		@Override
		public void entryChanged(Entry oldEntry, Entry newEntry) throws RemoteException, VirtualDiskException {
			RemoteTaskController.this.entryChanged(oldEntry, newEntry);
		}

		@Override
		public void entryCopied(Entry sourceEntry, Entry destinationEntry)
				throws RemoteException, VirtualDiskException {
			RemoteTaskController.this.entryCopied(sourceEntry, destinationEntry);
		}

		@Override
		public void writeToEntry(File file, long fileOffset, byte[] data)
				throws RemoteException, VirtualDiskException {
			RemoteTaskController.this.writeToEntry(file, fileOffset, data);
		}
		
		public void unexportRmiObject() throws NoSuchObjectException {
			UnicastRemoteObject.unexportObject(this, false);
		}
		
	}
	
	private static final String ACQUIRE_LOCK = "Acquiring lock...";
	private static final String RELEASE_LOCK = "Releasing lock...";
	private static final String TASK_DISCOVER_ITEMS = "Discover items";
	private static final String UPDATE_TO = " to ";
	
	private static final String DIKS_SERVICE_NAME = VirtualDiskServer.DISK_SERVICE_NAME;
	private static final String SYNCHRONIZE_SERVICE_NAME = VirtualDiskServer.SYNCHRONIZE_SERVICE_NAME;
	protected static final int BUFFER_SIZE = RemoteConnectionUtil.BUFFER_SIZE;

	protected static boolean isServerUri (URI uri) {
		return uri.getUserInfo() != null;
	}
	
	protected static RemoteConnection connect (URI uri, boolean createNewIfNecessary) throws AuthenticationException, RemoteException, VirtualDiskException, ConnectionException, NotBoundException {
		Registry registry;
		if (uri.getPort() == -1) {
			registry = LocateRegistry.getRegistry(uri.getHost());
		} else {
			registry = LocateRegistry.getRegistry(uri.getHost(),
					uri.getPort());
		}
		RemoteConnection connection = new RemoteConnection();
		if (isServerUri(uri)) {
			String userInfo = uri.getUserInfo();
			ISynchronizeServer synchronizeServer = (ISynchronizeServer) registry
					.lookup(SYNCHRONIZE_SERVICE_NAME);
			connection.setDisk(synchronizeServer);
			String diskName = uri.getQuery();
			String[] splitUserInfo = userInfo.split(":");
			if (splitUserInfo.length != 2) {
				throw new AuthenticationException("Invalid user info");
			}
			String userName = splitUserInfo[0];
			String password = splitUserInfo[1];
			if (synchronizeServer.diskExists(userName, diskName)) {
				connection.setDiskId(synchronizeServer.loadDisk(userName, password, diskName));
			} else {
				if (createNewIfNecessary) {
					connection.setDiskId(synchronizeServer.createDisk(userName, password, diskName));
				} else {
					throw new ConnectionException(
							"Disk does not exist.");
				}
			}
		} else {
			Path diskPath = new Path(uri.getQuery());
			ISimpleVirtualDisk remoteDisk = (ISimpleVirtualDisk) registry
					.lookup(DIKS_SERVICE_NAME);
			connection.setDisk(remoteDisk);
			if (remoteDisk.diskExists(diskPath)) {
				connection.setDiskId(remoteDisk.loadDisk(diskPath));
			} else {
				if (createNewIfNecessary) {
					connection.setDiskId(remoteDisk.createDisk(diskPath));
				} else {
					throw new ConnectionException(
							"Disk does not exist.");
				}
			}
		}
		return connection;
	}
	
	protected final Comparator<Entry> fileBeforeDirectoryComp = new FileBeforeDirectoryComparator();
	private final List<DiskEntryListener> diskEntryListener = new LinkedList<DiskEntryListener>();

	protected URI uri;
	protected RemoteConnection connection;
	protected RemoteDiskClient rmiClient;

	public RemoteTaskController() { }
	
	public RemoteTaskController(URI uri) {
		this.uri = uri;
	}

	/**
	 * Returns the virtual disk ID given by the remote host system for the disk
	 * on which this controller operates.
	 * 
	 * @return the virtual disk ID for the disk on which this controller
	 *         instance operates
	 */
	public Integer getDiskId() {
		return connection.getDiskId();
	}

	/**
	 * Returns the remote virtual disk on which this controller operates.
	 * 
	 * @return the virtual disk remote interface
	 */
	public IRemoteVirtualDisk getRemoteDisk() {
		return connection.getDisk();
	}

	protected void connect(boolean createNewIfNecessary, UpdateableTask<?> task) throws AuthenticationException, RemoteException, VirtualDiskException, ConnectionException, NotBoundException, InterruptedException {
		connection = connect(uri, createNewIfNecessary);
	}
	
	public void close() {
		if (isConnected()) {
			try {
				unregisterClient();
				unloadDisk();
			} catch (RemoteException | VirtualDiskException e) {
				// ignore, as we close it anyway
			} finally {
				connection = null;
			}
		}
	}
	
	protected void registerClient() throws RemoteException {
		if (rmiClient == null) {
			rmiClient = new RemoteDiskClient();
			connection.getDisk().registerClient(connection.getDiskId(), rmiClient);
		}
	}
	
	protected void unregisterClient() throws RemoteException {
		if (rmiClient != null) {
			connection.getDisk().unregisterClient(connection.getDiskId(), rmiClient);
			rmiClient.unexportRmiObject();
			rmiClient = null;
		}
	}
	
	@Override
	public boolean isConnected() {
		return connection != null;
	}
	
	protected void unloadDisk () throws RemoteException, VirtualDiskException {
		getRemoteDisk().unloadDisk(getDiskId());
	}
	
	protected void createFile (File file) throws RemoteException, VirtualDiskException {
		getRemoteDisk().createFile(getDiskId(), file);
	}

	protected void createDirectory (Directory directory) throws RemoteException, VirtualDiskException {
		getRemoteDisk().createDirectory(getDiskId(), directory);
	}

	protected void deleteEntry (Entry entry) throws RemoteException, VirtualDiskException {
		getRemoteDisk().deleteEntry(getDiskId(), entry);
	}

	protected void moveEntry (Entry oldEntry, Entry newEntry) throws RemoteException, VirtualDiskException {
		getRemoteDisk().moveEntry(getDiskId(), oldEntry, newEntry);
	}

	protected void copyEntry(Entry source, Entry destination)
			throws RemoteException, VirtualDiskException {
		getRemoteDisk().copyEntry(getDiskId(), source, destination);
	}
	
	protected Entry importFile(java.io.File file, String destination)
			throws RemoteException, VirtualDiskException, IOException {
		return importFile(getRemoteDisk(), getDiskId(), file, destination);
	}
	
	protected Entry importFile(IRemoteVirtualDisk disk, Integer diskId, java.io.File file, String destination)
			throws RemoteException, VirtualDiskException, IOException {
		
		if (file.isDirectory()) {
			Directory newDirectory = new Directory(
					new Path(destination), new Date().getTime());
			disk.createDirectory(diskId, newDirectory);
			return newDirectory;
		} else if (file.isFile()) {
			File diskFile = new File(new Path(destination),
					new Date().getTime(), file.length());
			disk.createFile(diskId, diskFile);
			// Import data
			FileInputStream inputStream = null;
			try {
				inputStream = new FileInputStream(file);
				long bytesToRead = file.length();
				long bytesRead = 0;
				byte[] buffer = new byte[BUFFER_SIZE];
				while (bytesToRead > 0) {
					int currentBytesRead = inputStream.read(buffer);
					if (currentBytesRead < buffer.length) {
						disk.write(diskId, diskFile, bytesRead,
										Arrays.copyOf(buffer,
												currentBytesRead));
					} else {
						disk.write(diskId, diskFile, bytesRead,
								buffer);
					}
					bytesToRead -= currentBytesRead;
					bytesRead += currentBytesRead;
				}
				return diskFile;
			} finally {
				if (inputStream != null) {
					inputStream.close();
				}
			}
		}
		return null;
	}
	
	@Override
	public UpdateableTask<Void> createConnectTask(final boolean createNewIfNecessary) {
		if (isConnected()) {
			throw new IllegalStateException("Already connected.");
		}

		return new UpdateableTask<Void>() {

			@Override
			protected Void callImpl() throws ConnectionException, AuthenticationException {
				if (isConnected()) {
					throw new IllegalStateException("Already connected.");
				}
				try {
					updateTitle("Connecting");
					updateMessage("Connecting to virtual disk...");
					connect(createNewIfNecessary, this);
					registerClient();
					//Regarding the PMD error, its important to catch all possible exceptions
					//Because the connection has to be cleaned up if an error occurred
				} catch (Exception e) {
					close();
					throw new ConnectionException(e);
				}
				return null;
			}

		};
	}

	@Override
	public UpdateableTask<Entry[]> createDirectoryEntriesTask(final Directory directory) {
		checkIsConnected();
		return new UpdateableTask<Entry[]>() {

			@Override
			protected Entry[] callImpl() throws RemoteException, VirtualDiskException {
				checkIsConnected();
				updateTitle("Retrieve directory entries");
				updateMessage("Retrieving directory entries...");
				return getRemoteDisk().getChildren(getDiskId(), directory);
			}

		};
	}

	@Override
	public UpdateableTask<Long> createFreeSpaceTask() {
		checkIsConnected();
		return new UpdateableTask<Long>() {

			@Override
			protected Long callImpl() throws RemoteException, VirtualDiskException {
				checkIsConnected();
				updateTitle("Calculate free space");
				updateMessage("Calculating free space...");
				return getRemoteDisk().getFreeSpace(getDiskId());
			}

		};
	}

	@Override
	public UpdateableTask<Long> createOccupiedSpaceTask() {
		checkIsConnected();
		return new UpdateableTask<Long>() {

			@Override
			protected Long callImpl() throws RemoteException, VirtualDiskException {
				checkIsConnected();
				updateTitle("Calculate occupied space");
				updateMessage("Calculating occupied space...");
				return getRemoteDisk().getOccupiedSpace(getDiskId());
			}

		};
	}

	@Override
	public UpdateableTask<Long> createUsedSpaceTask() {
		checkIsConnected();
		return new UpdateableTask<Long>() {

			@Override
			protected Long callImpl() throws RemoteException, VirtualDiskException {
				checkIsConnected();
				updateTitle("Calculate used space");
				updateMessage("Calculating used space...");
				return getRemoteDisk().getUsedSpace(getDiskId());
			}

		};
	}

	protected void acquireLock(UpdateableTask<?> task) throws RemoteException, InterruptedException {
		task.updateMessage(ACQUIRE_LOCK);
		getRemoteDisk().acquireLock(getDiskId());
	}
	
	protected void releaseLock(UpdateableTask<?> task) throws RemoteException {
		task.updateMessage(RELEASE_LOCK);
		getRemoteDisk().releaseLock(getDiskId());
	}
	
	@Override
	public UpdateableTask<Void> createFileTask(final File file) {
		checkIsConnected();
		return new UpdateableTask<Void>() {

			@Override
			protected Void callImpl() throws RemoteException, VirtualDiskException, InterruptedException {
				checkIsConnected();
				updateTitle("Create file");
				acquireLock(this);
				try {
					updateMessage("Creating file " + file.getPath());
					createFile(file);
					return null;
				} finally {
					releaseLock(this);
				}
			}

		};
	}
	
	@Override
	public UpdateableTask<Void> createDirectoryTask(final Directory directory) {
		checkIsConnected();
		return new UpdateableTask<Void>() {

			@Override
			protected Void callImpl() throws RemoteException, VirtualDiskException, InterruptedException {
				checkIsConnected();
				updateTitle("Create directory");
				acquireLock(this);
				try {
					updateMessage("Creating directory " + directory.getPath());
					createDirectory(directory);
					return null;
				} finally {
					releaseLock(this);
				}
			}

		};
	}
	
	@Override
	public UpdateableTask<Void> createDeleteEntriesTask(final Entry[] entries) {
		checkIsConnected();
		return new UpdateableTask<Void>() {

			@Override
			protected Void callImpl() throws RemoteException, VirtualDiskException, InterruptedException {
				checkIsConnected();
				updateTitle("Deleting entries");
				acquireLock(this);
				try {
					updateMessage(TASK_DISCOVER_ITEMS);
					SortedSet<Entry> subEntries = new TreeSet<Entry>(
							fileBeforeDirectoryComp);
					for (int i = 0; i < entries.length; i++) {
						subEntries.addAll(Arrays.asList(getRemoteDisk()
								.getAllChildrenBelow(getDiskId(), entries[i])));
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
								+ currentEntryNumber + 1 + " of " + totalEntries
								+ ")");
						deleteEntry(entry);
						currentEntryNumber++;
						updateProgress(currentEntryNumber, totalEntries);
						if (isCancelled()) {
							return null;
						}
					}
					return null;
				} finally {
					releaseLock(this);
				}
			}

		};
	}
	
	@Override
	public UpdateableTask<Void> createMoveTask(final Entry[] sourceEntries,
			final Path[] destinationPaths) {
		checkIsConnected();
		if (sourceEntries.length != destinationPaths.length) {
			throw new IllegalArgumentException(
					"Source and destination arrays have to be the same size");
		}
		return new UpdateableTask<Void>() {

			@Override
			protected Void callImpl() throws RemoteException, VirtualDiskException, InterruptedException {
				checkIsConnected();
				updateTitle("Moving entries");
				acquireLock(this);
				try {
					updateMessage("Prepare moving");
					// check if destination paths not already exist
					checkEntriesAlreadyExistOnDisk(destinationPaths);
					// move entries
					int totalEntriesToMove = sourceEntries.length;
					for (int i = 0; i < totalEntriesToMove; i++) {
						updateProgress(i, totalEntriesToMove);
						Entry oldEntry = sourceEntries[i];
						Entry newEntry = null;
						try {
							newEntry = (Entry) oldEntry.clone();
						} catch (CloneNotSupportedException e) { }
						newEntry.setPath(destinationPaths[i]);
						newEntry.setTimestamp(new Date().getTime());
						updateMessage("Moving entry " + oldEntry.getPath() + UPDATE_TO
								+ newEntry.getPath());
						moveEntry(oldEntry, newEntry);
						if (isCancelled()) {
							return null;
						}
					}
					updateProgress(totalEntriesToMove, totalEntriesToMove);
					return null;
				} finally {
					releaseLock(this);
				}
			}

		};
	}

	private String getFilePathAsDiskPath(java.io.File file) {
		return file.getPath().replaceAll("\\\\", "/");
	}
	
	@Override
	public UpdateableTask<Void> createImportTask(final String[] sourcePaths,
			final Path[] destinationPaths) {
		checkIsConnected();
		if (sourcePaths.length != destinationPaths.length) {
			throw new IllegalArgumentException(
					"Source and destination arrays have to be the same size");
		}
		return new UpdateableTask<Void>() {

			@Override
			protected Void callImpl() throws IOException, InterruptedException {
				checkIsConnected();
				updateTitle("Import entries");
				acquireLock(this);
				try {
					updateMessage("Prepare importing");
					// check if destination paths not already exist
					checkEntriesAlreadyExistOnDisk(destinationPaths);
					// check how many and which files need to be imported
					updateMessage(TASK_DISCOVER_ITEMS);
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
							updateMessage("Importing entry " + file.toString() + UPDATE_TO
									+ destination);
							importFile(file, destination);
							entriesImported++;
							if (isCancelled()) {
								return null;
							}
						}
					}
					updateProgress(entriesImported, totalEntriesToImport);
					return null;
				} finally {
					releaseLock(this);
				}
			}

		};
	}

	@Override
	public UpdateableTask<Void> createExportTask(final Entry[] sourceEntries,
			final String[] destinationPaths) {
		checkIsConnected();
		return new UpdateableTask<Void>() {

			private void exportFile(Entry entry, String destination)
					throws RemoteException, IOException {
				updateMessage("Exporting entry " + entry.getPath() + UPDATE_TO
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
							byte[] buffer = getRemoteDisk().read(getDiskId(), diskFile,
									bytesRead,
									(int) Math.min(bytesToRead, BUFFER_SIZE));
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
			protected Void callImpl() throws IOException, InterruptedException {
				checkIsConnected();
				updateTitle("Export entries");
				acquireLock(this);
				try {
					// check if destination paths not already exist
					checkEntriesAlreadyExistOnHost(destinationPaths);
					// check how many and which files need to be exported
					updateMessage(TASK_DISCOVER_ITEMS);
					@SuppressWarnings("unchecked")
					List<Entry>[] exportFiles = new List[sourceEntries.length];
					long totalEntriesToImport = 0;
					for (int i = 0; i < sourceEntries.length; i++) {
						exportFiles[i] = new LinkedList<Entry>();
						exportFiles[i].add(sourceEntries[i]);
						exportFiles[i].addAll(Arrays.asList(getRemoteDisk()
								.getAllChildrenBelow(getDiskId(), sourceEntries[i])));
						totalEntriesToImport += exportFiles[i].size();
						if (isCancelled()) {
							return null;
						}
					}
	
					// export all entries
					long entriesExported = 0;
					for (int i = 0; i < sourceEntries.length; i++) {
						Entry baseEntry = exportFiles[i].get(0);
						for (Entry entry : exportFiles[i]) {
							String destination = destinationPaths[i]
									+ entry.getPath()
											.getRelativePath(baseEntry.getPath())
											.getPath();
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
				} finally {
					releaseLock(this);
				}
			}

		};
	}
	
	@Override
	public UpdateableTask<Void> createCopyTask(final Entry[] sourceEntries,
			final Path[] destinationPaths) {
		checkIsConnected();
		return new UpdateableTask<Void>() {

			@Override
			protected Void callImpl() throws RemoteException, VirtualDiskException, InterruptedException {
				checkIsConnected();
				updateTitle("Copy entries");
				acquireLock(this);
				try {
					updateMessage(TASK_DISCOVER_ITEMS);
					// check if destination paths not already exist
					checkEntriesAlreadyExistOnDisk(destinationPaths);
					@SuppressWarnings("unchecked")
					List<Entry>[] copyFiles = new List[sourceEntries.length];
					long totalEntriesToCopy = 0;
					for (int i = 0; i < sourceEntries.length; i++) {
						copyFiles[i] = new LinkedList<Entry>();
						copyFiles[i].add(sourceEntries[i]);
						copyFiles[i].addAll(Arrays.asList(getRemoteDisk()
								.getAllChildrenBelow(getDiskId(), sourceEntries[i])));
						totalEntriesToCopy += copyFiles[i].size();
						if (isCancelled()) {
							return null;
						}
					}
	
					long entriesExported = 0;
					for (int i = 0; i < sourceEntries.length; i++) {
						Entry baseEntry = copyFiles[i].get(0);
						for (Entry entry : copyFiles[i]) {
							Path destination = new Path(destinationPaths[i]
									+ entry.getPath()
											.getRelativePath(baseEntry.getPath())
											.getPath());
							updateProgress(entriesExported, totalEntriesToCopy);
							Entry newEntry = null;
							try {
								newEntry = (Entry) entry.clone();
							} catch (CloneNotSupportedException e) { }
							newEntry.setPath(destination);
							newEntry.setTimestamp(new Date().getTime());
							updateMessage("Copy entry " + entry.getPath() + UPDATE_TO + newEntry.getPath());
							copyEntry(entry, newEntry);
							entriesExported++;
							if (isCancelled()) {
								return null;
							}
						}
					}
					updateProgress(entriesExported, totalEntriesToCopy);
					return null;
				} finally {
					releaseLock(this);
				}
			}

		};
	}

	@Override
	public UpdateableTask<Void> createDisconnectTask() {
		return new UpdateableTask<Void>() {

			@Override
			protected Void callImpl() throws RemoteException, VirtualDiskException {
				close();
				return null;
			}

		};
	}

	@Override
	public UpdateableTask<Entry[]> createSearchTask(final String term,
			final boolean isRegex, final boolean checkFiles,
			final boolean checkFolders, final boolean isCaseSensitive,
			final boolean checkChildren, final Entry... parents) {
		return new UpdateableTask<Entry[]>() {
			@Override
			protected Entry[] callImpl() throws RemoteException, VirtualDiskException {
				checkIsConnected();

				updateTitle(String.format("Searching for '%s'", term));
				updateMessage("searching...");

				return getRemoteDisk().search(getDiskId(), term, isRegex, checkFiles,
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

	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}

	public void entryAdded(final Entry entry) throws RemoteException, VirtualDiskException {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				for (DiskEntryListener listener : diskEntryListener) {
					listener.entryAdded(entry);
				}
			}
		});
	}
	
	public void entryDeleted(final Entry entry) throws RemoteException, VirtualDiskException {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				for (DiskEntryListener listener : diskEntryListener) {
					listener.entryDeleted(entry);
				}
			}
		});
	}

	public void entryChanged(final Entry oldEntry, final Entry newEntry) throws RemoteException, VirtualDiskException {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				for (DiskEntryListener listener : diskEntryListener) {
					listener.entryChanged(oldEntry, newEntry);
				}
			}
		});
	}

	public void entryCopied(final Entry sourceEntry, final Entry destinationEntry) throws RemoteException, VirtualDiskException {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				for (DiskEntryListener listener : diskEntryListener) {
					listener.entryAdded(destinationEntry);
				}
			}
		});
	}

	public void writeToEntry(final File file, final long fileOffset, final byte[] data) throws RemoteException, VirtualDiskException {
		//ignore
	}
	
	protected void checkIsConnected() {
		if (!isConnected()) {
			throw new IllegalStateException("Controller is not connected");
		}
	}

	private void checkEntriesAlreadyExistOnHost(final String[] entries)
			throws VirtualDiskException, RemoteException {
		for (String entry : entries) {
			java.io.File file = new java.io.File(entry);
			if (file.exists()) {
				throw new VirtualDiskException("File " + entry
						+ " already exists");
			}
		}
	}

	private void checkEntriesAlreadyExistOnDisk(final Path[] entries)
			throws VirtualDiskException, RemoteException {
		boolean[] entriesExist = getRemoteDisk().entriesExist(getDiskId(), entries);
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
