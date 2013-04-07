package ch.se.inf.ethz.jcd.batman.vdisk;

import java.io.IOException;

/**
 * Checked exception thrown when an error occurs in the {@link IVirtualDisk}
 */
public class VirtualDiskException extends IOException {

	/**
	 * {@inheritDoc}
	 */
	public VirtualDiskException() { }
	
	/**
	 * {@inheritDoc}
	 */
	public VirtualDiskException(String message) {
		super(message);
	}
	
	private static final long serialVersionUID = -2153480473542426033L;

}
