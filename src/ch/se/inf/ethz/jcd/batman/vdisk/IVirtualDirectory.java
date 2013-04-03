package ch.se.inf.ethz.jcd.batman.vdisk;

import java.io.IOException;



public interface IVirtualDirectory extends IVirtualDiskEntry {
	
	void removeMember (IVirtualDiskEntry member) throws VirtualDiskException, IOException;
	
	void addMember (IVirtualDiskEntry member) throws IOException;
	
	IVirtualDiskEntry getFirstMember ();
	
}
