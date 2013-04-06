package ch.se.inf.ethz.jcd.batman.io;

import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualDisk;
import ch.se.inf.ethz.jcd.batman.vdisk.VirtualDisk;

public class HostBridgeTest {
    private static final String NORMAL_SIZE_FILE_CONTENT = "This is some test string used to test some stuff"
            + " while using HostBridge's import and export."
            + "It's part of a project written by B. Steger and G. Wegberg\n";

    private static File normalSizeFile;

    private IVirtualDisk disk;
    private File diskFile;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // initialize the simple file
        normalSizeFile = File.createTempFile("HostBridgeTest", "");
        PrintWriter normalWriter = new PrintWriter(normalSizeFile);
        normalWriter.write(NORMAL_SIZE_FILE_CONTENT);
        normalWriter.close();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        normalSizeFile.delete();
    }

    @Before
    public void setUp() throws Exception {
        diskFile = new File("HostBridgeTest.vdisk");
        if(diskFile.exists()) {
            diskFile.delete();
        }
        
        disk = VirtualDisk.create(diskFile.getAbsolutePath());
    }

    @After
    public void tearDown() throws Exception {
        disk.close();
        diskFile.delete();
    }

    @Test
    public void testImportFile() throws IOException {
        VDiskFile virtualFile = new VDiskFile("/test", disk);
        HostBridge.importFile(normalSizeFile, virtualFile);
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                new VDiskFileInputStream(virtualFile.getPath(), disk)));

        String readContent = reader.readLine();
        reader.close();

        assertEquals(NORMAL_SIZE_FILE_CONTENT, readContent);

    }

}
