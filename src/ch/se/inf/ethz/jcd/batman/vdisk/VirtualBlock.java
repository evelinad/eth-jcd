package ch.se.inf.ethz.jcd.batman.vdisk;

import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class VirtualBlock implements IVirtualBlock {

	public static final IVirtualBlock loadPreviousBlock (IVirtualDisk disk, long position) throws IOException {
		long previousBlockSize = removeMetaFlagFromSize(disk.read(position - BLOCK_LENGTH_SIZE));
		return loadBlock(disk, position - previousBlockSize);
	}
	
	public static final IVirtualBlock loadNextBlock (IVirtualDisk disk, long position) throws IOException {
		long currentBlockSize = removeMetaFlagFromSize(disk.read(position));
		return loadBlock(disk, position + currentBlockSize);
	}
	
	public static final IVirtualBlock loadBlock (IVirtualDisk disk, long position) throws IOException {
		byte size = disk.read(position);
		if (checkIfAllocatedFlagSet(size)) {
			return DataBlock.load(disk, position);
		} else {
			return FreeBlock.loadBlock(disk, position);
		}
	}
	
	public static final long getSizeOfNextBlock (IVirtualDisk disk, long position) throws IOException {
		long currentBlockSize = removeMetaFlagFromSize(disk.read(position));
		return removeMetaFlagFromSize(disk.read(position + currentBlockSize));
	}
	
	public static final long getSizeOfPreviousBlock (IVirtualDisk disk, long position) throws IOException {
		return removeMetaFlagFromSize(disk.read(position - BLOCK_LENGTH_SIZE));
	}
	
	public static final boolean checkIfPreviousFree (IVirtualDisk disk, long position) throws IOException {
		return checkIfAllocatedFlagSet(disk.read(position - BLOCK_LENGTH_SIZE));
	}
	
	public static final boolean checkIfNextFree (IVirtualDisk disk, long position) throws IOException {
		long currentBlockSize = removeMetaFlagFromSize(disk.read(position));
		return checkIfAllocatedFlagSet(disk.read(position + currentBlockSize));
	}
	
	protected static final long setAllocatedFlag (long l, boolean allocated) {
		if (allocated) {
			return l | IN_USE_MASK;
		} else {
			return l & ~IN_USE_MASK;
		}
	}
	
	private static final long removeMetaFlagFromSize (long l) {
		return l & ~IN_USE_MASK;
	}
	
	private static final boolean checkIfAllocatedFlagSet (long l) {
		return ((l & IN_USE_MASK) != 0);
	}
	
	protected static final long BLOCK_LENGTH_SIZE = 8;

	protected static final long METADATA_START_SIZE = BLOCK_LENGTH_SIZE;
	protected static final long METADATA_END_SIZE = BLOCK_LENGTH_SIZE;
	protected static final long METADATA_SIZE = METADATA_START_SIZE + METADATA_END_SIZE;
	
	protected static final int BYTE_LENGTH = 1;
	protected static final int LONG_LENGTH = 8;
	
	private static final long IN_USE_MASK = 0x8000000000000000l;
	
	private IVirtualDisk disk;
	private long blockPosition;
	private long size;
	
	protected VirtualBlock(IVirtualDisk disk, long position, long size) throws IOException {
		this.disk = disk;
		this.blockPosition = position;
		this.size = size;
	}
	
	protected abstract long setMetaFlagsOnSize (long size);
	
	protected void readMetadata () throws IOException {
		size = removeMetaFlagFromSize(readLongRealPosition(0));
	}
	
	protected void updateMetadata () throws IOException {
		updateDiskSize();
	}
	
	protected void updateDiskSize () throws IOException {
		long size = setMetaFlagsOnSize(getDiskSize());
		writeRealPosition(0, size);
		writeRealPosition(getDiskSize() - BLOCK_LENGTH_SIZE, size);
	}
	
	protected IVirtualDisk getDisk () {
		return disk;
	}

	@Override
	public long getBlockPosition() {
		return blockPosition;
	}

	@Override
	public long getDiskSize() {
		return size;
	}

	protected long readLongRealPosition (long pos) throws IOException {
		byte[] longInBytes = new byte[LONG_LENGTH];
		disk.read(blockPosition + pos, longInBytes);
		return ByteBuffer.wrap(longInBytes).getLong();
	}
	
	protected void writeRealPosition (long pos, long l) throws IOException {
		disk.write(blockPosition + pos, ByteBuffer.allocate(8).putLong(l).array());
	}

	protected void writeRealPosition (long pos, byte b) throws IOException {
		disk.write(blockPosition + pos, b);
	}
	
	protected void writeRealPosition (long pos, byte[] b) throws IOException {
		disk.write(blockPosition + pos, b);
	}
	
	protected void writeRealPosition (long pos, byte[] b, int offset, int length) throws IOException {
		disk.write(blockPosition + pos, b, offset, length);
	}
	
	protected byte readRealPosition (long pos) throws IOException {
		return disk.read(blockPosition + pos);
	}
	
	protected int readRealPosition (long pos, byte[] b) throws IOException {
		return disk.read(blockPosition + pos,  b);
	}
	
	protected int readRealPosition (long pos, byte[] b, int offset, int length) throws IOException {
		return disk.read(blockPosition + pos, b, offset, length);
	}

}