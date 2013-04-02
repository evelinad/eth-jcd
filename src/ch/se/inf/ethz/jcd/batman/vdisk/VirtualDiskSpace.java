package ch.se.inf.ethz.jcd.batman.vdisk;

import java.util.ArrayList;
import java.util.List;

public class VirtualDiskSpace implements IVirtualDiskSpace {

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
	
	private List<DataBlock> blocks;
	private VirtualDiskSpacePosition position;
	
	public VirtualDiskSpace() {
		blocks = new ArrayList<DataBlock>();
	}
	
	private VirtualDiskSpacePosition calculatePosition (long position) {
		VirtualDiskSpacePosition vDiskPosition = new VirtualDiskSpacePosition();
		vDiskPosition.setPosition(position);
		int index = 0;
		for (; index < blocks.size(); index++) {
			DataBlock block = blocks.get(index);
			if (block.getDataSize() < position) {
				position -= block.getDataSize();
			} else {
				break;
			}
		}
		vDiskPosition.setBlockIndex(index);
		vDiskPosition.setBlockPosition(position);
		return vDiskPosition;
	}
	
	@Override
	public long getVirtualDiskPosition() {
		return blocks.get(0).getBlockPosition();
	}

	@Override
	public void changeSize(long newSize) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public long getSize() {
		long size = 0;
		for (DataBlock block : blocks) {
			size += block.getDataSize();
		}
		return size;
	}
	
	@Override
	public long getDiskSize() {
		long diskSize = 0;
		for (DataBlock block : blocks) {
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
	public void write(byte b) {
		write(position, b);
	}

	@Override
	public void write(long l) {
		write(position, l);
	}

	@Override
	public void write(byte[] b) {
		write(position, b);
	}

	@Override
	public void write(long pos, byte b) {
		write(calculatePosition(pos), b);
	}

	@Override
	public void write(long pos, long l) {
		write(calculatePosition(pos), l);
	}
	
	@Override
	public void write(long pos, byte[] b) {
		write(calculatePosition(pos), b);
	}

	@Override
	public byte read() {
		return read(position);
	}

	@Override
	public long read(byte[] b) {
		return read(position, b);
	}

	@Override
	public long readLong() {
		return read(position);
	}

	@Override
	public byte read(long pos) {
		return read(calculatePosition(pos));
	}

	@Override
	public long read(long pos, byte[] b) {
		return read(calculatePosition(pos), b);
	}

	@Override
	public long readLong(long pos) {
		return readLong(calculatePosition(pos));
	}
	
	private void write (VirtualDiskSpacePosition pos, byte b) {
		
	}
	
	private void write (VirtualDiskSpacePosition pos, long l) {
		
	}
	
	private void write (VirtualDiskSpacePosition pos, byte[] b) {
		
	}
	
	private byte read (VirtualDiskSpacePosition pos) {
		return 0;
	}
	
	private long read (VirtualDiskSpacePosition pos, byte[] b) {
		return 0;
	}
	
	private long readLong (VirtualDiskSpacePosition pos) {
		return 0;
	}


}
