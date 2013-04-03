package ch.se.inf.ethz.jcd.batman.vdisk;

/**
 * Represents a directory on the virtual disk.
 * 
 * A directory contains members ({@link IVirtualDiskEntry}) which belong to the
 * directory. This includes files and subdirectories inside the directory. As
 * defined by the {@link IVirtualDiskEntry} the member list is a doubly linked
 * list.
 * 
 */
public interface IVirtualDirectory extends IVirtualDiskEntry {

	/**
	 * Removes the given member from the list.
	 * 
	 * @param member
	 *            member to remove from the list
	 * @throws VirtualDiskException
	 *             TODO
	 */
	void removeMember(IVirtualDiskEntry member) throws VirtualDiskException;

	/**
	 * Adds the given member to the list.
	 * 
	 * @param member
	 *            member to add to the list
	 */
	void addMember(IVirtualDiskEntry member);

	/**
	 * Returns the first member of the list.
	 * 
	 * @see IVirtualDiskEntry#getNextEntry()
	 * @return the first member of the list.
	 */
	IVirtualDiskEntry getFirstMember();

}
