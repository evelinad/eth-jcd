package ch.se.inf.ethz.jcd.batman.model.util;

import java.util.Comparator;

import ch.se.inf.ethz.jcd.batman.model.Directory;
import ch.se.inf.ethz.jcd.batman.model.Entry;
import ch.se.inf.ethz.jcd.batman.model.File;

/**
 * Sorts entries in the following way: Files > Entries > Directories
 */
public class FileBeforeDirectoryComparator implements Comparator<Entry> {

	@Override
	public int compare(Entry entry1, Entry entry2) {
		if (entry1 instanceof File) {
			if (entry2 instanceof File) {
				return entry2.getPath().getPath()
						.compareTo(entry1.getPath().getPath());
			} else {
				return -1;
			}
		} else if (entry1 instanceof Directory) {
			if (entry2 instanceof Directory) {
				return entry2.getPath().getPath()
						.compareTo(entry1.getPath().getPath());
			} else {
				return 1;
			}
		} else {
			return entry2.getPath().getPath()
					.compareTo(entry1.getPath().getPath());
		}
	}

}
