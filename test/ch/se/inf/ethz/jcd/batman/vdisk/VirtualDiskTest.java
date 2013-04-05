package ch.se.inf.ethz.jcd.batman.vdisk;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.se.inf.ethz.jcd.batman.vdisk.util.VirtualDiskUtil;

public class VirtualDiskTest {
    private static final String TEST_DISK_DIR = System
            .getProperty("java.io.tmpdir");

    private static final long TEST_DISK_SIZE = 1024 * 300; // 300 KByte

    private File diskFile;
    private IVirtualDisk disk;

    @Before
    public void setUp() throws Exception {
        diskFile = new File(TEST_DISK_DIR, "virtualDiskTest.vdisk");
        if (diskFile.exists()) {
            diskFile.delete();
        }

        disk = VirtualDisk.create(diskFile.getPath(), TEST_DISK_SIZE);
    }

    @After
    public void tearDown() throws Exception {
        disk.close();
        diskFile.delete();
    }

    /**
     * Creates a directory structure of the following form: <code>
     * / --> /A/ --> /A/Asub/ --> /A/Asub/AsubSub/
     *   \--> /B/ --> /B/BSub/
     * </code>
     * 
     * After that the disk is closed and loaded again, to check if the directory
     * structure is still correct.
     * 
     * @throws IOException
     */
    @Test
    public void directoryStructureCloseLoadTest() throws IOException {
        IVirtualDirectory dirA = disk.createDirectory(disk.getRootDirectory(),
                "A");
        IVirtualDirectory dirB = disk.createDirectory(disk.getRootDirectory(),
                "B");
        IVirtualDirectory dirASub = disk.createDirectory(dirA, "Asub");
        IVirtualDirectory dirBSub = disk.createDirectory(dirB, "Bsub");
        IVirtualDirectory dirASubSub = disk.createDirectory(dirASub, "AsubSub");

        disk.close();
        disk = null; // just to make sure

        disk = VirtualDisk.load(diskFile.getPath());
        assertNotNull(disk);

        IVirtualDiskEntry loadedDirB = VirtualDiskUtil.getDirectoryMember(
                disk.getRootDirectory(), dirB.getName());
        assertNotNull(loadedDirB);

        IVirtualDiskEntry loadedDirA = VirtualDiskUtil.getDirectoryMember(
                disk.getRootDirectory(), dirA.getName());
        assertNotNull(loadedDirA);

        IVirtualDiskEntry loadedDirASub = VirtualDiskUtil.getDirectoryMember(
                (IVirtualDirectory) loadedDirA, dirASub.getName());
        assertNotNull(loadedDirASub);

        assertNotNull(VirtualDiskUtil.getDirectoryMember(
                (IVirtualDirectory) loadedDirASub, dirASubSub.getName()));

        assertNotNull(VirtualDiskUtil.getDirectoryMember(
                (IVirtualDirectory) loadedDirB, dirBSub.getName()));
    }
}
