package ch.se.inf.ethz.jcd.batman.cli.vdisk.directory;

import java.util.List;

import ch.se.inf.ethz.jcd.batman.cli.vdisk.IVirtualDiskMember;

public interface IDirectory extends IVirtualDiskMember {
	
	void addMember (IVirtualDiskMember member);
	
	List<IVirtualDiskMember> getMembers ();
	
}
