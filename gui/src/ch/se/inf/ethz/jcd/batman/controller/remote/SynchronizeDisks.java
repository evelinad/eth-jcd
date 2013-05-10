package ch.se.inf.ethz.jcd.batman.controller.remote;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import ch.se.inf.ethz.jcd.batman.browser.DiskEntryListener;
import ch.se.inf.ethz.jcd.batman.model.Directory;
import ch.se.inf.ethz.jcd.batman.model.Entry;
import ch.se.inf.ethz.jcd.batman.model.util.EntryNameComperator;
import ch.se.inf.ethz.jcd.batman.model.util.EntryPathComperator;
import ch.se.inf.ethz.jcd.batman.model.util.FileBeforeDirectoryComparator;
import ch.se.inf.ethz.jcd.batman.vdisk.VirtualDiskException;

public class SynchronizeDisks {

	private interface SynchronizeTask {
		
		String getMessage();
		
		void run() throws RemoteException, VirtualDiskException;

	}
	
	private final class DeleteTask implements SynchronizeTask {
		private final RemoteConnection connection;
		private final Entry entry;
		
		public DeleteTask(RemoteConnection connection, Entry entry) {
			this.connection = connection;
			this.entry = entry;
		}
	
		@Override
		public String getMessage() {
			return "Delete " + entry.getPath();
		}
		
		@Override
		public void run () throws RemoteException, VirtualDiskException {
			connection.getDisk().deleteEntry(connection.getDiskId(), entry.getPath());
			entryDeleted(entry, connection);
		}

	}

	private final class CopyTask implements SynchronizeTask {
		private final RemoteConnection sourceConnection;
		private final RemoteConnection destinationConnection;
		private final Entry entry;
		
		public CopyTask(RemoteConnection sourceConnection, RemoteConnection destinationConnection, Entry entry) {
			this.sourceConnection = sourceConnection;
			this.destinationConnection = destinationConnection;
			this.entry = entry;
		}

		@Override
		public String getMessage() {
			return "Copy " + entry.getPath();
		}

		@Override
		public void run() throws RemoteException, VirtualDiskException {
			if (destinationConnection == serverConnection) {
				Entry newEntry = null;
				try {
					newEntry = (Entry) entry.clone();
				} catch (CloneNotSupportedException e) { }
				newEntry.setTimestamp(new Date().getTime());
				localConnection.getDisk().updateLastModified(localConnection.getDiskId(), newEntry);
				entryChanged(entry, newEntry, localConnection);
				RemoteConnectionUtil.copySingleEntry(newEntry, sourceConnection, destinationConnection);
				entryAdded(newEntry, destinationConnection);
			} else {
				RemoteConnectionUtil.copySingleEntry(entry, sourceConnection, destinationConnection);
				entryAdded(entry, destinationConnection);
			}
		}
		
	}
	
	private final class RenameTask implements SynchronizeTask {
		private final RemoteConnection connection;
		private final Entry entry;
		private final Entry newEntry;
		
		public RenameTask(RemoteConnection connection, Entry entry, Entry newEntry) {
			this.connection = connection;
			this.entry = entry;
			this.newEntry = newEntry;
		}

		@Override
		public String getMessage() {
			return "Rename " + entry.getPath() + " to " + entry.getPath();
		}

		@Override
		public void run() throws RemoteException, VirtualDiskException {
			newEntry.setTimestamp(new Date().getTime());
			connection.getDisk().moveEntry(connection.getDiskId(), entry, newEntry);
			entryChanged(entry, newEntry, connection);
		}
		
	}
	
	private final class UpdateTimestampTask implements SynchronizeTask {
		private final RemoteConnection connection;
		private final Entry entry;
		private final Entry newEntry;
		
		public UpdateTimestampTask(RemoteConnection connection, Entry entry, Entry newEntry) {
			this.connection = connection;
			this.entry = entry;
			this.newEntry = newEntry;
		}

		@Override
		public String getMessage() {
			return "Update " + entry.getPath();
		}

		@Override
		public void run() throws RemoteException, VirtualDiskException {
			connection.getDisk().updateLastModified(connection.getDiskId(), newEntry);
			entryChanged(entry, newEntry, connection);
		}
		
	}
	
	private static final class ChangeList extends LinkedList<SynchronizeTask> {

		private static final long serialVersionUID = -1125789772914391557L;
		
