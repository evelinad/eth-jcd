package ch.se.inf.ethz.jcd.batman.vdisk.search;

/**
 * Wrapper for all available settings of a search.
 * 
 */
public class Settings {
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

	public boolean isCheckFolders() {
		return checkFolders;
	}

	public void setCheckFolders(boolean onlyFolders) {
		this.checkFolders = onlyFolders;
	}

	public boolean isCheckFiles() {
		return checkFiles;
	}

	public void setCheckFiles(boolean onlyFiles) {
		this.checkFiles = onlyFiles;
	}

	public boolean isCheckSubFolders() {
		return checkSubFolders;
	}

	public void setCheckSubFolders(boolean checkSubFolders) {
		this.checkSubFolders = checkSubFolders;
	}
}
