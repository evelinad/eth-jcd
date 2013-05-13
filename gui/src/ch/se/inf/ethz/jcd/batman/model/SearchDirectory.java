package ch.se.inf.ethz.jcd.batman.model;

public class SearchDirectory extends Directory implements Cloneable {
	private static final long serialVersionUID = -5311729100226286904L;

	private final String term;
	private final boolean regex;
	private final boolean checkFiles;
	private final boolean checkFolders;
	private final boolean caseSensitive;
	private final boolean checkChildren;

	public SearchDirectory(final Path path, final String term,
			final boolean regex, final boolean checkFiles,
			final boolean checkFolders, final boolean caseSensitive,
			final boolean checkChildren) {
		super(path);
		this.term = term;
		this.regex = regex;
		this.checkFiles = checkFiles;
		this.checkFolders = checkFolders;
		this.caseSensitive = caseSensitive;
		this.checkChildren = checkChildren;
	}

	public String getTerm() {
		return term;
	}

	public boolean isRegex() {
		return regex;
	}

	public boolean isCheckFiles() {
		return checkFiles;
	}

	public boolean isCheckFolders() {
		return checkFolders;
	}

	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	public boolean isCheckChildren() {
		return checkChildren;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