		private boolean newEntry = false;
		
		public ChangeList() { }
		
		public ChangeList(boolean newEntry) {
			this.newEntry = newEntry;
		}

		public ChangeList(boolean newEntry, SynchronizeTask task) {
			this.newEntry = newEntry;
			add(task);
		}
		
		public boolean containsNewEntry() {
			return newEntry;
		}

		public void setContainsNewEntry(boolean containsNewEntry) {
			this.newEntry = containsNewEntry;
		}
		
		private void checkNew(SynchronizeTask e) {
			if (!(e instanceof DeleteTask)) {
				newEntry = true;
			}
		}
		
		private void checkNew(Collection<? extends SynchronizeTask> c) {
			if (!newEntry) {
				if (c instanceof ChangeList) {
					newEntry = ((ChangeList) c).containsNewEntry();
				} else {
					for (SynchronizeTask task : c) {
						checkNew(task);
					}
				}
			}
		}
		
		@Override
		public boolean add(SynchronizeTask e) {
			checkNew(e);
			return super.add(e);
		}
		
		@Override
		public void add(int index, SynchronizeTask e) {
			checkNew(e);
			super.add(index, e);
		}
		
		@Override
		public boolean addAll(Collection<? extends SynchronizeTask> c) {
			checkNew(c);
			return super.addAll(c);
		}
		
		@Override
		public boolean addAll(int index, Collection<? extends SynchronizeTask> c) {
			checkNew(c);
			return super.addAll(index, c);
		}
		
		@Override
		public void addFirst(SynchronizeTask e) {
			checkNew(e);
			super.addFirst(e);
		}
		
		@Override
		public void addLast(SynchronizeTask e) {
			checkNew(e);
			super.addLast(e);
		}
	}
	
	
	private static final String CONFLICT_NAME_END = ".local";
	private static final Comparator<Entry> NAME_COMPARATOR = new EntryNameComperator();
	private static final Comparator<Entry> PATH_COMPERATOR = new EntryPathComperator();
	private static final Comparator<Entry> FILE_BEFORE_DIRECTORY_COMPARATOR = new FileBeforeDirectoryComparator();
		
	private final RemoteConnection serverConnection;
	private final RemoteConnection localConnection;
	
	private final List<DiskEntryListener> localDiskEntryListener = new LinkedList<DiskEntryListener>();
	private final List<DiskEntryListener> serverDiskEntryListener = new LinkedList<DiskEntryListener>();
	
	private long lastSynchronized;
	private UpdateableTask<?> task;
	
	public SynchronizeDisks(RemoteConnection serverConnection, RemoteConnection localConnection) {
		this.serverConnection = serverConnection;
		this.localConnection = localConnection;
	}
	
	public void addLocalDiskEntryListener(DiskEntryListener listener) {
		if (!localDiskEntryListener.contains(listener)) {
			localDiskEntryListener.add(listener);
		}
	}

	public void removeLocalDiskEntryListener(DiskEntryListener listener) {
		localDiskEntryListener.remove(listener);
	}

	public void addServerDiskEntryListener(DiskEntryListener listener) {
		if (!serverDiskEntryListener.contains(listener)) {
			serverDiskEntryListener.add(listener);
		}
	}

	public void removeServerDiskEntryListener(DiskEntryListener listener) {
		serverDiskEntryListener.remove(listener);
	}
	
	private void entryAdded(final Entry entry, final RemoteConnection connection) {
		if (connection == localConnection) {
			for (DiskEntryListener listener : localDiskEntryListener) {
				listener.entryAdded(entry);
			}	
		} else if (connection == serverConnection) {
			for (DiskEntryListener listener : serverDiskEntryListener) {
				listener.entryAdded(entry);
			}
		}
	}

	private void entryDeleted(final Entry entry, final RemoteConnection connection) {
		if (connection == localConnection) {
			for (DiskEntryListener listener : localDiskEntryListener) {
				listener.entryDeleted(entry);
			}	
		} else if (connection == serverConnection) {
			for (DiskEntryListener listener : serverDiskEntryListener) {
				listener.entryDeleted(entry);
			}
		}
	}

