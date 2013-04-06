package ch.se.inf.ethz.jcd.batman.io;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.FileAlreadyExistsException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualDisk;
import ch.se.inf.ethz.jcd.batman.vdisk.VirtualDisk;

public class HostBridgeTest {
    /**
     * Test content for the normal sized file. Must not have any newline
     * separators in it!
     */
    private static final String NORMAL_SIZE_FILE_CONTENT = "This is some test string used to test some stuff"
            + " while using HostBridge's import and export."
            + "It's part of a project written by B. Steger and G. Wegberg";

    private static File normalSizeFile;

    private IVirtualDisk disk;
    private File diskFile;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // initialize the simple file
        normalSizeFile = File.createTempFile("HostBridgeTest", "");
        PrintWriter normalWriter = new PrintWriter(normalSizeFile);
        normalWriter.write(NORMAL_SIZE_FILE_CONTENT + System.lineSeparator());
        normalWriter.close();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        normalSizeFile.delete();
    }

    @Before
    public void setUp() throws Exception {
        diskFile = new File("HostBridgeTest.vdisk");
        if (diskFile.exists()) {
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
    public void testImportExportNormalSizeFile() throws IOException {
        final String virtualFilePath = "/test";

        // import normalSizeFile
        VDiskFile virtualFileImport = new VDiskFile(virtualFilePath, disk);
        HostBridge.importFile(normalSizeFile, virtualFileImport);

        BufferedReader reader = new BufferedReader(new InputStreamReader(
                new VDiskFileInputStream(virtualFileImport)));

        String readContent = reader.readLine();
        reader.close();

        assertEquals(NORMAL_SIZE_FILE_CONTENT, readContent);

        // close disk
        disk.close();
        disk = null;

        // reopen disk
        disk = VirtualDisk.load(diskFile.getAbsolutePath());

        // export to disk
        File exportTarget = File.createTempFile("HostBridgeTest", "");
        exportTarget.delete();

        VDiskFile virtualFileExport = new VDiskFile(virtualFilePath, disk);

        HostBridge.exportFile(virtualFileExport, exportTarget);

        // read exported file
        BufferedReader exportedFileReader = new BufferedReader(new FileReader(
                exportTarget));

        String exportedFileContent = exportedFileReader.readLine();
        exportedFileReader.close();

        assertEquals(NORMAL_SIZE_FILE_CONTENT, exportedFileContent);

        // clean up
        exportTarget.delete();

    }

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
        VDiskFile existingFile = new VDiskFile("/import", disk);
        assertFalse(existingFile.exists());

        assertTrue(existingFile.createNewFile());

        HostBridge.importFile(normalSizeFile, existingFile);
    }

    @Test(expected = FileNotFoundException.class)
    public void testImportToLocationWithoutParent() throws IOException {
        VDiskFile noParentFile = new VDiskFile("/dir/file", disk);

        assertFalse(noParentFile.exists());
        assertFalse(noParentFile.getParentFile().exists());

        HostBridge.importFile(normalSizeFile, noParentFile);
    }

    @Test(expected = FileNotFoundException.class)
    public void testExportNonExistingFile() throws IOException {
        VDiskFile notExistingFile = new VDiskFile("/doNotExist", disk);
        assertFalse(notExistingFile.exists());

        File exportTarget = File.createTempFile("HostBridgeTest",
                "testExportNonExistingFile");
        exportTarget.delete();
        assertFalse(exportTarget.exists());
        
        HostBridge.exportFile(notExistingFile, exportTarget);
    }
    
    @Test(expected = FileAlreadyExistsException.class)
    public void testExportToExistingFile() throws IOException {
        VDiskFile existingFile = new VDiskFile("/file", disk);
        existingFile.createNewFile();
        
        File exportTarget = File.createTempFile("HostBridgeTest", "testExportToExistingFile");
        exportTarget.deleteOnExit();
        
        HostBridge.exportFile(existingFile, exportTarget);
    }

}
