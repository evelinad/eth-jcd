package ch.se.inf.ethz.jcd.batman.vdisk;

public interface IVirtualDiskSpace {

	long getVirtualDiskPosition();
	
	void changeSize (long newSize);
	
	long getSize ();
	
	long getPosition ();
	
	void seek (long pos);
	
	void write (byte b);

	void write (long l);
	
	void write (byte[] b);
	
	void write (long pos, byte b);
	
	void write (long pos, long l);
	
	void write (long pos, byte[] b);
	
	byte read ();
	
	long read (byte[] b);
	
	long readLong ();
	
	byte read (long pos);
	
	long read (long pos, byte[] b);
	
	long readLong (long pos);

	long getDiskSize();
}
