package ch.se.inf.ethz.jcd.batman.vdisk;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class VirtualDiskSpaceTest extends NewDiskPerTest {

    @Test
    public void sizeTest() throws IOException {
		IVirtualDiskSpace diskSpace = VirtualDiskSpace.create(disk, 100);
		assertEquals(100, diskSpace.getSize());
		
		//Test expand
		diskSpace.changeSize(200);
		assertEquals(200, diskSpace.getSize());
		
		//Test truncate
		diskSpace.changeSize(50);
		assertEquals(50, diskSpace.getSize());
	}
    
    @Test
    public void seekTest () throws IOException {
    	IVirtualDiskSpace diskSpace = VirtualDiskSpace.create(disk, 100);
    	assertEquals(0, diskSpace.getPosition());
    	
    	diskSpace.seek(10);
    	assertEquals(10, diskSpace.getPosition());
    	
    	diskSpace.seek(1000);
    	assertEquals(1000, diskSpace.getPosition());
    }
    
    @Test
    public void readWriteTest() throws IOException {
    	IVirtualDiskSpace diskSpace = VirtualDiskSpace.create(disk, 100);
    	
    	//Test byte read/write
    	diskSpace.seek(0);
    	diskSpace.write((byte) 1);
    	diskSpace.seek(0);
    	assertEquals(1, diskSpace.read());
    	
    	diskSpace.write(5, (byte) 2);
    	assertEquals(2, diskSpace.read(5));
    	
    	//Test long read/write
    	diskSpace.seek(0);
    	diskSpace.writeLong(3);
    	diskSpace.seek(0);
    	assertEquals(3, diskSpace.readLong());
    	
    	diskSpace.writeLong(6, 4);
    	assertEquals(4, diskSpace.readLong(6));
    	
    	//Test byte[]
    	byte[] testArray = new byte[] {0, 1, 2, 3, 4, 5};
    	byte[] bufferArray = new byte[testArray.length];
    	diskSpace.seek(0);
    	diskSpace.write(testArray);
    	diskSpace.seek(0);
    	diskSpace.read(bufferArray);
    	assertArrayEquals(testArray, bufferArray);
    	
    	diskSpace.write(20, testArray);
    	bufferArray = new byte[testArray.length];
    	diskSpace.read(20, bufferArray);
    	Assert.assertArrayEquals(testArray, bufferArray);
    }
    
    @Test
    public void autoExpandTest () throws IOException {
    	//Test write at end of file
    	IVirtualDiskSpace diskSpace = VirtualDiskSpace.create(disk, 100);
    	diskSpace.seek(99);
    	diskSpace.writeLong(5);
    	assertEquals(5, diskSpace.readLong(99));
    	
    	//Test write at place which doesn't currently exist in the diskSpace
    	diskSpace.writeLong(10000, 5);
    	assertEquals(5, diskSpace.readLong(10000));
    }
    
    @Test
    public void truncateTest ()  throws IOException {
    	//Test if the data size matches the new size, if the new size is smaller than the original.
    	IVirtualDiskSpace diskSpace = VirtualDiskSpace.create(disk, 100);
    	diskSpace.changeSize(50);
    	assertEquals(50, diskSpace.getSize());
    }
    
    /**
     * Test if offset is set correctly after read/write.
     * @throws IOException
     */
    @Test
    public void positionTest () throws IOException {
    	IVirtualDiskSpace diskSpace = VirtualDiskSpace.create(disk, 100);
    	
    	//Test read byte
    	diskSpace.seek(0);
    	diskSpace.read();
    	assertEquals(1, diskSpace.getPosition());
    	
    	//Test read long
    	diskSpace.seek(0);
    	diskSpace.readLong();
    	assertEquals(8, diskSpace.getPosition());
    	
    	//Test read byte array
    	byte[] buffer = new byte[5];
    	diskSpace.seek(0);
    	diskSpace.read(buffer);
    	assertEquals(5, diskSpace.getPosition());
    	
    	//Test write byte
    	diskSpace.seek(0);
    	diskSpace.write((byte) 1);
    	assertEquals(1, diskSpace.getPosition());
    	
    	//Test write long
    	diskSpace.seek(0);
    	diskSpace.writeLong(1);
    	assertEquals(8, diskSpace.getPosition());
    	
    	//Test write byte array
    	byte[] testArray = new byte[] {0, 1, 2, 3, 4};
    	diskSpace.seek(0);
    	diskSpace.read(testArray);
    	assertEquals(5, diskSpace.getPosition());
    }
    
    @Test
    public void freeTest () throws IOException {
    	IVirtualDiskSpace diskSpace = VirtualDiskSpace.create(disk, 100);
    	diskSpace.free();
    	boolean exceptionThrown = false;
    	try {
    		diskSpace.write((byte) 1);
    	} catch (VirtualDiskException e) {
    		exceptionThrown = true;
    	}
    	Assert.assertTrue(exceptionThrown);
    }
    
}
