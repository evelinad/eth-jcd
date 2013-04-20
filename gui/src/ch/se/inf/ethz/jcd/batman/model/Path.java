package ch.se.inf.ethz.jcd.batman.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Path implements Serializable {

	private static final long serialVersionUID = -6366450141021999089L;

	public static final String SEPERATOR = "/";

	private SimpleStringProperty path;
	private StringBinding name;

	public Path() {
		this(SEPERATOR);
	}

	public Path(String path) {
		this.path = new SimpleStringProperty(path);
		this.name = new StringBinding() {

			@Override
			protected String computeValue() {
				return extractName();
			}
		};
	}

	private String extractName() {
		String path = getPath();
		int lastSeperatorIndex = path.lastIndexOf(SEPERATOR);
		String namePart = path.substring(lastSeperatorIndex + 1);

		if (namePart.isEmpty()) {
			return SEPERATOR;
		} else {
			return namePart;
		}
	}

	public StringProperty pathProperty() {
		return path;
	}

	public String getPath() {
		return path.get();
	}

	public void setPath(String path) {
		this.path.set(path);
	}

	public Path getParentPath() {
		String path = getPath();
		if (path.equals(SEPERATOR)) {
			return null;
		}

		int lastSeperatorIndex = path.lastIndexOf(SEPERATOR);
		if (lastSeperatorIndex <= 0) {
			return new Path(SEPERATOR);
		}
		return new Path(path.substring(0, lastSeperatorIndex));
	}

	public StringBinding nameBinding() {
		return name;
	}

	public String getName() {
		return name.get();
	}

	@Override
	public String toString() {
		return getPath();
	}

	@Override
	public int hashCode() {
		return path.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Path) {
			Path entry = (Path) obj;
			return path.equals(entry.path);
		}
		return false;
	}

	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.writeObject(getPath());
	}

	private void readObject(ObjectInputStream ois)
			throws ClassNotFoundException, IOException {
		path = new SimpleStringProperty((String) ois.readObject());
		name = new StringBinding() {

			@Override
			protected String computeValue() {
				return extractName();
			}
		};
	}
}
