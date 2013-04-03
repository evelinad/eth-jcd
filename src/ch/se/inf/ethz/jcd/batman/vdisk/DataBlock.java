package ch.se.inf.ethz.jcd.batman.vdisk;

import java.io.IOException;
import java.nio.ByteBuffer;


public class DataBlock implements IDataBlock {

	public static final IDataBlock load (IVirtualDisk disk, long position) throws IOException {
		return new DataBlock(disk, position);
	}
	
	public static final IDataBlock create (IVirtualDisk disk, long position, long size, long dataSize, long next) throws IOException {
		return new DataBlock(disk, position, size, dataSize, next);
	}
	
	private static final long BLOCK_LENGTH_SIZE = 8;
	private static final long NEXT_SIZE = 8;
	private static final long DATA_LENGTH_SIZE = 8;

	private static final long METADATA_START_SIZE = BLOCK_LENGTH_SIZE + NEXT_SIZE + DATA_LENGTH_SIZE;
	private static final long METADATA_END_SIZE = BLOCK_LENGTH_SIZE;
	private static final long METADATA_SIZE = METADATA_START_SIZE + METADATA_END_SIZE;
	public static final long MIN_BLOCK_SIZE = METADATA_SIZE + 1;
	
	private static final int BYTE_LENGTH = 1;
	private static final int LONG_LENGTH = 8;
	
	private static final long IN_USE_MASK = 0x8000000000000000l;
	
	private IVirtualDisk disk;
	private long blockPosition;
	private long size;
	private long next;
	private long dataSize;
	
	public DataBlock(IVirtualDisk disk, long position) throws IOException {
		this.disk = disk;
		this.blockPosition = position;
		readMetadata();
	}
	
	public DataBlock(IVirtualDisk disk, long position, long size, long dataSize, long next) throws IOException {
		this.disk = disk;
		this.blockPosition = position;
		this.size = size;
		this.dataSize = dataSize;
		this.next = next;
		updateMetadata();
	}
	
	private void readMetadata () throws IOException {
		size = readLongRealPosition(0) & ~IN_USE_MASK;
		next = readLongRealPosition(BLOCK_LENGTH_SIZE);
		dataSize = readLongRealPosition(BLOCK_LENGTH_SIZE + NEXT_SIZE);
	}
	
	private void updateMetadata () throws IOException {
		long size = getDiskSize() | IN_USE_MASK;
		writeRealPosition(0, size);
		writeRealPosition(getDiskSize() - BLOCK_LENGTH_SIZE, size);
		updateNextBlock();
		updateDataSize();
	}
	
	private void updateNextBlock () throws IOException {
		writeRealPosition(BLOCK_LENGTH_SIZE, next);
	}
	
	private void updateDataSize () throws IOException {
		writeRealPosition(BLOCK_LENGTH_SIZE + NEXT_SIZE, dataSize);
	}
	
	private void writeRealPosition (long pos, byte b) throws IOException {
		disk.write(blockPosition + pos, b);
	}
	
	private void writeRealPosition (long pos, byte[] b) throws IOException {
		disk.write(blockPosition + pos, b);
	}
	
	private void writeRealPosition (long pos, long l) throws IOException {
		disk.write(blockPosition + pos, ByteBuffer.allocate(8).putLong(l).array());
	}
	
	private void writeRealPosition (long pos, byte[] b, int offset, int length) throws IOException {
		disk.write(blockPosition + pos, b, offset, length);
	}
	
	private byte readRealPosition (long pos) throws IOException {
		return disk.read(blockPosition + pos);
	}
	
	private int readRealPosition (long pos, byte[] b) throws IOException {
		return disk.read(blockPosition + pos,  b);
	}
	
	private long readLongRealPosition (long pos) throws IOException {
		byte[] longInBytes = new byte[LONG_LENGTH];
		disk.read(blockPosition + pos, longInBytes);
		return ByteBuffer.wrap(longInBytes).getLong();
	}
	
	private int readRealPosition (long pos, byte[] b, int offset, int length) throws IOException {
		return disk.read(blockPosition + pos, b, offset, length);
	}
	
	private void checkDataRange (long pos, int length) {
		if (pos < 0 || (pos + length) > dataSize) {
			throw new RuntimeException("Illegal data range");
		}
	}
	
	@Override
	public void write(long pos, byte b) throws IOException {
		checkDataRange(pos, BYTE_LENGTH);
		writeRealPosition(pos + METADATA_START_SIZE, b);
	}

	@Override
	public void write(long pos, byte[] b) throws IOException {
		checkDataRange(pos, b.length);
		writeRealPosition(pos + METADATA_START_SIZE, b);
	}

	@Override
	public void write(long pos, long l) throws IOException {
		checkDataRange(pos, LONG_LENGTH);
		writeRealPosition(pos + METADATA_START_SIZE, l);
	}
	
	@Override
	public byte read(long pos) throws IOException {
		checkDataRange(pos, BYTE_LENGTH);
		return readRealPosition(pos + METADATA_START_SIZE);
	}

	@Override
	public int read(long pos, byte[] b) throws IOException {
		checkDataRange(pos, b.length);
		return readRealPosition(pos + METADATA_START_SIZE, b);
	}

	@Override
	public long readLong(long pos) throws IOException {
		checkDataRange(pos, LONG_LENGTH);
		return readLongRealPosition(pos + METADATA_START_SIZE);
	}
	
	@Override
	public long getNextBlock() {
		return next;
	}

	@Override
	public long getDataSize() {
		return dataSize;
	}

	@Override
	public long getBlockPosition() {
		return blockPosition;
	}

	@Override
	public long getDiskSize() {
		return size;
	}

	private long getMaxDataSize () {
		return size - METADATA_SIZE;
	}
	
	@Override
	public long getFreeSize() {
		return getMaxDataSize() - dataSize;
	}

	@Override
	public void setDataSize(long size) throws IOException {
		this.dataSize = size;
		updateDataSize();
	}

	@Override
	public void write(long pos, byte[] b, int offset, int length)
			throws IOException {
		checkDataRange(pos, length);
		writeRealPosition(pos + METADATA_START_SIZE, b, offset, length);
	}

	@Override
	public int read(long pos, byte[] b, int offset, int length)
			throws IOException {
		checkDataRange(pos, length);
		return readRealPosition(pos + METADATA_START_SIZE, b, offset, length);
	}

	@Override
	public void setNextBlock(long nextBlock) throws IOException {
		this.next = nextBlock;
		updateNextBlock();
	}

}