	private void entryChanged(final Entry oldEntry, final Entry newEntry, final RemoteConnection connection) {
		if (connection == localConnection) {
			for (DiskEntryListener listener : localDiskEntryListener) {
				listener.entryChanged(oldEntry, newEntry);
			}	
		} else if (connection == serverConnection) {
			for (DiskEntryListener listener : serverDiskEntryListener) {
				listener.entryChanged(oldEntry, newEntry);
			}
		}
	}
	
	
	
	public void synchronizeDisks(UpdateableTask<?> task) throws RemoteException, VirtualDiskException {
		this.task = task;
		task.updateTitle("Synchronize disks");
		task.updateMessage("Initialize...");
		lastSynchronized = 0;
		try {
			AdditionalLocalDiskInformation diskInformation = RemoteConnectionUtil.getDiskInformation(localConnection);
			if (diskInformation != null) {
				lastSynchronized = diskInformation.getLastSynchronized();
			}
		} catch (VirtualDiskException | RemoteException e) {
			//ignore because its possible that the disk has never been synchronized and no disk information exist
		}
		task.updateMessage("Discovering changes...");
		Directory rootDirectory = new Directory();
		synchronizeChanges(synchronizeDirectory(rootDirectory));
	}

	private void synchronizeChanges(ChangeList changes) throws RemoteException, VirtualDiskException {
		task.updateMessage("Synchronizing entries");
		long numberOfTasks = changes.size();
		long tasksDone = 0;
		task.updateProgress(tasksDone, numberOfTasks);
		for (SynchronizeTask change : changes) {
			task.updateMessage(change.getMessage());
			change.run();
			task.updateProgress(++tasksDone, numberOfTasks);
		}
	}
	
	private ChangeList copyRenamedEntry (Entry baseEntry, Entry renamedEntry, RemoteConnection sourceConnection, RemoteConnection destinationConnection) throws RemoteException, VirtualDiskException {
		ChangeList changes = new ChangeList();
		changes.setContainsNewEntry(true);
		String basePath = baseEntry.getPath().getPath();
		String renamedPath = renamedEntry.getPath().getPath();
		changes.add(new CopyTask(sourceConnection, destinationConnection, renamedEntry));
		Entry[] children = sourceConnection.getDisk().getAllChildrenBelow(sourceConnection.getDiskId(), baseEntry);
		Arrays.sort(children, PATH_COMPERATOR);
		for (Entry child : children) {
			child.getPath().setPath(child.getPath().getPath().replace(basePath, renamedPath));
			changes.add(new CopyTask(sourceConnection, destinationConnection, child));
		}
		return changes;
	}
	
	private ChangeList copyEntry(Entry entry, RemoteConnection sourceConnection, RemoteConnection destinationConnection) throws RemoteException, VirtualDiskException {
		ChangeList changes = new ChangeList();
		changes.setContainsNewEntry(true);
		changes.add(new CopyTask(sourceConnection, destinationConnection, entry));
		Entry[] children = sourceConnection.getDisk().getAllChildrenBelow(sourceConnection.getDiskId(), entry);
		Arrays.sort(children, PATH_COMPERATOR);
		for (Entry child : children) {
			changes.add(new CopyTask(sourceConnection, destinationConnection, child));
		}
		return changes;
	}
	
	private ChangeList deleteEntry(Entry entry, RemoteConnection connection) throws RemoteException, VirtualDiskException {
		ChangeList changes = new ChangeList();
		Entry[] children = connection.getDisk().getAllChildrenBelow(connection.getDiskId(), entry);
		Arrays.sort(children, FILE_BEFORE_DIRECTORY_COMPARATOR);
		for (Entry child : children) {
			changes.add(new DeleteTask(connection, child));
		}
		changes.add(new DeleteTask(connection, entry));
		return changes;
	}
	
	private ChangeList synchronizeEntry (Entry entry, RemoteConnection sourceConnection, RemoteConnection destinationConnection) throws RemoteException, VirtualDiskException {
		if (entry instanceof Directory) {
			if (entry.getTimestamp() > lastSynchronized) {
				//Directory has changed, copy all sub entries to destination connection
				return copyEntry(entry, sourceConnection, destinationConnection);
			} else {
				ChangeList changes = synchronizeDirectory((Directory) entry);
				if (changes.containsNewEntry()) {
					//Entry in directory changed, don't delete the directory
					changes.addFirst(new CopyTask(sourceConnection, destinationConnection, entry));
					return changes;
				} else {
					//No sub entries changed, delete the directory
					changes.addLast(new DeleteTask(sourceConnection, entry));
					return changes;
				}
			}
		} else {
			//File
			if (entry.getTimestamp() < lastSynchronized) {
				return new ChangeList(false, new DeleteTask(sourceConnection, entry));
			} else {
				return new ChangeList(true, new CopyTask(sourceConnection, destinationConnection, entry));
			}
		}
	}

