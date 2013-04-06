package ch.se.inf.ethz.jcd.batman.io;

import static org.junit.Assert.*;

import java.io.File;
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
    private InputStream reader;
    private OutputStream writer;

    @Before
    public void setUp() throws Exception {
        // create disk
        diskFile = new File("VDiskFileInputStreamTest.vdisk");
        diskFile.delete();

        disk = VirtualDisk.create(diskFile.getAbsolutePath());

        // create streams
        VDiskFile file = new VDiskFile("/test", disk);
        file.createNewFile();

        reader = new VDiskFileInputStream(file);
        writer = new VDiskFileOutputStream(file, false);
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
}
