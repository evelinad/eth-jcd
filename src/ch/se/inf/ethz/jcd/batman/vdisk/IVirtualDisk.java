package ch.se.inf.ethz.jcd.batman.vdisk;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;


public interface IVirtualDisk extends AutoCloseable, Closeable {
    
    byte[] MAGIC_NUMBER = new byte[] {(byte) 0xDE, (byte) 0xAD, (byte) 0xC0, (byte) 0xFF, (byte) 0xEE, 0x00, 0x00, 0x00};
    
    char PATH_SEPARATOR = '/';

	IVirtualDirectory getRootDirectory ();
	
	long getSize () throws IOException;
	
	long getFreeSpace () throws IOException;
	
	long getOccupiedSpace () throws IOException;
	
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
	
	URI getHostLocation();
	
}
