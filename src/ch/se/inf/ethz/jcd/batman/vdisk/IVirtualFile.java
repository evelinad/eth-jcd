package ch.se.inf.ethz.jcd.batman.vdisk;

import java.io.IOException;


public interface IVirtualFile extends IVirtualDiskEntry {
	
	long getDataPosition () throws IOException;
	
	void seek (long position) throws IOException;
	
	int read (byte[] b) throws IOException;
	
	byte read () throws IOException;

	void write (byte b) throws IOException;
	
	void write (byte[] b) throws IOException;
	
	void setSize (long size) throws IOException;
	
	long getSize () throws IOException;
	
	long getDiskSize () throws IOException;
	
}
