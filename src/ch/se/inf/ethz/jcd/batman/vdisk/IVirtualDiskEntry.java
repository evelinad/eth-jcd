package ch.se.inf.ethz.jcd.batman.vdisk;


public interface IVirtualDiskEntry {

	void setName (String name);
	
	String getName ();

	IVirtualDirectory getParent ();
	
	long getTimeStamp ();
	
	void createNew ();
	
	void delete ();
	
	boolean exists ();
	
}
