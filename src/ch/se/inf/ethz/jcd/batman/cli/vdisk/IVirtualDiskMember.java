package ch.se.inf.ethz.jcd.batman.cli.vdisk;

import ch.se.inf.ethz.jcd.batman.cli.vdisk.directory.IDirectory;

public interface IVirtualDiskMember {

	void setName (String name);
	
	String getName ();

	IDirectory getParent ();
	
	long getTimeStamp ();
	
	IVirtualDiskMember getNextMember ();
	
	void setNextMember (IVirtualDiskMember next);
}
