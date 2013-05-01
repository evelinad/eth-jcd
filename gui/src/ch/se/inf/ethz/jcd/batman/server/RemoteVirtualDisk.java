package ch.se.inf.ethz.jcd.batman.server;

import java.io.FileInputStream;
import java.io.IOException;
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
import ch.se.inf.ethz.jcd.batman.vdisk.search.Settings;
import ch.se.inf.ethz.jcd.batman.vdisk.search.VirtualDiskSearch;

public class RemoteVirtualDisk implements IRemoteVirtualDisk {

	private final Map<Integer, IVirtualDisk> diskMap;
	private int nextId = Integer.MIN_VALUE;

	public RemoteVirtualDisk() {
		diskMap = new HashMap<Integer, IVirtualDisk>();
	}

	public Map<Integer, IVirtualDisk> getDiskMap () {
		return diskMap;
	}

	protected int getNextId() {
		return nextId++;
	}
	
	@Override
	public long getFreeSpace(int id) throws RemoteException,
			VirtualDiskException {
		try {
			return getDisk(id).getFreeSpace();
		} catch (Exception e) {
			throw new VirtualDiskException("Could not query free disk space.",
					e);
		}
	}

	@Override
	public long getOccupiedSpace(int id) throws RemoteException,
			VirtualDiskException {
		try {
			return getDisk(id).getOccupiedSpace();
		} catch (Exception e) {
			throw new VirtualDiskException(
					"Could not query occupied disk space.", e);
		}
	}

	@Override
	public long getUsedSpace(int id) throws RemoteException,
			VirtualDiskException {
		try {
			return getDisk(id).getSize();
		} catch (Exception e) {
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
		} catch (Exception e) {
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
		} catch (Exception e) {
			throw new VirtualDiskException("Could not create directory at "
					+ directory.getPath(), e);
		}
	}

	@Override
	public void deleteEntry(int id, Path path) throws RemoteException,
			VirtualDiskException {
		try {
			VDiskFile diskFile = new VDiskFile(path.getPath(), getDisk(id));
			if (!diskFile.delete()) {
				throw new VirtualDiskException("Could not delete file at "
						+ path);
			}
		} catch (Exception e) {
			throw new VirtualDiskException("Could not delete file at " + path,
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
				IVirtualFile vFile = (IVirtualFile) diskFile.getDiskEntry();
				vFile.seek(fileOffset);
				vFile.write(data);
			} else {
				throw new IllegalArgumentException(file.getPath()
						+ " is not a file.");
			}
		} catch (Exception e) {
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
		} catch (Exception e) {
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
		} catch (Exception e) {
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
		} catch (Exception e) {
			throw new VirtualDiskException(
					"Could not query all sub entrys for entry "
							+ entry.getPath(), e);
		}
	}
	
	@Override
	public void moveEntry(int id, Entry entry, Path newPath)
			throws RemoteException, VirtualDiskException {
		try {
			IVirtualDisk disk = getDisk(id);
			VDiskFile diskEntry = new VDiskFile(entry.getPath().getPath(), disk);
			VDiskFile renameEntry = new VDiskFile(newPath.getPath(), disk);
			if (!diskEntry.renameTo(renameEntry)) {
				throw new VirtualDiskException();
			}
		} catch (Exception e) {
			throw new VirtualDiskException("Could not rename entry "
					+ entry.getPath() + " to " + newPath, e);
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
		} catch (Exception e) {
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
		} catch (Exception e) {
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
		} catch (Exception e) {
			throw new VirtualDiskException("Could not execute search", e);
		}
	}

	@Override
	public void copyEntry(int id, Entry source, Path destination)
			throws RemoteException, VirtualDiskException {
		IVirtualDisk disk = getDisk(id);
		try {
			if (source instanceof File) {
				VDiskFile sourceFile = new VDiskFile(
						source.getPath().getPath(), disk);
				if (!sourceFile.copyTo(new VDiskFile(destination.getPath(),
						disk))) {
					throw new VirtualDiskException("Copy errror");
				}
			} else if (source instanceof Directory) {
				VDiskFile newDirectory = new VDiskFile(destination.getPath(),
						disk);
				if (!newDirectory.mkdir()) {
					throw new VirtualDiskException(
							"Could not create directory at " + destination);
				}
			} else {
				throw new VirtualDiskException("Invalid disk entry type "
						+ source.getClass());
			}
		} catch (Exception e) {
			throw new VirtualDiskException("Could not copy entry "
					+ source.getPath() + " to " + destination, e);
		}

	}

	@Override
	protected void finalize() throws Throwable {
		for (IVirtualDisk disk : diskMap.values()) {
			disk.close();
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

	protected IVirtualDisk getDisk(int id) {
		IVirtualDisk disk = diskMap.get(id);
		if (disk == null) {
			throw new IllegalArgumentException("Invalid id.");
		}
		return disk;
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
	
}
