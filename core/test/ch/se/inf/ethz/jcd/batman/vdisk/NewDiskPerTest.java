package ch.se.inf.ethz.jcd.batman.vdisk;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;

import ch.se.inf.ethz.jcd.batman.vdisk.impl.VirtualDisk;

public class NewDiskPerTest {

	private static final String TEST_DISK_DIR = System
            .getProperty("java.io.tmpdir");

    protected File diskFile;
    protected IVirtualDisk disk;

    protected IVirtualDisk createNewDisk (String path) throws IOException {
    	return VirtualDisk.create(path);
    }
    
    protected IVirtualDisk loadDisk () throws IOException {
    	return loadDisk(diskFile.getPath());
    } 
    
    protected IVirtualDisk loadDisk (String path) throws IOException {
    	return VirtualDisk.load(path);
    } 
    
    @Before
    public void setUp() throws Exception {
        diskFile = new File(TEST_DISK_DIR, "virtualDiskTest.vdisk");
        if (diskFile.exists()) {
            diskFile.delete();
        }

        disk = createNewDisk(diskFile.getPath());
    }

    @After
    public void tearDown() throws Exception {
        disk.close();
        diskFile.delete();
    }
}
