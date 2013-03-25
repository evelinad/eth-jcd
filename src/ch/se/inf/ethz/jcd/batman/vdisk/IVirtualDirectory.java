package ch.se.inf.ethz.jcd.batman.vdisk;

import java.util.List;


public interface IVirtualDirectory extends IVirtualDiskEntry {
	
	void addMember (IVirtualDiskEntry member);
	
	List<IVirtualDiskEntry> getMembers ();
	
}
