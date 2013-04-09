package ch.se.inf.ethz.jcd.batman.vdisk;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import ch.se.inf.ethz.jcd.batman.vdisk.impl.DataBlock;
import ch.se.inf.ethz.jcd.batman.vdisk.impl.VirtualBlock;

public class DataBlockTest extends NewDiskPerTest {

	@Test
	public void validTest() throws IOException {
		IDataBlock[] allocateBlock = disk.allocateBlock(100);
		for (IDataBlock block : allocateBlock) {
			assertTrue(block.isValid());
			block.free();
			assertFalse(block.isValid());
		}
	}

	@Test
	public void nextTest() throws IOException {
		//Create Block structure
		IDataBlock[] allocateBlock = disk.allocateBlock(100);
		IDataBlock lastBlock = null;
		for (IDataBlock block : allocateBlock) {
			lastBlock = block;
		}
		IDataBlock[] newAllocateBlock = disk.allocateBlock(100);
		lastBlock.setNextBlock(newAllocateBlock[0].getBlockPosition());
		assertEquals(newAllocateBlock[0].getBlockPosition(), lastBlock.getNextBlock());
		
		//Close and load the disk
		disk.close();
		disk = loadDisk();
		
		//Check if the block structure is still intact
		IDataBlock lastBlockCheck = DataBlock.load(disk, lastBlock.getBlockPosition());
		assertEquals(lastBlock.getDataSize(), lastBlockCheck.getDataSize());
		assertEquals(lastBlock.getDiskSize(), lastBlockCheck.getDiskSize());
		assertEquals(lastBlock.getFreeSize(), lastBlockCheck.getFreeSize());
		assertEquals(lastBlock.getNextBlock(), lastBlockCheck.getNextBlock());
	}
	
	@Test
	public void readWriteTest () throws IOException {
		//Minimum size for data blocks is 128 which means the allocated block can't be split
		IDataBlock[] allocateBlock = disk.allocateBlock(50);
		IDataBlock dataBlock = allocateBlock[0];
		
		//Check byte read/write
		dataBlock.write(0, (byte) 1);
		assertEquals(1, dataBlock.read(0));
		dataBlock.write(49, (byte) 2);
		assertEquals(2, dataBlock.read(49));
		
		boolean byteWriteException = false;
		try {
			dataBlock.write(50, (byte) 1);
		} catch (IllegalArgumentException e) {
			byteWriteException = true;
		}
		assertTrue(byteWriteException);
		
		//Check byte read/write
		dataBlock.writeLong(0, 3);
		assertEquals(3, dataBlock.readLong(0));
		dataBlock.write(42, (byte) 4);
		assertEquals(4, dataBlock.read(42));
		
		boolean longWriteException = false;
		try {
			dataBlock.writeLong(43, 1);
		} catch (IllegalArgumentException e) {
			longWriteException = true;
		}
		assertTrue(longWriteException);
		
		//Check byte array read/write
		byte[] testArray = new byte[] {0, 1, 2, 3, 4, 5};
		byte[] buffer = new byte[testArray.length];
		dataBlock.write(0, testArray);
		dataBlock.read(0, buffer);
		assertArrayEquals(testArray, buffer);
		dataBlock.write(44, testArray);
		buffer = new byte[testArray.length];
		dataBlock.read(44, buffer);
		assertArrayEquals(testArray, buffer);
		
		boolean byteArrayWriteException = false;
		try {
			dataBlock.write(45, testArray);
		} catch (IllegalArgumentException e) {
			byteArrayWriteException = true;
		}
		assertTrue(byteArrayWriteException);
	}
	
	@Test
	public void allocationFreeTest () throws IOException {
		//Minimum size for data blocks is 128 which means the allocated block can't be split
		IDataBlock[] allocateBlock1 = disk.allocateBlock(50);
		IDataBlock[] allocateBlock2 = disk.allocateBlock(50);
		IDataBlock[] allocateBlock3 = disk.allocateBlock(50);
		long firstAllocatedBlockPosition = allocateBlock1[0].getBlockPosition();
		assertFalse(VirtualBlock.checkIfPreviousFree(disk, allocateBlock2[0].getBlockPosition()));
		assertFalse(VirtualBlock.checkIfNextFree(disk, allocateBlock2[0].getBlockPosition()));
		assertEquals(128, VirtualBlock.getSizeOfPreviousBlock(disk, allocateBlock2[0].getBlockPosition()));
		assertEquals(128, VirtualBlock.getSizeOfNextBlock(disk, allocateBlock2[0].getBlockPosition()));
		
		allocateBlock1[0].free();
		allocateBlock3[0].free();
		assertTrue(VirtualBlock.checkIfPreviousFree(disk, allocateBlock2[0].getBlockPosition()));
		assertTrue(VirtualBlock.checkIfNextFree(disk, allocateBlock2[0].getBlockPosition()));
		
		allocateBlock2[0].free();
		IDataBlock[] allocateBlock4 = disk.allocateBlock(50);
		assertEquals(firstAllocatedBlockPosition, allocateBlock4[0].getBlockPosition());
	}
}
