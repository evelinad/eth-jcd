package ch.se.inf.ethz.jcd.batman.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import ch.se.inf.ethz.jcd.batman.vdisk.VirtualDiskException;

public class SynchronizeServer extends RemoteVirtualDisk implements
		ISynchronizeServer {

	protected static class UserData implements Serializable {

		private static final long serialVersionUID = 6660922465113408804L;

		private int id;
		private String userName;
		private byte[] hashedPassowrd;

		public UserData(int id, String userName, byte[] hashedPassword) {
			this.id = id;
			this.userName = userName;
			this.hashedPassowrd = hashedPassword;
		}

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public String getUserName() {
			return userName;
		}

		public void setUserName(String userName) {
			this.userName = userName;
		}

		public byte[] getHashedPassowrd() {
			return Arrays.copyOf(hashedPassowrd, hashedPassowrd.length);
		}

		public void setHashedPassowrd(byte[] hashedPassowrd) {
			this.hashedPassowrd = hashedPassowrd;
		}

	}

	private static final long SALT_SEED = 12312321;
	private static final String USER_FILE = "user.data";

	private Map<String, UserData> userMap = new HashMap<String, UserData>();
	private int nextId = 1;
	private final byte[] salt;

	public SynchronizeServer() {
		salt = new byte[16];
		new Random(SALT_SEED).nextBytes(salt);
		loadUsers();
	}

	@SuppressWarnings("unchecked")
	private void loadUsers() {
		File userFile = new File(USER_FILE);
		if (userFile.exists()) {
			ObjectInputStream objStream = null;
			try {
				objStream = new ObjectInputStream(new FileInputStream(userFile));
				nextId = objStream.readInt();
				Object object = objStream.readObject();
				if (object instanceof Map) {
					userMap = (Map<String, UserData>) object;
				}
			} catch (IOException | ClassNotFoundException e) {
				throw new InvalidUserDataException(e);
			} finally {
				if (objStream != null) {
					try {
						objStream.close();
					} catch (IOException e) {
						// Ignore as nothing can be done
					}
				}
			}
		}
	}

	private void saveUsers() throws IOException {
		ObjectOutputStream objStream = null;
		try {
			objStream = new ObjectOutputStream(new FileOutputStream(USER_FILE));
			objStream.writeInt(nextId);
			objStream.writeObject(userMap);
		} finally {
			if (objStream != null) {
				try {
					objStream.close();
				} catch (IOException e) {
					// Ignore as nothing can be done
				}
			}
		}
	}

	@Override
	public void createUser(String userName, String password)
			throws RemoteException, InvalidUserNameException,
			AuthenticationException, VirtualDiskException {
		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException(
					"Username can not be null or empty");
		}
		if (password == null || password.isEmpty()) {
			throw new IllegalArgumentException(
					"Password can not be null or empty");
		}
		if (userMap.containsKey(userName)) {
			throw new InvalidUserNameException("User " + userName
					+ " already exists");
		}
		UserData userData = new UserData(nextId++, userName,
				hashPassword(password));
		userMap.put(userName, userData);
		try {
			saveUsers();
		} catch (IOException e) {
			throw new VirtualDiskException("Unable to save users", e);
		}
	}

	private byte[] getHashedPassword(String userName) {
		return userMap.get(userName).getHashedPassowrd();
	}

	private int getUserId(String userName) {
		return userMap.get(userName).getId();
	}

	private byte[] hashPassword(String password) throws AuthenticationException {
		try {
			KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536,
					128);
			SecretKeyFactory f = SecretKeyFactory
					.getInstance("PBKDF2WithHmacSHA1");
			SecretKey secretKey = f.generateSecret(spec);
			return secretKey.getEncoded();
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new AuthenticationException("Passowrd hash error.", e);
		}
	}

	private void checkPassword(String userName, String password)
			throws AuthenticationException {
		if (!userMap.containsKey(userName)) {
			throw new AuthenticationException("User " + userName
					+ " does not exist.");
		}
		if (!Arrays.equals(getHashedPassword(userName), hashPassword(password))) {
			throw new AuthenticationException("Invalid password.");
		}
	}

	private String getDiskPath(String userName, String diskName) {
		return "" + getUserId(userName) + File.pathSeparator + diskName;
	}

	@Override
	public int createDisk(String userName, String password, String diskName)
			throws RemoteException, VirtualDiskException,
			AuthenticationException {
		checkPassword(userName, password);
		String diskPath = getDiskPath(userName, diskName);
		try {
			return createDiskImpl(diskPath);
		} catch (IOException e) {
			throw new VirtualDiskException("Could not create disk at "
					+ diskPath, e);
		}
	}

	@Override
	public void deleteDisk(String userName, String password, String diskName)
			throws RemoteException, VirtualDiskException,
			AuthenticationException {
		checkPassword(userName, password);

		String diskPath = getDiskPath(userName, diskName);
		LoadedDisk disk = getPathToDiskMap().get(
				new java.io.File(diskPath).toURI());
		if (disk != null && !disk.hasNoIds()) {
			throw new VirtualDiskException(
					"Could not delete disk, disk still in use");
		}
		try {
			File diskFile = new File(diskPath);
			if (isVirtualDisk(diskFile)) {
				if (!diskFile.delete()) {
					throw new VirtualDiskException(
							"Could not delete virtual disk at " + diskPath);
				}
			} else {
				throw new IllegalArgumentException(diskPath
						+ "  is not a virtual disk. File not deleted.");
			}
		} catch (IOException | IllegalArgumentException e) {
			throw new VirtualDiskException("Could not delete virtual disk at "
					+ diskPath, e);
		}
	}

	@Override
	public int loadDisk(String userName, String password, String diskName)
			throws RemoteException, VirtualDiskException,
			AuthenticationException {
		checkPassword(userName, password);
		String diskPath = getDiskPath(userName, diskName);
		try {
			return loadDiskImpl(diskPath);
		} catch (IOException e) {
			throw new VirtualDiskException(
					"Could not load disk at " + diskPath, e);
		}
	}

	@Override
	public boolean diskExists(String userName, String diskName)
			throws RemoteException, VirtualDiskException {
		if (userMap.containsKey(userName)) {
			return isVirtualDisk(new File(getDiskPath(userName, diskName)));
		}
		return false;
	}

}
