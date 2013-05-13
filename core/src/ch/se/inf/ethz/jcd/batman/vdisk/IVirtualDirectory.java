package ch.se.inf.ethz.jcd.batman.vdisk;

import java.io.IOException;

/**
 * Represents a directory on the virtual disk.
 * 
 * A directory contains members ({@link IVirtualDiskEntry}) which belong to the
 * directory. This includes files and subdirectories inside the directory. As
 * defined by the {@link IVirtualDiskEntry} the member list is a doubly linked
 * list.
 */
public interface IVirtualDirectory extends IVirtualDiskEntry {
	/**
	 * Removes the given member from the directory.
	 * 
	 * @param member
	 *            member to remove from the directory
	 * @throws VirtualDiskException
	 *             if the member is not part of this directory
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	void removeMember(IVirtualDiskEntry member) throws VirtualDiskException,
			IOException;

	/**
	 * Adds the given member to the list.
	 * 
	 * @param member
	 *            member to add to the list
	 */
	void addMember(IVirtualDiskEntry member) throws IOException;

	/**
	 * Returns the first member of the list.
	 * 
	 * @see IVirtualDiskEntry#getNextEntry()
	 * @return the first member of the list.
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	IVirtualDiskEntry getFirstMember() throws IOException;
}
