package ch.se.inf.ethz.jcd.batman.io.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Moves files without applying any transformations to the data.
 * 
 * @see HostBridge
 *
 */
public class DefaultMover implements DataMover {

    private static final int BUFFER_SIZE = 1024 * 1024; // 1 MiB

    /* (non-Javadoc)
     * @see ch.se.inf.ethz.jcd.batman.io.util.DataMover#importMove(java.io.InputStream, java.io.OutputStream)
     */
    @Override
    public void importMove(InputStream hostSource, OutputStream virtualTarget)
            throws IOException {
        move(hostSource, virtualTarget);
    }

    /* (non-Javadoc)
     * @see ch.se.inf.ethz.jcd.batman.io.util.DataMover#exportMove(java.io.InputStream, java.io.OutputStream)
     */
    @Override
    public void exportMove(InputStream virtualSource, OutputStream hostTarget)
            throws IOException {
        move(virtualSource, hostTarget);
    }

    private void move(InputStream reader, OutputStream writer)
            throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];

        int readAmount = 0;
        do {
            readAmount = reader.read(buffer);
            if (readAmount > 0) {
                writer.write(buffer, 0, readAmount);
            }
        } while (readAmount > 0);
    }
}
