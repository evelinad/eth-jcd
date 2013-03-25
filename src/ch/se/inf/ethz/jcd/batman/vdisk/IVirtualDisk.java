package ch.se.inf.ethz.jcd.batman.vdisk;


public interface IVirtualDisk {

	IVirtualDirectory getRootDirectory ();
	
	void setMaxSize (long maxSize);
	
	long getMaxSize ();
	
	long getSize ();
	
	void close ();
	
	int getSuperblockSize (); 
	
}
