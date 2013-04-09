package ch.se.inf.ethz.jcd.batman.io.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Interface for a DataMover as used by {@link HostBridge}.
 * 
 * The main job of a DataMover is to move files from an InputStream over to an
 * OutputStream. After this is done, both streams has to be closed.
 * 
 */
public interface DataMover {

    /**
     * Indicates that an import is done.
     * 
     * After the data is moved, both streams must be closed by the DataMover
     * implementation.
     * 
     * @param hostSource
     *            the data source (is on the host system)
     * @param virtualTarget
     *            the data target (is on the virtual disk)
     * @throws IOException
     */
    void importMove(InputStream hostSource, OutputStream virtualTarget)
            throws IOException;

    /**
     * Indicates that an export is done.
     * 
     * After the data is moved, both streams must be closed by the DataMover
     * implementation.
     * 
     * @param virtualSource
     *            the data source (is on the virtual disk)
     * @param hostTarget
     *            the data target (is on the host system)
     * @throws IOException
     */
    void exportMove(InputStream virtualSource, OutputStream hostTarget)
            throws IOException;

}
