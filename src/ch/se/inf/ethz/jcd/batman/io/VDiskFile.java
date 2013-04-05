/**
 * 
 */
package ch.se.inf.ethz.jcd.batman.io;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualDirectory;
import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualDisk;
import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualDiskEntry;
import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualFile;
import ch.se.inf.ethz.jcd.batman.vdisk.VirtualDiskException;
import ch.se.inf.ethz.jcd.batman.vdisk.util.VirtualDiskUtil;

/**
 * 
 *
 */
public class VDiskFile {

    private static final String PATH_SEPARATOR = String
            .valueOf(IVirtualDisk.PATH_SEPARATOR);

    // fields
    private final String pathname;
    private final IVirtualDisk disk;
    private IVirtualDiskEntry pathDiskEntry;

    // constructors
    /**
     * @param pathname
     * @throws IOException
     */
    public VDiskFile(String pathname, IVirtualDisk disk) throws IOException {
        this.pathname = pathname;
        this.disk = disk;
        this.pathDiskEntry = getDiskEntry(pathname);
    }

    /**
     * @param parent
     * @param child
     * @throws IOException
     */
    public VDiskFile(String parent, String child, IVirtualDisk disk)
            throws IOException {
        this(String
                .format("%s%s%s", parent, IVirtualDisk.PATH_SEPARATOR, child),
                disk);
    }

    /**
     * @param parent
     * @param child
     * @throws IOException
     */
    public VDiskFile(VDiskFile parent, String child)
            throws IOException {
        this(parent.getPath(), child, parent.getDisk());
    }

    // private methods

    private IVirtualDiskEntry getDiskEntry(String path) throws IOException {
        // split path into entry names
        String[] pathParts = pathname.split(PATH_SEPARATOR);
        if (pathParts.length == 0) {
            if (path.equals(PATH_SEPARATOR)) {
                return disk.getRootDirectory();
            } else {
                throw new VirtualDiskException(String.format(
                        "'%s' not a valid path", path));
            }
        }

        // check that the path starts with the root
        if (!pathParts[0].isEmpty()) {
            throw new VirtualDiskException("Path does not start at root");
        }

        /*
         * go down to the second to last entry name (the should all be
         * directories)
         */
        IVirtualDiskEntry currentEntry = disk.getRootDirectory();
        for (int curPartIndex = 1; curPartIndex < (pathParts.length - 1); curPartIndex++) {
            String pathPartName = pathParts[curPartIndex];

            // go one step deeper
            currentEntry = VirtualDiskUtil.getDirectoryMember(
                    (IVirtualDirectory) currentEntry, pathPartName);

            // check if we got an entry
            if (currentEntry == null) {
                return null;
            }

            // check that it still is a directory
            if (!(currentEntry instanceof IVirtualDirectory)) {
                return null;
            }
        }

        /*
         * we are now at the second to last name in the given file path.
         * Therefore currentEntry is still a directory (we checked above) but
         * it's child, that we search, does not have to be
         */
        String lastPart = pathParts[pathParts.length - 1];
        currentEntry = VirtualDiskUtil.getDirectoryMember(
                (IVirtualDirectory) currentEntry, lastPart);

        return currentEntry;

    }

    private Collection<IVirtualDiskEntry> getChilds() throws IOException {
        if (pathDiskEntry instanceof IVirtualDirectory) {
            return VirtualDiskUtil
                    .getDirectoryMembers((IVirtualDirectory) pathDiskEntry);
        } else {
            return Arrays.asList(new IVirtualDiskEntry[0]);
        }
    }

    // public methods
    
    /**
     * Returns the disk associated with the VDiskFile.
     * 
     * @return associated IVirtualDisk
     */
    public final IVirtualDisk getDisk() {
        return this.disk;
    }

    /**
     * Returns the path to the represented VDiskFile.
     * 
     * @return path to the file or directory
     */
    public String getPath() {
        return this.pathname;
    }

