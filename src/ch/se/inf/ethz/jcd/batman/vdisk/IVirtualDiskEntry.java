package ch.se.inf.ethz.jcd.batman.vdisk;

import java.io.IOException;


public interface IVirtualDiskEntry {

	void setName (String name) throws IOException;
	
	String getName ();

	IVirtualDirectory getParent ();
	
	void setParent (IVirtualDirectory directory);
	
	IVirtualDiskEntry getPreviousEntry ();
	
	void setPreviousEntry (IVirtualDiskEntry entry);
	
	IVirtualDiskEntry getNextEntry ();
	
	void setNextEntry (IVirtualDiskEntry entry) throws IOException;
	
	long getTimestamp ();
	
	void setTimestamp (long timestamp) throws IOException;
	
	void delete ();
	
	boolean exists ();
	
	long getPosition();
	
}
