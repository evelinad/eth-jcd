package ch.se.inf.ethz.jcd.batman.vdisk;

import java.io.Closeable;
import java.io.IOException;


public interface IVirtualDisk extends AutoCloseable, Closeable {

	IVirtualDirectory getRootDirectory ();
	
	void setMaxSize (long maxSize);
	
	long getMinSize ();
	
	long getMaxSize ();
	
	long getSize ();
	
	int getSuperblockSize (); 
	
	IVirtualDiskSpace getFreeSpace (long size);
	
	void write (long pos, byte b) throws IOException;
	
	void write (long pos, byte[] b) throws IOException;
	
	byte read (long pos) throws IOException;
	
	int read (long pos, byte[] b) throws IOException;
	
	IVirtualDirectory createDirectory (IVirtualDirectory parent, String name);
	
	IVirtualFile createFile (IVirtualDirectory parent, String name);
	
}
