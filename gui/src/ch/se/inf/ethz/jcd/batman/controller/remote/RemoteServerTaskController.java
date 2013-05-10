package ch.se.inf.ethz.jcd.batman.controller.remote;

import java.net.URI;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import ch.se.inf.ethz.jcd.batman.controller.ServerTaskController;
import ch.se.inf.ethz.jcd.batman.server.AuthenticationException;
import ch.se.inf.ethz.jcd.batman.server.ISynchronizeServer;
import ch.se.inf.ethz.jcd.batman.server.InvalidUserNameException;
import ch.se.inf.ethz.jcd.batman.server.VirtualDiskServer;

import javafx.concurrent.Task;

public class RemoteServerTaskController implements ServerTaskController {
	
	private static final String SYNCHRONIZE_SERVICE_NAME = VirtualDiskServer.SYNCHRONIZE_SERVICE_NAME;
	
	private final URI uri;
	
	public RemoteServerTaskController(URI uri) {
		this.uri = uri;
	}
	
	@Override
	public Task<Void> createNewUserTask(final String userName, final String password) {
		return new Task<Void>() {

			@Override
			protected Void call() throws RemoteException, InvalidUserNameException, AuthenticationException, NotBoundException {
				updateTitle("Creating user");
				updateMessage("Creating user...");
				Registry registry;
				if (uri.getPort() == -1) {
					registry = LocateRegistry.getRegistry(uri.getHost());
				} else {
					registry = LocateRegistry.getRegistry(uri.getHost(),
							uri.getPort());
				}
				ISynchronizeServer synchronizeServer = (ISynchronizeServer) registry
						.lookup(SYNCHRONIZE_SERVICE_NAME);
				synchronizeServer.createUser(userName, password);
				return null;
			}
			
		};
	}
	
}
