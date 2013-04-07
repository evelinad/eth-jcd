package ch.se.inf.ethz.jcd.batman.vdisk;

import java.io.IOException;

/**
 * Represents a block that is not used to save useful data.
 * 
 * This interface allows to create an implicit doubly linked list of free blocks
 * in the virtual disk. Large parts, which are normally used to store data (see
 * {@link IDataBlock}), are not used and may contain garbage.
 * 
 * The blocks need at least 32 byte to store the data needed for the free list. The data is
 * structured as follows:
 * 0x00 8 byte size and indicator for free block
 * 0x08 8 byte offset position of the previous block in the list
 * 0x10 8 byte offset position of the next block in the list
 * 0xnn n garbage
 * end  8 byte size and indicator for free block 
 * 
 * @see IVirtualBlock
 * @see IDataBlock
 */
public final class FreeBlock extends VirtualBlock implements IFreeBlock {
	
	/**
	 * Loads the free block stored at the offset position given by position.
	 * 
	 * @param disk the disk on which the block is stored
	 * @param position the offset position in bytes of the block
	 * @return the loaded block
	 * @throws IOException if the block at the given position is invalid or if an I/O error occurs
	 */
	public static final IFreeBlock load (IVirtualDisk disk, long position) throws IOException {
		FreeBlock block = new FreeBlock(disk, position, 0, 0, 0);
		block.readMetadata();
		return block;
	}
	
	/**
	 * Creates a free block at the given offset position.
	 * 
	 * @param disk the disk on which the block is created
	 * @param position the offset position in bytes of the block
	 * @param size the size of the newly created block
	 * @param previous the offset position of the previous block in the list
	 * @param next the offset position of the next block in the list
	 * @return the created block
	 * @throws IOException if an I/O error occurs
	 */
	public static final IFreeBlock create (IVirtualDisk disk, long position, long size, long previous, long next) throws IOException {
		FreeBlock block = new FreeBlock(disk, position, size, previous, next);
		block.updateMetadata();
		return block;
	}

	private static final long PREVIOUS_SIZE = 8;
	
	private long previous;
	private long next;
	
	private FreeBlock(IVirtualDisk disk, long position, long size, long previous, long next) throws IOException {
		super(disk, position, size);
		this.previous = previous;
		this.next = next;
	}
	
	protected void readMetadata () throws IOException {
		super.readMetadata();
		previous = readLongRealPosition(VirtualBlock.METADATA_START_SIZE);
		next = readLongRealPosition(VirtualBlock.METADATA_START_SIZE + PREVIOUS_SIZE);
	}
	
	protected void updateMetadata () throws IOException {
		super.updateMetadata();
		updatePreviousBlock();
		updateNextBlock();
	}

	private void updatePreviousBlock () throws IOException {
		writeRealPosition(VirtualBlock.METADATA_START_SIZE, previous);
	}
	
	private void updateNextBlock () throws IOException {
		writeRealPosition(VirtualBlock.METADATA_START_SIZE + PREVIOUS_SIZE, next);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getNextBlock() {
		return next;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setNextBlock(long nextBlock) throws IOException {
		this.next = nextBlock;
		updateNextBlock();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getPreviousBlock() {
		return previous;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setPreviousBlock(long previousBlock) throws IOException {
		this.previous = previousBlock;
		updatePreviousBlock();
	}

	@Override
	protected long setMetaFlagsOnSize(long size) {
		return VirtualBlock.setAllocatedFlag(size, false);
	}

}
