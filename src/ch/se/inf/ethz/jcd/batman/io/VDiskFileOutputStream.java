package ch.se.inf.ethz.jcd.batman.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Implements {@link java.io.OutputStream} for files on the Batman Virtual Disk.
 * 
 */
public class VDiskFileOutputStream extends OutputStream {
	// fields

	// constructors
	/**
	 * Default constructor cannot be used as this output stream has to be always
	 * associated with a specific file.
	 * 
	 * Internally used to initialize fields to default values.
	 */
	private VDiskFileOutputStream() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Creates a VDiskFileOutputStream by opening the file named by the filePath
	 * parameter.
	 * 
	 * If the file already exists it will overwrite the current content. If the
	 * file does not yet exist the file will be created.
	 * 
	 * @param filePath
	 *            path to the file to open
	 */
	public VDiskFileOutputStream(String filePath) {
		this(filePath, false);
	}

	/**
	 * Creates a VDiskFileOutputStream by opening the file named by the filePath
	 * parameter.
	 * 
	 * If the file already exists, the written content will be appended to the
	 * current data. If the file dies not yet exist the file will be created.
	 * 
	 * @param filePath
	 *            path to the file to open
	 * @param append
	 *            true if the written content should be appended to the current
	 *            data.
	 */
	public VDiskFileOutputStream(String filePath, boolean append) {
		this();
		// TODO
	}

	// public methods
	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		super.close();
	}

	@Override
	public void flush() throws IOException {
		// TODO Auto-generated method stub
		super.flush();
	}

	@Override
	public void write(int b) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void write(byte[] b) throws IOException {
		// TODO Auto-generated method stub
		super.write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		// TODO Auto-generated method stub
		super.write(b, off, len);
	}

	// protected methods
	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		super.finalize();
	}

	// private methods

}
