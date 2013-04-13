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
		if(path.equals(SEPERATOR)) {
			return null;
		}
		
		int lastSeperatorIndex = path.lastIndexOf(SEPERATOR);
		if (lastSeperatorIndex <= 0) {
			return new Path(SEPERATOR);
		}
		return new Path(path.substring(0, lastSeperatorIndex));
	}
	
	public String getName() {
		int lastSeperatorIndex = path.lastIndexOf(SEPERATOR);
		String namePart = path.substring(lastSeperatorIndex + 1);
		
		if(namePart.isEmpty()) {
			return SEPERATOR;
		} else {
			return namePart;
		}
	}
	
	@Override
	public String toString() {
		return getPath();
	}
}
