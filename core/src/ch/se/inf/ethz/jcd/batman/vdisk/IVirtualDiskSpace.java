package ch.se.inf.ethz.jcd.batman.vdisk;

import java.io.IOException;

/**
 * A {@link IVirtualDiskSpace} is a simple abstraction of a list of
 * {@link IDataBlock}.
 * 
 * A virtual disk space allows to work with a continuous space on the disk
 * without handling the jumps between data blocks and the management that is
 * connected with it.
 * 
 */
public interface IVirtualDiskSpace {

	/**
	 * Returns the position (address) of the space inside the virtual disk.
	 * 
	 * The position 0 would be the first byte of the virtual disk.
	 * 
	 * @return the position (address) of the space inside the virtual disk.
	 */
	long getVirtualDiskPosition();

	/**
	 * Changes the size of the disk space. If the new size is smaller than the
	 * current size, the additional space is marked as free. If the newSize is
	 * bigger, additional DataBlocks are allocated and added to the disk space.
	 * 
	 * @param newSize
	 *            the new size of the disk space
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	void changeSize(long newSize) throws IOException;

	/**
	 * Returns the usable size of the disk space.
	 * 
	 * @return the usable size of the disk space
	 */
	long getSize();

	/**
	 * Returns the offset position, measured in bytes from the beginning of the
	 * disk space.
	 * 
	 * @return the offset position.
	 */
	long getPosition();

	/**
	 * Sets the offset position, measured in bytes from the beginning of the
	 * disk space. The offset may be set beyond the end of the disk space.
	 * Setting the offset beyond the end of the disk space does not change the
	 * disk space size. The disk space size will change only by writing after
	 * the offset has been set beyond the end of the disk space.
	 * 
	 * @param pos
	 *            the offset position, measured in bytes from the beginning of
	 *            the disk space.
	 */
	void seek(long pos);

	/**
	 * Writes the specified byte to the disk space at the current offset.
	 * 
	 * @param b
	 *            the byte to be written
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	void write(byte b) throws IOException;

	/**
	 * Write the specified long to the disk space at the current offset.
	 * 
	 * @param l
	 *            the long to be written
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	void writeLong(long l) throws IOException;

	/**
	 * Write the specified byte array to the disk space at the current offset.
	 * 
	 * @param b
	 *            the byte array to be written
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	void write(byte[] b) throws IOException;

	/**
	 * Write the specified byte to the disk space at the offset given by pos.
	 * 
	 * @param pos
	 *            the offset position
	 * @param b
	 *            the byte to be written
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	void write(long pos, byte b) throws IOException;

	/**
	 * Write the specified long to the disk space at the offset given by pos.
	 * 
	 * @param pos
	 *            the offset position
	 * @param l
	 *            the long to be written
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	void writeLong(long pos, long l) throws IOException;

	/**
	 * Write the specified byte array to the disk space at the offset given by
	 * pos.
	 * 
	 * @param pos
	 *            the offset position
	 * @param b
	 *            the byte array to be written
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	void write(long pos, byte[] b) throws IOException;

	/**
	 * Reads a signed eight-bit value from the disk space at the current offset.
	 * 
	 * @return the next byte of the disk space.
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	byte read() throws IOException;

	/**
	 * Reads up to b.length bytes of bytes from the disk space into an array of
	 * bytes starting at the current offset.
	 * 
	 * @param b
	 *            the buffer into which the data is read.
	 * @return the total number of bytes written into b
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	int read(byte[] b) throws IOException;

	/**
	 * Reads a signed 64-bit value from the disk space at the current offset.
	 * 
	 * @return the next long of the disk space.
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	long readLong() throws IOException;

	/**
	 * Reads a signed eight-bit value from the disk space at offset given by
	 * pos.
	 * 
	 * @param pos
	 *            the offset at which the byte should be read.
	 * @return the byte which was read
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	byte read(long pos) throws IOException;

	/**
	 * Reads up to b.length bytes of data from the disk space into an array of
	 * bytes starting at the offset given by pos.
	 * 
	 * @param pos
	 *            the offset at which the byte array should be read.
	 * @param b
	 *            the buffer into which the data is read.
	 * @return the total number of bytes written into b
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	int read(long pos, byte[] b) throws IOException;

	/**
	 * Reads a signed 64-bit value from the disk space at the offset given by
	 * pos.
	 * 
	 * @param pos
	 *            the offset at which the long should be read.
	 * @return the long which was read
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	long readLong(long pos) throws IOException;

	/**
	 * Returns the amount of bytes which are needed to represent the disk space
	 * including all the meta data used to store the data blocks on the virtual
	 * disk.
	 * 
	 * @return the disk space used by the virtual space on the virtual disk in
	 *         bytes.
	 */
	long getDiskSize();

	/**
	 * Frees all the DataBlocks used by the disk space. The virtual space can't
	 * be used after it has been freed.
	 * 
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	void free() throws IOException;
}
