package ch.se.inf.ethz.jcd.batman.io.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * {@link DataMover} implementation that compresses the imported/exported data
 * with GZIP.
 *
 */
public class GZIPMover implements DataMover {
    @Override
    public void importMove(InputStream hostSource, OutputStream virtualTarget)
            throws IOException {
        GZIPOutputStream compressedOut = new GZIPOutputStream(virtualTarget);
        
        DefaultMover.move(hostSource, compressedOut);
        
        compressedOut.finish();
        compressedOut.close();
        hostSource.close();
    }

    @Override
    public void exportMove(InputStream virtualSource, OutputStream hostTarget)
            throws IOException {
        GZIPInputStream compressedIn = new GZIPInputStream(virtualSource);
        
        DefaultMover.move(compressedIn, hostTarget);
        
        virtualSource.close();
        hostTarget.close();
    }

}
