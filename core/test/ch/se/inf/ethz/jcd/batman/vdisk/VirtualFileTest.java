package ch.se.inf.ethz.jcd.batman.vdisk;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import ch.se.inf.ethz.jcd.batman.vdisk.util.VirtualDiskUtil;

public class VirtualFileTest extends NewDiskPerTest {

	@Test
	public void testDelete() throws IOException {
		IVirtualFile file = disk.createFile(disk.getRootDirectory(), "Test",
				100);
		assertTrue(file.exists());
		file.delete();
		assertFalse(file.exists());
	}

	@Test
	public void nameTest() throws IOException {
		// Test name set correctly
		IVirtualFile file = disk.createFile(disk.getRootDirectory(), "Test",
				100);
		assertEquals("Test", file.getName());

		// Test name change
		file.setName("Foobar");
		assertEquals("Foobar", file.getName());

		// Test invalid names
		boolean invalidNameException = false;
		try {
			file.setName("bla/foo");
		} catch (VirtualDiskException e) {
			invalidNameException = true;
		}
		assertTrue(invalidNameException);

		// Test same name
		boolean sameNameExceptionCreate = false;
		try {
			disk.createFile(disk.getRootDirectory(), "Foobar", 100);
		} catch (VirtualDiskException e) {
			sameNameExceptionCreate = true;
		}
		assertTrue(sameNameExceptionCreate);

		boolean sameNameExceptionChange = false;
		IVirtualFile sameFileName = disk.createFile(disk.getRootDirectory(),
				"bla", 100);
		try {
			sameFileName.setName("Foobar");
		} catch (VirtualDiskException e) {
			sameNameExceptionChange = true;
		}
		assertTrue(sameNameExceptionChange);
	}

	@Test
	public void timestampTest() throws IOException {
		IVirtualFile file = disk.createFile(disk.getRootDirectory(), "Test",
				100);
		file.setTimestamp(5);
		assertEquals(5, file.getTimestamp());
	}

	@Test
	public void sizeTest() throws IOException {
		IVirtualFile file = disk.createFile(disk.getRootDirectory(), "Test",
				100);
		assertEquals(100, file.getSize());
	}

	@Test
	public void parentTest() throws IOException {
		IVirtualFile file = disk.createFile(disk.getRootDirectory(), "Test",
				100);
		assertEquals(disk.getRootDirectory(), file.getParent());
	}

	@Test
	public void seekTest() throws IOException {
		IVirtualFile file = disk.createFile(disk.getRootDirectory(), "Test",
				100);
		assertEquals(0, file.getFilePointer());
		file.seek(50);
		assertEquals(50, file.getFilePointer());

		file.seek(50);
		file.write((byte) 0);
		assertEquals(51, file.getFilePointer());

		file.seek(50);
		byte[] testArray = new byte[10];
		file.write(testArray);
		assertEquals(60, file.getFilePointer());
	}

	@Test
	public void readWriteTest() throws IOException {
		IVirtualFile file = disk.createFile(disk.getRootDirectory(), "Test",
				100);
		file.seek(0);
		file.write((byte) 5);
		file.seek(10);
		byte[] testArray = new byte[] { 0, 1, 2, 3, 4, 5 };
		file.write(testArray);
		disk.close();

		disk = loadDisk();
		IVirtualDiskEntry fileLoaded = VirtualDiskUtil.getDirectoryMember(
				disk.getRootDirectory(), "Test");
		assertTrue(fileLoaded instanceof IVirtualFile);
		IVirtualFile fileCheck = (IVirtualFile) fileLoaded;
		assertEquals(100, fileCheck.getSize());
		fileCheck.seek(0);
		assertEquals(5, fileCheck.read());
		fileCheck.seek(10);
		byte[] buffer = new byte[6];
		fileCheck.read(buffer);
		assertArrayEquals(testArray, buffer);
	}

	@Test
	public void autoExpandTest() throws IOException {
		IVirtualFile file = disk.createFile(disk.getRootDirectory(), "Test",
				100);
		file.seek(100);
		file.write((byte) 0);
		assertEquals(101l, file.getSize());

		byte[] testArray = new byte[] { 0, 1, 2, 3, 4 };
		file = disk.createFile(disk.getRootDirectory(), "Test2", 100);
		file.seek(100);
		file.write(testArray);
		assertEquals(105, file.getSize());
	}
}
