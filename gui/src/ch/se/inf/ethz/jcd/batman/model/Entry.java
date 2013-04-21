package ch.se.inf.ethz.jcd.batman.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Model (as defined in MVC-Pattern) for a virtual disk entry.
 * 
 * This model can be serialized for transportation in case of a client/server
 * architecture.
 * 
 */
public class Entry implements Serializable, Cloneable {

	private static final long serialVersionUID = -6951589227362678760L;

	private SimpleObjectProperty<Path> path;
	private LongProperty timestamp = new SimpleLongProperty(0);

	public Entry() {
		this(new Path());
	}

	public Entry(Path path) {
		this(path, 0);
	}

	public Entry(Path path, long timestamp) {
		this.path = new SimpleObjectProperty<Path>(path);
		this.timestamp = new SimpleLongProperty(timestamp);
	}

	/**
	 * Returns the path of the entry.
	 * 
	 * @return location of the entry
	 */
	public Path getPath() {
		return path.get();
	}

	/**
	 * Sets the path of the entry.
	 * 
	 * @param path
	 *            new path for the entry
	 */
	public void setPath(Path path) {
		this.path.set(path);
	}

	/**
	 * Returns the timestamp of the entry
	 * 
	 * @return the timestamp of the entry
	 */
	public long getTimestamp() {
		return timestamp.get();
	}

	/**
	 * Sets the timestamp for the entry
	 * 
	 * @param timestamp
	 *            new timestamp for the entry
	 */
	public void setTimestamp(long timestamp) {
		this.timestamp.set(timestamp);
	}

	public LongProperty timestampProperty() {
		return timestamp;
	}

	public SimpleObjectProperty<Path> pathProperty() {
		return path;
	}

	@Override
	public int hashCode() {
		return path.get().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Entry) {
			Entry entry = (Entry) obj;
			return getTimestamp() == entry.getTimestamp()
					&& getPath().equals(entry.getPath());
		}
		return false;
	}

	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.writeObject(getPath());
		oos.writeLong(getTimestamp());
	}
	
	private void readObject(ObjectInputStream ois)
			throws ClassNotFoundException, IOException {
		path = new SimpleObjectProperty<Path>((Path) ois.readObject());
		timestamp = new SimpleLongProperty(ois.readLong());
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return new Entry(getPath(), getTimestamp());
	}
	
}
