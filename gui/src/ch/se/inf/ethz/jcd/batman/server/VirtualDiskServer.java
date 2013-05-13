package ch.se.inf.ethz.jcd.batman.server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Starting point for the virtual disk server.
 * 
 */
public final class VirtualDiskServer {

	public final static String DISK_SERVICE_NAME = "VirtualDisk";
	public final static String SYNCHRONIZE_SERVICE_NAME = "SynchronizeServer";

	private final static Logger LOGGER = Logger
			.getLogger(VirtualDiskServer.class.getName());

	private VirtualDiskServer() {
	}

	public static void main(final String[] args) {
		try {
			final Registry registry = LocateRegistry.getRegistry();

			// Start disk server
			final SimpleVirtualDisk rVirtualDisk = new SimpleVirtualDisk();
			final Remote remoteDisk = UnicastRemoteObject.exportObject(
					rVirtualDisk, 0);
			registry.rebind(DISK_SERVICE_NAME, remoteDisk);
			LOGGER.info(DISK_SERVICE_NAME + " bound");

			// Start synchronize server
			final SynchronizeServer synchronizeServer = new SynchronizeServer();
			final Remote remoteSynchronizeServer = UnicastRemoteObject
					.exportObject(synchronizeServer, 0);
			registry.rebind(SYNCHRONIZE_SERVICE_NAME, remoteSynchronizeServer);
			LOGGER.info(SYNCHRONIZE_SERVICE_NAME + " bound");
		} catch (RemoteException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}

}
