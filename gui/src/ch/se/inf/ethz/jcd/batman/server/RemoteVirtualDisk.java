package ch.se.inf.ethz.jcd.batman.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ch.se.inf.ethz.jcd.batman.io.VDiskFile;
import ch.se.inf.ethz.jcd.batman.model.Directory;
import ch.se.inf.ethz.jcd.batman.model.Entry;
import ch.se.inf.ethz.jcd.batman.model.File;
import ch.se.inf.ethz.jcd.batman.model.Path;
import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualDisk;
import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualFile;
import ch.se.inf.ethz.jcd.batman.vdisk.VirtualDiskException;
import ch.se.inf.ethz.jcd.batman.vdisk.impl.VirtualDisk;

public class RemoteVirtualDisk implements IRemoteVirtualDisk {

	private Map<Integer, IVirtualDisk> diskMap = new HashMap<Integer, IVirtualDisk>();
	private int nextId = 1;
	
	private int getNextId () {
		return nextId++;
	}
	
	private IVirtualDisk getDisk(int id) {
		IVirtualDisk disk = diskMap.get(id);
		if (disk == null) {
			throw new IllegalArgumentException("Invalid id.");
		}
		return disk;
	}
	
	private File createFileModel (VDiskFile entry) {
		return new File(new Path(entry.getPath()), entry.lastModified(), entry.getFileSize());
	}
	
	private Directory createDirectoryModel (VDiskFile entry) {
		return new Directory(new Path(entry.getPath()), entry.lastModified());
	}
	
	private Entry createModel (VDiskFile entry) {
		if (entry.isFile()) {
			return createFileModel(entry);
		} else {
			return createDirectoryModel(entry);
		}
	}
	
	@Override
	public int createDisk(Path path) throws RemoteException, VirtualDiskException {
		try {
			IVirtualDisk newDisk = VirtualDisk.create(path.getPath());
			int id = getNextId();
			diskMap.put(id, newDisk);
			return id;
		} catch (Exception e) {
			throw new VirtualDiskException("Could not create disk at " + path.getPath(), e);
		}
	}

	@Override
	public int loadDisk(Path path) throws RemoteException, VirtualDiskException {
		try {
			IVirtualDisk loadedDisk = VirtualDisk.load(path.getPath());
			int id = getNextId();
			diskMap.put(id, loadedDisk);
			return id;
		} catch (Exception e) {
			throw new VirtualDiskException("Could not load disk at " + path.getPath(), e);
		}
	}

	@Override
	public void unloadDisk(int id) throws RemoteException, VirtualDiskException {
		try {
			IVirtualDisk disk = getDisk(id);
			diskMap.remove(id);
			disk.close();
		} catch (Exception e) {
			throw new VirtualDiskException("Could not unload disk " + id, e);
		}
	}

	@Override
	public long getFreeSpace(int id) throws RemoteException, VirtualDiskException {
		try {
			return getDisk(id).getFreeSpace();
		} catch (Exception e) {
			throw new VirtualDiskException("Could not query free disk space.", e);
		}
	}

	@Override
	public long getOccupiedSpace(int id) throws RemoteException, VirtualDiskException {
		try {
			return getDisk(id).getOccupiedSpace();
		} catch (Exception e) {
			throw new VirtualDiskException("Could not query occupied disk space.", e);
		}
	}

	@Override
	public long getUsedSpace(int id) throws RemoteException, VirtualDiskException {
		try {
			return getDisk(id).getSize();
		} catch (Exception e) {
			throw new VirtualDiskException("Could not query used disk space.", e);
		}
	}

	@Override
	public File createFile(int id, Path path, long size) throws RemoteException, VirtualDiskException {
		try {
			VDiskFile diskFile = new VDiskFile(path.getPath(), getDisk(id));
			if (!diskFile.createNewFile(size)) {
				throw new VirtualDiskException("Could not create file at " + path);
			}
			return createFileModel(diskFile);
		} catch (Exception e) {
			throw new VirtualDiskException("Could not create file at " + path, e);
		}
	}

