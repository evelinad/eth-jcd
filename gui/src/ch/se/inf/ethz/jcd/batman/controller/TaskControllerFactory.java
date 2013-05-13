package ch.se.inf.ethz.jcd.batman.controller;

import java.net.URI;

import ch.se.inf.ethz.jcd.batman.controller.remote.RemoteServerTaskController;
import ch.se.inf.ethz.jcd.batman.controller.remote.RemoteSynchronizedTaskController;

/**
 * Factory to get the right {@link SynchronizedTaskController} for a given
 * {@link URI}.
 */
public class TaskControllerFactory {

	public static final String REMOTE_SCHEME = "batman";

	/**
	 * Returns the right {@link SynchronizedTaskController} for the given
	 * {@link URI}.
	 * 
	 * @param uri
	 *            the location of the virtual disk
	 * @return a {@link SynchronizedTaskController} that can execute commands on
	 *         the given virtual disk
	 */
	public static SynchronizedTaskController getController(URI uri) {
		if (REMOTE_SCHEME.equals(uri.getScheme())) {
			return new RemoteSynchronizedTaskController(uri);
		} else {
			throw new IllegalArgumentException("Unsupported scheme: "
					+ uri.getScheme());
		}
	}

	public static ServerTaskController getServerController(URI uri) {
		if (REMOTE_SCHEME.equals(uri.getScheme())) {
			return new RemoteServerTaskController(uri);
		} else {
			throw new IllegalArgumentException("Unsupported scheme: "
					+ uri.getScheme());
		}
	}

}
