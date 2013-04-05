package ch.se.inf.ethz.jcd.batman.vdisk;

import java.io.IOException;

/**
 * Represents a block containing metadata and data on the virtual Disk.
 * 
 * In contrast to a {@link IFreeBlock} this block contains data that is
 * meaningful.
 * 
 * <i>Metadata</i> contains information about the block itself and other blocks
 * which are in some kind of relation to the block.
 * 
 * <i>Data</i> is some kind of data that has to be stored on the virtual disk.
 * 
 */
public interface IDataBlock extends IVirtualBlock {

    /**
     * Writes the content of the provided byte b at position pos.
     * 
     * The position is relative to the beginning of the block. Therefore
     * position 0 is the first byte of the block.
     * 
     * @param pos
     *            position to write the byte into
     * @param b
     *            the byte to write at position pos
     * @throws IOException
     *             TODO
     */
    void write(long pos, byte b) throws IOException;

    /**
     * Writes the content of the provided byte array b into the data block at
     * the given position pos.
     * 
     * The position is relative to the beginning of the block. Therefore
     * position 0 is the first byte of the block.
     * 
     * @param pos
     *            position at which to start writing the content of b
     * @param b
     *            the data to write from position pos
     * @throws IOException
     *             TODO
     */
    void write(long pos, byte[] b) throws IOException;

    /**
     * Writes a long value l at the given position pos.
     * 
     * The position is relative to the beginning of the block. Therefore
     * position 0 is the first byte of the block.
     * 
     * @param pos
     *            position at which the long l should be written
     * @param l
     *            the value to be written at position pos
     * @throws IOException
     *             TODO
     */
    void write(long pos, long l) throws IOException;

    /**
     * Writes the given bytes inside the byte array b into the block starting at
     * the provided position pos.
     * 
     * The offset and length specify which part of the given byte array b will
     * be written into the block.
     * 
     * Position pos is relative to the start of the block. Therefore position 0
     * would be the first byte of the block.
     * 
     * @param pos
     *            the position to start writing the data into
     * @param b
     *            a byte array containing data to write
     * @param offset
     *            the index of the first byte to use from the byte array b
     * @param length
     *            the amount of bytes to write using the given byte array b
     * @throws IOException
     *             TODO
     */
    void write(long pos, byte[] b, int offset, int length) throws IOException;

    /**
     * Reads one byte of data starting at the given position pos.
     * 
     * The position is relative to the beginning of the block. Therefore
     * position 0 is the first byte of the block.
     * 
     * @param pos
     *            position of the byte to be read
     * @return the read byte at position pos
     * @throws IOException
     *             TODO
     */
    byte read(long pos) throws IOException;

    /**
     * Reads the data bytes starting at the given position pos until the
     * provided byte array b is full.
     * 
     * The position is relative to the beginning of the block. Therefore
     * position 0 is the first byte of the block.
     * 
     * @param pos
     *            position to start reading from
     * @param b
     *            byte array to fill the read bytes into
     * @return the amount of read bytes
     * @throws IOException
     *             TODO
     */
    int read(long pos, byte[] b) throws IOException;

    /**
     * Reads data bytes of length <i>length</i> starting at the given position
     * pos.
     * 
     * The read data is written into the provided byte array b starting at the
     * index provided by the offset.
     * 
     * The position is relative to the beginning of the block. Therefore
     * position 0 is the first byte of the block.
     * 
     * @param pos
     *            position to start reading from
     * @param b
     *            the byte array to write the read data into
     * @param offset
     *            the index at which to start writing into the byte array b
     * @param length
     *            the amount of bytes to read
     * @return the amount of bytes read
     * @throws IOException
     *             TODO
     */
    int read(long pos, byte[] b, int offset, int length) throws IOException;

    /**
     * Reads a long starting at the provided position pos.
     * 
     * The position is relative to the beginning of the block. Position 0 is the
     * first byte of the block.
     * 
     * @param pos
     *            position to start reading the long
     * @return the read long value
     * @throws IOException
     *             TODO
     */
    long readLong(long pos) throws IOException;

    /**
     * Returns the address of the next block referenced by the current one.
     * 
     * The address (location) is relative to the beginning of the underlying
     * virtual disk. Therefore position 0 is the first byte of the disk itself.
     * 
     * @see DataBlock#load(IVirtualDisk, long)
     * @return the address of the next block.
     */
    long getNextBlock();

    /**
     * Sets the address of the next block referenced by the current one.
     * 
     * The address (location) is relative to the beginning of the underlying
     * virtual disk. Therefore position 0 is the first byte of the disk itself.
     * 
     * @see #getNextBlock()
     * @param nextBlock
     *            the address of the next block.
     * @throws IOException
     *             TODO
     */
    void setNextBlock(long nextBlock) throws IOException;

    /**
     * Returns the size in bytes that is available for data inside the block.
     * 
     * @return the size in bytes that is available for data.
     */
    long getFreeSize();

    /**
     * Returns the size in bytes of the saved data inside the block. This does
     * not correspond to the size of the block on the disk.
     * 
     * @see #setDataSize(long)
     * @see #getDiskSize()
     * @return the size in bytes of data saved inside the block.
     */
    long getDataSize();

    /**
     * Changes the size of the data inside the block. This can be used to
     * allocate more space for data or to shrink the space.
     * 
     * TODO: cases and how the block reacts
     * 
     * @param size
     * @throws IOException
     */
    void setDataSize(long size) throws IOException;
    
    /**
     * Frees this data blocks on the virtual disk. After freeing the data block
     * it can't be used and it's status is invalid.
     * 
     * @throws IOException
     */
    void free () throws IOException;
    
    /**
     * Returns if the data block is currently valid, or if it has already been freed.
     * 
     * @return true if its still valid 
     */
    boolean isValid ();

}
