package ch.se.inf.ethz.jcd.batman.vdisk;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;


public class VirtualDiskUtil {

	public static Collection<IVirtualDiskEntry> getDirectoryMembers (IVirtualDirectory directory) throws IOException {
		List<IVirtualDiskEntry> members = new LinkedList<IVirtualDiskEntry>();
		for (
			IVirtualDiskEntry currentMember = directory.getFirstMember();
			currentMember != null;
			currentMember = currentMember.getNextEntry()
		) {
			members.add(currentMember);
		}
		return members;
	}
	
	public static IVirtualDiskEntry getDirectoryMember (IVirtualDirectory directory, String name) throws IOException {
		for (
			IVirtualDiskEntry currentMember = directory.getFirstMember();
			currentMember != null;
			currentMember = currentMember.getNextEntry()
		) {
			if (currentMember.getName().equals(name)) {
				return currentMember;
			}
		}
		return null;
	}
	
}
