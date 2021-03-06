package ch.se.inf.ethz.jcd.batman.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Model (as defined in MVC-Pattern) for a virtual disk path.
 * 
 */
public class Path implements Serializable, Cloneable {

	private static final long serialVersionUID = -6366450141021999089L;

	public static final String SEPERATOR = "/";

	private SimpleStringProperty pathProp;
	private StringBinding name;

	public Path() {
		this(SEPERATOR);
	}

	public Path(String path) {
		init(path);
	}

	public Path(Path parent, String name) {
		if (parent.getPath().endsWith("/")) {
			init(parent.getPath() + name);
		} else {
			init(parent.getPath() + SEPERATOR + name);
		}
	}

	private void init(String path) {
		this.pathProp = new SimpleStringProperty(path);
		this.name = new StringBinding() {

			@Override
			protected String computeValue() {
				return extractName();
			}
		};
	}

	/**
	 * Returns the path as a string
	 * 
	 * @return a string representing the path
	 */
	public String getPath() {
		return pathProp.get();
	}

	/**
	 * Sets the path to the given string
	 * 
	 * @param path
	 *            new path represented by the object
	 */
	public void setPath(String path) {
		this.pathProp.set(path);
		this.name.invalidate();
	}

	/**
	 * Returns the parent as a {@link Path} instance
	 * 
	 * @return the parent as a {@link Path} instance
	 */
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

	/**
	 * Returns the name of the object to which the path leads
	 * 
	 * @return the name as a string
	 */
	public String getName() {
		return name.get();
	}

	public StringBinding nameBinding() {
		return name;
	}

	public StringProperty pathProperty() {
		return pathProp;
	}

	@Override
	public String toString() {
		return getPath();
	}

	@Override
	public int hashCode() {
		return pathProp.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Path) {
			Path entry = (Path) obj;
			return getPath().equals(entry.getPath());
		}
		return false;
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

	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.writeObject(getPath());
	}

	private void readObject(ObjectInputStream ois)
			throws ClassNotFoundException, IOException {
		pathProp = new SimpleStringProperty((String) ois.readObject());
		name = new StringBinding() {

			@Override
			protected String computeValue() {
				return extractName();
			}
		};
	}

	public String[] split() {
		return pathProp.get().split(Path.SEPERATOR);
	}

	public boolean pathEquals(Path path) {
		return getPath().equals(path.getPath());
	}

	public Path getRelativePath(Path path) {
		return new Path(getPath().substring(path.getPath().length()));
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		final Path path = (Path) super.clone();
		path.pathProp = new SimpleStringProperty(getPath());
		path.name = new StringBinding() {

			@Override
			protected String computeValue() {
				return path.extractName();
			}
		};
		path.name.invalidate();
		return path;
	}

	public void changeName(String newName) {
		final String oldName = getName();
		if (!oldName.equals(SEPERATOR)) {
			String parentPath = getParentPath().getPath();
			if (!parentPath.endsWith(SEPERATOR)) {
				parentPath += SEPERATOR;
			}
			setPath(parentPath + newName);
		}
	}
}
