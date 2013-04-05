/**
 * 
 */
package ch.se.inf.ethz.jcd.batman.io;

import java.io.File;
import java.io.IOException;
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

    // constructors
    /**
     * @param pathname
     */
    public VDiskFile(String pathname, IVirtualDisk disk) {
        this.pathname = pathname;
        this.disk = disk;
    }

    /**
     * @param parent
     * @param child
     */
    public VDiskFile(String parent, String child, IVirtualDisk disk) {
        this(String
                .format("%s%s%s", parent, IVirtualDisk.PATH_SEPARATOR, child),
                disk);
    }

    /**
     * @param parent
     * @param child
     */
    public VDiskFile(File parent, String child, IVirtualDisk disk) {
        this(parent.getPath(), child, disk);
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
                throw new VirtualDiskException(String.format(
                        "'%s' does not exist as part of '%s'", pathPartName,
                        path));
            }

            // check that it still is a directory
            if (!(currentEntry instanceof IVirtualDirectory)) {
                throw new VirtualDiskException(String.format(
                        "'%s' is not a directory", pathPartName));
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

        if (currentEntry == null) {
            throw new VirtualDiskException(String.format(
                    "'%s' does not exist as part of '%s'", lastPart, path));
        }

        return currentEntry;

    }

    private Collection<IVirtualDiskEntry> getChilds() throws IOException {
        IVirtualDiskEntry parent = getDiskEntry(pathname);

        if (parent instanceof IVirtualDirectory) {
            return VirtualDiskUtil
                    .getDirectoryMembers((IVirtualDirectory) parent);
        } else if (parent instanceof IVirtualFile) {
            return null;
        } else {
            throw new RuntimeException();
        }
    }

    // public methods

    public String[] list() throws IOException {
        LinkedList<String> childNames = new LinkedList<String>();

        for (IVirtualDiskEntry entry : getChilds()) {
            childNames.add(entry.getName());
        }

        return childNames.toArray(new String[0]);
    }

    public File[] listFiles() {
        throw new UnsupportedOperationException(); // TODO
    }

    public boolean mkdir() {
        throw new UnsupportedOperationException(); // TODO
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
