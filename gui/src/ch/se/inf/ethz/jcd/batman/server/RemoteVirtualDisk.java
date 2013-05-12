package ch.se.inf.ethz.jcd.batman.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import ch.se.inf.ethz.jcd.batman.io.VDiskFile;
import ch.se.inf.ethz.jcd.batman.model.Directory;
import ch.se.inf.ethz.jcd.batman.model.Entry;
import ch.se.inf.ethz.jcd.batman.model.File;
import ch.se.inf.ethz.jcd.batman.model.Path;
import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualDisk;
import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualFile;
import ch.se.inf.ethz.jcd.batman.vdisk.VirtualDiskException;
import ch.se.inf.ethz.jcd.batman.vdisk.impl.VirtualDisk;
import ch.se.inf.ethz.jcd.batman.vdisk.search.Settings;
import ch.se.inf.ethz.jcd.batman.vdisk.search.VirtualDiskSearch;

public abstract class RemoteVirtualDisk implements IRemoteVirtualDisk {

	protected static class LoadedDisk {
		
		private final IVirtualDisk disk;
		private final List<Integer> ids;
		
		public LoadedDisk(IVirtualDisk disk) {
			this.disk = disk;
			ids = new LinkedList<Integer>();
		}
		
		public IVirtualDisk getDisk () {
			return disk;
		}
		
		public void addId (Integer id) {
			if (!ids.contains(id)) {
				ids.add(id);
			}
		}
		
		public void removeId (Integer id) {
			ids.remove(id);
		}
		
		public boolean hasNoIds () {
			return ids.isEmpty();
		}
	}
	
	private final Map<URI, LoadedDisk> pathToDiskMap;
	private final Map<Integer, LoadedDisk> idToDiskMap;
	private final Map<URI, List<IRemoteDiskClient>> clientMap;
	private int nextId = Integer.MIN_VALUE;

	public RemoteVirtualDisk() {
		pathToDiskMap = new HashMap<URI, LoadedDisk>();
		idToDiskMap = new HashMap<Integer, LoadedDisk>();
		clientMap = new HashMap<URI, List<IRemoteDiskClient>>();
	}
	
	protected int getNextId() {
		return nextId++;
	}
	
	protected synchronized int createDisk (String path) throws IOException {
		IVirtualDisk disk = VirtualDisk.create(path);
		LoadedDisk loadedDisk = new LoadedDisk(disk);
		pathToDiskMap.put(new java.io.File(path).toURI(), loadedDisk);
		int id = getNextId();
		loadedDisk.addId(id);
		idToDiskMap.put(id, loadedDisk);
		return id;
	}
	
	protected synchronized int loadDisk (String path) throws IOException {
		URI uri = new java.io.File(path).toURI();
		LoadedDisk loadedDisk = pathToDiskMap.get(uri);
		if (loadedDisk == null) {
			IVirtualDisk disk = VirtualDisk.load(path);
			loadedDisk = new LoadedDisk(disk);
			pathToDiskMap.put(uri, loadedDisk);
		}
		int id = getNextId();
		loadedDisk.addId(id);
		idToDiskMap.put(id, loadedDisk);
		return id;
	}
	
	@Override
	public synchronized void unloadDisk(int id) throws RemoteException, VirtualDiskException {
		try {
			LoadedDisk loadedDisk = idToDiskMap.get(id);
			idToDiskMap.remove(id);
			if (loadedDisk != null) {
				loadedDisk.removeId(id);
				if (loadedDisk.hasNoIds()) {
					pathToDiskMap.remove(loadedDisk.getDisk().getHostLocation());
					loadedDisk.getDisk().close();
				}
			}
		} catch (IOException | IllegalArgumentException e) {
			throw new VirtualDiskException("Could not unload disk " + id, e);
		}
	}
	
	@Override
	public long getFreeSpace(int id) throws RemoteException,
			VirtualDiskException {
		try {
			return getDisk(id).getFreeSpace();
		} catch (IOException | IllegalArgumentException e) {
			throw new VirtualDiskException("Could not query free disk space.",
					e);
		}
	}

