package ch.se.inf.ethz.jcd.batman.vdisk;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
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
	public static Collection<Object[]> getImplementations() throws IOException {
		// create disk a
	    File diskAFile = new File(TEST_DISK_DIR, "A.disk");
	    if(diskAFile.exists()) {
	        diskAFile.delete();
	    }
	    
		disks.add(diskAFile);
		
		IVirtualDisk diskA = VirtualDisk.create(diskAFile.getPath(), TEST_DISK_SIZE);

		Object[][] instances = { {diskA} };

		return Arrays.asList(instances);
	}

	// clean up
	@AfterClass
	public static void deleteDisks() {
		for (File file : disks) {
			file.delete();
		}
	}

	// fields
	private final IVirtualDisk disk;

	// constructors
	public IVirtualDiskTest(IVirtualDisk disk) {
		this.disk = disk;
	}

	// tests
	@Test
	public void sizeCorrectTest() throws IOException {
		assertEquals(disk.getMaxSize(), TEST_DISK_SIZE);
	}

	@Test
	public void rootDirectoryInitialStateTest() {
		assertNotNull(disk.getRootDirectory());
		assertNull(disk.getRootDirectory().getNextEntry());
		assertNull(disk.getRootDirectory().getPreviousEntry());
		assertTrue(disk.getRootDirectory().exists());
	}

	/**
	 * Creates the following directory tree: <code>
	 * / ---> /A/ ---> /A/C/
	 *   \---> /B/
	 * </code>
	 * 
	 * @throws IOException
	 */
	@Test
	public void creatDirectoryStructure() throws IOException {
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
		assertEquals(subDir1Name, subDir1.getName());
		assertEquals(root, subDir1.getParent());
		assertEquals(subDir1, root.getFirstMember());
		assertNull(subDir1.getFirstMember());

		// add second sub dir
		IVirtualDirectory subDir2 = disk.createDirectory(root, subDir2Name);
		assertNotNull(subDir2);
		assertTrue(subDir2.exists());
		assertEquals(subDir2Name, subDir2.getName());
		assertEquals(root, subDir2.getParent());
		assertNull(subDir2.getFirstMember());

		// add sub sub dir
		IVirtualDirectory subSubDir = disk.createDirectory(subDir1,
				subSubDirName);
		assertNotNull(subSubDir);
		assertTrue(subSubDir.exists());
		assertEquals(subSubDirName, subSubDir.getName());
		assertEquals(subDir1, subSubDir.getParent());
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

	/**
	 * Creates the following directory tree: <code>
	 * /  --> /subDir/ --> /subDir/subSubDir1/ --> /subDir/subSubDir1/subSubSubDir/
	 *                 \-> /subDir/subSubDir2/
	 * </code>
	 * 
	 * and deletes after that /subDir/subSubDir1 to check if subSubSubDir was
	 * deleted too. After that, to clean up, we delete subDir and check for any
	 * problems too.
	 * 
	 * @throws IOException
	 */
	@Test
	public void deleteSubDirectoriesAutomaticallyTest() throws IOException {
		IVirtualDirectory subDir = disk.createDirectory(
				disk.getRootDirectory(), "subDir");
		IVirtualDirectory subSubDir1 = disk.createDirectory(subDir,
				"subSubDir1");
		IVirtualDirectory subSubDir2 = disk.createDirectory(subDir,
				"subSubDir2");
		IVirtualDirectory subSubSubDir = disk.createDirectory(subSubDir1,
				"subSubSubDir");

		// check for correct structure
		assertEquals(disk.getRootDirectory(), subDir.getParent());
		assertEquals(subDir, disk.getRootDirectory().getFirstMember());

		assertEquals(subDir, subSubDir1.getParent());
		assertEquals(subDir, subSubDir2.getParent());

		assertEquals(subSubDir1, subSubSubDir.getParent());
		assertEquals(subSubSubDir, subSubDir1.getFirstMember());

		// delete subSubDir1
		subSubDir1.delete();

		// check for deletion
		assertFalse(subSubDir1.exists());
		assertFalse(subSubSubDir.exists());

		// clean up (delete subDir)
		subDir.delete();

		// check clean up
		assertFalse(subDir.exists());
		assertFalse(subSubDir2.exists());
		assertNull(disk.getRootDirectory().getFirstMember());
	}
	
	@Test
	public void sameDirectoryNameExceptionTest() throws IOException {
	    IVirtualDirectory dir = disk.createDirectory(disk.getRootDirectory(), "samename");
	    
	    boolean exceptionCatched = false;
	    try {
	        disk.createDirectory(disk.getRootDirectory(), "samename");
	    } catch (VirtualDiskException ex) {
	        exceptionCatched = true;
	    } finally {
	        dir.delete();
	        
	        assertTrue(exceptionCatched);
	    }
	}
}
