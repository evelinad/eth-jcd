package ch.se.inf.ethz.jcd.batman.vdisk;

import java.io.IOException;


public class DataBlock extends VirtualBlock implements IDataBlock {

	public static final IDataBlock load (IVirtualDisk disk, long position) throws IOException {
		DataBlock block = new DataBlock(disk, position, 0, 0, 0);
		block.readMetadata();
		return block;
	}
	
	public static final IDataBlock create (IVirtualDisk disk, long position, long size, long dataSize, long next) throws IOException {
		DataBlock block = new DataBlock(disk, position, size, dataSize, next);
		block.updateMetadata();
		return block;
	}
	
	private static final long NEXT_SIZE = 8;
	private static final long DATA_LENGTH_SIZE = 8;

	protected static final long METADATA_START_SIZE = VirtualBlock.METADATA_START_SIZE + NEXT_SIZE + DATA_LENGTH_SIZE;
	protected static final long METADATA_END_SIZE = VirtualBlock.METADATA_END_SIZE;
	protected static final long METADATA_SIZE = METADATA_START_SIZE + METADATA_END_SIZE;
	protected static final long MIN_BLOCK_SIZE = METADATA_SIZE + 1;
	
	private long next;
	private long dataSize;
	
	private DataBlock(IVirtualDisk disk, long position, long size, long dataSize, long next) throws IOException {
		super(disk, position, size);
		this.dataSize = dataSize;
		this.next = next;
	}
	
	protected void readMetadata () throws IOException {
		super.readMetadata();
		next = readLongRealPosition(VirtualBlock.METADATA_START_SIZE);
		dataSize = readLongRealPosition(VirtualBlock.METADATA_START_SIZE + NEXT_SIZE);
	}
	
	protected void updateMetadata () throws IOException {
		super.updateMetadata();
		updateNextBlock();
		updateDataSize();
	}
	
	private void updateNextBlock () throws IOException {
		writeRealPosition(VirtualBlock.METADATA_START_SIZE, next);
	}
	
	private void updateDataSize () throws IOException {
		writeRealPosition(VirtualBlock.METADATA_START_SIZE + NEXT_SIZE, dataSize);
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
	
	private long getMaxDataSize () {
		return getDiskSize() - METADATA_SIZE;
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

	@Override
	protected long setMetaFlagsOnSize(long size) {
		return VirtualBlock.setAllocatedFlag(size, true);
	}

}
