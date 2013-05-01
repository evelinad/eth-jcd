package ch.se.inf.ethz.jcd.batman.server;

public class AuthenticationException extends Exception {

	private static final long serialVersionUID = -2857967646373015030L;

	public AuthenticationException(String message) {
		super(message);
	}

	public AuthenticationException(String message, Throwable e) {
		super(message, e);
	}

}