    /**
     * Returns an indicator indicating if the represented file exists on the
     * virtual disk or not.
     * 
     * @return true if the represented file or directory exists, otherwise false
     */
    public boolean exists() {
        return pathDiskEntry != null;
    }

    /**
     * Returns an indicator if the VDiskFile is a directory.
     * 
     * @return true if VDiskFile is directory, otherwise false
     */
    public boolean isDirectory() {
        return pathDiskEntry != null
                && pathDiskEntry instanceof IVirtualDirectory;
    }

    /**
     * Returns an indicator if the VDiskFile is a file.
     * 
     * @return true if VDiskFile is a file, otherwise false
     */
    public boolean isFile() {
        return pathDiskEntry != null && pathDiskEntry instanceof IVirtualFile;
    }

    /**
     * Returns the name of the file or directory represented by VDiskFile.
     * 
     * @return the name of the represented VDiskFile
     */
    public String getName() {
        int lastSeperatorIndex = pathname.lastIndexOf(PATH_SEPARATOR);

        return pathname.substring(lastSeperatorIndex + 1);
    }

    /**
     * Returns a VDiskFile of the parent.
     * 
     * @return VDiskFile representing the parent
     */
    public VDiskFile getParentFile() {
        int lastSeperatorIndex = pathname.lastIndexOf(PATH_SEPARATOR);
        String parentPath = pathname.substring(0, lastSeperatorIndex);

        try {
            return new VDiskFile(parentPath, disk);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Returns the names of all child elements.
     * 
     * This does only work for directories. If it's not a directory or the
     * directory contains no children this method will return an empty array.
     * 
     * @see #isDirectory()
     * @return names of child elements
     * @throws IOException
     */
    public String[] list() throws IOException {
        LinkedList<String> childNames = new LinkedList<String>();

        if (exists()) {
            for (IVirtualDiskEntry entry : getChilds()) {
                childNames.add(entry.getName());
            }
        }

        return childNames.toArray(new String[0]);
    }

    /**
     * Returns an array of VDiskFile objects representing child elements.
     * 
     * This does only work for directories. If it's not a directory or the
     * directory contains no children this method will return an empty array.
     * 
     * @return VDiskFile object for each child element
     * @throws IOException
     */
    public VDiskFile[] listFiles() throws IOException {
        Collection<VDiskFile> files = new LinkedList<VDiskFile>();

        if (exists()) {
            for (IVirtualDiskEntry entry : getChilds()) {
                VDiskFile file = new VDiskFile(this, entry.getName());

                files.add(file);
            }
        }

        return files.toArray(new VDiskFile[0]);
    }

    /**
     * Creates a directory for the path represented by the VDiskFile.
     * 
     * This method will only create a directory if the parent if it already
     * exists.
     * 
     * @return true if and only if the directory was created
     * @throws IOException
     */
    public boolean mkdir() {
        VDiskFile parent = getParentFile();
        if (parent == null) {
            return false;
        }

        if (!exists() && parent.exists() && parent.isDirectory()) {
            try {
                pathDiskEntry = disk.createDirectory(
                        (IVirtualDirectory) parent.pathDiskEntry, getName());
            } catch (IOException e) {
                return false;
            }

            return pathDiskEntry != null;
        }

        return false;
    }

    public boolean mkdirs() {
        throw new UnsupportedOperationException(); // TODO
    }

    public boolean renameTo(File dest) {
        throw new UnsupportedOperationException(); // TODO
    }

    public boolean setLastModified(long time) {
        throw new UnsupportedOperationException(); // TODO
    }

    public long getTotalSpace() {
        throw new UnsupportedOperationException(); // TODO
    }

    public long getFreeSpace() {
        throw new UnsupportedOperationException(); // TODO
    }

    public long getUsableSpace() {
        throw new UnsupportedOperationException(); // TODO
    }

    public boolean equals(Object obj) {
        throw new UnsupportedOperationException(); // TODO
    }

    public int hashCode() {
        throw new UnsupportedOperationException(); // TODO
    }

    public String toString() {
        throw new UnsupportedOperationException(); // TODO
    }
}
