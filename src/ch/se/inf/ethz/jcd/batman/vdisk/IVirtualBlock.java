package ch.se.inf.ethz.jcd.batman.vdisk;


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
	 * Returns the size in bytes of the block on the disk. This includes the
	 * meta data of the block.
	 * 
	 * @see #getDataSize()
	 * @return the size in bytes of the whole block on the virtual disk.
	 */
	long getDiskSize();
	
}
