package ch.se.inf.ethz.jcd.batman.io;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.FileAlreadyExistsException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import ch.se.inf.ethz.jcd.batman.io.util.DataMover;
import ch.se.inf.ethz.jcd.batman.io.util.DefaultMover;
import ch.se.inf.ethz.jcd.batman.io.util.EncryptedMover;
import ch.se.inf.ethz.jcd.batman.io.util.GZIPMover;
import ch.se.inf.ethz.jcd.batman.io.util.HostBridge;
import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualDisk;
import ch.se.inf.ethz.jcd.batman.vdisk.impl.VirtualDisk;

@RunWith(value = Parameterized.class)
public class HostBridgeTest {
    /**
     * Test content for the normal sized file. Must not have any newline
     * separators in it!
     */
    private static final String NORMAL_SIZE_FILE_CONTENT = "This is some test string used to test some stuff"
            + " while using HostBridge's import and export."
            + "It's part of a project written by B. Steger and G. Wegberg\n";

    private static File normalSizeFile;

    private final DataMover mover;
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

    @Parameters
    public static Collection<Object[]> getParameters()
            throws InvalidKeyException, InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchPaddingException {
        // prepare encrypt/decrypt stuff
        final byte[] iv = { 1, 2, 3, 4, 5, 6, 7, 8 };
        SecretKey key = KeyGenerator.getInstance("DES").generateKey();
        AlgorithmParameterSpec algoParamSpec = new IvParameterSpec(iv);

        Cipher encryptCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
        encryptCipher.init(Cipher.ENCRYPT_MODE, key, algoParamSpec);

        Cipher decryptCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
        decryptCipher.init(Cipher.DECRYPT_MODE, key, algoParamSpec);

        // create movers
        Object[][] params = new Object[][] { { new DefaultMover() },
                { new GZIPMover() },
                { new EncryptedMover(encryptCipher, decryptCipher) } };

        return Arrays.asList(params);
    }

    public HostBridgeTest(DataMover mover) {
        this.mover = mover;
    }

    @Before
    public void setUp() throws Exception {
        diskFile = new File("HostBridgeTest.vdisk");
        diskFile.delete();

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
        HostBridge.importFile(normalSizeFile, virtualFileImport, this.mover);

        ByteArrayOutputStream contentOut = new ByteArrayOutputStream();
        InputStream reader = new VDiskFileInputStream(virtualFileImport);

        this.mover.exportMove(reader, contentOut);

        contentOut.close();
        reader.close();

        assertEquals(NORMAL_SIZE_FILE_CONTENT, contentOut.toString());

        // close disk
        disk.close();
        disk = null;

        // reopen disk
        disk = VirtualDisk.load(diskFile.getAbsolutePath());

        // export to disk
        File exportTarget = File.createTempFile("HostBridgeTest", "");
        exportTarget.delete();

        VDiskFile virtualFileExport = new VDiskFile(virtualFilePath, disk);

        HostBridge.exportFile(virtualFileExport, exportTarget, this.mover);

        // read exported file
        BufferedReader exportedFileReader = new BufferedReader(new FileReader(
                exportTarget));

        String exportedFileContent = exportedFileReader.readLine() + "\n";
        exportedFileReader.close();

        assertEquals(NORMAL_SIZE_FILE_CONTENT, exportedFileContent);

        // clean up
        exportTarget.delete();

    }

    @Test
    public void testImportNormalSizeFileIntoDirectory() throws IOException {
        final String virtualDirPath = "/test";

        // import
        VDiskFile virtualDir = new VDiskFile(virtualDirPath, disk);
        virtualDir.mkdir();

        HostBridge.importFile(normalSizeFile, virtualDir, this.mover);

        VDiskFile virtualTargetFile = new VDiskFile(virtualDir,
                normalSizeFile.getName());
        assertTrue(virtualTargetFile.isFile());

        ByteArrayOutputStream contentOut = new ByteArrayOutputStream();
        InputStream reader = new VDiskFileInputStream(virtualTargetFile);

        this.mover.exportMove(reader, contentOut);

        contentOut.close();
        reader.close();

        assertEquals(NORMAL_SIZE_FILE_CONTENT, contentOut.toString());
    }

