package ch.se.inf.ethz.jcd.batman.server;

import java.rmi.RemoteException;

import ch.se.inf.ethz.jcd.batman.vdisk.VirtualDiskException;

public interface ISynchronizeServer extends IRemoteVirtualDisk {

	void createUser(String userName, String password) throws RemoteException,
			InvalidUserNameException, AuthenticationException,
			VirtualDiskException;

	int createDisk(String userName, String password, String diskName)
			throws RemoteException, AuthenticationException,
			VirtualDiskException;

	void deleteDisk(String userName, String password, String diskName)
			throws RemoteException, AuthenticationException,
			VirtualDiskException;

	int loadDisk(String userName, String password, String diskName)
			throws RemoteException, AuthenticationException,
			VirtualDiskException;

	boolean diskExists(String userName, String diskName)
			throws RemoteException, VirtualDiskException;

}
