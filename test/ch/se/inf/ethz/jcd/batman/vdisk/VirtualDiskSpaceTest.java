package ch.se.inf.ethz.jcd.batman.vdisk;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

public class VirtualDiskSpaceTest extends NewDiskPerTest {

    @Test
    public void spaceSizeTest() throws IOException {
		IVirtualDiskSpace diskSpace = VirtualDiskSpace.create(disk, 100);
		assertEquals(100, diskSpace.getSize());
	}
}
