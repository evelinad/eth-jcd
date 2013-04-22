package ch.se.inf.ethz.jcd.batman.model;

public class SearchDirectory extends Directory {
	private static final long serialVersionUID = -5311729100226286904L;
	
	private String term;
	private boolean isRegex;
	private boolean checkFiles;
	private boolean checkFolders;
	private boolean isCaseSensitive;
	private boolean checkChildren;
	
	public SearchDirectory(Path path, String term, boolean isRegex, boolean checkFiles, 
			boolean checkFolders, boolean isCaseSensitive, boolean checkChildren) {
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
	public Object clone() {
		return new SearchDirectory((Path) getPath().clone() , term, isRegex, checkFiles, checkFolders, isCaseSensitive, checkChildren);
	}
}
