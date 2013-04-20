package ch.se.inf.ethz.jcd.batman.vdisk.search;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import ch.se.inf.ethz.jcd.batman.io.VDiskFile;

/**
 * Implements a search over file an directory names.
 * 
 */
public class VirtualDiskSearch {

	/**
	 * Searches for the given name in the tree that starts from the given parent
	 * 
	 * @param name
	 *            the name to search for
	 * @param parent
	 *            the starting point of the search
	 * @return a list of entries that contain the name
	 * @throws IOException
	 */
	public static List<VDiskFile> searchName(String name, VDiskFile parent)
			throws IOException {
		List<VDiskFile> foundEntries = new LinkedList<>();

		for (VDiskFile child : parent.listFiles()) {
			if (child.getName().contains(name)) {
				foundEntries.add(child);
			}

			foundEntries.addAll(searchName(name, child));
		}

		return foundEntries;
	}

}
