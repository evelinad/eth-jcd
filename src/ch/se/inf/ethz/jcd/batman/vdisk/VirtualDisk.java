package ch.se.inf.ethz.jcd.batman.vdisk;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;


public class VirtualDisk implements IVirtualDisk {

	public static VirtualDisk load (String path) {
		return null;
	}
	
	public static VirtualDisk create (String path, long maxSize) throws IOException {
		return new VirtualDisk(path, maxSize);
	}
	
	private static final byte[] MAGIC_NUMBER = new byte[] {(byte) 0xDE, (byte) 0xAD, (byte) 0xC0, (byte) 0xFF, (byte) 0xEE, 0x00, 0x00, 0x00};
	private static final int SUPERBLOCK_SIZE = 192;  
	private static final int FREE_LISTS_POSITION = 16;
	private static final int POSITION_SIZE = 8;
	private static final int NR_FREE_LISTS = 22;
	private static final int FREE_LIST_SIZE = NR_FREE_LISTS*POSITION_SIZE;
	private static final String ROOT_DIRECTORY_NAME = "root";
	private static final long MIN_BLOCK_SIZE = 128;
	
	private long maxSize;
	private RandomAccessFile file;
	private IVirtualDirectory rootDirectory;
	private List<Long> freeLists = new ArrayList<Long>();
	
	public VirtualDisk(String path, long maxSize) throws IOException {
		setMaxSize(maxSize);
		File f = new File(path);
		if (f.exists()) {
			throw new IllegalArgumentException("Can't create Virtual Diks at " + path + ". File already exists");
		}
		file = new RandomAccessFile(f, "rw");
		
		/*
		 * 0x00 8byte   MagicNumber
		 * 0x08 8byte   Reserved
		 * 0x10 112byte FreeLists
		 */
		file.write(MAGIC_NUMBER);
		initializeFreeList();
		extend(getMaxSize() - getSize());
		createRootDirectory();
	}
	
	private void extend (long amount) throws IOException {
		long freeBlockPosition = getSize();
		file.setLength( + amount);
		IFreeBlock newSpace = FreeBlock.create(this, freeBlockPosition, amount, 0, 0);
		addFreeBlockToList(newSpace);
	}
	
	/*
	 * Initialize the segregated free lists
	 * There is a class for each two power size
	 * 1 : 128-255
	 * ...
	 * 22 : 128*2^22-infinity
	 */
	private void initializeFreeList () throws IOException {
		file.seek(FREE_LISTS_POSITION);
		for (int i = 0; i < FREE_LIST_SIZE; i++) {
			file.write(0);
			freeLists.add(Long.valueOf(0));
		}
	}
	
	private void createRootDirectory() throws IOException {
		rootDirectory = createDirectory(null, ROOT_DIRECTORY_NAME);
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		close();
	}
	
	@Override
	public void close() throws IOException {
		if (file != null) {
			file.close();
		}
	}

	@Override
	public void setMaxSize(long maxSize) throws IOException {
		long fileLength = (file == null) ? 0 : file.length();
		if (fileLength > maxSize || getMinSize() > maxSize) {
			throw new IllegalArgumentException("Virtual file system can't be smaller than " + Math.max(maxSize, getMinSize()));
		}
		this.maxSize = maxSize;
	}
	
	@Override
	public long getMaxSize() {
		return maxSize;
	}

	@Override
	public long getSize() throws IOException {
		return file.length();
	}

	@Override
	public int getSuperblockSize() {
		return SUPERBLOCK_SIZE;
	}

	@Override
	public IVirtualDirectory getRootDirectory() {
		return rootDirectory;
	}

	@Override
	public long getMinSize() {
		return SUPERBLOCK_SIZE + DataBlock.MIN_BLOCK_SIZE;
	}

	@Override
	public IVirtualDirectory createDirectory(IVirtualDirectory parent,
			String name) throws IOException {
		return new VirtualDirectory(this, parent, name);
	}

