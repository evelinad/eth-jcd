package ch.se.inf.ethz.jcd.batman.vdisk;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//TODO implement block search properly
public final class VirtualDisk implements IVirtualDisk {

	public static IVirtualDisk load (String path) throws IOException {
		VirtualDisk virtualDisk = new VirtualDisk(path);
		virtualDisk.loadDisk();
		return virtualDisk;
	}
	
	public static IVirtualDisk create (String path) throws IOException {
		VirtualDisk virtualDisk = new VirtualDisk(path);
		virtualDisk.createDisk();
		return virtualDisk;
	}
	
	private static final byte[] MAGIC_NUMBER = new byte[] {(byte) 0xDE, (byte) 0xAD, (byte) 0xC0, (byte) 0xFF, (byte) 0xEE, 0x00, 0x00, 0x00};
	private static final int SUPERBLOCK_SIZE = 192;  
	private static final int FREE_LISTS_POSITION = 24;
	private static final int POSITION_SIZE = 8;
	private static final int NR_FREE_LISTS = 21;
	private static final int FREE_LIST_SIZE = NR_FREE_LISTS*POSITION_SIZE;
	private static final String ROOT_DIRECTORY_NAME = "root";
	private static final long MIN_BLOCK_SIZE = 128;
	private static final long MIN_EXTEND_SIZE = 1024;
	private static final long ROOT_DIRECTORY_POSITION = 8;
	
	private RandomAccessFile file;
	private IVirtualDirectory rootDirectory;
	private final List<Long> freeLists = new ArrayList<Long>();
	private final String path;
	
	/*
	 * 0x00 8byte   MagicNumber
	 * 0x08 8byte   Root Directory
	 * 0x10 8byte   Reserved
	 * 0x18 160byte FreeLists
	 */
	private VirtualDisk(String path) {
		this.path = path;
	}
	
	private void loadDisk () throws IOException {
		File f = new File(path);
		if (!f.exists()) {
			throw new IllegalArgumentException("Can't load Virtual Disk at " + path + ". File does not exist.");
		}
		file = new RandomAccessFile(f, "rw");
		if (file.length() < SUPERBLOCK_SIZE) {
			throw new IllegalArgumentException("Can't load Virtual Dsik " + path + ". Corrupt data.");
		}
		byte[] magicNumber = new byte[MAGIC_NUMBER.length];
		file.read(magicNumber);
		if (!Arrays.equals(MAGIC_NUMBER, magicNumber)) {
			throw new IllegalArgumentException("Can't load Virtual Dsik " + path + ". Wrong file type.");
		}
		readFreeLists();
		loadRootDirectory();
	}
	
	private void loadRootDirectory () throws IOException {
		file.seek(ROOT_DIRECTORY_POSITION);
		long rootDirectoryPosition = file.readLong();
		rootDirectory = VirtualDirectory.load(this, rootDirectoryPosition);
	}
	
	private void createDisk () throws IOException {
		File f = new File(path);
		if (f.exists()) {
			throw new IllegalArgumentException("Can't create Virtual Disk at " + path + ". File already exists.");
		}
		file = new RandomAccessFile(f, "rw");
		file.write(MAGIC_NUMBER);
		initializeFreeList();
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
	 * 21 : 128*2^21-infinity
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
		file.seek(ROOT_DIRECTORY_POSITION);
		file.writeLong(rootDirectory.getPosition());
	}
	
	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}
	
	@Override
	public void close() throws IOException {
		if (file != null) {
			file.close();
		}
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
	public IVirtualDirectory createDirectory(IVirtualDirectory parent,
			String name) throws IOException {
		IVirtualDirectory directory = VirtualDirectory.create(this, name);
		if (parent != null) {
			parent.addMember(directory);
		}
		return directory;
	}

	@Override
	public IVirtualFile createFile(IVirtualDirectory parent, String name, long size) throws IOException {
		IVirtualFile file = VirtualFile.create(this, name, size);
		if (parent != null) {
			parent.addMember(file);
		}
		return file;
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
		if (block.isValid()) {
			freeRange(block.getBlockPosition(), block.getDiskSize());	
		}
	}
	
	@Override
    public URI getHostLocation() {
        return new File(path).toURI();
    }

	private void removeFreeBlockFromList (IFreeBlock block) throws IOException {
		if (block.getPreviousBlock() == 0) {
			//First block in the list
			int freeListIndex = getFreeListIndex(block.getDiskSize());
			freeLists.set(freeListIndex, block.getNextBlock());
		} else {
			//Middle/end block
			IFreeBlock previousBlock = FreeBlock.load(this, block.getPreviousBlock());
			if (block.getNextBlock() == 0) {
				previousBlock.setNextBlock(0);
			} else {
				IFreeBlock nextBlock = FreeBlock.load(this, block.getNextBlock());
				previousBlock.setNextBlock(nextBlock.getBlockPosition());
				nextBlock.setPreviousBlock(previousBlock.getBlockPosition());
			}
		}
	}
	
	private void addFreeBlockToList (IFreeBlock block) throws IOException {
		int freeListIndex = getFreeListIndex(block.getDiskSize());
		long firstFreeListEntry = freeLists.get(freeListIndex);
		if (firstFreeListEntry == 0) {
			block.setNextBlock(0);
		} else {
			IFreeBlock previousFirstBlock = FreeBlock.load(this, firstFreeListEntry);
			previousFirstBlock.setPreviousBlock(block.getBlockPosition());
			block.setNextBlock(previousFirstBlock.getBlockPosition());	
		}
		freeLists.set(freeListIndex, block.getBlockPosition());
	}

	private int getFreeListIndex(long length) {
		long correcteLength = length;
		if (correcteLength < MIN_BLOCK_SIZE) {
			correcteLength = MIN_BLOCK_SIZE;
		}
		int index = (int) (Math.log(correcteLength/MIN_BLOCK_SIZE)/Math.log(2));
		return index > FREE_LIST_SIZE - 1 ? FREE_LIST_SIZE - 1 : index;
	}
	
	private boolean isFirstBlock (long position) {
		return position <= SUPERBLOCK_SIZE;
	}
	
	private boolean isLastBlock (long position, long size) throws IOException {
		return (position + size) >= getSize();
	}
	
	private void freeRange (long position, long size) throws IOException {
		//check if previous or/and next is free
		long freeBlockStart = position;
		long freeBlockSize = size;
		if (!isFirstBlock(position)) {
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
		while (!blocksAllocated) {
			for (int freeListIndex = getFreeListIndex(size); freeListIndex < freeLists.size() && !blocksAllocated; freeListIndex++) {
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
				//Free the allocated blocks and extend the disk
				for (IDataBlock block : allocatedBlocks) {
					block.free();
				}
				allocatedBlocks.clear();
				extend(Math.max(size, MIN_EXTEND_SIZE));
			}
		}
		return allocatedBlocks.toArray(new IDataBlock[allocatedBlocks.size()]);
	}

	private void readFreeLists () throws IOException {
		file.seek(FREE_LISTS_POSITION);
		freeLists.clear();
		for (int i = 0; i < NR_FREE_LISTS; i++) {
			freeLists.add(file.readLong());
		}
	}
	
}
