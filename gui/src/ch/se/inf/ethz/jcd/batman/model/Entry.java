package ch.se.inf.ethz.jcd.batman.model;

import java.io.Serializable;

public class Entry implements Serializable {

	private static final long serialVersionUID = -6951589227362678760L;
	
	private Path path;
	private long timestamp;
	
	public Entry() {
		path = new Path();
	}
	
	public Entry(Path path) {
		this.path = path;
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
	
	@Override
	public int hashCode() {
		return path.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Entry) {
			Entry entry = (Entry) obj;
			return timestamp == entry.timestamp && path.equals(entry.path);
		}
		return false;
	}
}
