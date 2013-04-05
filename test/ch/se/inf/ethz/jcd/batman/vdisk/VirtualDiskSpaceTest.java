package ch.se.inf.ethz.jcd.batman.vdisk;

import static org.junit.Assert.assertEquals;

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
    	Assert.assertArrayEquals(testArray, bufferArray);
    	
    	diskSpace.write(20, testArray);
    	bufferArray = new byte[testArray.length];
    	diskSpace.read(20, bufferArray);
    	Assert.assertArrayEquals(testArray, bufferArray);

    }
}
