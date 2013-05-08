package ch.se.inf.ethz.jcd.batman.controller.remote;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javafx.application.Platform;
import javafx.concurrent.Task;
import ch.se.inf.ethz.jcd.batman.controller.ConnectionException;
import ch.se.inf.ethz.jcd.batman.controller.SynchronizedTaskController;
import ch.se.inf.ethz.jcd.batman.controller.SynchronizedTaskControllerState;
import ch.se.inf.ethz.jcd.batman.controller.SynchronizedTaskControllerStateListener;
import ch.se.inf.ethz.jcd.batman.model.Directory;
import ch.se.inf.ethz.jcd.batman.model.Entry;
import ch.se.inf.ethz.jcd.batman.model.File;
import ch.se.inf.ethz.jcd.batman.server.AuthenticationException;
import ch.se.inf.ethz.jcd.batman.server.IRemoteVirtualDisk;
import ch.se.inf.ethz.jcd.batman.vdisk.VirtualDiskException;

public class RemoteSynchronizedTaskController extends RemoteTaskController implements SynchronizedTaskController {

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
		if (serverUri != null && !isServerUri(serverUri)) {
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
				AdditionalLocalDiskInformation diskInformation = RemoteConnectionUtil.getDiskInformation(connection);
				if (diskInformation != null && diskInformation.isLinked()) {
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
	
	protected void connect(boolean createNewIfNecessary, UpdateableTask<?> task) throws AuthenticationException, RemoteException, VirtualDiskException, ConnectionException, NotBoundException {
		try {
			if (uri != null) {
				connection = connect(uri, createNewIfNecessary);
			}
			if (serverUri != null) {
				serverConnection = connect(serverUri, createNewIfNecessary);
			}
			if (isLocalConnected() && isServerConnected()) {
				synchronizeDisks(task);
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
	
	private void closeLocalConnection() throws RemoteException, VirtualDiskException {
		connection.getDisk().unloadDisk(connection.getDiskId());
		connection = null;
	}
	
	private void closeServerConnection() throws RemoteException, VirtualDiskException {
		serverConnection.getDisk().unloadDisk(serverConnection.getDiskId());
		serverConnection = null;
	}
	
	protected void unloadDisk () throws RemoteException, VirtualDiskException {
		if (isLocalConnected()) {
			if (isServerConnected()) {
				updateLastSynchronized(new Date().getTime());
			}
			closeLocalConnection();
		}
		if (isServerConnected()) {
			closeServerConnection();
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
	
	protected Entry importFile(java.io.File file, String destination)
			throws RemoteException, VirtualDiskException, IOException {
		Entry entry = null;
		if (isLocalConnected()) {
			entry = importFile(connection.getDisk(), connection.getDiskId(), file, destination);
		}
		if (isServerConnected()) {
			entry = importFile(serverConnection.getDisk(), serverConnection.getDiskId(), file, destination);
		}
		return entry;
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
	
	protected void updateLastSynchronized(long lastSynchronized) {
		try {
			AdditionalLocalDiskInformation localDiskInformation = RemoteConnectionUtil.getDiskInformation(connection);
			if (localDiskInformation != null) {
				localDiskInformation.setLastSynchronized(lastSynchronized);
				saveLocalDiskInformation(localDiskInformation);
			}
		} catch (Exception e) {
			//Don't update last synchronized if no valid disk information exist
		}
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
				updateLastSynchronized(new Date().getTime());
				closeServerConnection();
				updateState();
				return null;
			}

		};
	}

	
	
	private void saveLocalDiskInformation (AdditionalLocalDiskInformation diskInformation) throws RemoteException, VirtualDiskException {
		connection.getDisk().saveAdditionalDiskInformation(connection.getDiskId(), diskInformation.toByteArray());
	}
	
	@Override
	public Task<Void> createGoOnlineTask(final String password) {
		checkIsLocalConnected();
		return new UpdateableTask<Void>() {

			@Override
			protected Void call() throws RemoteException, VirtualDiskException, AuthenticationException, ConnectionException, NotBoundException, URISyntaxException {
				checkIsLocalConnected();
				AdditionalLocalDiskInformation localDiskInformation = RemoteConnectionUtil.getDiskInformation(connection);
				if (localDiskInformation == null) {
					throw new VirtualDiskException("No valid disk information found");
				}
				URI serverUri = localDiskInformation.createUri(password);
				if (serverUri == null || !isServerUri(serverUri)) {
					throw new IllegalArgumentException("Illegal server uri: " + serverUri);
				}
				updateTitle("Connecting to server");
				updateMessage("Connecting to server...");
				serverConnection = connect(serverUri, true);
				RemoteSynchronizedTaskController.this.serverUri = serverUri;
				synchronizeDisks(this);
				updateState();
				return null;
			}

		};
	}


	@Override
	public Task<Void> createLinkDiskTask(final String server, final String userName, final String password, final String diskName) {
		checkIsLocalConnected();
		return new UpdateableTask<Void>() {

			@Override
			protected Void call() throws RemoteException, VirtualDiskException, AuthenticationException, ConnectionException, NotBoundException, URISyntaxException {
				checkIsLocalConnected();
				AdditionalLocalDiskInformation diskInformation = new AdditionalLocalDiskInformation(userName, server, diskName, 0);
				URI serverUri = diskInformation.createUri(password);
				if (serverUri == null || !isServerUri(serverUri)) {
					throw new IllegalArgumentException("Illegal server uri: " + serverUri);
				}
				updateTitle("Connecting to server");
				updateMessage("Connecting to server...");
				serverConnection = connect(serverUri, true);
				RemoteSynchronizedTaskController.this.serverUri = serverUri;
				saveLocalDiskInformation(diskInformation);
				synchronizeDisks(this);
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
		return new UpdateableTask<Void>() {

			@Override
			protected Void call() throws RemoteException, VirtualDiskException, AuthenticationException, ConnectionException, NotBoundException {
				checkIsServerConnected();
				updateTitle("Connecting to local disk");
				updateMessage("Connecting to local disk...");
				connection = connect(uri, true);
				RemoteSynchronizedTaskController.this.uri = localUri;
				String[] userInfoSplit = serverUri.getUserInfo().split(":");
				saveLocalDiskInformation(new AdditionalLocalDiskInformation(userInfoSplit[0], serverUri.getHost(), userInfoSplit[1], 0));
				synchronizeDisks(this);
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
	
	public void synchronizeDisks (UpdateableTask<?> task) throws RemoteException, VirtualDiskException {
		if (!isLocalConnected() || !isServerConnected()) {
			throw new IllegalStateException("Can't synchronize disks if not connected to both server and local disk");
		}
		SynchronizeDisks synchronizeDisks = new SynchronizeDisks(serverConnection, connection);
		synchronizeDisks.synchronizeDisks(task);
	}
}
