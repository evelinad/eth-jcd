package ch.se.inf.ethz.jcd.batman.controller.remote;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;

import ch.se.inf.ethz.jcd.batman.controller.TaskControllerFactory;

public final class AdditionalLocalDiskInformation implements Serializable {
	
	private static final long serialVersionUID = -2547837072567273526L;

	protected static AdditionalLocalDiskInformation readFromByteArray(byte[] data) throws IOException, ClassNotFoundException {
		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		ObjectInputStream ois = new ObjectInputStream(bis);
		Object object = ois.readObject();
		if (object instanceof AdditionalLocalDiskInformation) {
			return (AdditionalLocalDiskInformation) object;
		}
		return null;
	}
	
	private final String userName;
	private final String server;
	private final String diskName;
	private long lastSynchronized;
	
	public AdditionalLocalDiskInformation(String userName, String server, String diskName, long lastSynchronized) {
		this.userName = userName;
		this.server = server;
		this.diskName = diskName;
		this.lastSynchronized = lastSynchronized;
	}
	
	public boolean isLinked() {
		return userName != null && !userName.isEmpty();
	}
	
	public URI createUri (String password) throws URISyntaxException {
		return new URI(TaskControllerFactory.REMOTE_SCHEME + "://" + userName + ":" + 
				password + "@" + server + "?" + diskName);
	}
	
	public void setLastSynchronized(long lastSynchronized) {
		this.lastSynchronized = lastSynchronized;
	}
	
	public long getLastSynchronized() {
		return lastSynchronized;
	}
	
	public byte[] toByteArray() {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
			ObjectOutputStream oos = new ObjectOutputStream(bos); 
			oos.writeObject(this);
			oos.close();
			return bos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}
