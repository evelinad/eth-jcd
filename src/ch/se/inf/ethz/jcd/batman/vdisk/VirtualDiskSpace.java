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
		// TODO Auto-generated method stub

	}

	@Override
	public void write(byte b) {
		// TODO Auto-generated method stub

	}

	@Override
	public void write(long l) {
		// TODO Auto-generated method stub

	}

	@Override
	public void write(byte[] b) {
		// TODO Auto-generated method stub

	}

	@Override
	public void write(long pos, byte b) {
		// TODO Auto-generated method stub

	}

	@Override
	public void write(long pos, byte[] b) {
		// TODO Auto-generated method stub

	}

	@Override
	public byte read() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long read(byte[] b) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long readLong() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public byte read(long pos) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long read(long pos, byte[] b) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long readLong(long pos) {
		// TODO Auto-generated method stub
		return 0;
	}

}
