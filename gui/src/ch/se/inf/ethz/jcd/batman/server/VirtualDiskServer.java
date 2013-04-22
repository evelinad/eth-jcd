package ch.se.inf.ethz.jcd.batman.server;

import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * Starting point for the virtual disk server.
 * 
 */
public final class VirtualDiskServer {

	public final static String SERVICE_NAME = "VirtualDisk";

	private VirtualDiskServer () {}

	public static void main(final String[] args) {
		try {
			final String name = SERVICE_NAME;
			final RemoteVirtualDisk rVirtualDisk = new RemoteVirtualDisk();
			final Remote remote = UnicastRemoteObject.exportObject(rVirtualDisk, 0);
			final Registry registry = LocateRegistry.getRegistry();
			registry.rebind(name, remote);
			System.out.println(SERVICE_NAME + " bound");
		} catch (Exception e) {
			System.err.println(SERVICE_NAME + " exception:");
			e.printStackTrace();
		}
	}

}
