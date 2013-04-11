package ch.se.inf.ethz.jcd.batman.model;

import java.io.Serializable;

public class Path implements Serializable {

	private static final long serialVersionUID = -6366450141021999089L;

	public static final String SEPERATOR = "/";
	
	private String path;
	
	public Path (String path) {
		setPath(path);
	}

	public Path() {
		setPath(SEPERATOR);
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Path getParentPath () {
		int lastSeperatorIndex = path.lastIndexOf(SEPERATOR);
		if (lastSeperatorIndex <= 0) {
			return new Path(SEPERATOR);
		}
		return new Path(path.substring(0, lastSeperatorIndex));
	}
	
	public String getName() {
		String[] splitPath = path.split(SEPERATOR);
		return splitPath[splitPath.length - 1];
	}
	
	@Override
	public String toString() {
		return getPath();
	}
}
