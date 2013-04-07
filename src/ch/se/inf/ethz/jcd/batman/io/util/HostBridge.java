package ch.se.inf.ethz.jcd.batman.io.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;

import ch.se.inf.ethz.jcd.batman.io.VDiskFile;
import ch.se.inf.ethz.jcd.batman.io.VDiskFileInputStream;
import ch.se.inf.ethz.jcd.batman.io.VDiskFileOutputStream;

/**
 * Provides static methods that can be used to create connections between the
 * hosts files and the virtual disk.
 * 
 * To move the data a {@link DefaultMover} is used. This can be used to e.g.
 * compress the data.
 * 
 */
public class HostBridge {
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
     * @param mover
     *            a moving strategy
     * @throws IOException
     *             TODO
     */
    public static void importFile(File hostFile, VDiskFile virtualFile,
            DataMover mover) throws IOException {
        File absHostFile = hostFile.getAbsoluteFile();

        if (!absHostFile.exists()) {
            throw new FileNotFoundException();
        }

        if (virtualFile.exists()) {
            if (absHostFile.isFile() && virtualFile.isDirectory()) {
                /*
                 * We have to move the host file into an existing virtual
                 * directory
                 */
                importFileIntoDirectory(absHostFile, virtualFile, mover);
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
                importFileIntoFile(absHostFile, virtualFile, mover);
            } else if (absHostFile.isDirectory()) {
                /*
                 * Host file is a directory and our target does not exist yet.
                 */
                importDirectoryIntoDirectory(absHostFile, virtualFile, mover);
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
     * @param hostFile
     *            where to export to
     * @param mover
     *            a moving strategy
     * @throws IOException
     *             TODO
     */
    public static void exportFile(VDiskFile virtualFile, File hostFile,
            DataMover mover) throws IOException {
        File absHostFile = hostFile.getAbsoluteFile();

        if (!virtualFile.exists()) {
            throw new FileNotFoundException();
        }

        if (absHostFile.exists()) {
            if (virtualFile.isFile() && absHostFile.isDirectory()) {
                /*
                 * export given virtual file into the given host directory
                 */
                exportFileIntoDirectory(virtualFile, absHostFile, mover);
            } else {
                /*
                 * all other cases are not supported as we would overwrite
                 * existing data
                 */
                throw new FileAlreadyExistsException(absHostFile.getPath());
            }
        } else {
            if (virtualFile.isFile()) {
                /*
                 * export given virtual file into the host file system by
                 * creating the not yet existing host file
                 */
                if (!absHostFile.getParentFile().exists()) {
                    throw new FileNotFoundException(absHostFile.getParent());
                }

                exportFileIntoFile(virtualFile, absHostFile, mover);
            } else if (virtualFile.isDirectory()) {
                /*
                 * export a virtual directory into a not yet existing directory
                 * on the host system
                 */
                if (!absHostFile.getParentFile().exists()) {
                    throw new FileNotFoundException(absHostFile.getParent());
                }

                exportDirectoryIntoDirectory(virtualFile, absHostFile, mover);
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    private static void exportFileIntoFile(VDiskFile virtualFile,
            File hostFile, DataMover mover) throws IOException {
        FileOutputStream writer = new FileOutputStream(hostFile);
        VDiskFileInputStream reader = new VDiskFileInputStream(virtualFile);

        mover.exportMove(reader, writer);
    }

    private static void exportFileIntoDirectory(VDiskFile virtualFile,
            File hostDir, DataMover mover) throws IOException {
        assert virtualFile.exists();
        assert hostDir.isDirectory();

        File hostFile = new File(hostDir, virtualFile.getName());
        hostFile.createNewFile();

        FileOutputStream writer = new FileOutputStream(hostFile);
        VDiskFileInputStream reader = new VDiskFileInputStream(virtualFile);

        mover.exportMove(reader, writer);
    }

    private static void exportDirectoryIntoDirectory(VDiskFile virtualDir,
            File hostDir, DataMover mover) throws IOException {
        assert !hostDir.exists();
        assert virtualDir.isDirectory();

        hostDir.mkdir();

        for (VDiskFile child : virtualDir.listFiles()) {
            if (child.isFile()) {
                exportFileIntoDirectory(child, hostDir, mover);
            } else if (child.isDirectory()) {
                exportDirectoryIntoDirectory(child,
                        new File(hostDir, child.getName()), mover);
            }
        }
    }

    private static void importFileIntoFile(File hostFile,
            VDiskFile virtualFile, DataMover mover) throws IOException {
        assert hostFile.isFile();
        assert !virtualFile.exists();

        if (!virtualFile.getParentFile().exists()) {
            throw new FileNotFoundException("Parent does not exists");
        }

        virtualFile.createNewFile(hostFile.length());

        FileInputStream reader = new FileInputStream(hostFile);
        VDiskFileOutputStream writer = new VDiskFileOutputStream(virtualFile,
                false);

        mover.importMove(reader, writer);
    }

    private static void importFileIntoDirectory(File hostFile,
            VDiskFile virtualDir, DataMover mover) throws IOException {
        assert hostFile.isFile();
        assert virtualDir.isDirectory();

        VDiskFile targetFile = new VDiskFile(virtualDir, hostFile.getName());
        long hostFileSize = hostFile.length();
        targetFile.createNewFile(hostFileSize < 1L ? 1L : hostFileSize);

        FileInputStream reader = new FileInputStream(hostFile);
        VDiskFileOutputStream writer = new VDiskFileOutputStream(targetFile,
                false);

        mover.importMove(reader, writer);
    }

    private static void importDirectoryIntoDirectory(File hostDir,
            VDiskFile virtualDir, DataMover mover) throws IOException {
        assert hostDir.isDirectory();
        assert !virtualDir.exists();

        virtualDir.mkdir();

        for (File hostChild : hostDir.listFiles()) {
            if (hostChild.isFile()) {
                importFileIntoDirectory(hostChild, virtualDir, mover);
            } else if (hostChild.isDirectory()) {
                importDirectoryIntoDirectory(hostChild, new VDiskFile(
                        virtualDir, hostChild.getName()), mover);
            }
        }
    }
}