    @Test
    public void testImportExportDirectory() throws IOException {
        // create temporary host directory
        File hostDir = File.createTempFile("HostBridgeTest",
                "testImportExportDirectory");
        
        hostDir.delete();
        hostDir.mkdir();
        
        // create host directory structure
        File.createTempFile("someFileA", "root", hostDir);
        File.createTempFile("someFileB", "root", hostDir);
        
        File hostSubDir = File.createTempFile("sub", "", hostDir);
        hostSubDir.delete();
        hostSubDir.mkdir();
        
        File.createTempFile("someFileC", "sub", hostSubDir);
        
        // target virtual directory
        VDiskFile virtualDir = new VDiskFile("/test", disk);
        VDiskFile virtualSubDir = new VDiskFile(virtualDir, hostSubDir.getName());
        
        // import directory structure
        HostBridge.importFile(hostDir, virtualDir, this.mover);
        
        // check virtual structure
        List<String> hostDirChilds = Arrays.asList(hostDir.list());
        List<String> virtualDirChilds = Arrays.asList(virtualDir.list());
        
        assertEquals(hostDirChilds.size(), virtualDirChilds.size());
        assertTrue(hostDirChilds.containsAll(virtualDirChilds));
        
        List<String> hostSubDirChilds = Arrays.asList(hostSubDir.list());
        List<String> virtualSubDirChilds = Arrays.asList(virtualSubDir.list());
        
        assertEquals(hostSubDirChilds.size(), virtualSubDirChilds.size());
        assertTrue(hostSubDirChilds.containsAll(virtualSubDirChilds));
        
        // export directory structure
        File exportHostDir = File.createTempFile("exportHostDir", "");
        exportHostDir.delete();
        
        File exportHostSubDir = new File(exportHostDir, virtualSubDir.getName());
        
        HostBridge.exportFile(virtualDir, exportHostDir, this.mover);
        
        // check exported host dir structure
        List<String> exportedHostDirChilds = Arrays.asList(exportHostDir.list());
        assertEquals(virtualDirChilds.size(), exportedHostDirChilds.size());
        assertTrue(virtualDirChilds.containsAll(exportedHostDirChilds));
        
        List<String> exportedHostSubDirChilds = Arrays.asList(exportHostSubDir.list());
        assertEquals(virtualSubDirChilds.size(), exportedHostSubDirChilds.size());
        assertTrue(virtualSubDirChilds.containsAll(exportedHostSubDirChilds));
        
        // clean up
        exportHostDir.delete();
        hostDir.delete();
    }

    @Test(expected = FileNotFoundException.class)
    public void testImportNonExistingFile() throws IOException {
        File notExistingFile = File.createTempFile("HostBridgeTest",
                "testImportNonExistingFile");
        notExistingFile.delete();

        assertFalse(notExistingFile.exists());

        VDiskFile importTarget = new VDiskFile("/import", disk);

        HostBridge.importFile(notExistingFile, importTarget, this.mover);
    }

    @Test(expected = FileAlreadyExistsException.class)
    public void testImportToExistingLocation() throws IOException {
        VDiskFile existingFile = new VDiskFile("/import", disk);
        assertFalse(existingFile.exists());

        assertTrue(existingFile.createNewFile());

        HostBridge.importFile(normalSizeFile, existingFile, this.mover);
    }

    @Test(expected = FileNotFoundException.class)
    public void testImportToLocationWithoutParent() throws IOException {
        VDiskFile noParentFile = new VDiskFile("/dir/file", disk);

        assertFalse(noParentFile.exists());
        assertFalse(noParentFile.getParentFile().exists());

        HostBridge.importFile(normalSizeFile, noParentFile, this.mover);
    }

    @Test(expected = FileNotFoundException.class)
    public void testExportNonExistingFile() throws IOException {
        VDiskFile notExistingFile = new VDiskFile("/doNotExist", disk);
        assertFalse(notExistingFile.exists());

        File exportTarget = File.createTempFile("HostBridgeTest",
                "testExportNonExistingFile");
        exportTarget.delete();
        assertFalse(exportTarget.exists());

        HostBridge.exportFile(notExistingFile, exportTarget, this.mover);
    }

    @Test(expected = FileAlreadyExistsException.class)
    public void testExportToExistingFile() throws IOException {
        VDiskFile existingFile = new VDiskFile("/file", disk);
        existingFile.createNewFile();

        File exportTarget = File.createTempFile("HostBridgeTest",
                "testExportToExistingFile");
        exportTarget.deleteOnExit();

        HostBridge.exportFile(existingFile, exportTarget, this.mover);
    }

}
