package ch.se.inf.ethz.jcd.batman.vdisk;

/**
 * A {@link IVirtualDiskSpace} is a simple abstraction of a list of
 * {@link IDataBlock}.
 * 
 * A virtual disk space allows to work with a continouse
 * space on the disk without handling the jumps between data blocks and the
 * management that is connected with it.
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
	
	void changeSize(long newSize);

	long getSize();

	long getPosition();

	void seek(long pos);

	void write(byte b);

	void write(long l);

	void write(byte[] b);

	void write(long pos, byte b);

	void write(long pos, long l);

	void write(long pos, byte[] b);

	byte read();

	long read(byte[] b);

	long readLong();

	byte read(long pos);

	long read(long pos, byte[] b);

	long readLong(long pos);

	long getDiskSize();
}
