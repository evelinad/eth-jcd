package ch.se.inf.ethz.jcd.batman.vdisk;

import java.io.IOException;


/**
 * Represents a file on the virtual disk.
 */
public interface IVirtualFile extends IVirtualDiskEntry {
	
	/**
	 * Returns the offset position, measured in bytes from the beginning of the file.
	 * 
	 * @return the offset position. 
	 */
	long getDataPosition () throws IOException;
	
	/**
	 * Sets the offset position, measured in bytes from the beginning of the file.
	 * The offset may be set beyond the end of the file. Setting the offset beyond the end 
	 * of the file does not change the file length. The file length will change only by writing 
	 * after the offset has been set beyond the end of the file.
	 * 
	 * @param pos the offset position, measured in bytes from the beginning of the file.
	 */
	void seek (long position) throws IOException;
	
	/**
	 * Reads up to b.length bytes of bytes from the file into an array of bytes 
	 * starting at the current offset.
	 * 
	 * @param b the buffer into which the data is read.
	 * @return the total number of bytes written into b
	 * @throws IOException if an I/O error occurs
	 */
	int read (byte[] b) throws IOException;
	
	/**
	 * Reads a signed eight-bit value from the file at the current offset.
	 * 
	 * @return the next byte of the disk space.
	 * @throws IOException if an I/O error occurs
	 */
	byte read () throws IOException;

	/**
	 * Writes the specified byte to the file at the current offset.
	 * 
	 * @param b the byte to be written
	 * @throws IOException if an I/O error occurs
	 */
	void write (byte b) throws IOException;
	
	/**
	 * Write the specified byte array to the file at the current offset.
	 * 
	 * @param b the byte array to be written
	 * @throws IOException if an I/O error occurs
	 */
	void write (byte[] b) throws IOException;
	
	
	/**
	 * Sets the size of this file.
	 * 
	 * If the present size of the file as returned by the size method
	 * is greater than the newSize argument then the file will be truncated. 
	 * 
	 * If the present size of the file as returned by the size method is smaller than the newSize argument 
	 * then the file will be extended. In this case, the contents of the extended portion of the file are 
	 * not defined.
	 * 
	 * @param newSize the desired size of the file.
	 * @throws IOException if an I/O error occurs
	 */
	void setSize (long newSize) throws IOException;
	
	/**
	 * Returns the size of this file.
	 * 
	 * @return the size of this file, measured in bytes.
	 * @throws IOException if an I/O error occurs
	 */
	long getSize () throws IOException;
	
	/**
	 * Returns the actual disk space used to store this files data.
	 * 
	 * @return the disk space used to stor this file, measured in bytes.
	 * @throws IOException  if an I/O error occurs
	 */
	long getDataDiskSize () throws IOException;
	
}
