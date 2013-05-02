package ch.se.inf.ethz.jcd.batman.controller.remote;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;

import javafx.application.Platform;
import javafx.concurrent.Task;
import ch.se.inf.ethz.jcd.batman.controller.ConnectionException;
import ch.se.inf.ethz.jcd.batman.controller.SynchronizedTaskController;
import ch.se.inf.ethz.jcd.batman.controller.SynchronizedTaskControllerState;
import ch.se.inf.ethz.jcd.batman.controller.SynchronizedTaskControllerStateListener;
import ch.se.inf.ethz.jcd.batman.controller.TaskControllerFactory;
import ch.se.inf.ethz.jcd.batman.model.Directory;
import ch.se.inf.ethz.jcd.batman.model.Entry;
import ch.se.inf.ethz.jcd.batman.model.File;
import ch.se.inf.ethz.jcd.batman.server.AuthenticationException;
import ch.se.inf.ethz.jcd.batman.server.IRemoteVirtualDisk;
import ch.se.inf.ethz.jcd.batman.vdisk.VirtualDiskException;

public class RemoteSynchronizedTaskController extends RemoteTaskController implements SynchronizedTaskController {

	protected static final class AdditionalLocalDiskInformation implements Serializable {
		
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
		
		private String userName;
		private String server;
		private String diskName;
		
		public AdditionalLocalDiskInformation(String userName, String server, String diskName) {
			this.userName = userName;
			this.server = server;
			this.diskName = diskName;
		}
		
		public boolean isLinked() {
			return (userName != null && !userName.isEmpty());
		}
		
