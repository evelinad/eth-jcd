package ch.se.inf.ethz.jcd.batman.vdisk;

import java.io.IOException;

/**
 * Represents an entry inside the virtual disk. This is the basic interface that
 * is extended by directories ({@link IVirtualDirectory}) and files (
 * {@link IVirtualFile}.
 * 
 */
public interface IVirtualDiskEntry {

	/**
	 * Sets the name of the entry.
	 * 
	 * The length of the name is only limited by the size of the disk itself.
	 * The given name will be encoded into UTF-8 to save it on the disk.
	 * 
	 * @param name
	 *            the name of the virtual disk entry
	 */
	void setName(String name) throws IOException;

	/**
	 * Returns the name of the virtual disk entry.
	 * 
	 * @return the name of the virtual disk entry
	 */
	String getName();

	/**
	 * Returns the parent directory of the virtual disk entry.
	 * 
	 * Only the root directory will return null.
	 * 
	 * @return the parent directory or null if it is the root directory itself
	 */
	IVirtualDirectory getParent();

	/**
	 * Sets the parent of the virtual disk entry to the given directory.
	 * 
	 * @param directory
	 *            the parent of the virtual disk entry
	 * @throws VirtualDiskException 
	 * @throws IOException 
	 */
	void setParent(IVirtualDirectory directory) throws VirtualDiskException, IOException;

	/**
	 * Returns the previous virtual disk entry.
	 * 
	 * Every virtual disk entry (except the root directory) is part of a doubly
	 * linked list containing virtual disk entries ({@link IVirtualDiskEntry})
	 * that are on the same hierarchical level, therefore they have the same
	 * parent ({@link #getParent()}). This method will return the previous entry
	 * in this doubly linked list. It returns null if there is no previous
	 * member.
	 * 
	 * @see IVirtualDirectory#getFirstMember()
	 * @return previous member or null if there is none
	 */
	IVirtualDiskEntry getPreviousEntry();

	/**
	 * Sets the previous virtual disk entry.
	 * 
	 * @see #getPreviousEntry()
	 * @param entry
	 *            the new previous entry
	 */
	void setPreviousEntry(IVirtualDiskEntry entry);

	/**
	 * Returns the next virtual disk entry.
	 * 
	 * Every virtual disk entry (except the root directory) is part of a doubly
	 * linked list containing virtual disk entries ({@link IVirtualDiskEntry})
	 * that are on the same hierarchical level, therefore they have the same
	 * parent ({@link #getParent()}). This method will return the next entry in
	 * this doubly linked list. It returns null if there is no next member.
	 * 
	 * @return next member or null if there is none
	 * @throws IOException 
	 */
	IVirtualDiskEntry getNextEntry() throws IOException;

	/**
	 * Sets the next virtual disk entry.
	 * 
	 * @see #getNextEntry()
	 * @param entry
	 *            the new next entry
	 */
	void setNextEntry(IVirtualDiskEntry entry) throws IOException;

	/**
	 * Returns the timestamp of the virtual disk entry.
	 * 
	 * @return the timestamp of the virtual disk entry
	 */
	long getTimestamp();

	/**
	 * Sets the timestamp to the given timestamp.
	 * 
	 * @param timestamp
	 *            new timestamp for the virtual disk entry
	 */
	void setTimestamp(long timestamp) throws IOException;

	/**
	 * Deletes the virtual disk entry.
	 * 
	 * For details how this is exactly executed read the documentation of the
	 * specific implementations.
	 * @throws IOException 
	 */
	void delete() throws IOException;

	/**
	 * Indicates if the virtual disk entry is still on the disk or was deleted.
	 * 
	 * @return true if it is still on the disk, otherwise false
	 */
	boolean exists();

	/**
	 * Returns the position of the entry inside the virtual disk.
	 * 
	 * The returned position is relative to the virtual disk beginning. Position
	 * 0 would be the first byte of the virtual disk.
	 * 
	 * @return the position of the entry inside the virtual disk
	 */
	long getPosition();

}
