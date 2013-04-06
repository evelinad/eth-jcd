package ch.se.inf.ethz.jcd.batman.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
     * @param absHostFile
     *            what to import
     * @param virtualFile
     *            where to import into
     * @throws IOException
     *             TODO
     */
    public static void importFile(File hostFile, VDiskFile virtualFile)
            throws IOException {
        File absHostFile = hostFile.getAbsoluteFile();
        
        if(!absHostFile.exists()) {
            throw new FileNotFoundException();
        }
        
        if (virtualFile.exists()) {
            if (absHostFile.isFile() && virtualFile.isDirectory()) {
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
            if (absHostFile.isFile()) {
                /*
                 * Imports a file on the host system into a not yet existing
                 * file
                 */
                importFileIntoFile(absHostFile, virtualFile);
            } else if (absHostFile.isDirectory()) {
                throw new UnsupportedOperationException(); // TODO
            } else {
                /*
                 * We only support directories and files for now.
                 */
                throw new UnsupportedOperationException();
            }
        }
    }

    /**
     * Exports the given VDiskFile (may be a file or directory) into the given
     * File on the host system.
     * 
     * @param virtualFile
     *            what to export
     * @param absHostFile
     *            where to export to
     * @throws IOException TODO
     */
    public static void exportFile(VDiskFile virtualFile, File hostFile) throws IOException {
        File absHostFile = hostFile.getAbsoluteFile();
        
        if(!virtualFile.exists()) {
            throw new FileNotFoundException();
        }
        
        if(absHostFile.exists()) {
            if(virtualFile.isFile() && absHostFile.isDirectory()) {
                /*
                 * export given virtual file into the given host directory
                 */
                throw new UnsupportedOperationException(); // TODO
            } else {
                /*
                 * all other cases are not supported as we would overwrite
                 * existing data
                 */
                throw new UnsupportedOperationException();
            }
        } else {
            if(virtualFile.isFile()) {
                /*
                 * export given virtual file into the host file system by
                 * creating the not yet existing host file
                 */
                if(!absHostFile.getParentFile().exists()) {
                    throw new FileNotFoundException(absHostFile.getParent());
                }
                
                exportFileIntoFile(virtualFile, absHostFile);
            } else if(virtualFile.isDirectory()) {
                /*
                 * export a virtual directory into a not yet existing directory
                 * on the host system
                 */
                if(!absHostFile.getParentFile().exists()) {
                    throw new FileNotFoundException(absHostFile.getParent());
                }
                
                throw new UnsupportedOperationException(); // TODO
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    private static void exportFileIntoFile(VDiskFile virtualFile, File hostFile) throws IOException {
        FileOutputStream writer = new FileOutputStream(hostFile);
        VDiskFileInputStream reader = new VDiskFileInputStream(virtualFile);
        
        moveData(reader, writer);
        
        reader.close();
        writer.close();
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

    private static void moveData(InputStream reader, OutputStream writer)
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