	@Override
	public long getOccupiedSpace(int id) throws RemoteException,
			VirtualDiskException {
		try {
			return getDisk(id).getOccupiedSpace();
		} catch (IOException | IllegalArgumentException e) {
			throw new VirtualDiskException(
					"Could not query occupied disk space.", e);
		}
	}

	@Override
	public long getUsedSpace(int id) throws RemoteException,
			VirtualDiskException {
		try {
			return getDisk(id).getSize();
		} catch (IOException | IllegalArgumentException e) {
			throw new VirtualDiskException("Could not query used disk space.",
					e);
		}
	}

	@Override
	public void createFile(int id, File file) throws RemoteException,
			VirtualDiskException {
		try {
			VDiskFile diskFile = new VDiskFile(file.getPath().getPath(),
					getDisk(id));
			if (!diskFile.createNewFile(file.getSize())) {
				throw new VirtualDiskException("Could not create file at "
						+ file.getPath());
			}
			diskFile.setLastModified(file.getTimestamp());
			notifyEntryAdded(id, file);
		} catch (IOException | IllegalArgumentException e) {
			throw new VirtualDiskException("Could not create file at "
					+ file.getPath(), e);
		}
	}

	@Override
	public void createDirectory(int id, Directory directory)
			throws RemoteException, VirtualDiskException {
		try {
			VDiskFile diskFile = new VDiskFile(directory.getPath().getPath(),
					getDisk(id));
			if (!diskFile.mkdirs()) {
				throw new VirtualDiskException("Could not create directory at "
						+ directory.getPath());
			}
			diskFile.setLastModified(directory.getTimestamp());
			notifyEntryAdded(id, directory);
		} catch (IOException | IllegalArgumentException e) {
			throw new VirtualDiskException("Could not create directory at "
					+ directory.getPath(), e);
		}
	}

	@Override
	public void deleteEntry(int id, Entry entry) throws RemoteException,
			VirtualDiskException {
		try {
			VDiskFile diskFile = new VDiskFile(entry.getPath().getPath(), getDisk(id));
			if (!diskFile.delete()) {
				throw new VirtualDiskException("Could not delete file at "
						+ entry.getPath());
			}
			notifyEntryDeleted(id, entry);
		} catch (IOException | IllegalArgumentException e) {
			throw new VirtualDiskException("Could not delete file at " + entry.getPath(),
					e);
		}
	}

	@Override
	public void write(int id, File file, long fileOffset, byte[] data)
			throws RemoteException, VirtualDiskException {
		try {
			VDiskFile diskFile = new VDiskFile(file.getPath().getPath(),
					getDisk(id));
			if (diskFile.isFile()) {
				long oldFileSize = diskFile.getFileSize();
				IVirtualFile vFile = (IVirtualFile) diskFile.getDiskEntry();
				vFile.seek(fileOffset);
				vFile.write(data);
				notifyWriteToEntry(id, file, fileOffset, data);
				if (diskFile.getFileSize() != oldFileSize) {
					notifyEntryChanged(id, file, createModel(diskFile));
				}
			} else {
				throw new IllegalArgumentException(file.getPath()
						+ " is not a file.");
			}
		} catch (IOException | IllegalArgumentException e) {
			throw new VirtualDiskException("Could not write to file "
					+ file.getPath() + " at " + fileOffset, e);
		}
	}

	@Override
	public byte[] read(int id, File file, long fileOffset, int length)
			throws RemoteException, VirtualDiskException {
		try {
			VDiskFile diskFile = new VDiskFile(file.getPath().getPath(),
					getDisk(id));
			if (diskFile.isFile()) {
				IVirtualFile vFile = (IVirtualFile) diskFile.getDiskEntry();
				vFile.seek(fileOffset);
				byte[] buffer = new byte[length];
				int bytesRead = vFile.read(buffer);
				if (bytesRead < length) {
					return Arrays.copyOf(buffer, bytesRead);
				} else {
					return buffer;
				}
			} else {
				throw new IllegalArgumentException(file.getPath()
						+ " is not a file.");
			}
		} catch (IOException | IllegalArgumentException e) {
			throw new VirtualDiskException("Could not write to file "
					+ file.getPath() + " at " + fileOffset, e);
		}
	}

