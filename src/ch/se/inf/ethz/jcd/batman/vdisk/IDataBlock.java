package ch.se.inf.ethz.jcd.batman.vdisk;

import java.io.IOException;

public interface IDataBlock {

	long getBlockPosition ();
	
	void write (long pos, byte b) throws IOException;
	
	void write (long pos, byte[] b) throws IOException;
	
	void write (long pos, long l) throws IOException;
	
	byte read (long pos) throws IOException;
	
	long read (long pos, byte[] b) throws IOException;
	
	long readLong (long pos) throws IOException;
	
	long getNextBlock ();
	
	long getFreeSize ();
	
	long getDataSize ();
	
	void setDataSize (long size) throws IOException;
	
	long getDiskSize ();
	
}
