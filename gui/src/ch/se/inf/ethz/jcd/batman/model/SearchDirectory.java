package ch.se.inf.ethz.jcd.batman.model;

public class SearchDirectory extends Directory {
	private static final long serialVersionUID = -5311729100226286904L;
	
	private final Entry[] results;
	
	private SearchDirectory(Entry[] entries) {
		this.results = entries;
	}
	
	public SearchDirectory(Entry[] entries, String searchTerm) {
		this.results = entries;
		setPath(new Path(String.format("Search: '%s'", searchTerm)));
	}
	
	public Entry[] getResults() {
		return results;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		SearchDirectory searchDirectory = new SearchDirectory(getResults());
		searchDirectory.setPath(getPath());
		return searchDirectory;
	}
}
