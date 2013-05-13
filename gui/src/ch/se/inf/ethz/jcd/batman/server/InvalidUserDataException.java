package ch.se.inf.ethz.jcd.batman.server;

public class InvalidUserDataException extends RuntimeException {

	private static final long serialVersionUID = -7760268049416925937L;

	public InvalidUserDataException(Throwable t) {
		super(t);
	}

}
