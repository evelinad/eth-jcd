package ch.se.inf.ethz.jcd.batman.vdisk;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;



public class VirtualDisk implements IVirtualDisk {

	public static VirtualDisk load (String path) {
		return null;
	}
	
	public static VirtualDisk create (String path, long maxSize) {
		return new VirtualDisk(path, maxSize);
	}
	
	private static final byte DEFAULT_PLACEMENT_STRATEGY = PlacementStrategyFactory.MIXED_STRATEGY;
	private static final long MIN_SIZE_IN_BYTES = 128;
	private static final String VERSION = "v1.0\0\0\0\0";
	private static final byte[] MAGIC_NUMBER = new byte[] {(byte) 0xDE, (byte) 0xAD, (byte) 0xC0, (byte) 0xFF, (byte) 0xEE};
	private static final int SUPERBLOCK_SIZE = 64;  
	
	private long maxSize;
	private byte pStrategyType; 
	private IPlacementStrategy pStrategy;
	private RandomAccessFile file;
	
	public VirtualDisk(String path, long maxSize) {
		setMaxSize(maxSize);
		pStrategyType = DEFAULT_PLACEMENT_STRATEGY;
		try {
			File f = new File(path);
			if (f.exists()) {
				throw new IllegalArgumentException("Can't create Virtual Diks at " + path + ". File already exists");
			}
			file = new RandomAccessFile(f, "rw");
			
			/*
			 * 0x00 5byte MagicNumber
			 * 0x05 8byte Version
			 * 0x0D 1byte PlacementStrategy
			 * 0x0E - 0xFF currently not used
			 */
			file.write(MAGIC_NUMBER);
			file.write(VERSION.getBytes());
			file.write(pStrategyType);
			file.setLength(getSuperblockSize());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		pStrategy = PlacementStrategyFactory.getPlacementStrategy(pStrategyType, this, file);
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		close();
	}
	
	@Override
	public IVirtualDirectory getRootDirectory() {
		return pStrategy.getRootDirectory();
	}
	
	@Override
	public void close() {
		if (file != null) {
			try {
				file.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void setMaxSize(long maxSize) {
		long fileLength = 0;
		try {
			fileLength = file.length();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (fileLength > maxSize || maxSize < MIN_SIZE_IN_BYTES) {
			throw new IllegalArgumentException("Virtual file system can't be smaller than " + Math.max(maxSize, fileLength));
		}
		this.maxSize = maxSize;
	}

	@Override
	public long getMaxSize() {
		return maxSize;
	}

	@Override
	public long getSize() {
		try {
			return file.length();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public int getSuperblockSize() {
		return SUPERBLOCK_SIZE;
	}
	
	
	
}