	@Override
	public Entry[] getChildren(int id, Entry entry) throws RemoteException,
			VirtualDiskException {
		try {
			VDiskFile directoryEntry = new VDiskFile(entry.getPath().getPath(),
					getDisk(id));
			VDiskFile[] diskEntrys = directoryEntry.listFiles();
			List<Entry> entryList = new LinkedList<Entry>();
			for (VDiskFile diskEntry : diskEntrys) {
				entryList.add(createModel(diskEntry));
			}
			return entryList.toArray(new Entry[entryList.size()]);
		} catch (IOException | IllegalArgumentException e) {
			throw new VirtualDiskException("Could not query entrys for entry "
					+ entry.getPath(), e);
		}
	}

	@Override
	public Entry[] getAllChildrenBelow(int id, Entry entry)
			throws RemoteException, VirtualDiskException {
		try {
			List<Entry> subEntrys = new LinkedList<Entry>();
			VDiskFile directoryEntry = new VDiskFile(entry.getPath().getPath(),
					getDisk(id));
			addAllSubEntrysToList(directoryEntry, subEntrys);
			return subEntrys.toArray(new Entry[subEntrys.size()]);
		} catch (IOException | IllegalArgumentException e) {
			throw new VirtualDiskException(
					"Could not query all sub entrys for entry "
							+ entry.getPath(), e);
		}
	}
	
	@Override
	public void moveEntry(int id, Entry entry, Entry newEntry)
			throws RemoteException, VirtualDiskException {
		try {
			IVirtualDisk disk = getDisk(id);
			VDiskFile diskEntry = new VDiskFile(entry.getPath().getPath(), disk);
			VDiskFile renameEntry = new VDiskFile(newEntry.getPath().getPath(), disk);
			if (!diskEntry.renameTo(renameEntry)) {
				throw new VirtualDiskException();
			}
			diskEntry.setLastModified(newEntry.getTimestamp());
			notifyEntryChanged(id, entry, newEntry);
		} catch (IOException | IllegalArgumentException e) {
			throw new VirtualDiskException("Could not rename entry "
					+ entry.getPath() + " to " + newEntry.getPath(), e);
		}
	}

	@Override
	public boolean[] entriesExist(int id, Path[] entryPaths)
			throws RemoteException, VirtualDiskException {
		try {
			IVirtualDisk disk = getDisk(id);
			boolean[] exist = new boolean[entryPaths.length];
			for (int i = 0; i < entryPaths.length; i++) {
				exist[i] = new VDiskFile(entryPaths[i].getPath(), disk)
						.exists();
			}
			return exist;
		} catch (IOException | IllegalArgumentException e) {
			throw new VirtualDiskException("Could not check if entries  "
					+ Arrays.toString(entryPaths) + " exist.", e);
		}
	}

	@Override
	public Entry[] getEntries(int id, Path[] entryPaths)
			throws RemoteException, VirtualDiskException {
		try {
			IVirtualDisk disk = getDisk(id);
			Entry[] entries = new Entry[entryPaths.length];
			for (int i = 0; i < entryPaths.length; i++) {
				entries[i] = createModel(new VDiskFile(entryPaths[i].getPath(),
						disk));
			}
			return entries;
		} catch (IOException | IllegalArgumentException e) {
			throw new VirtualDiskException("Could not load entries  "
					+ Arrays.toString(entryPaths), e);
		}
	}

