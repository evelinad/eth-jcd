package ch.se.inf.ethz.jcd.batman.server;

public class InvalidUserNameException extends Exception {

	private static final long serialVersionUID = 8040299903305545020L;

	public InvalidUserNameException(String message) {
		super(message);
	}

}
