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
	 * Wrapper for all available settings of a search.
	 * 
	 */
	public static final class Settings {
		private boolean caseSensitive;
		private boolean checkFolders;
		private boolean checkFiles;
		private boolean checkSubFolders;

		public Settings() {
			caseSensitive = false;
			checkFolders = false;
			checkFiles = false;
			checkSubFolders = false;
		}

		public boolean isCaseSensitive() {
			return caseSensitive;
		}

		public void setCaseSensitive(boolean caseSensitive) {
			this.caseSensitive = caseSensitive;
		}

		public boolean checkFolders() {
			return checkFolders;
		}

		public void setCheckFolders(boolean onlyFolders) {
			this.checkFolders = onlyFolders;
		}

		public boolean checkFiles() {
			return checkFiles;
		}

		public void setCheckFiles(boolean onlyFiles) {
			this.checkFiles = onlyFiles;
		}

		public boolean checkSubFolders() {
			return checkSubFolders;
		}

		public void setCheckSubFolders(boolean checkSubFolders) {
			this.checkSubFolders = checkSubFolders;
		}
	}

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
		if (settings.isCaseSensitive()) {
			searchTerm = searchTerm.toLowerCase();
		}

		for (VDiskFile parent : parents) {
			VDiskFile[] children = parent.listFiles();
			for (VDiskFile child : children) {
				boolean check = (settings.checkFiles() && child.isFile())
						|| (settings.checkFolders && child.isDirectory());

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

			if (settings.checkSubFolders()) {
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
			for(VDiskFile child : children) {
				boolean check = (settings.checkFiles() && child.isFile())
						|| (settings.checkFolders && child.isDirectory());
				
				if(check) {
					if(term.matcher(child.getName()).find()) {
						foundEntries.add(child);
					}
				}
			}
			
			if(settings.checkSubFolders()) {
				foundEntries.addAll(searchName(settings, term, children));
			}
		}

		return foundEntries;
	}

}
