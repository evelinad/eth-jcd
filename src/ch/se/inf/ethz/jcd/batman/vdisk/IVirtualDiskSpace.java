package ch.se.inf.ethz.jcd.batman.vdisk;

import java.io.IOException;

/**
 * A {@link IVirtualDiskSpace} is a simple abstraction of a list of
 * {@link IDataBlock}.
 * 
 * A virtual disk space allows to work with a continouse space on the disk
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

	void changeSize(long newSize) throws IOException;

	long getSize();

	long getPosition();

	void seek(long pos);

	void write(byte b) throws IOException;

	void write(long l) throws IOException;

	void write(byte[] b) throws IOException;

	void write(long pos, byte b) throws IOException;

	void write(long pos, long l) throws IOException;

	void write(long pos, byte[] b) throws IOException;

	byte read() throws IOException;

	int read(byte[] b) throws IOException;

	long readLong() throws IOException;

	byte read(long pos) throws IOException;

	int read(long pos, byte[] b) throws IOException;

	long readLong(long pos) throws IOException;

	long getDiskSize();
	
	void free() throws IOException;
}
