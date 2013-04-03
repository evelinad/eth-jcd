package ch.se.inf.ethz.jcd.batman.vdisk;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class IVirtualDiskTest {
	// parameter generation
	private static final String TEST_DISK_DIR = System
			.getProperty("java.io.tmpdir");
	private static final long TEST_DISK_SIZE = 1024 * 1024;
	
	// disks to clean up
	private static List<File> disks = new ArrayList<File>();

	@Parameterized.Parameters
	public static Collection<Object[]> getImplementations() {
		File diskA = new File(TEST_DISK_DIR, "A.disk");
		disks.add(diskA);

		Object[][] instances = { new Object[] { new VirtualDisk(
				diskA.getPath(), TEST_DISK_SIZE) } };

		return Arrays.asList(instances);
	}
	
	// clean up
	@AfterClass
	public static void deleteDisks() {
		for(File file : disks) {
			file.delete();
		}
	}

	// fields
	private IVirtualDisk disk;

	// constructors
	public IVirtualDiskTest(IVirtualDisk disk) {
		this.disk = disk;
	}

	// tests
	@Test
	public void sizeCorrectTest() {
		assertEquals(disk.getSize(), TEST_DISK_SIZE);
	}

	@Test
	public void rootDirectoryInitialStateTest() {
		assertNotNull(disk.getRootDirectory());
		assertNull(disk.getRootDirectory().getNextEntry());
		assertNull(disk.getRootDirectory().getPreviousEntry());
		assertEquals(disk.getRootDirectory().getName(), "/");
		assertTrue(disk.getRootDirectory().exists());
	}
	
	/**
	 * Creates the following directory tree:
	 * <code>
	 * / ---> /A/ ---> /A/C/
	 * |---> /B/
	 * </code>
	 */
	@Test
	public void creatDirectoryStructure() {
		String subDir1Name = "A";
		String subDir2Name = "B";
		String subSubDirName = "C";
		
		IVirtualDirectory root = disk.getRootDirectory();
		assertNull(root.getNextEntry());
		assertNull(root.getPreviousEntry());
		assertNull(root.getFirstMember());
		
		// add first sub dir
		IVirtualDirectory subDir1 = disk.createDirectory(root, subDir1Name);
		assertNotNull(subDir1);
		assertTrue(subDir1.exists());
		assertEquals(subDir1.getName(), subDir1Name);
		assertEquals(subDir1.getParent(), root);
		assertEquals(root.getFirstMember(), subDir1);
		assertNull(subDir1.getFirstMember());
		
		// add second sub dir
		IVirtualDirectory subDir2 = disk.createDirectory(root, subDir2Name);
		assertNotNull(subDir2);
		assertTrue(subDir2.exists());
		assertEquals(subDir2.getName(), subDir2Name);
		assertEquals(subDir2.getParent(), root);
		assertNull(subDir2.getFirstMember());
		
		// add sub sub dir
		IVirtualDirectory subSubDir = disk.createDirectory(subDir1, subSubDirName);
		assertNotNull(subSubDir);
		assertTrue(subSubDir.exists());
		assertEquals(subSubDir.getName(), subSubDirName);
		assertEquals(subSubDir.getParent(), subDir1);
		assertNull(subSubDir.getFirstMember());
		assertNull(subSubDir.getNextEntry());
		assertNull(subSubDir.getPreviousEntry());
		
		// clean up the nice way
		subSubDir.delete();
		subDir2.delete();
		subDir1.delete();
		
		assertNull(root.getNextEntry());
		assertNull(root.getPreviousEntry());
		assertNull(root.getFirstMember());
		assertFalse(subSubDir.exists());
		assertFalse(subDir2.exists());
		assertFalse(subDir1.exists());
		
	}
}