	@Override
	public Entry[] search(int id, String term, boolean isRegex,
			boolean checkFiles, boolean checkFolders, boolean isCaseSensitive,
			boolean checkChildren, Entry[] parents) throws RemoteException,
			VirtualDiskException {
		IVirtualDisk disk = getDisk(id);

		try {
			Collection<VDiskFile> parentFiles = new LinkedList<>();
			for (Entry parent : parents) {
				parentFiles
						.add(new VDiskFile(parent.getPath().getPath(), disk));
			}

			Settings settings = new Settings();
			settings.setCaseSensitive(isCaseSensitive);
			settings.setCheckFiles(checkFiles);
			settings.setCheckFolders(checkFolders);
			settings.setCheckSubFolders(checkChildren);

			List<VDiskFile> results = null;
			if (isRegex) {
				results = VirtualDiskSearch.searchName(settings,
						Pattern.compile(term),
						parentFiles.toArray(new VDiskFile[0]));
			} else {
				results = VirtualDiskSearch.searchName(settings, term,
						parentFiles.toArray(new VDiskFile[0]));
			}

			List<Entry> resultEntries = new LinkedList<>();
			for (VDiskFile result : results) {
				if (result.isFile()) {
					resultEntries.add(new File(new Path(result.getPath()),
							result.lastModified(), result.getFileSize()));
				} else if (result.isDirectory()) {
					resultEntries.add(new Directory(new Path(result.getPath()),
							result.lastModified()));
				}
			}

			return resultEntries.toArray(new Entry[0]);
		} catch (IOException | IllegalArgumentException e) {
			throw new VirtualDiskException("Could not execute search", e);
		}
	}

	@Override
	public void copyEntry(int id, Entry source, Entry destination)
			throws RemoteException, VirtualDiskException {
		IVirtualDisk disk = getDisk(id);
		try {
			VDiskFile destinationEntry = new VDiskFile(destination.getPath().getPath(), disk);
			destinationEntry.setLastModified(destination.getTimestamp());
			if (source instanceof File) {
				VDiskFile sourceFile = new VDiskFile(
						source.getPath().getPath(), disk);
				if (!sourceFile.copyTo(destinationEntry)) {
					throw new VirtualDiskException("Copy errror");
				}
				destinationEntry.setLastModified(destination.getTimestamp());
			} else if (source instanceof Directory) {
				
				if (!destinationEntry.mkdir()) {
					throw new VirtualDiskException(
							"Could not create directory at " + destination);
				}
				destinationEntry.setLastModified(destination.getTimestamp());
			} else {
				throw new VirtualDiskException("Invalid disk entry type "
						+ source.getClass());
			}
			notifyEntryCopied(id, source, destination);
		} catch (IOException | IllegalArgumentException e) {
			throw new VirtualDiskException("Could not copy entry "
					+ source.getPath() + " to " + destination, e);
		}

	}
	
	@Override
	public byte[] getAdditionalDiskInformation(int id) throws RemoteException, VirtualDiskException {
		try {
			return getDisk(id).getAdditionalDiskInformation();
		} catch (IOException | IllegalArgumentException e) {
			throw new VirtualDiskException("Could not read additional disk information", e);
		}
	}
	
