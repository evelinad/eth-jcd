package ch.se.inf.ethz.jcd.batman.io;

import static org.junit.Assert.*;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Date;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import ch.se.inf.ethz.jcd.batman.io.util.DefaultMover;
import ch.se.inf.ethz.jcd.batman.io.util.HostBridge;
import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualDisk;
import ch.se.inf.ethz.jcd.batman.vdisk.VirtualDisk;

public class BigImportExportTest {

    private IVirtualDisk disk;
    private File diskFile;

    @Before
    public void setUp() throws Exception {
        diskFile = new File("BigImportExportTest.vdisk");
        diskFile.delete();

        disk = VirtualDisk.create(diskFile.getAbsolutePath());
    }

    @After
    public void tearDown() throws Exception {
        disk.close();
        diskFile.delete();
    }

    @Test
    public void testReallyBigImportExport() throws IOException {
        // create the big file
        File bigHostFile = File.createTempFile("BigImportExportTest",
                "testReallyBigImportExport");
        System.out.println(bigHostFile.getAbsolutePath());
        
        OutputStream hostWriter = new BufferedOutputStream(
                new FileOutputStream(bigHostFile));

        long targetSize = 1024L * 1024L * 1024L * 15; // 15 GiBy
        byte[] contentPart = { 0x1, 0xA, 0xB, 0x2, 0xC, 0xE };

        long currentSize = 0;
        while (currentSize < targetSize) {
            hostWriter.write(contentPart);
            currentSize += contentPart.length;
        }

        hostWriter.close();
        System.out.println("big file written. starting import");

        // import the big file
        VDiskFile virtualTarget = new VDiskFile("/big", disk);

        long importStartTime = System.currentTimeMillis();
        HostBridge.importFile(bigHostFile, virtualTarget, new DefaultMover());
        long importStopTime = System.currentTimeMillis();

        long importTimeDelta = importStopTime - importStartTime;
        long importDeltaMin = importTimeDelta / (1000L * 60L);
        long importDeltaSec = (importTimeDelta % (1000L * 60L)) / 1000L;
        long importDeltaMs = importTimeDelta - (importDeltaSec * 1000L) - (importDeltaMin * 1000L * 60L);
        System.out.println(String.format("%d min, %d sec, %d ms",
                importDeltaMin, importDeltaSec, importDeltaMs));
        
        // check imported data
        System.out.println("checking imported file");
        
        InputStream virtualReader = new VDiskFileInputStream(virtualTarget);
        byte[] readBuffer = new byte[contentPart.length];
        
        // to check whole file takes to long. check start and end.
        virtualReader.read(readBuffer);
        assertArrayEquals(contentPart, readBuffer);
        
        virtualReader.skip(currentSize - 2 * contentPart.length);
        
        readBuffer = new byte[contentPart.length];
        virtualReader.read(readBuffer);
        assertArrayEquals(contentPart, readBuffer);
        
        virtualReader.close();

        // clean up
        bigHostFile.delete();
    }

}
