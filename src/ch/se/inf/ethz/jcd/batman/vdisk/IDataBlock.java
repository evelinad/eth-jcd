package ch.se.inf.ethz.jcd.batman.vdisk;

import java.io.IOException;

/**
 * Represents a block containing metadata and data on the virtual disk.
 * 
 * <i>Metadata</i> includes information about the block itself, how it relates
 * to other virtual disk entries and more.
 * 
 * <i>Data</i> is the information stored inside the block. This can for example
 * be part of the content of a file on the virtual disk.
 * 
 */
public interface IDataBlock extends IVirtualBlock {

	/**
	 * Writes the content of the provided byte b at position pos.
	 * 
	 * The position is relative to the beginning of the block. Position 0 is the
	 * first byte of the block.
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
	 * The position is relative to the beginning of the block. Position 0 is the
	 * first byte of the block.
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
	 * The position is relative to the beginning of the block. Position 0 is the
	 * first byte of the block.
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
	 * Reads one byte of data starting at the given position pos.
	 * 
	 * The position is relative to the beginning of the block. Position 0 is the
	 * first byte of the block.
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
	 * The position is relative to the beginning of the block. Position 0 is the
	 * first byte of the block.
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
	 * @see DataBlock#load(IVirtualDisk, long)
	 * @return the address of the next block.
	 */
	long getNextBlock();

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

	public void write(long pos, byte[] b, int offset, int length)
			throws IOException;

	public int read(long pos, byte[] b, int offset, int length)
			throws IOException;

	void setNextBlock(long nextBlock) throws IOException;

}
