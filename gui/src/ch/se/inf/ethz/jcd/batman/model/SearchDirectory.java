package ch.se.inf.ethz.jcd.batman.model;

public class SearchDirectory extends Directory {
	private static final long serialVersionUID = -5311729100226286904L;

	private final String term;
	private final boolean isRegex;
	private final boolean checkFiles;
	private final boolean checkFolders;
	private final boolean isCaseSensitive;
	private final boolean checkChildren;

	public SearchDirectory(final Path path, final String term, final boolean isRegex,
			final boolean checkFiles, final boolean checkFolders, final boolean isCaseSensitive,
			final boolean checkChildren) {
		super(path);
		this.term = term;
		this.isRegex = isRegex;
		this.checkFiles = checkFiles;
		this.checkFolders = checkFolders;
		this.isCaseSensitive = isCaseSensitive;
		this.checkChildren = checkChildren;
	}

	public String getTerm() {
		return term;
	}

	public boolean isRegex() {
		return isRegex;
	}

	public boolean isCheckFiles() {
		return checkFiles;
	}

	public boolean isCheckFolders() {
		return checkFolders;
	}

	public boolean isCaseSensitive() {
		return isCaseSensitive;
	}

	public boolean isCheckChildren() {
		return checkChildren;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