		public URI createUri (String password) throws URISyntaxException {
			return new URI(TaskControllerFactory.REMOTE_SCHEME + "://" + userName + ":" + 
					password + "@" + server + "?" + diskName);
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
	
	private SynchronizedTaskControllerState state = DEFAULT_STATE;
	private URI serverUri;
	private RemoteConnection serverConnection;
	private final List<SynchronizedTaskControllerStateListener> stateListener = new LinkedList<SynchronizedTaskControllerStateListener>();
	
	public RemoteSynchronizedTaskController(URI uri) {
		if (isServerUri(uri)) {
			initialize(null, uri);
		} else {
			initialize(uri, null);
		}
	}
	
	public RemoteSynchronizedTaskController(URI localUri, URI serverUri) {
		initialize(localUri, serverUri);
	}

	private void initialize(URI localUri, URI serverUri) {
		if (localUri != null && isServerUri(localUri)) {
			throw new IllegalArgumentException("Illegal local uri: " + localUri);
		}
		if (serverUri != null && !isServerUri(localUri)) {
			throw new IllegalArgumentException("Illegal server uri " + serverUri);
		}
		this.uri = localUri;
		this.serverUri = serverUri;
	}
	
	private boolean isLocalConnected () {
		return connection != null;
	}
	
	private boolean isServerConnected () {
		return serverConnection != null;
	}
	
	public Integer getDiskId() {
		if (isLocalConnected()) {
			return connection.getDiskId();
		} else if (isServerConnected()) {
			return serverConnection.getDiskId();
		} else {
			return null;
		}
	}

	public IRemoteVirtualDisk getRemoteDisk() {
		if (isLocalConnected()) {
			return connection.getDisk();
		} else if (isServerConnected()) {
			return serverConnection.getDisk();
		} else {
			return null;
		}
	}

	private void updateState() {
		if (isLocalConnected() && isServerConnected()) {
			setState(SynchronizedTaskControllerState.BOTH_CONNECTED);
		} else if (isLocalConnected()) {
			try {
				AdditionalLocalDiskInformation diskInformation = getLocalDiskInformation();
				if (diskInformation.isLinked()) {
					setState(SynchronizedTaskControllerState.LOCAL_LINKED);
				} else {
					setState(SynchronizedTaskControllerState.LOCAL_UNLINKED_CONNECTED);
				}
			} catch (Exception e) {
				//No valid disk information found
				setState(SynchronizedTaskControllerState.LOCAL_UNLINKED_CONNECTED);
			}
		} else if (isServerConnected()) {
			setState(SynchronizedTaskControllerState.SERVER_CONNECTED);
		} else {
			setState(SynchronizedTaskControllerState.DISCONNECTED);
		}
	}
	
	protected void connect(boolean createNewIfNecessary) throws AuthenticationException, RemoteException, VirtualDiskException, ConnectionException, NotBoundException {
		try {
			if (uri != null) {
				connection = connect(uri, createNewIfNecessary);
			}
			if (serverUri != null) {
				serverConnection = connect(serverUri, createNewIfNecessary);
			}
			if (isLocalConnected() && isServerConnected()) {
				//TODO synchronize
			}
			updateState();
		} catch (Exception e) {
			close();
		}
	}
	
	public void close() {
		if (isConnected()) {
			try {
				unloadDisk();
			} catch (RemoteException | VirtualDiskException e) {
				// ignore, as we close it anyway
			} finally {
				connection = null;
				serverConnection = null;
				updateState();
			}
		}
	}
	
	@Override
	public boolean isConnected() {
		return isLocalConnected() || isServerConnected();
	}
	
	private void closeServerConnection() throws RemoteException, VirtualDiskException {
		serverConnection.getDisk().unloadDisk(serverConnection.getDiskId());
		serverConnection = null;
		serverUri = null;
	}
	
	protected void unloadDisk () throws RemoteException, VirtualDiskException {
		if (isLocalConnected()) {
			connection.getDisk().unloadDisk(connection.getDiskId());
		}
		if (isServerConnected()) {
			serverConnection.getDisk().unloadDisk(serverConnection.getDiskId());
		}
	}
	
	protected void createFile (File file) throws RemoteException, VirtualDiskException {
		if (isLocalConnected()) {
			connection.getDisk().createFile(connection.getDiskId(), file);
		}
		if (isServerConnected()) {
			serverConnection.getDisk().createFile(serverConnection.getDiskId(), file);
		}
	}

	protected void createDirectory (Directory directory) throws RemoteException, VirtualDiskException {
		if (isLocalConnected()) {
			connection.getDisk().createDirectory(connection.getDiskId(), directory);
		}
		if (isServerConnected()) {
			serverConnection.getDisk().createDirectory(serverConnection.getDiskId(), directory);
		}
	}

	protected void deleteEntry (Entry entry) throws RemoteException, VirtualDiskException {
		if (isLocalConnected()) {
			connection.getDisk().deleteEntry(connection.getDiskId(), entry.getPath());
		}
		if (isServerConnected()) {
			serverConnection.getDisk().deleteEntry(serverConnection.getDiskId(), entry.getPath());
		}
	}

	protected void moveEntry (Entry oldEntry, Entry newEntry) throws RemoteException, VirtualDiskException {
		if (isLocalConnected()) {
			connection.getDisk().moveEntry(connection.getDiskId(), oldEntry, newEntry);
		}
		if (isServerConnected()) {
			serverConnection.getDisk().moveEntry(serverConnection.getDiskId(), oldEntry, newEntry);
		}
	}

	protected void copyEntry(Entry source, Entry destination)
			throws RemoteException, VirtualDiskException {
		if (isLocalConnected()) {
			connection.getDisk().copyEntry(connection.getDiskId(), source, destination);
		}
		if (isServerConnected()) {
			serverConnection.getDisk().copyEntry(serverConnection.getDiskId(), source, destination);
		}
	}
	
	protected void importFile(java.io.File file, String destination)
			throws RemoteException, VirtualDiskException, IOException {
		if (isLocalConnected()) {
			importFile(connection.getDisk(), connection.getDiskId(), file, destination);
		}
		if (isServerConnected()) {
			importFile(serverConnection.getDisk(), serverConnection.getDiskId(), file, destination);
		}
	}
	
	protected void checkIsLocalConnected () {
		if (!isLocalConnected()) {
			throw new IllegalStateException("Controller is not connected to local disk");
		}
	}
	
	protected void checkIsServerConnected() {
		if (!isServerConnected()) {
			throw new IllegalStateException("Controller is not connected to server disk");
		}
	}
	
	protected void checkIsBothConnected() {
		checkIsLocalConnected();
		checkIsServerConnected();
	}
	
	@Override
	public Task<Void> createGoOfflineTask() {
		checkIsBothConnected();
		return new Task<Void>() {

			@Override
			protected Void call() throws RemoteException, VirtualDiskException {
				checkIsBothConnected();
				updateTitle("Disconnecting from server");
				updateMessage("Disconnecting from server...");
				closeServerConnection();
				updateState();
				return null;
			}

		};
	}

	private AdditionalLocalDiskInformation getLocalDiskInformation () throws RemoteException, VirtualDiskException {
		byte[] additionalDiskInformation = connection.getDisk().getAdditionalDiskInformation(connection.getDiskId());
		if (additionalDiskInformation == null || additionalDiskInformation.length == 0) {
			throw new VirtualDiskException("Invalid disk information");
		}
		try {
			AdditionalLocalDiskInformation localDiskInformation = AdditionalLocalDiskInformation.readFromByteArray(additionalDiskInformation);
			if (localDiskInformation == null) {
				throw new VirtualDiskException("Invalid disk information");
			}
			return localDiskInformation;
		} catch (Exception e) {
			throw new VirtualDiskException("Invalid disk information", e);
		}
	}
	
	private void saveLocalDiskInformation (AdditionalLocalDiskInformation diskInformation) throws RemoteException, VirtualDiskException {
		connection.getDisk().saveAdditionalDiskInformation(connection.getDiskId(), diskInformation.toByteArray());
	}
	
	@Override
	public Task<Void> createGoOnlineTask(final String password) {
		checkIsLocalConnected();
		return new Task<Void>() {

			@Override
			protected Void call() throws RemoteException, VirtualDiskException, AuthenticationException, ConnectionException, NotBoundException, URISyntaxException {
				checkIsLocalConnected();
				AdditionalLocalDiskInformation localDiskInformation = getLocalDiskInformation();
				URI serverUri = localDiskInformation.createUri(password);
				if (serverUri == null || !isServerUri(serverUri)) {
					throw new IllegalArgumentException("Illegal server uri: " + serverUri);
				}
				updateTitle("Connecting to server");
				updateMessage("Connecting to server...");
				serverConnection = connect(serverUri, true);
				RemoteSynchronizedTaskController.this.serverUri = serverUri;
				//TODO synchronize disks
				updateState();
				return null;
			}

		};
	}


	@Override
	public Task<Void> createLinkDiskTask(final String server, final String userName, final String password, final String diskName) {
		checkIsLocalConnected();
		return new Task<Void>() {

			@Override
			protected Void call() throws RemoteException, VirtualDiskException, AuthenticationException, ConnectionException, NotBoundException, URISyntaxException {
				checkIsLocalConnected();
				AdditionalLocalDiskInformation diskInformation = new AdditionalLocalDiskInformation(userName, server, diskName);
				URI serverUri = diskInformation.createUri(password);
				if (serverUri == null || !isServerUri(serverUri)) {
					throw new IllegalArgumentException("Illegal server uri: " + serverUri);
				}
				updateTitle("Connecting to server");
				updateMessage("Connecting to server...");
				serverConnection = connect(serverUri, true);
				RemoteSynchronizedTaskController.this.serverUri = serverUri;
				saveLocalDiskInformation(diskInformation);
				//TODO synchronize disks
				updateState();
				return null;
			}

		};
	}
	
	@Override
	public Task<Void> createDownloadDiskTask(final URI localUri) {
		if (localUri == null || isServerUri(localUri)) {
			throw new IllegalArgumentException("Illegal local uri: " + localUri);
		}
		checkIsServerConnected();
		return new Task<Void>() {

			@Override
			protected Void call() throws RemoteException, VirtualDiskException, AuthenticationException, ConnectionException, NotBoundException {
				checkIsServerConnected();
				updateTitle("Connecting to local disk");
				updateMessage("Connecting to local disk...");
				connection = connect(uri, true);
				RemoteSynchronizedTaskController.this.uri = localUri;
				String[] userInfoSplit = serverUri.getUserInfo().split(":");
				saveLocalDiskInformation(new AdditionalLocalDiskInformation(userInfoSplit[0], serverUri.getHost(), userInfoSplit[1]));
				//TODO synchronize disks
				updateState();
				return null;
			}

		};
	}
	
	@Override
	public void addStateListener(
			SynchronizedTaskControllerStateListener listener) {
		if (!stateListener.contains(listener)) {
			stateListener.add(listener);
		}
	}

	@Override
	public void removeStateListener(
			SynchronizedTaskControllerStateListener listener) {
		stateListener.remove(listener);
	}

	public void setState(final SynchronizedTaskControllerState newState) {
		if (newState != state) {
			final SynchronizedTaskControllerState oldState = state;
			state = newState;
			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					for (SynchronizedTaskControllerStateListener listener : stateListener) {
						listener.stateChanged(oldState, newState);
					}
				}
			});
		}
	}

	@Override
	public SynchronizedTaskControllerState getState() {
		return state;
	}
}
