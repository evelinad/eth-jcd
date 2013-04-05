package ch.se.inf.ethz.jcd.batman.vdisk;

import java.io.File;

import org.junit.After;
import org.junit.Before;

public class NewDiskPerTest {

	private static final String TEST_DISK_DIR = System
            .getProperty("java.io.tmpdir");

    protected File diskFile;
    protected IVirtualDisk disk;

    @Before
    public void setUp() throws Exception {
        diskFile = new File(TEST_DISK_DIR, "virtualDiskTest.vdisk");
        if (diskFile.exists()) {
            diskFile.delete();
        }

        disk = VirtualDisk.create(diskFile.getPath());
    }

    @After
    public void tearDown() throws Exception {
        disk.close();
        diskFile.delete();
    }
}
