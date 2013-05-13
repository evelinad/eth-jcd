package ch.se.inf.ethz.jcd.batman.vdisk.util;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualDirectory;
import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualDiskEntry;

/**
 * Utility class to work with a VirtualDisk.
 * 
 * This class provides some static methods for convenience and to prevent code
 * duplication.
 * 
 */
public class VirtualDiskUtil {

	/**
	 * Returns a collection of members of a directory.
	 * 
	 * A member of a directory is a {@link IVirtualDiskEntry} that belongs to
	 * the given directory. Therefore this method returns children of the given
	 * directory.
	 * 
	 * @param directory
	 *            the parent directory of the returned members
	 * @return a collection of members which are children of the given directory
	 * @throws IOException
	 *             in case of an {@link IOException} of the underlying virtual
	 *             disk
	 */
	public static Collection<IVirtualDiskEntry> getDirectoryMembers(
			IVirtualDirectory directory) throws IOException {

		List<IVirtualDiskEntry> members = new LinkedList<IVirtualDiskEntry>();
		IVirtualDiskEntry firstMember = directory.getFirstMember();
		if (firstMember != null) {
			for (IVirtualDiskEntry currentMember : firstMember) {
				members.add(currentMember);
			}
		}

		return members;
	}

	/**
	 * Searches the {@link IVirtualDiskEntry} whose parent is the given
	 * {@link IVirtualDirectory} directory and has the given name.
	 * 
	 * This method may return null if no entry was found.
	 * 
	 * @param directory
	 *            the directory in which to search for the given name
	 * @param name
	 *            the name to search inside the directory
	 * @return a {@link IVirtualDiskEntry} if the member was found, otherwise
	 *         null
	 * @throws IOException
	 *             in case of an {@link IOException} of the underlying virtual
	 *             disk
	 */
	public static IVirtualDiskEntry getDirectoryMember(
			IVirtualDirectory directory, String name) throws IOException {

		IVirtualDiskEntry firstChild = directory.getFirstMember();

		if (firstChild == null) {
			return null;
		} else {
			for (IVirtualDiskEntry currentMember : firstChild) {
				if (currentMember.getName().equals(name)) {
					return currentMember;
				}
			}

			return null;
		}
	}

	/**
	 * Returns all members as a collection of names.
	 * 
	 * Works the same way as {@link #getDirectoryMembers(IVirtualDirectory)}.
	 * 
	 * May return an empty list if no members were found.
	 * 
	 * @see #getDirectoryMembers(IVirtualDirectory)
	 * @param directory
	 *            the parent directory of the returned members
	 * @return a list of member names or null if none found
	 * @throws IOException
	 *             in case of an {@link IOException} of the underlying virtual
	 *             disk
	 */
	public static Collection<String> getDirectoryMemberNames(
			IVirtualDirectory directory) throws IOException {

		Collection<String> names = new LinkedList<String>();
		IVirtualDiskEntry firstChild = directory.getFirstMember();

		if (firstChild != null) {
			for (IVirtualDiskEntry entry : directory.getFirstMember()) {
				names.add(entry.getName());
			}
		}

		return names;
	}

}
