package ch.se.inf.ethz.jcd.batman.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;

public class File extends Entry {

	private static final long serialVersionUID = -6866287625459765024L;

	private SimpleLongProperty size = new SimpleLongProperty(0);

	public File() {
		super();
	}
	
	public File(Path path) {
		this(path, 0, 0);
	}
	
	public File(Path path, long timestamp, long size) {
		super(path, timestamp);
		this.size.set(size);
	}
	
	public LongProperty sizeProperty() {
		return size;
	}
	
	public long getSize() {
		return size.get();
	}

	public void setSize(long size) {
		this.size.set(size);
	}
	
	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.writeLong(getSize());
	}
	
	private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
		size = new SimpleLongProperty(ois.readLong());
	}
	
}
