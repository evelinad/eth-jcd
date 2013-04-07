package ch.se.inf.ethz.jcd.batman.vdisk;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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
	
	private static final int SUPERBLOCK_SIZE = 192;  
	private static final int FREE_LISTS_POSITION = 24;
	private static final int POSITION_SIZE = 8;
	private static final int NR_FREE_LISTS = 21;
	private static final String ROOT_DIRECTORY_NAME = "root";
	private static final long MIN_BLOCK_SIZE = 128;
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
	
	private IFreeBlock extend (long amount) throws IOException {
		long freeBlockPosition = file.length();
		file.setLength(freeBlockPosition + amount);
		IFreeBlock newSpace = FreeBlock.create(this, freeBlockPosition, amount, 0, 0);
		addFreeBlockToList(newSpace);
		return newSpace;
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
		for (int i = 0; i < NR_FREE_LISTS; i++) {
			file.writeLong(0);
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
			file.seek(FREE_LISTS_POSITION + freeListIndex * POSITION_SIZE);
			file.writeLong(block.getNextBlock());
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
		file.seek(FREE_LISTS_POSITION + freeListIndex * POSITION_SIZE);
		file.writeLong(block.getBlockPosition());
	}

	private int getFreeListIndex(long length) {
		long correcteLength = length;
		if (correcteLength < MIN_BLOCK_SIZE) {
			correcteLength = MIN_BLOCK_SIZE;
		}
		int index = (int) (Math.log(correcteLength/MIN_BLOCK_SIZE)/Math.log(2));
		return index > NR_FREE_LISTS - 1 ? NR_FREE_LISTS - 1 : index;
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
		long metaDataSize = DataBlock.METADATA_SIZE;
		long remainingDataSize = dataSize;
		//Search the usable free blocks and extend the disk if necessary
		List<IFreeBlock> usableFreeBlocks = new LinkedList<IFreeBlock>();
		//First search through the big blocks and try to find a continuous block
		for (int index = getFreeListIndex(remainingDataSize + metaDataSize); index < freeLists.size(); index++) {
			IFreeBlock freeBlock = null;
			for (long nextEntry = freeLists.get(index); nextEntry != 0; nextEntry = freeBlock.getNextBlock()) {
				 freeBlock = FreeBlock.load(this, nextEntry);
				 if (freeBlock.getDiskSize() >= dataSize + metaDataSize) {
					 remainingDataSize -= freeBlock.getDiskSize() - metaDataSize;
					 usableFreeBlocks.add(freeBlock); 
				 }
			}
		}
		//If no continuous block was found try to fit some smaller blocks together
		for (int index = freeLists.size() - 1; index >= 0 && remainingDataSize > 0; index--) {
			IFreeBlock freeBlock = null;
			for (long nextEntry = freeLists.get(index); nextEntry != 0 && remainingDataSize > 0; nextEntry = freeBlock.getNextBlock()) {
				 freeBlock = FreeBlock.load(this, nextEntry);
				 remainingDataSize -= freeBlock.getDiskSize() - metaDataSize;
				 usableFreeBlocks.add(freeBlock);
			} 
		}
		if (remainingDataSize > 0) {
			IFreeBlock newFreeBlock = extend(Math.max(MIN_BLOCK_SIZE, remainingDataSize + metaDataSize));
			usableFreeBlocks.add(0, newFreeBlock);
		}
		//Allocate the freeBlocks and return them
		LinkedList<IDataBlock> allocatedDataBlocks = new LinkedList<IDataBlock>();
		long remainingDataSizeToAllocate = dataSize;
		for (IFreeBlock freeBlock : usableFreeBlocks) {
			long remainingSizeToAllocate = remainingDataSizeToAllocate + metaDataSize;
			IDataBlock allocatedBlock;
			removeFreeBlockFromList(freeBlock);
			if (freeBlock.getDiskSize() > remainingSizeToAllocate) {
				if (isBlockSplittable(freeBlock, remainingSizeToAllocate)) {
					allocatedBlock = splitBlock(freeBlock, remainingSizeToAllocate, remainingDataSizeToAllocate);
				} else {
					allocatedBlock = DataBlock.create(this, freeBlock.getBlockPosition(), freeBlock.getDiskSize(), remainingDataSizeToAllocate, 0);
				}
				remainingDataSizeToAllocate = 0;
			} else {
				allocatedBlock = DataBlock.create(this, freeBlock.getBlockPosition(), freeBlock.getDiskSize(), freeBlock.getDiskSize() - metaDataSize, 0);
				remainingDataSizeToAllocate -= freeBlock.getDiskSize() - metaDataSize;
			}
			if (!allocatedDataBlocks.isEmpty()) {
				allocatedDataBlocks.getLast().setNextBlock(allocatedBlock.getBlockPosition());
			}
			allocatedDataBlocks.add(allocatedBlock);
		}
		return allocatedDataBlocks.toArray(new IDataBlock[allocatedDataBlocks.size()]);
	}

	private void readFreeLists () throws IOException {
		file.seek(FREE_LISTS_POSITION);
		freeLists.clear();
		for (int i = 0; i < NR_FREE_LISTS; i++) {
			freeLists.add(file.readLong());
		}
	}

	@Override
	public long getFreeSpace() throws IOException {
		long freeSpace = 0;
		for (Long freeListPosition : freeLists) {
			IFreeBlock freeBlock = null;
			for (long nextEntry = freeListPosition; nextEntry != 0; nextEntry = freeBlock.getNextBlock()) {
				 freeBlock = FreeBlock.load(this, nextEntry);
				 freeSpace += freeBlock.getDiskSize();
			}
		}
		return freeSpace;
	}

	@Override
	public long getOccupiedSpace() throws IOException {
		return getSize() - getFreeSpace();
	}
	
}