	@Override
	public Directory createDirectory(int id, Path path) throws RemoteException, VirtualDiskException {
		try {
			VDiskFile diskFile = new VDiskFile(path.getPath(), getDisk(id));
			if (!diskFile.mkdirs()) {
				throw new VirtualDiskException("Could not create directory at " + path);
			}
			return new Directory(new Path(diskFile.getPath()), diskFile.lastModified());
		} catch (Exception e) {
			throw new VirtualDiskException("Could not create directory at " + path, e);
		}
	}

	@Override
	public void deleteEntry(int id, Path path) throws RemoteException, VirtualDiskException {
		try {
			VDiskFile diskFile = new VDiskFile(path.getPath(), getDisk(id));
			if (!diskFile.delete()) {
				throw new VirtualDiskException("Could not delete file at " + path);
			}
		} catch (Exception e) {
			throw new VirtualDiskException("Could not delete file at " + path, e);
		}
	}

	@Override
	public void write(int id, File file, long fileOffset, byte[] data) throws RemoteException, VirtualDiskException {
		try {
			VDiskFile diskFile = new VDiskFile(file.getPath().getPath(), getDisk(id));
			if (diskFile.isFile()) {
				IVirtualFile vFile = (IVirtualFile) diskFile.getDiskEntry();
				vFile.seek(fileOffset);
				vFile.write(data);
			} else {
				throw new IllegalArgumentException(file.getPath() + " is not a file.");
			}
		} catch (Exception e) {
			throw new VirtualDiskException("Could not write to file " + file.getPath() + " at " + fileOffset, e);
		}
	}

	@Override
	public byte[] read(int id, File file, long fileOffset, int length) throws RemoteException, VirtualDiskException {
		try {
			VDiskFile diskFile = new VDiskFile(file.getPath().getPath(), getDisk(id));
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
				throw new IllegalArgumentException(file.getPath() + " is not a file.");
			}
		} catch (Exception e) {
			throw new VirtualDiskException("Could not write to file " + file.getPath() + " at " + fileOffset, e);
		}
	}
	
	@Override
	public Entry[] getEntrys(int id, Directory directory)
			throws RemoteException, VirtualDiskException {
		try {
			VDiskFile diskFile = new VDiskFile(directory.getPath().getPath(), getDisk(id));
			VDiskFile[] entrys = diskFile.listFiles();
			List<Entry> entryList = new LinkedList<Entry>();
			for (VDiskFile entry : entrys) {
				entryList.add(createModel(entry));
			}
			return entryList.toArray(new Entry[entryList.size()]);
		} catch (Exception e) {
			throw new VirtualDiskException("Could not query entrys for directory " + directory.getPath(), e);
		}
	}

	@Override
	protected void finalize() throws Throwable {
		for (IVirtualDisk disk : diskMap.values()) {
			disk.close();
		}
		super.finalize();
	}

	@Override
	public void deleteDisk(Path path) throws RemoteException, VirtualDiskException {
		try  {
	        java.io.File diskFile = new java.io.File(path.getPath());
	        if (isVirtualDisk(diskFile)) {
	            if (!diskFile.delete()) {
	            	throw new VirtualDiskException("Could not delete virtual disk at " + path); 
	            }
	        } else {
	            throw new IllegalArgumentException(path + "  is not a virtual disk. File not deleted.");
	        }
		} catch (Exception e) {
			throw new VirtualDiskException("Could not delete virtual disk at " + path, e);
		}
	}

	private boolean isVirtualDisk (java.io.File diskFile)  {
		if (!diskFile.exists()) {
			return false;
		}
		// buffer for magic number
		byte[] readMagicNumber = new byte[IVirtualDisk.MAGIC_NUMBER.length];
        FileInputStream reader;
		try {
			reader = new FileInputStream(
			        diskFile.getAbsolutePath());
	        reader.read(readMagicNumber);
	        reader.close();
		} catch (IOException e) {
			return false;
		}

        return Arrays.equals(IVirtualDisk.MAGIC_NUMBER, readMagicNumber);
	}
	
	@Override
	public boolean diskExists(Path path) throws RemoteException,
			VirtualDiskException {
		return isVirtualDisk(new java.io.File(path.getPath()));
	}
	
}
