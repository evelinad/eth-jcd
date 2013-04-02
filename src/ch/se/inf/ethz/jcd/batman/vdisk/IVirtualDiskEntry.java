package ch.se.inf.ethz.jcd.batman.vdisk;


public interface IVirtualDiskEntry {

	void setName (String name);
	
	String getName ();

	IVirtualDirectory getParent ();
	
	void setParent (IVirtualDirectory directory);
	
	IVirtualDiskEntry getPreviousEntry ();
	
	void setPreviousEntry (IVirtualDiskEntry entry);
	
	IVirtualDiskEntry getNextEntry ();
	
	void setNextEntry (IVirtualDiskEntry entry);
	
	long getTimestamp ();
	
	void setTimestamp (long timestamp);
	
	void delete ();
	
	boolean exists ();
	
	long getPosition();
	
}
