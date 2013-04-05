package ch.se.inf.ethz.jcd.batman.vdisk;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

public class VirtualFileTest extends NewDiskPerTest {

	@Test
	public void testDelete() throws IOException {
		IVirtualFile file = disk.createFile(disk.getRootDirectory(), "Test", 100);
		assertTrue(file.exists());
		file.delete();
		assertFalse(file.exists());
	}

	@Test
	public void nameTest() throws IOException {
		//Test name set correctly
		IVirtualFile file = disk.createFile(disk.getRootDirectory(), "Test", 100);
		assertEquals("Test", file.getName());
		
		//Test name change
		file.setName("Foobar");
		assertEquals("Foobar", file.getName());
		
		//Test invalid names
		boolean invalidNameException = false;
		try {
			file.setName("bla/foo");
		} catch (VirtualDiskException e) {
			invalidNameException = true;
		}
		assertTrue(invalidNameException);
		
		//Test same name
		boolean sameNameExceptionCreate = false;
		try {
			disk.createFile(disk.getRootDirectory(), "Foobar", 100);
		} catch (VirtualDiskException e) {
			sameNameExceptionCreate = true;
		}
		assertTrue(sameNameExceptionCreate);	
		
		boolean sameNameExceptionChange = false;
		IVirtualFile sameFileName = disk.createFile(disk.getRootDirectory(), "bla", 100);
		try {
			sameFileName.setName("Foobar");
		} catch (VirtualDiskException e) {
			sameNameExceptionChange = true;
		}
		assertTrue(sameNameExceptionChange);
	}
	
	@Test
	public void timestampTest() throws IOException {
		IVirtualFile file = disk.createFile(disk.getRootDirectory(), "Test", 100);
		file.setTimestamp(5);
		assertEquals(5, file.getTimestamp());
	}
	
	@Test
	public void sizeTest() throws IOException {
		IVirtualFile file = disk.createFile(disk.getRootDirectory(), "Test", 100);
		assertEquals(100, file.getSize());
	}
	
	@Test
	public void parentTest() throws IOException {
		IVirtualFile file = disk.createFile(disk.getRootDirectory(), "Test", 100);
		assertEquals(disk.getRootDirectory(), file.getParent());
	}
	
	@Test
	public void seekTest() throws IOException {
		IVirtualFile file = disk.createFile(disk.getRootDirectory(), "Test", 100);
		assertEquals(0, file.getFilePointer());
	}
}