	@Override
	public IVirtualFile createFile(IVirtualDirectory parent, String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void write(long pos, byte b) throws IOException {
		file.seek(pos);
		file.write(b);
	}

	@Override
	public void write(long pos, byte[] b) throws IOException {
		file.seek(pos);
		file.write(b);
	}

	@Override
	public byte read(long pos) throws IOException {
		file.seek(pos);
		return file.readByte();
	}

	@Override
	public int read(long pos, byte[] b) throws IOException {
		file.seek(pos);
		return file.read(b);
	}

	@Override
	public void write(long pos, byte[] b, int offset, int length)
			throws IOException {
		file.seek(pos);
		file.write(b, offset, length);
	}

	@Override
	public int read(long pos, byte[] b, int offset, int length)
			throws IOException {
		file.seek(pos);
		return file.read(b, offset, length);
	}

	@Override
	public void freeBlock(IDataBlock block) throws IOException {
		long next = block.getNextBlock();
		freeRange(block.getBlockPosition(), block.getDiskSize());
		while (next != 0) {
			IDataBlock toFreeBlock = DataBlock.load(this, next);
			next = toFreeBlock.getNextBlock();
			freeRange(toFreeBlock.getBlockPosition(), toFreeBlock.getDiskSize());
		}
		
	}

	private void removeFreeBlockFromList (IFreeBlock block) throws IOException {
		if (block.getPreviousBlock() == 0) {
			//First block in the list
			int freeListIndex = getFreeListIndex(block.getDiskSize());
			freeLists.set(freeListIndex, block.getNextBlock());
		} else {
			//Middle/end block
			IFreeBlock previousBlock = FreeBlock.load(this, block.getPreviousBlock());
			if (block.getNextBlock() != 0) {
				IFreeBlock nextBlock = FreeBlock.load(this, block.getNextBlock());
				previousBlock.setNextBlock(nextBlock.getBlockPosition());
				nextBlock.setPreviousBlock(previousBlock.getBlockPosition());
			} else {
				previousBlock.setNextBlock(0);
			}
		}
	}
	
	private void addFreeBlockToList (IFreeBlock block) throws IOException {
		int freeListIndex = getFreeListIndex(block.getDiskSize());
		long firstFreeListEntry = freeLists.get(freeListIndex);
		if (firstFreeListEntry != 0) {
			IFreeBlock previousFirstBlock = FreeBlock.load(this, firstFreeListEntry);
			previousFirstBlock.setPreviousBlock(block.getBlockPosition());
			block.setNextBlock(previousFirstBlock.getBlockPosition());
		} else {
			block.setNextBlock(0);
		}
		freeLists.set(freeListIndex, block.getBlockPosition());
	}

	private int getFreeListIndex(long length) {
		if (length < MIN_BLOCK_SIZE) {
			length = MIN_BLOCK_SIZE;
		}
		int index = (int) (Math.log(length/MIN_BLOCK_SIZE)/Math.log(2));
		return ((index > FREE_LIST_SIZE - 1) ? FREE_LIST_SIZE - 1 : index);
	}
	
	private boolean isFirstBlock (long position, long size) {
		return position <= SUPERBLOCK_SIZE;
	}
	
	private boolean isLastBlock (long position, long size) throws IOException {
		return (position + size) >= getSize();
	}
	
	private void freeRange (long position, long size) throws IOException {
		//check if previous or/and next is free
		long freeBlockStart = position;
		long freeBlockSize = size;
		if (!isFirstBlock(position, size)) {
			IVirtualBlock previousBlock = VirtualBlock.loadPreviousBlock(this, position);
			if (previousBlock instanceof IFreeBlock) {
				freeBlockStart -= previousBlock.getDiskSize();
				freeBlockSize += previousBlock.getDiskSize();
				removeFreeBlockFromList((IFreeBlock) previousBlock);
			}
		}
		if (!isLastBlock(position, size)) {
			IVirtualBlock nextBlock = VirtualBlock.loadNextBlock(this, position);
			if (nextBlock instanceof IFreeBlock) {
				freeBlockSize += nextBlock.getDiskSize();
				removeFreeBlockFromList((IFreeBlock) nextBlock);
			}
		}
		addFreeBlockToList(FreeBlock.create(this, freeBlockStart, freeBlockSize, 0, 0));
	}
	
	private boolean isBlockSplittable (IFreeBlock block, long size) {
		return (block.getDiskSize() - size) >= MIN_BLOCK_SIZE;
	}
	
	private IDataBlock splitBlock (IFreeBlock freeBlock, long size, long dataSize) throws IOException {
		IDataBlock dataBlock = DataBlock.create(this, freeBlock.getBlockPosition(), size, dataSize, 0);
		IFreeBlock newFreeBlock = FreeBlock.create(this, freeBlock.getBlockPosition() + size, freeBlock.getDiskSize() - size, 0, 0);
		addFreeBlockToList(newFreeBlock);
		return dataBlock;
	}
	
	@Override
	public IDataBlock[] allocateBlock(long dataSize) throws IOException {
		//TODO connect smaller blocks if no larger block is available
		//TODO remove MetadataSize from data block
		long size = dataSize + DataBlock.METADATA_SIZE;
		if (size < MIN_BLOCK_SIZE) {
			size = MIN_BLOCK_SIZE;
		}
		List<IDataBlock> allocatedBlocks = new ArrayList<IDataBlock>();
		boolean blocksAllocated = false;
		for (int freeListIndex = getFreeListIndex(size); freeListIndex < FREE_LIST_SIZE && !blocksAllocated; freeListIndex++) {
			long nextEntry = freeLists.get(freeListIndex);
			while (nextEntry != 0) {
				IFreeBlock freeBlock = FreeBlock.load(this, nextEntry);
				if (freeBlock.getDiskSize() > size) {
					removeFreeBlockFromList(freeBlock);
					if (isBlockSplittable(freeBlock, size)) {
						allocatedBlocks.add(splitBlock(freeBlock, size, dataSize));
					} else {
						allocatedBlocks.add(DataBlock.create(this, freeBlock.getBlockPosition(), freeBlock.getDiskSize(), dataSize, 0));
					}
					blocksAllocated = true;
					break;
				}
				nextEntry = freeBlock.getNextBlock();
			}
		}
		if (!blocksAllocated) {
			throw new OutOfDiskSpaceException();
		}
		return allocatedBlocks.toArray(new IDataBlock[allocatedBlocks.size()]);
	}
	
	private void readFreeLists () throws IOException {
		file.seek(FREE_LISTS_POSITION);
		freeLists.clear();
		for (int i = 0; i < FREE_LIST_SIZE; i++) {
			freeLists.add(file.readLong());
		}
	}
	
}
