package ch.se.inf.ethz.jcd.batman.vdisk;

/**
 * Represents the states in which a virtual disk entry (
 * {@link IVirtualDiskEntry},
 * {@link ch.se.inf.ethz.jcd.batman.vdisk.impl.VirtualDiskEntry}) can be.
 * 
 * @see ch.se.inf.ethz.jcd.batman.vdisk.impl.VirtualDiskEntry
 */
public enum FileState {
	/**
	 * Represents a state in which the virtual disk entry is still valid and
	 * available on disk.
	 */
	CREATED,

	/**
	 * Represents a state in which the virtual disk entry is not valid anymore
	 * and not on disk anymore.
	 */
	DELETED;
}
