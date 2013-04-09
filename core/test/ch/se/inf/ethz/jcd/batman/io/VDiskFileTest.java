package ch.se.inf.ethz.jcd.batman.io;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Scanner;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualDisk;
import ch.se.inf.ethz.jcd.batman.vdisk.VirtualDiskException;
import ch.se.inf.ethz.jcd.batman.vdisk.impl.VirtualDisk;

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
    public void diskTest() throws IOException {
        VDiskFile testFile = new VDiskFile("/test", disk);
        assertEquals(disk, testFile.getDisk());
    }

    @Test
    public void nameTest() throws IOException {
        String fileName = "/test";
        VDiskFile testFile = new VDiskFile(fileName, disk);
        assertEquals(fileName, testFile.getPath());
        assertEquals("test", testFile.getName());
        testFile.createNewFile();
        assertEquals("test", testFile.getDiskEntry().getName());
    }

    @Test(expected = VirtualDiskException.class)
    public void invalidNameTest() throws IOException {
        new VDiskFile("test", disk);
    }

    @Test(expected = VirtualDiskException.class)
    public void invalidName2Test() throws IOException {
        new VDiskFile("a", disk);
    }

    @Test(expected = VirtualDiskException.class)
    public void invalidCreateTest() throws IOException {
    	new VDiskFile("bar", "foo", disk);
    }
    
    @Test
    public void createDeleteTest() throws IOException {
        // Test create File
    	VDiskFile testFile = new VDiskFile("/test", disk);
        assertFalse(testFile.isDirectory());
        assertFalse(testFile.isFile());
        assertFalse(testFile.delete());
        VDiskFile testFileSameName = new VDiskFile("/test", disk);
        assertFalse(testFile.exists());
        assertFalse(testFile.createNewFile(0));
        assertTrue(testFile.createNewFile());
        assertTrue(testFile.isFile());
        assertFalse(testFile.isDirectory());
        assertTrue(testFile.exists());
        assertEquals(disk.getRootDirectory(), testFile.getDiskEntry()
                .getParent());
        assertEquals(disk.getRootDirectory(), testFile.getParentFile()
                .getDiskEntry());

        //Create with parent
        VDiskFile parent = new VDiskFile("/", disk);
        VDiskFile withParent = new VDiskFile(parent, "foo");
        assertEquals("/foo", withParent.getPath());
        
        // Test create File under /test/foo
        VDiskFile testFooFile = new VDiskFile("/test/foo", disk);
        assertFalse(testFooFile.createNewFile());
        assertFalse(testFile.mkdir());
        assertFalse(testFooFile.mkdirs());

        // Test rootDirectory
        VDiskFile rootDirectory = new VDiskFile("/", disk);
        assertTrue(rootDirectory.exists());
        assertFalse(rootDirectory.isFile());
        assertTrue(rootDirectory.isDirectory());

        // Test create File with same name
        assertFalse(testFileSameName.createNewFile());
        assertTrue(testFileSameName.exists());
        assertTrue(testFile.delete());
        assertFalse(testFile.exists());
        assertFalse(testFileSameName.exists());
        assertTrue(testFileSameName.createNewFile(10));
        assertTrue(testFileSameName.exists());

        // Test create Directory with same name
        VDiskFile testDirectory = new VDiskFile("/test", disk);
        assertFalse(testDirectory.mkdir());
        testFileSameName.delete();
        assertTrue(testDirectory.mkdir());

        // Test sub directory from test
        VDiskFile subTestDirectory = new VDiskFile("/test/foo", disk);
        assertFalse(subTestDirectory.exists());
        assertTrue(subTestDirectory.mkdir());
        assertTrue(subTestDirectory.isDirectory());
        assertFalse(subTestDirectory.isFile());
        assertTrue(subTestDirectory.exists());

        // Test mkdirs
        VDiskFile mkdirsTestDirectory = new VDiskFile("/foo/bar", disk);
        VDiskFile fooDirectory = new VDiskFile("/foo", disk);
        assertFalse(mkdirsTestDirectory.exists());
        assertFalse(fooDirectory.exists());
        assertFalse(mkdirsTestDirectory.mkdir());
        assertTrue(mkdirsTestDirectory.mkdirs());
        assertTrue(mkdirsTestDirectory.exists());
        assertTrue(mkdirsTestDirectory.isDirectory());
        assertFalse(mkdirsTestDirectory.isFile());
        assertTrue(fooDirectory.exists());
        assertTrue(fooDirectory.isDirectory());
        assertFalse(fooDirectory.isFile());
        assertFalse(fooDirectory.delete());
        assertTrue(mkdirsTestDirectory.delete());
        assertTrue(fooDirectory.delete());
        VDiskFile failMkdir = new VDiskFile("/", disk);
        assertFalse(failMkdir.mkdir());
    }

    @Test
    public void listTest() throws IOException {
        VDiskFile testFile = new VDiskFile("/test", disk);
        assertEquals(0, testFile.list().length);
        assertEquals(0, testFile.listFiles().length);
        testFile.createNewFile();
        assertEquals(0,testFile.list().length);

        VDiskFile testDirectory = new VDiskFile("/foo", disk);
        testDirectory.mkdir();
        assertTrue(testDirectory.list().length == 0);
        assertTrue(testDirectory.listFiles().length == 0);

        VDiskFile directoryMemberFile = new VDiskFile("/foo/bar", disk);
        directoryMemberFile.createNewFile();
        assertTrue(testDirectory.list().length == 1);
        assertTrue(testDirectory.listFiles().length == 1);

        VDiskFile subDirectory = new VDiskFile("/foo/foo", disk);
        subDirectory.mkdir();
        assertTrue(testDirectory.list().length == 2);
        assertTrue(testDirectory.listFiles().length == 2);
        assertTrue(Arrays.asList(testDirectory.list()).contains("bar"));
        assertTrue(Arrays.asList(testDirectory.list()).contains("foo"));
        assertTrue(Arrays.asList(testDirectory.listFiles()).contains(
                new VDiskFile("/foo/bar", disk)));
        assertTrue(Arrays.asList(testDirectory.listFiles()).contains(
                new VDiskFile("/foo/foo", disk)));
    }

    @Test
    public void totalSpaceTest() throws IOException {
        VDiskFile testFile = new VDiskFile("/test", disk);
        assertEquals(0, testFile.getTotalSpace());
        testFile.createNewFile(100);
        assertTrue(testFile.getTotalSpace() >= 100);
    }

    @Test
    public void lastModifiedTest() throws IOException {
        VDiskFile testFile = new VDiskFile("/test", disk);
        testFile.setLastModified(10);
        assertEquals(0, testFile.lastModified());

        testFile.createNewFile();
        testFile.setLastModified(10);
        assertEquals(10, testFile.lastModified());

    }

    @Test
    public void renameTest() throws IOException {
        VDiskFile testFile = new VDiskFile("/test", disk);
        VDiskFile renameFile = new VDiskFile("/bla", disk);
        assertFalse(testFile.renameTo(renameFile));
        testFile.createNewFile();
        assertFalse(renameFile.exists());
        assertTrue(testFile.exists());
        assertTrue(testFile.renameTo(renameFile));
        assertTrue(renameFile.exists());
        assertTrue(testFile.exists());
        assertEquals(testFile.getPath(), "/bla");
    }

    @Test
    public void equalsHashTest() throws IOException {
        VDiskFile testFile = new VDiskFile("/test", disk);
        VDiskFile test2File = new VDiskFile("/bla", disk);
        VDiskFile test3File = new VDiskFile("/test", disk);
        assertFalse(testFile.equals(test2File));
        assertTrue(testFile.equals(test3File));
        assertEquals(testFile.hashCode(), test3File.hashCode());
    }

    @Test
    public void copyToTest() throws IOException {
        final String testContent = "Hello World";

        VDiskFile source = new VDiskFile("/source", disk);
        VDiskFile target = new VDiskFile("/target", disk);

        source.createNewFile();
        OutputStreamWriter sourceOut = new OutputStreamWriter(
                new VDiskFileOutputStream(source, false));

        sourceOut.write(testContent + System.lineSeparator());
        sourceOut.close();

        assertFalse(target.exists());
        source.copyTo(target);
        assertTrue(target.isFile());

        Scanner targetIn = new Scanner(new VDiskFileInputStream(target));
        final String copyContent = targetIn.nextLine();
        targetIn.close();
        
        assertEquals(testContent, copyContent);
    }
}
