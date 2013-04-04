package ch.se.inf.ethz.jcd.batman.vdisk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class VirtualDiskUtil {

	public static IVirtualDiskEntry[] getDirectoryEntrys (IVirtualDirectory directory) throws IOException {
		List<IVirtualDiskEntry> members = new ArrayList<IVirtualDiskEntry>();
		for (
			IVirtualDiskEntry currentMember = directory.getFirstMember();
			currentMember != null;
			currentMember = currentMember.getNextEntry()
		) {
			members.add(currentMember);
		}
		return members.toArray(new IVirtualDiskEntry[members.size()]);
	}
	
}
