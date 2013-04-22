package ch.se.inf.ethz.jcd.batman.controller;

/**
 * Checked exception for connection problems to a virtual disk.
 * 
 */
public class ConnectionException extends Exception {

	private static final long serialVersionUID = -2014256483984759833L;

	public ConnectionException(String message) {
		super(message);
	}

	public ConnectionException(Throwable t) {
		super(t);
	}

	public ConnectionException(String message, Throwable t) {
		super(message, t);
	}
}
