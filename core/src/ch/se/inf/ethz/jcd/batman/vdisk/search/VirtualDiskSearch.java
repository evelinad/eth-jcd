package ch.se.inf.ethz.jcd.batman.vdisk.search;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import ch.se.inf.ethz.jcd.batman.io.VDiskFile;

/**
 * Implements a search over file and directory names.
 * 
 */
public class VirtualDiskSearch {

	/**
	 * Searches the given term inside entry names.
	 * 
	 * @param settings
	 *            search settings to use
	 * @param term
	 *            the term to search
	 * @param parents
	 *            list of parent directories where the search should start
	 * @return list of found disk entries
	 * @throws IOException
	 */
	public static List<VDiskFile> searchName(Settings settings, String term,
			VDiskFile... parents) throws IOException {
		List<VDiskFile> foundEntries = new LinkedList<>();

		String searchTerm = term;
		if (!settings.isCaseSensitive()) {
			searchTerm = searchTerm.toLowerCase();
		}

		for (VDiskFile parent : parents) {
			VDiskFile[] children = parent.listFiles();
			for (VDiskFile child : children) {
				boolean check = settings.isCheckFiles() && child.isFile()
						|| settings.isCheckFolders() && child.isDirectory();

				if (check) {
					if (settings.isCaseSensitive()) {
						if (child.getName().contains(searchTerm)) {
							foundEntries.add(child);
						}
					} else {
						if (child.getName().toLowerCase().contains(searchTerm)) {
							foundEntries.add(child);
						}
					}
				}
			}

			if (settings.isCheckSubFolders()) {
				foundEntries.addAll(searchName(settings, term, children));
			}
		}

		return foundEntries;
	}

	public static List<VDiskFile> searchName(Settings settings, Pattern term,
			VDiskFile... parents) throws IOException {
		List<VDiskFile> foundEntries = new LinkedList<>();

		for (VDiskFile parent : parents) {
			VDiskFile[] children = parent.listFiles();
			for (VDiskFile child : children) {
				boolean check = settings.isCheckFiles() && child.isFile()
						|| settings.isCheckFolders() && child.isDirectory();

				if (check) {
					if (term.matcher(child.getName()).find()) {
						foundEntries.add(child);
					}
				}
			}

			if (settings.isCheckSubFolders()) {
				foundEntries.addAll(searchName(settings, term, children));
			}
		}

		return foundEntries;
	}

}