	@Override
	public void saveAdditionalDiskInformation(int id, byte[] information) throws RemoteException, VirtualDiskException {
		try {
			getDisk(id).saveAdditionalDiskInformation(information);
		} catch (IOException | IllegalArgumentException e) {
			throw new VirtualDiskException("Could not save additional disk information", e);
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		for (LoadedDisk loadedDisk : idToDiskMap.values()) {
			loadedDisk.getDisk().close();
		}
		super.finalize();
	}

	protected boolean isVirtualDisk(java.io.File diskFile) {
		if (!diskFile.exists()) {
			return false;
		}
		// buffer for magic number
		byte[] readMagicNumber = new byte[IVirtualDisk.MAGIC_NUMBER.length];
		FileInputStream reader;
		try {
			reader = new FileInputStream(diskFile.getAbsolutePath());
			reader.read(readMagicNumber);
			reader.close();
		} catch (IOException e) {
			return false;
		}

		return Arrays.equals(IVirtualDisk.MAGIC_NUMBER, readMagicNumber);
	}

	private void addAllSubEntrysToList(VDiskFile directory,
			List<Entry> entryList) throws IOException {
		for (VDiskFile entry : directory.listFiles()) {
			entryList.add(createModel(entry));
			if (entry.isDirectory()) {
				addAllSubEntrysToList(entry, entryList);
			}
		}
	}

	protected IVirtualDisk getDisk(int id) throws IllegalArgumentException {
		LoadedDisk loadedDisk = idToDiskMap.get(id);
		if (loadedDisk == null || loadedDisk.getDisk() == null) {
			throw new IllegalArgumentException("Invalid id.");
		}
		return loadedDisk.getDisk();
	}

	private File createFileModel(VDiskFile entry) {
		return new File(new Path(entry.getPath()), entry.lastModified(),
				entry.getFileSize());
	}

	private Directory createDirectoryModel(VDiskFile entry) {
		return new Directory(new Path(entry.getPath()), entry.lastModified());
	}

	private Entry createModel(VDiskFile entry) {
		if (entry.isFile()) {
			return createFileModel(entry);
		} else {
			return createDirectoryModel(entry);
		}
	}
	
	public Entry updateLastModified(int id, Entry entry, long newTimestamp) throws RemoteException, VirtualDiskException {
		try {
			IVirtualDisk disk = getDisk(id);
			VDiskFile file = new VDiskFile(entry.getPath().getPath(), disk);
			if (!file.exists()) {
				throw new VirtualDiskException("Can't update last modified for " + entry.getPath() + ", entry does not exist.");
			}
			file.setLastModified(newTimestamp);
			Entry newEntry = createModel(file);
			notifyEntryChanged(id, entry, newEntry);
			return newEntry;
		} catch (IOException | IllegalArgumentException e) {
			throw new VirtualDiskException("Could not update last modified for " + entry.getPath(), e);
		}
	}
	
	private List<IRemoteDiskClient> getClientMap (int id) {
		return clientMap.get(getDisk(id).getHostLocation());
	}
	
	protected void notifyEntryAdded(int id, Entry entry) throws RemoteException, VirtualDiskException {
		List<IRemoteDiskClient> list = getClientMap(id);
		if (list != null) {
			for (IRemoteDiskClient client : list) {
				client.entryAdded(entry);
			}
		}
	}

	protected void notifyEntryDeleted(int id, Entry entry) throws RemoteException, VirtualDiskException {
		List<IRemoteDiskClient> list = getClientMap(id);
		if (list != null) {
			for (IRemoteDiskClient client : list) {
				client.entryDeleted(entry);
			}
		}
	}

	protected void notifyEntryChanged(int id, Entry oldEntry, Entry newEntry) throws RemoteException, VirtualDiskException {
		List<IRemoteDiskClient> list = getClientMap(id);
		if (list != null) {
			for (IRemoteDiskClient client : list) {
				client.entryChanged(oldEntry, newEntry);
			}
		}
	}
	
	protected void notifyEntryCopied(int id, Entry sourceEntry, Entry destinationEntry) throws RemoteException, VirtualDiskException {
		List<IRemoteDiskClient> list = getClientMap(id);
		if (list != null) {
			for (IRemoteDiskClient client : list) {
				client.entryCopied(sourceEntry, destinationEntry);
			}
		}
	}
	
	protected void notifyWriteToEntry(int id, File file, long fileOffset, byte[] data) throws RemoteException, VirtualDiskException {
		List<IRemoteDiskClient> list = getClientMap(id);
		if (list != null) {
			for (IRemoteDiskClient client : list) {
				client.writeToEntry(file, fileOffset, data);
			}
		}
	}
	
	public void registerClient(int id, IRemoteDiskClient client) {
		URI uri = getDisk(id).getHostLocation();
		List<IRemoteDiskClient> list = clientMap.get(uri);
		if (list == null) {
			list = new LinkedList<IRemoteDiskClient>();
			clientMap.put(uri, list);
		}
		if (!list.contains(client)) {
			list.add(client);
		}
	}
	
	public void unregisterClient(int id, IRemoteDiskClient client){
		List<IRemoteDiskClient> list = getClientMap(id);
		if (list != null) {
			list.remove(client);
			if (list.isEmpty()) {
				clientMap.remove(id);
			}
		}
	}
	
	public void aquireLock(int id, Entry entry) throws RemoteException {
		
	}
	
	public void releaseLock(int id, Entry entry) throws RemoteException {
		
	}
	
}
