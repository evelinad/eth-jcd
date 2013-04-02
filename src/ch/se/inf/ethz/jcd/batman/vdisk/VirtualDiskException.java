package ch.se.inf.ethz.jcd.batman.vdisk;

import java.io.IOException;

public class VirtualDiskException extends IOException {

	public VirtualDiskException() { }
	
	public VirtualDiskException(String message) {
		super(message);
	}
	
	private static final long serialVersionUID = -2153480473542426033L;

}
