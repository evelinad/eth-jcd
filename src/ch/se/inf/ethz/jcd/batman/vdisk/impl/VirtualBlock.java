package ch.se.inf.ethz.jcd.batman.vdisk.impl;

import java.io.IOException;
import java.nio.ByteBuffer;

import ch.se.inf.ethz.jcd.batman.vdisk.IDataBlock;
import ch.se.inf.ethz.jcd.batman.vdisk.IFreeBlock;
import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualBlock;
import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualDisk;

/**
 * Represents a block on the virtual disk.
 * 
 * This interface provides the common contract that all kinds of blocks have to
 * provide to the outside world.
 * 
 * @see IDataBlock
 * @see IFreeBlock
 * 
 */
public abstract class VirtualBlock implements IVirtualBlock {

    /**
     * Loads the previous block.
     * 
     * @param disk
     *            the disk on which the block is stored
     * @param position
     *            the offset position in bytes of the current block
     * @return the previous block
     * @throws IOException
     *             if an I/O error occurs
     */
    public static final IVirtualBlock loadPreviousBlock(IVirtualDisk disk,
            long position) throws IOException {
        long previousBlockSize = removeMetaFlagFromSize(readLong(disk, position
                - BLOCK_LENGTH_SIZE));
        return loadBlock(disk, position - previousBlockSize);
    }

    /**
     * Loads the next block.
     * 
     * @param disk
     *            the disk on which the block is stored
     * @param position
     *            the offset position in bytes of the current block
     * @return the next block
     * @throws IOException
     *             if an I/O error occurs
     */
    public static final IVirtualBlock loadNextBlock(IVirtualDisk disk,
            long position) throws IOException {
        long currentBlockSize = removeMetaFlagFromSize(readLong(disk, position));
        return loadBlock(disk, position + currentBlockSize);
    }

    /**
     * Loads the block ad the offset position given by position.
     * 
     * @param disk
     *            the disk on which the block is stored.
     * @param position
     *            the offset position of the block
     * @return the loaded block
     * @throws IOException
     *             if an I/O error occurs
     */
    public static final IVirtualBlock loadBlock(IVirtualDisk disk, long position)
            throws IOException {
        long size = readLong(disk, position);
        if (checkIfAllocatedFlagSet(size)) {
            return DataBlock.load(disk, position);
        } else {
            return FreeBlock.load(disk, position);
        }
    }

    /**
     * Return the size in bytes of the next block.
     * 
     * @param disk
     *            the disk on which the block is stored
     * @param position
     *            the offset position of the current block
     * @return the size in bytes of the next block
     * @throws IOException
     *             if an I/O error occurs
     */
    public static final long getSizeOfNextBlock(IVirtualDisk disk, long position)
            throws IOException {
        long currentBlockSize = removeMetaFlagFromSize(readLong(disk, position));
        return removeMetaFlagFromSize(readLong(disk, position
                + currentBlockSize));
    }

    /**
     * Return the size in bytes of the previous block.
     * 
     * @param disk
     *            the disk on which the block is stored
     * @param position
     *            the offset position of the current block
     * @return the size in bytes of the previous block
     * @throws IOException
     *             if an I/O error occurs
     */
    public static final long getSizeOfPreviousBlock(IVirtualDisk disk,
            long position) throws IOException {
        return removeMetaFlagFromSize(readLong(disk, position
                - BLOCK_LENGTH_SIZE));
    }

    /**
     * Checks if the previous block is an instance of {@link IFreeBlock}.
     * 
     * @param disk
     *            the disk on which the block is stored
     * @param position
     *            the position of the current block
     * @return true if the previous block is an instance of {@link IFreeBlock}
     *         otherwise false
     * @throws IOException
     *             if an I/O error occurs
     */
    public static final boolean checkIfPreviousFree(IVirtualDisk disk,
            long position) throws IOException {
        return !checkIfAllocatedFlagSet(readLong(disk, position
                - BLOCK_LENGTH_SIZE));
    }

