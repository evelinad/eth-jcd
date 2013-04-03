package ch.se.inf.ethz.jcd.batman.vdisk;

import java.io.IOException;

public interface IVirtualDiskSpace {

	long getVirtualDiskPosition();
	
	void changeSize (long newSize) throws IOException;
	
	long getSize ();
	
	long getPosition ();
	
	void seek (long pos);
	
	void write (byte b) throws IOException;

	void write (long l) throws IOException;
	
	void write (byte[] b) throws IOException;
	
	void write (long pos, byte b) throws IOException;
	
	void write (long pos, long l) throws IOException;
	
	void write (long pos, byte[] b) throws IOException;
	
	byte read () throws IOException;
	
	int read (byte[] b) throws IOException;
	
	long readLong () throws IOException;
	
	byte read (long pos) throws IOException;
	
	int read (long pos, byte[] b) throws IOException;
	
	long readLong (long pos) throws IOException;

	long getDiskSize();
}
