package ch.se.inf.ethz.jcd.batman.controller;

import java.net.URI;

import ch.se.inf.ethz.jcd.batman.controller.remote.RemoteTaskController;

/**
 * Factory to get the right {@link TaskController} for a given {@link URI}.
 * 
 * 
 */
public class TaskControllerFactory {

	private static final String REMOTE_SCHEME = "batman";

	/**
	 * Returns the right {@link TaskController} for the given {@link URI}.
	 * 
	 * @param uri
	 *            the location of the virtual disk
	 * @return a {@link TaskController} that can execute commands on the given
	 *         virtual disk
	 */
	public static TaskController getController(URI uri) {
		if (REMOTE_SCHEME.equals(uri.getScheme())) {
			return new RemoteTaskController(uri);
		} else {
			throw new IllegalArgumentException("Unsupported scheme: "
					+ uri.getScheme());
		}
	}

}
