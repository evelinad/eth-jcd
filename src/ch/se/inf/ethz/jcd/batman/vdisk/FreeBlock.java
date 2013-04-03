package ch.se.inf.ethz.jcd.batman.vdisk;

import java.io.IOException;

public class FreeBlock extends VirtualBlock implements IFreeBlock {
	
	public static final IFreeBlock load (IVirtualDisk disk, long position) throws IOException {
		FreeBlock block = new FreeBlock(disk, position, 0, 0, 0);
		block.readMetadata();
		return block;
	}
	
	public static final IFreeBlock create (IVirtualDisk disk, long position, long size, long previous, long next) throws IOException {
		FreeBlock block = new FreeBlock(disk, position, size, previous, next);
		block.updateMetadata();
		return block;
	}

	private static final long PREVIOUS_SIZE = 8;
	private static final long NEXT_SIZE = 8;

	protected static final long METADATA_START_SIZE = VirtualBlock.METADATA_START_SIZE + PREVIOUS_SIZE + NEXT_SIZE;
	protected static final long METADATA_END_SIZE = VirtualBlock.METADATA_END_SIZE;
	protected static final long METADATA_SIZE = METADATA_START_SIZE + METADATA_END_SIZE;
	protected static final long MIN_BLOCK_SIZE = METADATA_SIZE;
	
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
	
	@Override
	public long getNextBlock() {
		return next;
	}

	@Override
	public void setNextBlock(long nextBlock) throws IOException {
		this.next = nextBlock;
		updateNextBlock();
	}

	@Override
	public long getPreviousBlock() {
		return previous;
	}

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
