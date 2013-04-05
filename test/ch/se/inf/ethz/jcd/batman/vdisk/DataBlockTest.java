package ch.se.inf.ethz.jcd.batman.vdisk;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

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
	
}
