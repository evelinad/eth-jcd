package ch.se.inf.ethz.jcd.batman.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileAlreadyExistsException;

/**
 * Provides static methods that can be used to create connections between the
 * hosts files and the virtual disk.
 * 
 */
public class HostBridge {
    private static final int BUFFER_SIZE = 1024 * 1024; // 1 MiB

    /**
     * Imports the given host File (may be a file or directory) into the given
     * VDiskFile.
     * 
     * The parent of virtualFile has to exists otherwise an
     * {@link UnsupportedOperationException} is thrown.
     * 
     * @param hostFile
     *            what to import
     * @param virtualFile
     *            where to import into
     * @throws IOException
     *             TODO
     */
    public static void importFile(File hostFile, VDiskFile virtualFile)
            throws IOException {
        if (virtualFile.exists()) {
            if (hostFile.isFile() && virtualFile.isDirectory()) {
                /*
                 * We have to move the host file into an existing virtual
                 * directory
                 */
                throw new UnsupportedOperationException(); // TODO
            } else {
                /*
                 * Possible cases: - hostFile is a directory and the virtualFile
                 * already exists - hostFile is a File and the virtualFile is a
                 * file but already exists
                 * 
                 * In both cases we can not import the host file as it would
                 * overwrite the existing file/directory
                 */
                throw new FileAlreadyExistsException(virtualFile.getPath());
            }
        } else {
            if (hostFile.isFile()) {
                /*
                 * Imports a file on the host system into a not yet existing
                 * file
                 */
                importFileIntoFile(hostFile, virtualFile);
            } else if (hostFile.isDirectory()) {
                throw new UnsupportedOperationException(); // TODO
            } else {
                /*
                 * We only support directories and files for now.
                 */
                throw new UnsupportedOperationException();
            }
        }
    }

    private static void importFileIntoFile(File hostFile, VDiskFile virtualFile)
            throws IOException {
        assert hostFile.isFile();
        assert !virtualFile.exists();

        if (!virtualFile.getParentFile().exists()) {
            throw new UnsupportedOperationException("Parent does not exists");
        }

        virtualFile.createNewFile(hostFile.length());

        FileInputStream reader = new FileInputStream(hostFile);
        VDiskFileOutputStream writer = new VDiskFileOutputStream(virtualFile,
                false);

        moveData(reader, writer);
        
        reader.close();
        writer.close();
    }

    private static void moveData(InputStream reader, OutputStream writer) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        
        int readAmount = 0;
        do {
            readAmount = reader.read(buffer);
            if (readAmount > 0) {
            	writer.write(buffer, 0, readAmount);
            }
        } while(readAmount > 0);
    }
}
