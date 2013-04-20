package ch.se.inf.ethz.jcd.batman.model;

/**
 * Model (as defined in MVC-Pattern) for a directory inside a virtual disk.
 * 
 * @see Entry
 * 
 */
public class Directory extends Entry {

	private static final long serialVersionUID = 5544830256663532103L;

	public Directory() {
		super();
	}

	public Directory(Path path) {
		super(path);
	}

	public Directory(Path path, long timestamp) {
		super(path, timestamp);
	}

}