    /**
     * Checks if the next block is an instance of {@link IFreeBlock}.
     * 
     * @param disk
     *            the disk on which the block is stored
     * @param position
     *            the position of the current block
     * @return true if the next block is an instance of {@link IFreeBlock}
     *         otherwise false
     * @throws IOException
     *             if an I/O error occurs
     */
    public static final boolean checkIfNextFree(IVirtualDisk disk, long position)
            throws IOException {
        long currentBlockSize = removeMetaFlagFromSize(readLong(disk, position));
        return !checkIfAllocatedFlagSet(readLong(disk, position
                + currentBlockSize));
    }

    private static final long readLong(IVirtualDisk disk, long pos)
            throws IOException {
        byte[] longInBytes = new byte[LONG_LENGTH];
        disk.read(pos, longInBytes);
        return ByteBuffer.wrap(longInBytes).getLong();
    }

    protected static final long setAllocatedFlag(long l, boolean allocated) {
        if (allocated) {
            return l | IN_USE_MASK;
        } else {
            return l & ~IN_USE_MASK;
        }
    }

    private static final long removeMetaFlagFromSize(long l) {
        return l & ~IN_USE_MASK;
    }

    private static final boolean checkIfAllocatedFlagSet(long l) {
        return (l & IN_USE_MASK) != 0;
    }

    protected static final long BLOCK_LENGTH_SIZE = 8;

    protected static final long METADATA_START_SIZE = BLOCK_LENGTH_SIZE;
    protected static final long METADATA_END_SIZE = BLOCK_LENGTH_SIZE;
    protected static final long METADATA_SIZE = METADATA_START_SIZE
            + METADATA_END_SIZE;

    protected static final int BYTE_LENGTH = 1;
    protected static final int LONG_LENGTH = 8;

    private static final long IN_USE_MASK = 0x8000000000000000l;

    private final IVirtualDisk disk;
    private final long blockPosition;
    private long size;

    protected VirtualBlock(IVirtualDisk disk, long position, long size)
            throws IOException {
        this.disk = disk;
        this.blockPosition = position;
        this.size = size;
    }

    protected abstract long setMetaFlagsOnSize(long size);

    protected void readMetadata() throws IOException {
        size = removeMetaFlagFromSize(readLongRealPosition(0));
    }

    protected void updateMetadata() throws IOException {
        updateDiskSize();
    }

    protected void updateDiskSize() throws IOException {
        long size = setMetaFlagsOnSize(getDiskSize());
        writeRealPosition(0, size);
        writeRealPosition(getDiskSize() - BLOCK_LENGTH_SIZE, size);
    }

    protected IVirtualDisk getDisk() {
        return disk;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getBlockPosition() {
        return blockPosition;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getDiskSize() {
        return size;
    }

    protected long readLongRealPosition(long pos) throws IOException {
        byte[] longInBytes = new byte[LONG_LENGTH];
        disk.read(blockPosition + pos, longInBytes);
        return ByteBuffer.wrap(longInBytes).getLong();
    }

    protected void writeRealPosition(long pos, long l) throws IOException {
        disk.write(blockPosition + pos, ByteBuffer.allocate(8).putLong(l)
                .array());
    }

    protected void writeRealPosition(long pos, byte b) throws IOException {
        disk.write(blockPosition + pos, b);
    }

    protected void writeRealPosition(long pos, byte[] b) throws IOException {
        disk.write(blockPosition + pos, b);
    }

    protected void writeRealPosition(long pos, byte[] b, int offset, int length)
            throws IOException {
        disk.write(blockPosition + pos, b, offset, length);
    }

    protected byte readRealPosition(long pos) throws IOException {
        return disk.read(blockPosition + pos);
    }

    protected int readRealPosition(long pos, byte[] b) throws IOException {
        return disk.read(blockPosition + pos, b);
    }

    protected int readRealPosition(long pos, byte[] b, int offset, int length)
            throws IOException {
        return disk.read(blockPosition + pos, b, offset, length);
    }

}
