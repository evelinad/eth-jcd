package ch.se.inf.ethz.jcd.batman.model;

public class File extends Entry {

	private static final long serialVersionUID = -6866287625459765024L;

	private long size;

	public File() {
		super();
	}
	
	public File(Path path, long timestamp, long size) {
		super(path, timestamp);
		this.size = size;
	}
	
	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}
	
}
