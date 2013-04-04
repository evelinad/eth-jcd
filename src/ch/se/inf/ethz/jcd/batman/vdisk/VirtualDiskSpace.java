package ch.se.inf.ethz.jcd.batman.vdisk;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class VirtualDiskSpace implements IVirtualDiskSpace {

	public static IVirtualDiskSpace load (IVirtualDisk disk, long position) throws IOException {
		VirtualDiskSpace virtualSpace = new VirtualDiskSpace(disk);
		virtualSpace.load(position);
		return virtualSpace;
	}
	
	public static IVirtualDiskSpace create (IVirtualDisk disk, long size) throws IOException {
		VirtualDiskSpace virtualSpace = new VirtualDiskSpace(disk);
		virtualSpace.create(size);
		return virtualSpace;
	}
	
	private static class VirtualDiskSpacePosition {
		
		private long position;
		private int blockIndex;
		private long blockPosition;
		
		public long getPosition() {
			return position;
		}
		
		public void setPosition(long position) {
			this.position = position;
		}

		public int getBlockIndex() {
			return blockIndex;
		}

		public void setBlockIndex(int blockIndex) {
			this.blockIndex = blockIndex;
		}

		public long getBlockPosition() {
			return blockPosition;
		}

		public void setBlockPosition(long blockPosition) {
			this.blockPosition = blockPosition;
		}
		
	}
	
	private static final int BYTE_LENGTH = 1;
	private static final int LONG_LENGTH = 8;
	
	private IVirtualDisk disk;
	private List<IDataBlock> blocks = new ArrayList<IDataBlock>();
	private VirtualDiskSpacePosition position;
	
	private VirtualDiskSpace(IVirtualDisk disk) throws IOException {
		this.disk = disk;
		this.position = new VirtualDiskSpacePosition();
	}
	
	private void create (long size) throws IOException {
		changeSize(size);
	}
	
	private void load (long position) throws IOException {
		IDataBlock block;
		for (
			block = DataBlock.load(disk, position); 
			block.getNextBlock() != 0;
			block = DataBlock.load(disk, block.getNextBlock())
		) {
			blocks.add(block);
		}
		blocks.add(block);
	}
	
	private VirtualDiskSpacePosition calculatePosition (long position) {
		VirtualDiskSpacePosition vDiskPosition = new VirtualDiskSpacePosition();
		vDiskPosition.setPosition(position);
		long blockPosition = position;
		int index = 0;
		for (; index < blocks.size(); index++) {
			IDataBlock block = blocks.get(index);
			if (block.getDataSize() < blockPosition) {
				blockPosition -= block.getDataSize();
			} else {
				break;
			}
		}
		vDiskPosition.setBlockIndex(index);
		vDiskPosition.setBlockPosition(blockPosition);
		return vDiskPosition;
	}
	
	private VirtualDiskSpacePosition addPosition (VirtualDiskSpacePosition base, long add) {
		VirtualDiskSpacePosition newPosition = new VirtualDiskSpacePosition();
		newPosition.setPosition(base.getPosition() + add);
		int index = base.getBlockIndex();
		long blockPosition = base.getBlockPosition() + add;
		for (; index < blocks.size(); index++) {
			IDataBlock block = blocks.get(index);
			if (block.getDataSize() < blockPosition) {
				blockPosition -= block.getDataSize();
			} else {
				break;
			}
		}
		newPosition.setBlockIndex(index);
		newPosition.setBlockPosition(blockPosition);
		return newPosition;
	}
	
	@Override
	public long getVirtualDiskPosition() {
		return blocks.get(0).getBlockPosition();
	}

	@Override
	public void changeSize(long newSize) throws IOException {
		if (newSize < 1) {
			throw new IllegalArgumentException("Virtual space can't be smaller than 1");
		}
		long currentSize = getSize();
		if (currentSize < newSize) {
			extend(newSize - currentSize);
		} else if (currentSize > newSize) {
			truncate(currentSize - newSize);
		}
	}

	private void truncate (long amount) throws IOException {
		IDataBlock freeBlock = null;
		for (int i = blocks.size() - 1; amount <= 0 && i >= 0; i--) {
			IDataBlock block =  blocks.get(i);
			long dataSize = block.getDataSize();
			if (amount > dataSize) {
				blocks.remove(i);
				amount -= dataSize;
				freeBlock = block;
			} else {
				block.setDataSize(dataSize - amount);
				amount = 0;
			}
		}
		if (freeBlock != null) {
			disk.freeBlock(freeBlock);
		}
	}
	
	private void extend (long amount) throws IOException {
		//Use the the last block if there is still some free space
		IDataBlock lastBlock = getLastBlock();
		if (lastBlock != null) {
			long freeSize = lastBlock.getFreeSize();
			if (freeSize >= 0) {
				long extendSize = Math.min(amount, freeSize);
				lastBlock.setDataSize(lastBlock.getDataSize() + extendSize);
				amount -= extendSize;
			}
		}
		//Request the rest from the disk and add it to the list
		if (amount > 0) {
			IDataBlock[] allocatetBlocks = disk.allocateBlock(amount);
			if (lastBlock != null) {
				lastBlock.setNextBlock(allocatetBlocks[0].getBlockPosition());
			}
			for (int i = 0; i < allocatetBlocks.length; i++) {
				blocks.add(allocatetBlocks[i]);
			}
		}
	}
	
	private IDataBlock getLastBlock () {
		return (blocks.isEmpty() ? null : blocks.get(blocks.size()-1));
	}
	
	@Override
	public long getSize() {
		long size = 0;
		for (IDataBlock block : blocks) {
			size += block.getDataSize();
		}
		return size;
	}
	
	@Override
	public long getDiskSize() {
		long diskSize = 0;
		for (IDataBlock block : blocks) {
			diskSize += block.getDiskSize();
		}
		return diskSize;
	}
	
	@Override
	public long getPosition() {
		return position.getPosition();
	}

	@Override
	public void seek(long pos) {
		position = calculatePosition(pos);
	}

	@Override
	public void write(byte b) throws IOException {
		write(position, b);
		position = addPosition(position, BYTE_LENGTH);
	}

	@Override
	public void write(long l) throws IOException {
		write(position, l);
		position = addPosition(position, LONG_LENGTH);
	}

	@Override
	public void write(byte[] b) throws IOException {
		write(position, b);
		position = addPosition(position, b.length);
	}

	@Override
	public void write(long pos, byte b) throws IOException {
		write(calculatePosition(pos), b);
	}

	@Override
	public void write(long pos, long l) throws IOException {
		write(calculatePosition(pos), l);
	}
	
	@Override
	public void write(long pos, byte[] b) throws IOException {
		write(calculatePosition(pos), b);
	}

	@Override
	public byte read() throws IOException {
		byte b = read(position);
		position = addPosition(position, BYTE_LENGTH);
		return b;
	}

	@Override
	public int read(byte[] b) throws IOException {
		int l = read(position, b);
		position = addPosition(position, b.length);
		return l;
	}

	@Override
	public long readLong() throws IOException {
		long l = readLong(position);
		position = addPosition(position, LONG_LENGTH);
		return l;
	}

	@Override
	public byte read(long pos) throws IOException {
		return read(calculatePosition(pos));
	}

	@Override
	public int read(long pos, byte[] b) throws IOException {
		return read(calculatePosition(pos), b);
	}

	@Override
	public long readLong(long pos) throws IOException {
		return readLong(calculatePosition(pos));
	}
	
	private long getRemainingSpace(IDataBlock block, long blockPosition) {
		return block.getDataSize()-blockPosition;
	}
	
	private long getRemainingSpace(VirtualDiskSpacePosition pos) {
		return getRemainingSpace(blocks.get(pos.getBlockIndex()), pos.getBlockPosition());
	}
	
	private IDataBlock getDataBlock (VirtualDiskSpacePosition pos) {
		return blocks.get(pos.getBlockIndex());
	}
	
	private void write (VirtualDiskSpacePosition pos, byte b) throws IOException {
		allocateSpace(pos, BYTE_LENGTH);
		getDataBlock(pos).write(pos.getBlockPosition(), b);
	}
	
	private void write (VirtualDiskSpacePosition pos, long l) throws IOException {
		write(pos, ByteBuffer.allocate(8).putLong(l).array());
	}
	
	private void allocateSpace(VirtualDiskSpacePosition pos,long length) throws IOException {
		long remainingSpace = getRemainingSpace(pos);
		if (remainingSpace < length) {
			extend(length-remainingSpace);
		}
	}
	
	private void write (VirtualDiskSpacePosition pos, byte[] b) throws IOException {
		allocateSpace(pos, b.length);
		int bytesWritten = 0;
		while (bytesWritten != b.length) {
			long remainingSpace = getRemainingSpace(pos);
			if (remainingSpace == 0) {
				throw new VirtualDiskException("DiskSpace too small!");
			}
			int bytesToWrite = b.length - bytesWritten;
			int currentBytesWritten = 0;
			if (bytesToWrite <= remainingSpace) {
				getDataBlock(pos).write(pos.getBlockPosition(), b, bytesWritten, bytesToWrite);
				currentBytesWritten = bytesToWrite;
			} else {
				getDataBlock(pos).write(pos.getBlockPosition(), b, bytesWritten, (int) remainingSpace);
				currentBytesWritten = (int) remainingSpace;
			}
			bytesWritten += currentBytesWritten;
			pos = addPosition(pos, currentBytesWritten);
		}
	}
	
	private byte read (VirtualDiskSpacePosition pos) throws IOException {
		if (getRemainingSpace(pos) <= 0) {
			throw new VirtualDiskException("End of VirtualSpace reached.");
		}
		return getDataBlock(pos).read(pos.getBlockPosition());
	}
	
	private int read (VirtualDiskSpacePosition pos, byte[] b) throws IOException {
		long readableBytes = getRemainingSpace(pos);
		int readLength = (int) Math.min(b.length, readableBytes);
		int bytesRead = 0;
		while (bytesRead != readLength) {
			long remainingSpace = getRemainingSpace(pos);
			int bytesToRead = readLength - bytesRead;
			int currentBytesRead = 0;
			if (bytesToRead <= remainingSpace) {
				getDataBlock(pos).read(pos.getBlockPosition(), b, bytesRead, bytesToRead);
				currentBytesRead = bytesToRead;
			} else {
				getDataBlock(pos).read(pos.getBlockPosition(), b, bytesRead, (int) remainingSpace);
				currentBytesRead = (int) remainingSpace;
			}
			bytesRead += currentBytesRead;
			pos = addPosition(pos, currentBytesRead);
		}
		return bytesRead;
	}
	
	private long readLong (VirtualDiskSpacePosition pos) throws IOException {
		byte[] longInBytes = new byte[LONG_LENGTH];
		read(pos, longInBytes);
		return ByteBuffer.wrap(longInBytes).getLong();
	}

}
