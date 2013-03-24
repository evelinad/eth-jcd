package ch.se.inf.ethz.jcd.batman.cli.vdisk;

import ch.se.inf.ethz.jcd.batman.cli.vdisk.directory.IDirectory;

public interface IVirtualDisk {

	IDirectory getRootDirectory ();
	
	void setMaxSize (long maxSize);
	
	long getMaxSize ();
	
	long getSize ();
	
	void close ();
	
	int getSuperblockSize (); 
	
}
