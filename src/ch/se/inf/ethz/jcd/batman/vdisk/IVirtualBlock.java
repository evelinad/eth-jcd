package ch.se.inf.ethz.jcd.batman.vdisk;

/**
 * Represents a block on the virtual disk.
 * 
 * This interface provides the common contract that all kinds of blocks have to
 * provide to the outside world.
 * 
 * @see IDataBlock
 * @see IFreeBlock
 *
 */
public interface IVirtualBlock {

	/**
	 * Returns the position (address) of the block inside the virtual disk.
	 * 
	 * Address 0 would be the first byte inside the virtual disk.
	 * 
	 * @return the address of the block
	 */
	long getBlockPosition();
	
	/**
	 * Returns the size in bytes of the block on the disk.
	 * 
	 * @return the size in bytes of the whole block on the virtual disk.
	 */
	long getDiskSize();
	
}
