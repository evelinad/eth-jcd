package ch.se.inf.ethz.jcd.batman.server;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import ch.se.inf.ethz.jcd.batman.io.VDiskFile;
import ch.se.inf.ethz.jcd.batman.model.Directory;
import ch.se.inf.ethz.jcd.batman.model.Entry;
import ch.se.inf.ethz.jcd.batman.model.File;
import ch.se.inf.ethz.jcd.batman.model.Path;
import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualDisk;
import ch.se.inf.ethz.jcd.batman.vdisk.impl.VirtualDisk;

public class RemoteVirtualDisk implements IRemoteVirtualDisk {

	private Map<Integer, IVirtualDisk> diskMap = new HashMap<Integer, IVirtualDisk>();
	private int nextId = 1;
	
	private int getNextId () {
		return nextId++;
	}
	
	@Override
	public int createDisk(Path path) throws RemoteException {
		try {
			IVirtualDisk newDisk = VirtualDisk.create(path.getPath());
			int id = getNextId();
			diskMap.put(id, newDisk);
			return id;
		} catch (Exception e) {
			throw new RemoteException("Could not create disk at " + path.getPath(), e);
		}
	}

	@Override
	public int loadDisk(Path path) throws RemoteException {
		try {
			IVirtualDisk loadedDisk = VirtualDisk.load(path.getPath());
			int id = getNextId();
			diskMap.put(id, loadedDisk);
			return id;
		} catch (Exception e) {
			throw new RemoteException("Could not load disk at " + path.getPath(), e);
		}
	}

	@Override
	public void unloadDisk(int id) throws RemoteException {
		try {
			IVirtualDisk disk = diskMap.get(id);
			diskMap.remove(id);
			if (disk != null) {
				disk.close();
			}
		} catch (Exception e) {
			throw new RemoteException("Could not unload disk " + id, e);
		}
	}

	@Override
	public long getFreeSpace(int id) throws RemoteException {
		try {
			IVirtualDisk disk = diskMap.get(id);
			if (disk == null) {
				throw new IllegalArgumentException("Invalid id.");
			}
			return disk.getFreeSpace();
		} catch (Exception e) {
			throw new RemoteException("Could not query free disk space.", e);
		}
	}

	@Override
	public long getOccupiedSpace(int id) throws RemoteException {
		try {
			IVirtualDisk disk = diskMap.get(id);
			if (disk == null) {
				throw new IllegalArgumentException("Invalid id.");
			}
			return disk.getOccupiedSpace();
		} catch (Exception e) {
			throw new RemoteException("Could not query occupied disk space.", e);
		}
	}

	@Override
	public long getUsedSpace(int id) throws RemoteException {
		try {
			IVirtualDisk disk = diskMap.get(id);
			if (disk == null) {
				throw new IllegalArgumentException("Invalid id.");
			}
			return disk.getSize();
		} catch (Exception e) {
			throw new RemoteException("Could not query used disk space.", e);
		}
	}

	@Override
	public File createFile(int id, Path path, long size) throws RemoteException {
		try {
			IVirtualDisk disk = diskMap.get(id);
			if (disk == null) {
				throw new IllegalArgumentException("Invalid id.");
			}
			VDiskFile diskFile = new VDiskFile(path.getPath(), disk);
			diskFile.createNewFile(size);
			return new File(new Path(diskFile.getPath()), diskFile.lastModified(), diskFile.getFileSize());
		} catch (Exception e) {
			throw new RemoteException("Could not create file at " + path, e);
		}
	}

	@Override
	public Directory createDirectory(int id, Path path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteEntry(int id, Path path) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int write(int id, File file, long fileOffset, byte[] data) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public byte[] read(int id, File file, long fileOffset) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Entry[] getEntrys(int id, Directory directory)
			throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void finalize() throws Throwable {
		for (IVirtualDisk disk : diskMap.values()) {
			disk.close();
		}
		super.finalize();
	}
	
}
