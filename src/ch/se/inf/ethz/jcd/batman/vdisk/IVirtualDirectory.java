package ch.se.inf.ethz.jcd.batman.vdisk;



public interface IVirtualDirectory extends IVirtualDiskEntry {
	
	void removeMember (IVirtualDiskEntry member) throws VirtualDiskException;
	
	void addMember (IVirtualDiskEntry member);
	
	IVirtualDiskEntry getFirstMember ();
	
}
