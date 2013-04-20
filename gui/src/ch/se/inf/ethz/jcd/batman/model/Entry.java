package ch.se.inf.ethz.jcd.batman.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;

public class Entry implements Serializable {

	private static final long serialVersionUID = -6951589227362678760L;
	
	private SimpleObjectProperty<Path> path = new SimpleObjectProperty<Path>(new Path());
	private LongProperty timestamp = new SimpleLongProperty(0);
	
	public Entry() { }
	
	public Entry(Path path) {
		this.path.set(path);
	}
	
	public Entry(Path path, long timestamp) {
		this.path.set(path);
		this.timestamp.set(timestamp);
	}
	
	public SimpleObjectProperty<Path> pathProperty() {
		return path;
	}
	
	public Path getPath() {
		return path.get();
	}
	
	public void setPath(Path path) {
		this.path.set(path);
	}

	public LongProperty timestampProperty() {
		return timestamp;
	}
	
	public long getTimestamp() {
		return timestamp.get();
	}

	public void setTimestamp(long timestamp) {
		this.timestamp.set(timestamp);
	}
	
	@Override
	public int hashCode() {
		return path.get().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Entry) {
			Entry entry = (Entry) obj;
			return getTimestamp() == entry.getTimestamp() && getPath().equals(entry.getPath());
		}
		return false;
	}
	
	protected void writeObjectToStream (ObjectOutputStream oos) throws IOException {
		oos.writeObject(getPath());
	    oos.writeLong(getTimestamp());
	}
	
	private void writeObject(ObjectOutputStream oos) throws IOException {
		writeObjectToStream(oos);
	}
	
	protected void readObjectFromStream(ObjectInputStream ois) throws ClassNotFoundException, IOException {
		path = new SimpleObjectProperty<Path>((Path) ois.readObject());
		timestamp = new SimpleLongProperty(ois.readLong());
	}
	
	private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
		readObjectFromStream(ois);
	}
}
