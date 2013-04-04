package ch.se.inf.ethz.jcd.batman.vdisk;

import java.io.Closeable;
import java.io.IOException;


public interface IVirtualDisk extends AutoCloseable, Closeable {

	IVirtualDirectory getRootDirectory ();
	
	void setMaxSize (long maxSize) throws IOException;
	
	long getMinSize ();
	
	long getMaxSize ();
	
	long getSize () throws IOException;
	
	int getSuperblockSize (); 
	
	void freeBlock (IDataBlock block) throws IOException;
	
	IDataBlock[] allocateBlock (long size) throws IOException;
	
	void write (long pos, byte b) throws IOException;
	
	void write (long pos, byte[] b) throws IOException;

	void write (long pos, byte[] b, int offset, int length) throws IOException;
	
	byte read (long pos) throws IOException;
	
	int read (long pos, byte[] b) throws IOException;

	int read (long pos, byte[] b, int offset, int length) throws IOException;
	
	IVirtualDirectory createDirectory (IVirtualDirectory parent, String name) throws IOException;
	
	IVirtualFile createFile (IVirtualDirectory parent, String name, long size) throws IOException;
	
}
