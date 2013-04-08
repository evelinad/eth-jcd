package ch.se.inf.ethz.jcd.batman.vdisk.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import ch.se.inf.ethz.jcd.batman.vdisk.IDataBlock;
import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualDisk;
import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualDiskSpace;
import ch.se.inf.ethz.jcd.batman.vdisk.VirtualDiskException;

/**
 * A {@link IVirtualDiskSpace} is a simple abstraction of a list of
 * {@link IDataBlock}.
 * 
 * A virtual disk space allows to work with a continuous space on the disk
 * without handling the jumps between data blocks and the management that is
 * connected with it.
 * 
 */
public class VirtualDiskSpace implements IVirtualDiskSpace {

    /**
     * Loads a VirtualDiskSpace located at the given offset position in the
     * {@link IVirtualDisk}.
     * 
     * @param disk
     *            the disk from which the VirtualDiskSpace should be loaded.
     * @param position
     *            the offset position in bytes at which the VirtualDiskSpace is
     *            stored.
     * @return the loaded VirtualDiskSpace
     * @throws IOException
     *             if an I/O error occurs
     */
    public static IVirtualDiskSpace load(IVirtualDisk disk, long position)
            throws IOException {
        VirtualDiskSpace virtualSpace = new VirtualDiskSpace(disk);
        virtualSpace.load(position);
        return virtualSpace;
    }

    /**
     * Creates a {@link VirtualDiskSpace} at the given offset position in the
     * {@link IVirtualDisk} with the specified amount of space in bytes.
     * 
     * @param disk
     *            the disk in which the {@link VirtualDiskSpace} should be
     *            created.
     * @param size
     *            the size of the newly created {@link VirtualDiskSpace}
     * @return the newly created {@link VirtualDiskSpace}
     * @throws IOException
     *             if an I/O error occurs
     */
    public static IVirtualDiskSpace create(IVirtualDisk disk, long size)
            throws IOException {
        VirtualDiskSpace virtualSpace = new VirtualDiskSpace(disk);
        virtualSpace.create(size);
        return virtualSpace;
    }

    /**
     * Stores the {@link IDataBlock} index and offset for an offset in the
     * {@link VirtualDiskSpace}.
     */
    private static class VirtualDiskSpacePosition {

        private long position;
        private int blockIndex;
        private long blockPosition;

        public long getPosition() {
            return position;
        }

        public void setPosition(long position) {
            this.position = position;
        }

        public int getBlockIndex() {
            return blockIndex;
        }

        public void setBlockIndex(int blockIndex) {
            this.blockIndex = blockIndex;
        }

        public long getBlockPosition() {
            return blockPosition;
        }

        public void setBlockPosition(long blockPosition) {
            this.blockPosition = blockPosition;
        }

    }

    private static final int BYTE_LENGTH = 1;
    private static final int LONG_LENGTH = 8;

    private final IVirtualDisk disk;
    private final List<IDataBlock> blocks = new ArrayList<IDataBlock>();
    private VirtualDiskSpacePosition position;

    private VirtualDiskSpace(IVirtualDisk disk) throws IOException {
        this.disk = disk;
        this.position = new VirtualDiskSpacePosition();
    }

    private void create(long size) throws IOException {
        changeSize(size);
    }

    private void load(long position) throws IOException {
        IDataBlock block;
        for (block = DataBlock.load(disk, position); block.getNextBlock() != 0; block = DataBlock
                .load(disk, block.getNextBlock())) {
            blocks.add(block);
        }
        blocks.add(block);
    }

    private VirtualDiskSpacePosition calculatePosition(long position) {
        VirtualDiskSpacePosition vDiskPosition = new VirtualDiskSpacePosition();
        vDiskPosition.setPosition(position);
        long blockPosition = position;
        int index = 0;
        for (; index < blocks.size(); index++) {
            IDataBlock block = blocks.get(index);
            if (block.getDataSize() <= blockPosition) {
                blockPosition -= block.getDataSize();
            } else {
                break;
            }
        }
        vDiskPosition.setBlockIndex(index);
        vDiskPosition.setBlockPosition(blockPosition);
        return vDiskPosition;
    }

