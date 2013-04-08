package ch.se.inf.ethz.jcd.batman.vdisk;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;

/**
 * Represents a VirtualDisk.
 * 
 * The VirtualDisk manages {@link IDataBlock} which can be dynamically allocated
 * and freed. The {@link IDataBlock} are persistently stored. It's also possible
 * to directly write/read from the underlining storage unit with the different
 * read/write methods.
 */
public interface IVirtualDisk extends AutoCloseable, Closeable {

	/**
	 * This number is used to identify that file opened is actually a virtual
	 * file disk.
	 */
	byte[] MAGIC_NUMBER = new byte[] { (byte) 0xDE, (byte) 0xAD, (byte) 0xC0,
			(byte) 0xFF, (byte) 0xEE, 0x00, 0x00, 0x00 };

	/**
	 * The path separator between directories.
	 */
	char PATH_SEPARATOR = '/';

	/**
	 * Returns the root directory of the virtual disk.
	 * 
	 * @return the root directory of the virtual disk
	 */
	IVirtualDirectory getRootDirectory();

	/**
	 * Returns the size in bytes used to store the virtual disk.
	 * 
	 * @return the size in bytes used to store the virtual disk
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	long getSize() throws IOException;

	/**
	 * Returns the amount of free space in the virtual disk.
	 * 
	 * @return the amount of free space in bytes in the virtual disk
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	long getFreeSpace() throws IOException;

	/**
	 * Returns the amount of occupied space in the virtual disk.
	 * 
	 * @return the amount of occupied space in bytes in the virtual disk
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	long getOccupiedSpace() throws IOException;

	/**
	 * Invalidates the {@link IDataBlock} given by block and marks the space
	 * previously occupied by the {@link IDataBlock} as free.
	 * 
	 * @param block
	 *            the block to free.
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	void freeBlock(IDataBlock block) throws IOException;

	/**
	 * Allocates {@link IDataBlock} to store the number of bytes given by size.
	 * The blocks are returned as an array and internally linked with a linked
	 * list. Which means the first block has set next to the second block and so
	 * on.
	 * 
	 * @param size
	 *            the number of bytes the data blocks have to be able to store
	 * @return the allocated blocks
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	IDataBlock[] allocateBlock(long size) throws IOException;

	/**
	 * Write the specified byte to the underlying file at the offset given by
	 * pos.
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
	 * Write the specified byte array to the underlying file at the offset given
	 * by pos.
	 * 
	 * @param pos
	 *            the offset position
	 * @param b
	 *            the data
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	void write(long pos, byte[] b) throws IOException;

	/**
	 * Writes length bytes from the specified byte array starting at offset off
	 * to the underlying file at the offset given by pos.
	 * 
	 * @param pos
	 *            the offset position
	 * @param b
	 *            the data
	 * @param offset
	 *            the start offset in the data.
	 * @param length
	 *            the number of bytes to write.
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	void write(long pos, byte[] b, int offset, int length) throws IOException;

	/**
	 * Reads a signed eight-bit value from the underlying file at offset given
	 * by pos.
	 * 
	 * @param pos
	 *            the offset at which the byte should be read.
	 * @return the byte which was read
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	byte read(long pos) throws IOException;

	/**
	 * Reads up to b.length bytes of data from the underlying file into an array
	 * of bytes starting at the offset given by pos.
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
	 * Reads up to length bytes of data from the underlying file starting at the
	 * offset given by pos into an array of bytes
	 * 
	 * @param pos
	 *            the offset at which the byte array should be read.
	 * @param b
	 *            the buffer into which the data is read.
	 * @param offset
	 *            the start offset in array b at which the data is written.
	 * @param length
	 *            the maximum number of bytes read
	 * @return the total number of bytes written into b
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	int read(long pos, byte[] b, int offset, int length) throws IOException;

	/**
	 * Creates a directory in the virtual disk with the given name.
	 * 
	 * @param parent
	 *            the parent of the newly created directory
	 * @param name
	 *            the name of the newly created directory
	 * @return the created directory
	 * @throws IOException
	 *             if the directory name was invalid or an I/O error occurred
	 */
	IVirtualDirectory createDirectory(IVirtualDirectory parent, String name)
			throws IOException;

	/**
	 * Creates a file in the virtual disk with the given name.
	 * 
	 * @param parent
	 *            the parent of the newly created file
	 * @param name
	 *            the name of the newly created file
	 * @return the created file
	 * @throws IOException
	 *             if the file name was invalid or an I/O error occurred
	 */
	IVirtualFile createFile(IVirtualDirectory parent, String name, long size)
			throws IOException;

	/**
	 * Returns a URI that represents the location of the virtual disk on a host
	 * system
	 * 
	 * @return the URI to the host location of the virtual disk
	 */
	URI getHostLocation();

}
