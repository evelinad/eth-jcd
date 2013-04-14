package ch.se.inf.ethz.jcd.batman.controller;

import java.net.URI;

import ch.se.inf.ethz.jcd.batman.controller.remote.RemoteWorkerController;

public class WorkerControllerFactory {

	private static final String REMOTE_SCHEME = "batman";
	
	public static WorkerController getController (URI uri) {
		if (REMOTE_SCHEME.equals(uri.getScheme())) {
			return new RemoteWorkerController(uri);
		} else {
			throw new IllegalArgumentException("Unsupported scheme: " + uri.getScheme());
		}
	}
	
}
