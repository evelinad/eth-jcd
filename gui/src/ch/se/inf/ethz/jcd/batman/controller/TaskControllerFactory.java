package ch.se.inf.ethz.jcd.batman.controller;

import java.net.URI;

import ch.se.inf.ethz.jcd.batman.controller.remote.RemoteTaskController;

public class TaskControllerFactory {

	private static final String REMOTE_SCHEME = "batman";
	
	public static TaskController getController (URI uri) {
		if (REMOTE_SCHEME.equals(uri.getScheme())) {
			return new RemoteTaskController(uri);
		} else {
			throw new IllegalArgumentException("Unsupported scheme: " + uri.getScheme());
		}
	}
	
}
