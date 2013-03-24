package ch.se.inf.ethz.jcd.batman.cli.vdisk.file;

import ch.se.inf.ethz.jcd.batman.cli.vdisk.IVirtualDiskMember;

public interface IFile extends IVirtualDiskMember {

	void createNewFile ();
	
	boolean exists ();
	
	long getSize ();
	
	void delete ();
	
}
