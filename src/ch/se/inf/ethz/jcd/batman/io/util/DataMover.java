package ch.se.inf.ethz.jcd.batman.io.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Interface for a DataMover as used by {@link HostBridge}.
 *
 */
public interface DataMover {

    public abstract void importMove(InputStream hostSource,
            OutputStream virtualTarget) throws IOException;

    public abstract void exportMove(InputStream virtualSource,
            OutputStream hostTarget) throws IOException;

}
