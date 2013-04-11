package ch.se.inf.ethz.jcd.batman.model;

import java.io.Serializable;

public class Entry implements Serializable {

	private static final long serialVersionUID = -6951589227362678760L;
	
	private Path path;
	private long timestamp;
	
	public Entry() {
		path = new Path();
	}
	
	public Entry(Path path, long timestamp) {
		this.path = path;
		this.timestamp = timestamp;
	}
	public Path getPath() {
		return path;
	}
	
	public void setPath(Path path) {
		this.path = path;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
}
