package ch.se.inf.ethz.jcd.batman.server;

import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * Starting point for the virtual disk server.
 *
 */
public class VirtualDiskServer {

	public final static String SERVICE_NAME = "VirtualDisk";

	public static void main(String[] args) {
		try {
			String name = SERVICE_NAME;
			RemoteVirtualDisk rVirtualDisk = new RemoteVirtualDisk();
			Remote remote = UnicastRemoteObject.exportObject(rVirtualDisk, 0);
			Registry registry = LocateRegistry.getRegistry();
			registry.rebind(name, remote);
			System.out.println(SERVICE_NAME + " bound");
		} catch (Exception e) {
			System.err.println(SERVICE_NAME + " exception:");
			e.printStackTrace();
		}
	}

}
