package ch.se.inf.ethz.jcd.batman.server;

import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class VirtualDiskServer {

	public final static String NAME = "VirtualDisk";
	
	/*
	 * cd C:\Users\Benji\Documents\GitHub\eth-jcd\gui\bin
	 * "C:\Program Files\Java\jdk1.7.0_17\bin\rmiregistry.exe"
	 */
	public static void main(String[] args) {
		try {
			String name = NAME;
			RemoteVirtualDisk rVirtualDisk = new RemoteVirtualDisk();
			Remote remote = UnicastRemoteObject.exportObject(rVirtualDisk, 0);
			Registry registry = LocateRegistry.getRegistry();
			registry.rebind(name, remote);
			System.out.println(NAME + " bound");
		} catch (Exception e) {
			System.err.println(NAME + " exception:");
			e.printStackTrace();
		}
	}
	
}