	private ChangeList updateEntry (Entry entry, RemoteConnection sourceConnection, RemoteConnection destinationConnection) throws RemoteException, VirtualDiskException {
		ChangeList changes = deleteEntry(entry, destinationConnection);
		changes.addAll(copyEntry(entry, sourceConnection, destinationConnection));
		return changes;
	}
	
	private boolean contains (Entry[] entries, String path) {
		if (entries != null) {
			for (Entry entry : entries) {
				if (entry.getPath().getPath().equals(path)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private ChangeList synchronizeDirectory (Directory directory) throws RemoteException, VirtualDiskException {
		ChangeList changes = new ChangeList(false);
		Entry[] localChildren = RemoteConnectionUtil.getChildrenSorted(localConnection, directory);
		Entry[] serverChildren = RemoteConnectionUtil.getChildrenSorted(serverConnection, directory);
		int localIndex = 0;
		int serverIndex = 0;
		while (localIndex < localChildren.length || serverIndex < serverChildren.length) {
			if (localIndex < localChildren.length && serverIndex < serverChildren.length) {
				Entry localEntry = localChildren[localIndex];
				Entry serverEntry = serverChildren[serverIndex];
				if (localEntry.getPath().equals(serverEntry.getPath())) {
					//Same file paths
					if (localEntry instanceof Directory && serverEntry instanceof Directory) {
						//If both are directories, compare the content
						changes.addAll(synchronizeDirectory((Directory) localEntry));
						if (localEntry.getTimestamp() > lastSynchronized || serverEntry.getTimestamp() > lastSynchronized) {
							if (localEntry.getTimestamp() > serverEntry.getTimestamp()) {
								changes.add(new UpdateTimestampTask(serverConnection, serverEntry, localEntry));
							} else {
								changes.add(new UpdateTimestampTask(localConnection, localEntry, serverEntry));
							}
						}
					} else {
						if (localEntry.getTimestamp() > lastSynchronized && serverEntry.getTimestamp() > lastSynchronized) {
							//Both entries have changed
							String baseEntryName = localEntry.getPath().getName() + CONFLICT_NAME_END;
							String newName = baseEntryName;
							int id = 1;
							while (contains(localChildren, newName) || contains(serverChildren, newName)) {
								newName = baseEntryName + id++;
							}
							Entry newEntry = null;
							try {
								 newEntry = (Entry) localEntry.clone();
							} catch (CloneNotSupportedException e) { }
							newEntry.getPath().changeName(newName);
							//rename local entry
							changes.add(new RenameTask(localConnection, localEntry, newEntry));
							//copy server entries to local disk
							changes.addAll(copyEntry(serverEntry, serverConnection, localConnection));
							//copy renamed entry to server disk
							changes.addAll(copyRenamedEntry(localEntry, newEntry, localConnection, serverConnection));
						} else if (localEntry.getTimestamp() > lastSynchronized) {
							//Only local entry has changed
							changes.addAll(updateEntry(localEntry, localConnection, serverConnection));
						} else if (serverEntry.getTimestamp() > lastSynchronized) {
							//Only server entry has changed
							changes.addAll(updateEntry(serverEntry, serverConnection, localConnection));
						}
					}
					localIndex++;
					serverIndex++;
				} else if (NAME_COMPARATOR.compare(localEntry, serverEntry) < 0) {
					changes.addAll(synchronizeEntry(localChildren[localIndex++], localConnection, serverConnection));
				} else {
					changes.addAll(synchronizeEntry(serverChildren[serverIndex++], serverConnection, localConnection));
				}
			} else if (localIndex < localChildren.length) {
				changes.addAll(synchronizeEntry(localChildren[localIndex++], localConnection, serverConnection));
			} else if (serverIndex < serverChildren.length) {
				changes.addAll(synchronizeEntry(serverChildren[serverIndex++], serverConnection, localConnection));
			}
		}
		return changes;
	}
}
