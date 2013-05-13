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
import ch.se.inf.ethz.jcd.batman.controller.ConnectionException;
import ch.se.inf.ethz.jcd.batman.controller.SynchronizedTaskController;
import ch.se.inf.ethz.jcd.batman.controller.SynchronizedTaskControllerState;
import ch.se.inf.ethz.jcd.batman.controller.SynchronizedTaskControllerStateListener;
import ch.se.inf.ethz.jcd.batman.controller.UpdateableTask;
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
	private RemoteDiskClient serverRmiClient;
	private boolean synchronizing = false;
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
	
	private boolean isBothConnected() {
		return isLocalConnected() && isServerConnected();
	}
	
	private void registerServerClient () throws RemoteException {
		if (serverRmiClient == null) {
			serverRmiClient = new RemoteDiskClient();
			serverConnection.getDisk().registerClient(serverConnection.getDiskId(), serverRmiClient);
		}
	}
	
	protected void registerClient() throws RemoteException {
		switch (state) {
		case BOTH_CONNECTED:
			registerServerClient();
			break;
		case DISCONNECTED:
			break;
		case LOCAL_LINKED:
			super.registerClient();
			break;
		case LOCAL_UNLINKED_CONNECTED:
			super.registerClient();
			break;
		case SERVER_CONNECTED:
			registerServerClient();
			break;
		default:
			break;
		}
	}
	
	protected void unregisterClient() throws RemoteException {
		super.unregisterClient();
		if (serverRmiClient != null) {
			serverConnection.getDisk().unregisterClient(serverConnection.getDiskId(), serverRmiClient);
			serverRmiClient = null;
		}
	}
	
	public Integer getDiskId() {
		if (isServerConnected()) {
			return serverConnection.getDiskId();
		} else if (isLocalConnected()) {
			return connection.getDiskId();
		} else {
			return null;
		}
	}

	public IRemoteVirtualDisk getRemoteDisk() {
		if (isServerConnected()) {
			return serverConnection.getDisk();
		} else if (isLocalConnected()) {
			return connection.getDisk();
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
			} catch (IOException e) {
				//No valid disk information found
				setState(SynchronizedTaskControllerState.LOCAL_UNLINKED_CONNECTED);
			}
		} else if (isServerConnected()) {
			setState(SynchronizedTaskControllerState.SERVER_CONNECTED);
		} else {
			setState(SynchronizedTaskControllerState.DISCONNECTED);
		}
	}
	
	protected void connect(boolean createNewIfNecessary, UpdateableTask<?> task) throws AuthenticationException, RemoteException, VirtualDiskException, ConnectionException, NotBoundException, InterruptedException {
		if (uri != null) {
			connection = connect(uri, createNewIfNecessary);
		}
		if (serverUri != null) {
			serverConnection = connect(serverUri, createNewIfNecessary);
		}
		acquireLock(task);
		try {
			if (isLocalConnected() && isServerConnected()) {
				synchronizeDisks(task, connection);
			}
			updateState();
			registerClient();
		} finally {
			releaseLock(task);
		}
	}
	
	public void close() {
		if (isConnected()) {
			try {
				unregisterClient();
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
		} catch (IOException e) {
			//Don't update last synchronized if no valid disk information exist
			//Otherwise different disk information could be overwritten
		}
	}
	
	@Override
	public UpdateableTask<Void> createGoOfflineTask() {
		checkIsBothConnected();
		return new UpdateableTask<Void>() {

			@Override
			protected Void callImpl() throws RemoteException, VirtualDiskException {
				checkIsBothConnected();
				updateTitle("Disconnecting from server");
				updateMessage("Disconnecting from server...");
				updateLastSynchronized(new Date().getTime());
				unregisterClient();
				closeServerConnection();
				updateState();
				registerClient();
				return null;
			}

		};
	}

	private void saveLocalDiskInformation (AdditionalLocalDiskInformation diskInformation) throws RemoteException, VirtualDiskException {
		connection.getDisk().saveAdditionalDiskInformation(connection.getDiskId(), diskInformation.toByteArray());
	}
	
	@Override
	public UpdateableTask<Void> createGoOnlineTask(final String password) {
		checkIsLocalConnected();
		return new UpdateableTask<Void>() {

			@Override
			protected Void callImpl() throws RemoteException, VirtualDiskException, AuthenticationException, ConnectionException, NotBoundException, URISyntaxException, InterruptedException {
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
				acquireLock(this);
				try {
					synchronizeDisks(this, connection);
					unregisterClient();
					updateState();
					registerClient();
					return null;
				} finally {
					releaseLock(this);
				}
			}

		};
	}

	@Override
	public UpdateableTask<Void> createLinkDiskTask(final String server, final String userName, final String password, final String diskName) {
		checkIsLocalConnected();
		return new UpdateableTask<Void>() {

			@Override
			protected Void callImpl() throws RemoteException, VirtualDiskException, AuthenticationException, ConnectionException, NotBoundException, URISyntaxException, InterruptedException {
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
				acquireLock(this);
				try {
					synchronizeDisks(this, connection);
					unregisterClient();
					updateState();
					registerClient();
					return null;
				} finally {
					releaseLock(this);
				}
			}

		};
	}
	
	@Override
	public UpdateableTask<Void> createDownloadDiskTask(final URI localUri) {
		if (localUri == null || isServerUri(localUri)) {
			throw new IllegalArgumentException("Illegal local uri: " + localUri);
		}
		checkIsServerConnected();
		return new UpdateableTask<Void>() {

			@Override
			protected Void callImpl() throws RemoteException, VirtualDiskException, AuthenticationException, ConnectionException, NotBoundException, InterruptedException {
				checkIsServerConnected();
				updateTitle("Connecting to local disk");
				updateMessage("Connecting to local disk...");
				connection = connect(localUri, true);
				RemoteSynchronizedTaskController.this.uri = localUri;
				String[] userInfoSplit = serverUri.getUserInfo().split(":");
				saveLocalDiskInformation(new AdditionalLocalDiskInformation(userInfoSplit[0], serverUri.getHost(), serverUri.getQuery(), 0));
				acquireLock(this);
				try {
					synchronizeDisks(this, serverConnection);
					updateState();
					return null;
				} finally {
					releaseLock(this);
				}
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
	
	public void synchronizeDisks (UpdateableTask<?> task, RemoteConnection prevConnectedConnection) throws RemoteException, VirtualDiskException {
		if (!isLocalConnected() || !isServerConnected()) {
			throw new IllegalStateException("Can't synchronize disks if not connected to both server and local disk");
		}
		synchronizing = true;
		try {
			SynchronizeDisks synchronizeDisks = new SynchronizeDisks(serverConnection, connection);
			synchronizeDisks.synchronizeDisks(task);
		} finally {
			synchronizing = false;
		}
	}
	
	private boolean synchronizeChanges () {
		return isBothConnected() && !synchronizing;
	}
	
	public void entryAdded(final Entry entry) throws RemoteException, VirtualDiskException {
		super.entryAdded(entry);
		if (synchronizeChanges()) {
			if (entry instanceof Directory) {
				connection.getDisk().createDirectory(connection.getDiskId(), (Directory) entry);
			} else if (entry instanceof File) {
				connection.getDisk().createFile(connection.getDiskId(), (File) entry);
			}
		}
	}
	
	public void entryDeleted(final Entry entry) throws RemoteException, VirtualDiskException {
		super.entryDeleted(entry);
		if (synchronizeChanges()) {
			connection.getDisk().deleteEntry(connection.getDiskId(), entry);
		}
	}

	public void entryChanged(final Entry oldEntry, final Entry newEntry) throws RemoteException, VirtualDiskException {
		super.entryChanged(oldEntry, newEntry);
		if (synchronizeChanges()) {
			if (oldEntry.getPath().equals(newEntry.getPath())) {
				if (oldEntry.getTimestamp() != newEntry.getTimestamp()) {
					connection.getDisk().updateLastModified(connection.getDiskId(), oldEntry, newEntry.getTimestamp());
				}
			} else {
				connection.getDisk().moveEntry(connection.getDiskId(), oldEntry, newEntry);
			}
		}
	}

	public void entryCopied(final Entry sourceEntry, final Entry destinationEntry) throws RemoteException, VirtualDiskException {
		super.entryCopied(sourceEntry, destinationEntry);
		if (synchronizeChanges()) {
			connection.getDisk().copyEntry(connection.getDiskId(), sourceEntry, destinationEntry);
		}
	}

	public void writeToEntry(final File file, final long fileOffset, final byte[] data) throws RemoteException, VirtualDiskException {
		super.writeToEntry(file, fileOffset, data);
		if (synchronizeChanges()) {
			connection.getDisk().write(connection.getDiskId(), file, fileOffset, data);
		}
	}
}
