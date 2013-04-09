package ch.se.inf.ethz.jcd.batman.vdisk.impl;

import java.io.IOException;

import ch.se.inf.ethz.jcd.batman.vdisk.IDataBlock;
import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualBlock;
import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualDisk;
import ch.se.inf.ethz.jcd.batman.vdisk.VirtualDiskException;

/**
 * Implementation of {@link IDataBlock}.
 * 
 * The data blocks are stored as a linked list and each data block has a pointer
 * to the next block in the list.
 * 
 * The blocks need at 32 byte to store the meta data the rest can be used for
 * the data. The structure of the block is as follows: 0x00 8 byte size and
 * indicator for data block 0x08 8 byte offset of the next data block in the
 * list 0x10 8 number of bytes in the data block which are used for data
 * (maximum of data block size - meta data size) 0xnn n data end 8 byte size and
 * indicator for data block
 * 
 * @see IVirtualBlock
 * @see IDataBlock
 */
public final class DataBlock extends VirtualBlock implements IDataBlock {

    private static final long NEXT_SIZE = 8;
    private static final long DATA_LENGTH_SIZE = 8;

    private static final long METADATA_START_SIZE = VirtualBlock.METADATA_START_SIZE
            + NEXT_SIZE + DATA_LENGTH_SIZE;
    private static final long METADATA_END_SIZE = VirtualBlock.METADATA_END_SIZE;
    /**
     * The amount of bytes needed to store the meta data of the data block.
     */
    public static final long METADATA_SIZE = METADATA_START_SIZE
            + METADATA_END_SIZE;
    /**
     * The size of the smallest possible data block.
     */
    public static final long MIN_BLOCK_SIZE = METADATA_SIZE + 1;

    /**
     * Loads the data block stored at the offset position given by position.
     * 
     * @param disk
     *            the disk on which the block is stored
     * @param position
     *            the offset position in bytes of the block
     * @return the loaded block
     * @throws IOException
     *             if the block at the given position is invalid or if an I/O
     *             error occurs
     */
    public static IDataBlock load(final IVirtualDisk disk, final long position)
            throws IOException {
        final DataBlock block = new DataBlock(disk, position, 0, 0, 0);
        block.readMetadata();
        block.valid = true;
        return block;
    }

    /**
     * Creates a data block at the given offset position.
     * 
     * @param disk
     *            the disk on which the block is created
     * @param position
     *            the offset position in bytes of the block
     * @param size
     *            the size of the newly created block
     * @param dataSize
     *            the size in the block which can be used to store data
     * @param next
     *            the offset position of the next block in the list
     * @return the created block
     * @throws IOException
     *             if an I/O error occurs
     */
    public static IDataBlock create(final IVirtualDisk disk,
            final long position, final long size, final long dataSize,
            final long next) throws IOException {
        final DataBlock block = new DataBlock(disk, position, size, dataSize,
                next);
        block.updateMetadata();
        block.valid = true;
        return block;
    }

    private transient long next;
    private long dataSize;
    private boolean valid;

    private DataBlock(final IVirtualDisk disk, final long position,
            final long size, final long dataSize, final long next)
            throws IOException {
        super(disk, position, size);
        this.dataSize = dataSize;
        this.next = next;
    }

    protected void readMetadata() throws IOException {
        super.readMetadata();
        next = readLongRealPosition(VirtualBlock.METADATA_START_SIZE);
        dataSize = readLongRealPosition(VirtualBlock.METADATA_START_SIZE
                + NEXT_SIZE);
    }

    protected void updateMetadata() throws IOException {
        super.updateMetadata();
        updateNextBlock();
        updateDataSize();
    }

    private void updateNextBlock() throws IOException {
        writeRealPosition(VirtualBlock.METADATA_START_SIZE, next);
    }

    private void updateDataSize() throws IOException {
        writeRealPosition(VirtualBlock.METADATA_START_SIZE + NEXT_SIZE,
                dataSize);
    }

    private void checkDataRange(final long pos, final int length) {
        if (pos < 0 || (pos + length) > dataSize) {
            throw new IllegalArgumentException("Illegal data range");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(final long pos, final byte data) throws IOException {
        checkValidTrue();
        checkDataRange(pos, BYTE_LENGTH);
        writeRealPosition(pos + METADATA_START_SIZE, data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(final long pos, final byte[] data) throws IOException {
        checkValidTrue();
        checkDataRange(pos, data.length);
        writeRealPosition(pos + METADATA_START_SIZE, data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeLong(final long pos, final long data) throws IOException {
        checkValidTrue();
        checkDataRange(pos, LONG_LENGTH);
        writeRealPosition(pos + METADATA_START_SIZE, data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte read(final long pos) throws IOException {
        checkValidTrue();
        checkDataRange(pos, BYTE_LENGTH);
        return readRealPosition(pos + METADATA_START_SIZE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read(final long pos, final byte[] data) throws IOException {
        checkValidTrue();
        checkDataRange(pos, data.length);
        return readRealPosition(pos + METADATA_START_SIZE, data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long readLong(final long pos) throws IOException {
        checkValidTrue();
        checkDataRange(pos, LONG_LENGTH);
        return readLongRealPosition(pos + METADATA_START_SIZE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getNextBlock() {
        return next;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getDataSize() {
        return dataSize;
    }

    private long getMaxDataSize() {
        return getDiskSize() - METADATA_SIZE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getFreeSize() {
        return getMaxDataSize() - dataSize;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDataSize(final long size) throws IOException {
        checkValidTrue();
        this.dataSize = size;
        updateDataSize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(final long pos, final byte[] b, final int offset,
            final int length) throws IOException {
        checkValidTrue();
        checkDataRange(pos, length);
        writeRealPosition(pos + METADATA_START_SIZE, b, offset, length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read(final long pos, final byte[] data, final int offset,
            final int length) throws IOException {
        checkValidTrue();
        checkDataRange(pos, length);
        return readRealPosition(pos + METADATA_START_SIZE, data, offset, length);
    }

    /**
     * {@inheritDoc}
     */
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
     * @throws VirtualDiskException
     *             if the status is not valid
     */
    private void checkValidTrue() throws VirtualDiskException {
        if (!isValid()) {
            throw new VirtualDiskException("Block is not valid.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValid() {
        return valid;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void free() throws IOException {
        if (isValid()) {
            getDisk().freeBlock(this);
            valid = false;
        }
    }

}
