package ch.se.inf.ethz.jcd.batman.io;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualDisk;
import ch.se.inf.ethz.jcd.batman.vdisk.VirtualDisk;

public class VDiskFileInputOutputStreamsTest {
    private File diskFile;
    private IVirtualDisk disk;
    private VDiskFile virtualFile;
    private InputStream reader;
    private OutputStream writer;

    @Before
    public void setUp() throws Exception {
        // create disk
        diskFile = new File("VDiskFileInputStreamTest.vdisk");
        diskFile.delete();

        disk = VirtualDisk.create(diskFile.getAbsolutePath());

        // create streams
        virtualFile = new VDiskFile("/test", disk);
        virtualFile.createNewFile();

        reader = new VDiskFileInputStream(virtualFile);
        writer = new VDiskFileOutputStream(virtualFile, false);
    }

    @After
    public void tearDown() throws Exception {
        reader.close();
        writer.close();

        disk.close();
        diskFile.delete();
    }

    @Test
    public void testWriteReadByteArray() throws IOException {
        byte[] toWrite = { 0x00, 0x01, 0xA, 0xA, 0xF };
        writer.write(toWrite);

        byte[] readValues = new byte[toWrite.length];
        reader.read(readValues);

        assertArrayEquals(toWrite, readValues);
    }

    @Test
    public void testWriteReadSingleByte() throws IOException {
        byte value = 0xC;

        writer.write(value);
        assertEquals(value, reader.read());
    }

    @Test
    public void testWriteReadByteArrayWithOffset() throws IOException {
        // the first and last three bytes should not be changed after reading
        int doNotChange = 3;
        byte[] expectedValue = { 0x0, 0xF, 0x0, 0xA, 0xB, 0xC, 0xD, 0xE, 0x0,
                0xF, 0x0 };

        byte[] readBuffer = Arrays.copyOf(expectedValue, expectedValue.length);
        Arrays.fill(readBuffer, doNotChange + 1, readBuffer.length
                - doNotChange, (byte) 0x1);

        // write
        writer.write(expectedValue, doNotChange, expectedValue.length - 2
                * doNotChange);

        // read
        reader.read(readBuffer, doNotChange, readBuffer.length - 2
                * doNotChange);

        // check
        assertArrayEquals(expectedValue, readBuffer);
    }

    @Test
    public void testInputStreamSkeep() throws IOException {
        // prepare test data
        int firstRead = 4;
        int skip = 5;
        int secondRead = 4;

        byte[] toWrite = { 0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8, 0x9, 0xA,
                0xB, 0xC, 0xD, 0xE };

        byte[] firstExpected = { 0x1, 0x2, 0x3, 0x4 };
        byte[] secondExpected = { 0xA, 0xB, 0xC, 0xD };

        // write
        writer.write(toWrite);

        // read & check
        byte[] firstReadValue = new byte[firstRead];
        assertEquals(firstRead, reader.read(firstReadValue));
        assertArrayEquals(firstExpected, firstReadValue);

        reader.skip(skip);

        byte[] secondReadValue = new byte[secondRead];
        assertEquals(secondRead, reader.read(secondReadValue));
        assertArrayEquals(secondExpected, secondReadValue);
    }

    @Test
    public void testInputStreamSkeepOverFileSize() throws IOException {
        long fileSize = virtualFile.getTotalSpace();

        /*
         * as we skip way more than the amount of data inside the file, the
         * returned value must be lower than fileSize.
         */
        assertTrue(fileSize > reader.skip(fileSize));

        // we just need a buffer bigger than 1
        byte[] readBuffer = { 0x0, 0x0, 0x0 };
        byte[] expected = Arrays.copyOf(readBuffer, readBuffer.length);

        // we should not be able to read anything as we skiped to the end.
        assertEquals(0, reader.read(readBuffer));
        assertArrayEquals(expected, readBuffer);
    }
    
    @Test
    public void testAppendOutput() throws IOException {
        // test data
        byte[] initialData = {0xA, 0xC, 0xA};
        byte[] appendData = {0xF, 0xD, 0x1, 0x5};
        
        // write initial data
        writer.write(initialData);
        
        // read initial data
        byte[] readInitialData = new byte[initialData.length];
        reader.read(readInitialData);
        assertArrayEquals(initialData, readInitialData);
        
        // append new data
        writer.close();
        writer = new VDiskFileOutputStream(virtualFile, true);
        writer.write(appendData);
        
        // read appended data
        byte[] readAppendData = new byte[appendData.length];
        reader.read(readAppendData);
        assertArrayEquals(appendData, readAppendData);
    }
    
    @SuppressWarnings("resource")
    @Test(expected = FileNotFoundException.class)
    public void testOutputFileNotExist() throws IOException {
        VDiskFile nonExistingFile = new VDiskFile("/nonexisting", disk);
        
        new VDiskFileOutputStream(nonExistingFile, false);
    }
    
    @SuppressWarnings("resource")
    @Test(expected = FileNotFoundException.class)
    public void testInputFileNotExist() throws IOException {
        VDiskFile nonExistingFile = new VDiskFile("/nonexisting", disk);
        
        new VDiskFileInputStream(nonExistingFile);
    }
}
