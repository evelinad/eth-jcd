package ch.se.inf.ethz.jcd.batman.io;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualDisk;
import ch.se.inf.ethz.jcd.batman.vdisk.VirtualDisk;

public class VDiskFileTest {
    
	private IVirtualDisk disk;
    private File diskFile;

    @Before
    public void setUp() throws Exception {
        diskFile = new File("VDiskFileTest.vdisk");
        diskFile.delete();

        disk = VirtualDisk.create(diskFile.getAbsolutePath());
    }

    @After
    public void tearDown() throws Exception {
        disk.close();
        diskFile.delete();
    }

    @Test
    public void createDeleteTest() throws IOException {
        //Test create File
    	VDiskFile testFile = new VDiskFile("/test", disk);
    	VDiskFile testFileSameName = new VDiskFile("/test", disk);
    	assertFalse(testFile.exists());
    	assertTrue(testFile.createNewFile());
    	assertTrue(testFile.isFile());
    	assertFalse(testFile.isDirectory());
    	assertTrue(testFile.exists());
    	
    	//Test create File with same name
        assertFalse(testFileSameName.createNewFile());
        assertTrue(testFileSameName.exists());
    	
        assertTrue(testFile.delete());
        assertFalse(testFile.exists());
        
        
    }

    /*
    @Test(expected = FileNotFoundException.class)
    public void testImportNonExistingFile() throws IOException {
        File notExistingFile = File.createTempFile("HostBridgeTest",
                "testImportNonExistingFile");
        notExistingFile.delete();

        assertFalse(notExistingFile.exists());

        VDiskFile importTarget = new VDiskFile("/import", disk);

        HostBridge.importFile(notExistingFile, importTarget);
    }

    
    @Test(expected = FileAlreadyExistsException.class)
    public void testImportToExistingLocation() throws IOException {
    	
    }*/
}