    private VirtualDiskSpacePosition addPosition(VirtualDiskSpacePosition base,
            long add) {
        VirtualDiskSpacePosition newPosition = new VirtualDiskSpacePosition();
        newPosition.setPosition(base.getPosition() + add);
        int index = base.getBlockIndex();
        long blockPosition = base.getBlockPosition() + add;
        for (; index < blocks.size(); index++) {
            IDataBlock block = blocks.get(index);
            if (block.getDataSize() <= blockPosition) {
                blockPosition -= block.getDataSize();
            } else {
                break;
            }
        }
        newPosition.setBlockIndex(index);
        newPosition.setBlockPosition(blockPosition);
        return newPosition;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getVirtualDiskPosition() {
        return blocks.get(0).getBlockPosition();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void changeSize(long newSize) throws IOException {
        if (newSize < 1) {
            throw new IllegalArgumentException(
                    "Virtual space can't be smaller than 1");
        }
        long currentSize = getSize();
        if (currentSize < newSize) {
            extend(newSize - currentSize);
        } else if (currentSize > newSize) {
            truncate(currentSize - newSize);
        }
    }

    private void truncate(long amount) throws IOException {
        IDataBlock freeBlock = null;
        long truncateAmount = amount;
        for (int i = blocks.size() - 1; truncateAmount > 0 && i >= 0; i--) {
            IDataBlock block = blocks.get(i);
            long dataSize = block.getDataSize();
            if (truncateAmount > dataSize) {
                blocks.remove(i);
                truncateAmount -= dataSize;
                freeBlock = block;
            } else {
                block.setDataSize(dataSize - truncateAmount);
                truncateAmount = 0;
            }
        }
        if (freeBlock != null) {
            disk.freeBlock(freeBlock);
        }
    }

    private void extend(long amount) throws IOException {
        // Use the the last block if there is still some free space
        IDataBlock lastBlock = getLastBlock();
        long extendAmount = amount;
        if (lastBlock != null) {
            long freeSize = lastBlock.getFreeSize();
            if (freeSize >= 0) {
                long extendSize = Math.min(extendAmount, freeSize);
                lastBlock.setDataSize(lastBlock.getDataSize() + extendSize);
                extendAmount -= extendSize;
            }
        }
        // Request the rest from the disk and add it to the list
        if (extendAmount > 0) {
            IDataBlock[] allocatetBlocks = disk.allocateBlock(extendAmount);
            if (lastBlock != null) {
                lastBlock.setNextBlock(allocatetBlocks[0].getBlockPosition());
            }
            for (int i = 0; i < allocatetBlocks.length; i++) {
                blocks.add(allocatetBlocks[i]);
            }
        }
    }

    private IDataBlock getLastBlock() {
        return blocks.isEmpty() ? null : blocks.get(blocks.size() - 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getSize() {
        long size = 0;
        for (IDataBlock block : blocks) {
            size += block.getDataSize();
        }
        return size;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getDiskSize() {
        long diskSize = 0;
        for (IDataBlock block : blocks) {
            diskSize += block.getDiskSize();
        }
        return diskSize;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getPosition() {
        return position.getPosition();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void seek(long pos) {
        position = calculatePosition(pos);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(byte b) throws IOException {
        write(position, b);
        position = addPosition(position, BYTE_LENGTH);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeLong(long l) throws IOException {
        write(position, l);
        position = addPosition(position, LONG_LENGTH);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(byte[] b) throws IOException {
        write(position, b);
        position = addPosition(position, b.length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(long pos, byte b) throws IOException {
        write(calculatePosition(pos), b);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeLong(long pos, long l) throws IOException {
        write(calculatePosition(pos), l);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(long pos, byte[] b) throws IOException {
        write(calculatePosition(pos), b);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte read() throws IOException {
        byte b = read(position);
        position = addPosition(position, BYTE_LENGTH);
        return b;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read(byte[] b) throws IOException {
        int l = read(position, b);
        position = addPosition(position, b.length);
        return l;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long readLong() throws IOException {
        long l = readLong(position);
        position = addPosition(position, LONG_LENGTH);
        return l;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte read(long pos) throws IOException {
        return read(calculatePosition(pos));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read(long pos, byte[] b) throws IOException {
        return read(calculatePosition(pos), b);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long readLong(long pos) throws IOException {
        return readLong(calculatePosition(pos));
    }

    private long getRemainingSpace(IDataBlock block, long blockPosition) {
        return block.getDataSize() - blockPosition;
    }

    private long getRemainingSpaceInBlock(VirtualDiskSpacePosition pos) {
        if (pos.getBlockIndex() < blocks.size()) {
            return getRemainingSpace(blocks.get(pos.getBlockIndex()),
                    pos.getBlockPosition());
        } else {
            return 0;
        }
    }

    private long getRemainingSpace(VirtualDiskSpacePosition pos) {
        long remainingSpace = getRemainingSpaceInBlock(pos);
        for (int index = pos.getBlockIndex() + 1; index < blocks.size(); index++) {
            remainingSpace += getDataBlock(index).getDataSize();
        }
        return remainingSpace;
    }

    private IDataBlock getDataBlock(VirtualDiskSpacePosition pos) {
        return blocks.get(pos.getBlockIndex());
    }

    private IDataBlock getDataBlock(int index) {
        return blocks.get(index);
    }

    private void write(VirtualDiskSpacePosition pos, long l) throws IOException {
        write(pos, ByteBuffer.allocate(8).putLong(l).array());
    }

    private boolean allocateSpace(VirtualDiskSpacePosition pos, long length)
            throws IOException {
        long currentSize = getSize();
        long sizeNeeded = pos.getPosition() + length;
        if (currentSize < sizeNeeded) {
            extend(sizeNeeded - currentSize);
            return true;
        }
        return false;
    }

    private void write(VirtualDiskSpacePosition pos, byte b) throws IOException {
        VirtualDiskSpacePosition currentPos = pos;
        if (allocateSpace(currentPos, BYTE_LENGTH)) {
            // If the disk space changed, the position needs to be recalculated
            currentPos = calculatePosition(currentPos.getPosition());
        }
        getDataBlock(currentPos).write(currentPos.getBlockPosition(), b);
    }

    private void write(VirtualDiskSpacePosition pos, byte[] b)
            throws IOException {
        VirtualDiskSpacePosition currentPos = pos;
        if (allocateSpace(pos, b.length)) {
            // If the disk space changed, the position needs to be recalculated
            currentPos = calculatePosition(currentPos.getPosition());
        }
        int bytesWritten = 0;
        while (bytesWritten != b.length) {
            long remainingSpace = getRemainingSpaceInBlock(currentPos);
            if (remainingSpace == 0) {
                throw new VirtualDiskException("DiskSpace too small!");
            }
            int bytesToWrite = b.length - bytesWritten;
            int currentBytesWritten = 0;
            if (bytesToWrite <= remainingSpace) {
                getDataBlock(currentPos).write(currentPos.getBlockPosition(),
                        b, bytesWritten, bytesToWrite);
                currentBytesWritten = bytesToWrite;
            } else {
                getDataBlock(currentPos).write(currentPos.getBlockPosition(),
                        b, bytesWritten, (int) remainingSpace);
                currentBytesWritten = (int) remainingSpace;
            }
            bytesWritten += currentBytesWritten;
            currentPos = addPosition(currentPos, currentBytesWritten);
        }
    }

    private byte read(VirtualDiskSpacePosition pos) throws IOException {
        if (getRemainingSpaceInBlock(pos) <= 0) {
            throw new VirtualDiskException("End of VirtualSpace reached.");
        }
        return getDataBlock(pos).read(pos.getBlockPosition());
    }

    private int read(VirtualDiskSpacePosition pos, byte[] b) throws IOException {
        VirtualDiskSpacePosition currentPos = pos;
        long readableBytes = getRemainingSpace(currentPos);
        int readLength = (int) Math.min(b.length, readableBytes);
        int bytesRead = 0;
        while (bytesRead < readLength) {
            long remainingSpace = getRemainingSpaceInBlock(currentPos);
            int bytesToRead = readLength - bytesRead;
            int currentBytesRead = 0;
            if (bytesToRead <= remainingSpace) {
                getDataBlock(currentPos).read(currentPos.getBlockPosition(), b,
                        bytesRead, bytesToRead);
                currentBytesRead = bytesToRead;
            } else {
                getDataBlock(currentPos).read(currentPos.getBlockPosition(), b,
                        bytesRead, (int) remainingSpace);
                currentBytesRead = (int) remainingSpace;
            }
            bytesRead += currentBytesRead;
            currentPos = addPosition(currentPos, currentBytesRead);
        }
        return bytesRead;
    }

    private long readLong(VirtualDiskSpacePosition pos) throws IOException {
        byte[] longInBytes = new byte[LONG_LENGTH];
        read(pos, longInBytes);
        return ByteBuffer.wrap(longInBytes).getLong();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void free() throws IOException {
        for (IDataBlock block : blocks) {
            block.free();
        }
    }

}
