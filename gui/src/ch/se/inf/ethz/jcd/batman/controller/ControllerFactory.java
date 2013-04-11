package ch.se.inf.ethz.jcd.batman.controller;

import java.net.URI;

import ch.se.inf.ethz.jcd.batman.controller.remote.RemoteController;

public class ControllerFactory {

	private static final String REMOTE_SCHEME = "batman";
	
	public static Controller getController (URI uri) {
		if (REMOTE_SCHEME.equals(uri.getScheme())) {
			return new RemoteController(uri);
		} else {
			throw new IllegalArgumentException("Unsupported scheme: " + uri.getScheme());
		}
	}
	
}
