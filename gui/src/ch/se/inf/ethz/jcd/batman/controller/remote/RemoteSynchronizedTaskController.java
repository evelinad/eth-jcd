package ch.se.inf.ethz.jcd.batman.controller.remote;

import java.io.IOException;
import java.net.URI;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;

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

	private SynchronizedTaskControllerState state = SynchronizedTaskControllerState.DISCONNECTED;
	private URI serverUri;
	private RemoteConnection serverConnection;
	private final List<SynchronizedTaskControllerStateListener> stateListener = new LinkedList<SynchronizedTaskControllerStateListener>();
	
	public RemoteSynchronizedTaskController(URI localUri, URI serverUri) {
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

	private void changeState() {
		if (isLocalConnected() && isServerConnected()) {
			setState(SynchronizedTaskControllerState.BOTH_CONNECTED);
		} else if (isLocalConnected()) {
			setState(SynchronizedTaskControllerState.LOCAL_CONNECTED);
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
			changeState();
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
				changeState();
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
				changeState();
				return null;
			}

		};
	}

	@Override
	public Task<Void> createGoOnlineTask(final URI serverUri) {
		if (serverUri == null || isServerUri(serverUri)) {
			throw new IllegalArgumentException("Illegal server uri: " + serverUri);
		}
		checkIsLocalConnected();
		return new Task<Void>() {

			@Override
			protected Void call() throws RemoteException, VirtualDiskException, AuthenticationException, ConnectionException, NotBoundException {
				checkIsLocalConnected();
				RemoteSynchronizedTaskController.this.serverUri = serverUri;
				updateTitle("Connecting to server");
				updateMessage("Connecting to server...");
				serverConnection = connect(serverUri, true);
				//synchronize disks
				changeState();
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
				RemoteSynchronizedTaskController.this.uri = localUri;
				updateTitle("Connecting to local disk");
				updateMessage("Connecting to local disk...");
				connection = connect(uri, true);
				//synchronize disks
				changeState();
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

	public void setState(SynchronizedTaskControllerState newState) {
		if (newState != state) {
			SynchronizedTaskControllerState oldState = state;
			state = newState;
			for (SynchronizedTaskControllerStateListener listener : stateListener) {
				listener.stateChanged(oldState, newState);
			}
		}
	}
}
