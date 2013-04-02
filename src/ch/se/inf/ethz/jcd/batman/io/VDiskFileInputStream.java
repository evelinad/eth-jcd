/**
 * 
 */
package ch.se.inf.ethz.jcd.batman.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Implements {@link java.io.InputStream} for files on the Batman Virtual Disk
 * 
 */
public class VDiskFileInputStream extends InputStream {
	// fields

	// constructors
	/**
	 * Default constructor cannot be used as this input stream has to be always
	 * associated with a specific file.
	 * 
	 * Internally used to initialize fields to default values
	 */
	private VDiskFileInputStream() {
		// TODO
	}

	/**
	 * Creates a VDiskFileInputStream by opening the file named by the filePath
	 * parameter.
	 * 
	 * @param filePath
	 *            path to the file to open
	 */
	public VDiskFileInputStream(String filePath) throws FileNotFoundException {
		this();
		// TODO
	}

	// public methods
	
	@Override
	public int available() throws IOException {
		// TODO Auto-generated method stub
		return super.available();
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		super.close();
	}

	@Override
	public int read() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int read(byte[] b) throws IOException {
		// TODO Auto-generated method stub
		return super.read(b);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		// TODO Auto-generated method stub
		return super.read(b, off, len);
	}

	@Override
	public long skip(long n) throws IOException {
		// TODO Auto-generated method stub
		return super.skip(n);
	}
	
	/**
	 * Using {@link #mark(int)} and {@link #reset()} is not supported.
	 * As specified it will always return false.
	 * 
	 * @see java.io.InputStream#markSupported()
	 */
	@Override
	public boolean markSupported() {
		return false;
	}
	
	/**
	 * Not supported.
	 * 
	 * @see #markSupported()
	 */
	@Override
	public synchronized void mark(int readlimit) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Not supported.
	 * 
	 * @see #markSupported()
	 */
	@Override
	public synchronized void reset() throws IOException {
		throw new UnsupportedOperationException();
	}

	// protected methods

	// private methods

}
