package ch.se.inf.ethz.jcd.batman.vdisk;

import java.io.IOException;


public final class DataBlock extends VirtualBlock implements IDataBlock {

	private static final long NEXT_SIZE = 8;
	private static final long DATA_LENGTH_SIZE = 8;

	private static final long METADATA_START_SIZE = VirtualBlock.METADATA_START_SIZE + NEXT_SIZE + DATA_LENGTH_SIZE;
	private static final long METADATA_END_SIZE = VirtualBlock.METADATA_END_SIZE;
	public static final long METADATA_SIZE = METADATA_START_SIZE + METADATA_END_SIZE;
	public static final long MIN_BLOCK_SIZE = METADATA_SIZE + 1;
	
	public static IDataBlock load (final IVirtualDisk disk, final long position) throws IOException {
		final DataBlock block = new DataBlock(disk, position, 0, 0, 0);
		block.readMetadata();
		block.valid = true;
		return block;
	}
	
	public static IDataBlock create (final IVirtualDisk disk, final long position, final long size, final long dataSize, final long next) throws IOException {
		final DataBlock block = new DataBlock(disk, position, size, dataSize, next);
		block.updateMetadata();
		block.valid = true;
		return block;
	}
	
	private transient long next;
	private long dataSize;
	private boolean valid;
	
	private DataBlock(final IVirtualDisk disk, final long position, final long size, final long dataSize, final long next) throws IOException {
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
		
	private void checkDataRange (final long pos, final int length) {
		if (pos < 0 || (pos + length) > dataSize) {
			throw new IllegalArgumentException("Illegal data range");
		}
	}
	
	@Override
	public void write(final long pos, final byte data) throws IOException {
		checkValidTrue();
		checkDataRange(pos, BYTE_LENGTH);
		writeRealPosition(pos + METADATA_START_SIZE, data);
	}

	@Override
	public void write(final long pos, final byte[] data) throws IOException {
		checkValidTrue();
		checkDataRange(pos, data.length);
		writeRealPosition(pos + METADATA_START_SIZE, data);
	}

	@Override
	public void write(final long pos, final long data) throws IOException {
		checkValidTrue();
		checkDataRange(pos, LONG_LENGTH);
		writeRealPosition(pos + METADATA_START_SIZE, data);
	}
	
	@Override
	public byte read(final long pos) throws IOException {
		checkValidTrue();
		checkDataRange(pos, BYTE_LENGTH);
		return readRealPosition(pos + METADATA_START_SIZE);
	}

	@Override
	public int read(final long pos, final byte[] data) throws IOException {
		checkValidTrue();
		checkDataRange(pos, data.length);
		return readRealPosition(pos + METADATA_START_SIZE, data);
	}

	@Override
	public long readLong(final long pos) throws IOException {
		checkValidTrue();
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
	public void setDataSize(final long size) throws IOException {
		checkValidTrue();
		this.dataSize = size;
		updateDataSize();
	}

	@Override
	public void write(final long pos, final byte[] b, final int offset, final int length)
			throws IOException {
		checkValidTrue();
		checkDataRange(pos, length);
		writeRealPosition(pos + METADATA_START_SIZE, b, offset, length);
	}

	@Override
	public int read(final long pos, final byte[] data, final int offset, final int length)
			throws IOException {
		checkValidTrue();
		checkDataRange(pos, length);
		return readRealPosition(pos + METADATA_START_SIZE, data, offset, length);
	}

	@Override
	public void setNextBlock(final long nextBlock) throws IOException {
		checkValidTrue();
		this.next = nextBlock;
		updateNextBlock();
	}

	@Override
	protected long setMetaFlagsOnSize(final long size) {
		return VirtualBlock.setAllocatedFlag(size, true);
	}

	/**
	 * Throws a {@link VirtualDiskException} if the status is not valid.
	 * 
	 * @throws VirtualDiskException if the status is not valid
	 */
	private void checkValidTrue() throws VirtualDiskException {
		if (!isValid()) {
			throw new VirtualDiskException("Block is not valid.");
		}
	}
	
	@Override
	public boolean isValid () {
		return valid;
	}

	@Override
	public void free() throws IOException {
		if (isValid()) {
			getDisk().freeBlock(this);
			valid = false;
		}
	}

}
