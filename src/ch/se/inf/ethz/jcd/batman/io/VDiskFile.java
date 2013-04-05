/**
 * 
 */
package ch.se.inf.ethz.jcd.batman.io;

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
    private String pathname;
    private final IVirtualDisk disk;
    private IVirtualDiskEntry pathDiskEntry;

    // constructors
    /**
     * Constructs a VDiskFile using the given path and a virtual disk.
     * 
     * The given path does not have to exist yet on the disk.
     * 
     * @param pathname
     *            the path to a file or directory
     * @param disk
     *            the disk to which the path belongs
     * @throws IOException
     *             TODO
     */
    public VDiskFile(String pathname, IVirtualDisk disk) throws IOException {
        this.pathname = pathname;
        this.disk = disk;
        this.pathDiskEntry = getDiskEntry(this.pathname);
    }

    /**
     * Constructs a VDiskFile using the given parent path, a child name and a
     * virtual disk.
     * 
     * The given path does not have to exist yet on the disk. This holds for the
     * parent as for the child.
     * 
     * @param parent
     *            the path to the parent directory
     * @param child
     *            the name of the child
     * @param disk
     *            the disk to which parent and child belong
     * @throws IOException
     *             TODO
     */
    public VDiskFile(String parent, String child, IVirtualDisk disk)
            throws IOException {
        if (parent.equals(PATH_SEPARATOR)) {
            this.pathname = String.format("%s%s", PATH_SEPARATOR, child);
        } else {
            this.pathname = String.format("%s%s%s", parent,
                    IVirtualDisk.PATH_SEPARATOR, child);
        }

        this.disk = disk;
        this.pathDiskEntry = getDiskEntry(this.pathname);
    }

    /**
     * Constructs a VDiskFile using the given parent and a child's name.
     * 
     * The given parent and child do not have to exist yet.
     * 
     * @param parent
     *            a VDiskFile representing the parent
     * @param child
     *            the name of the child
     * @throws IOException
     *             TODO
     * @throws NullPointerException
     *             if parent is null
     */
    public VDiskFile(VDiskFile parent, String child) throws IOException {
        this(parent.getPath(), child, parent.getDisk());
    }

    // private methods

    /**
     * Returns a IVirtualDiskEntry for the given path.
     * 
     * @param path
     *            path for which to return a IVirtualDiskEntry
     * @return the corresponding IVirtualDiskEntry or null. Null is returned if
     *         the path is not valid or no disk entry exists yet
     * @throws IOException
     *             TODO
     */
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

    /**
     * Returns the children for the VDiskFile.
     * 
     * A returned empty list may indicate that the directory has no children or
     * that the VDiskFile does not represent a directory at all.
     * 
     * @see #isDirectory()
     * @return a collection of children. Will never return a null value!
     * @throws IOException
     *             TODO
     */
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

        if (parentPath.isEmpty()) {
            // ok, the parent is the root dir
            parentPath = PATH_SEPARATOR;
        }

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

    /**
     * Creates a directory for the path represented by the VDiskFile.
     * 
     * This method will try to create any parent that does not yet exist along
     * the way of the path.
     * 
     * @return true if directory was created, otherwise false
     */
    public boolean mkdirs() {
        VDiskFile parent = getParentFile();
        if (parent == null) {
            return false;
        }

        if (!parent.exists()) {
            if (!parent.mkdirs()) {
                return false;
            }
        }

        return mkdir();
    }

    /**
     * Moves the VDiskFile to the given destination.
     * 
     * This method can be used to rename and/or move a VDiskFile inside the same
     * disk.
     * 
     * @param dest
     *            the new destination of the VDiskFile
     * @return true in case of success, otherwise false
     * @throws RuntimeException
     *             in case of a fatal error resulting in an unstable state of
     *             the underlying disk
     */
    public boolean renameTo(VDiskFile dest) {
        if (dest.getDisk() != this.disk) {
            return false;
        }

        if (dest.exists() || !this.exists()) {
            return false;
        }

        VDiskFile newParent = dest.getParentFile();

        if (!newParent.isDirectory()) {
            return false;
        }

        IVirtualDirectory oldParentDir = (IVirtualDirectory) getParentFile().pathDiskEntry;
        IVirtualDirectory newParentDir = (IVirtualDirectory) newParent.pathDiskEntry;

        try {
            oldParentDir.removeMember(this.pathDiskEntry);
        } catch (IOException e) {
            return false;
        }

        try {
            this.pathDiskEntry.setName(dest.getName());
        } catch (IOException e) {
            try {
                oldParentDir.addMember(this.pathDiskEntry);
            } catch (IOException eInner) {
                throw new RuntimeException(eInner);
            }

            return false;
        }

        try {
            newParentDir.addMember(this.pathDiskEntry);
        } catch (IOException e) {
            try {
                this.pathDiskEntry.setName(this.getName());
                oldParentDir.addMember(this.pathDiskEntry);
            } catch (IOException eInner) {
                throw new RuntimeException(eInner);
            }

            return false;
        }

        this.pathname = dest.getPath();
        return true;
    }

    /**
     * Sets the last modified value.
     * 
     * @param time
     *            the new time value
     * @return true in case of success, otherwise false
     */
    public boolean setLastModified(long time) {
        if (this.exists()) {
            try {
                this.pathDiskEntry.setTimestamp(time);
                return true;
            } catch (IOException e) {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Returns the last modified value.
     * 
     * @return the last modified value
     */
    public long lastModified() {
        if (this.exists()) {
            return this.pathDiskEntry.getTimestamp();
        } else {
            return 0L;
        }
    }

    /**
     * Returns the size of the object represented by VDiskFile.
     * 
     * If the object does not yet exist on disk 0L is returned. In case of an
     * exception a negative value is returned.
     * 
     * @return size of the represented object. If the object does not exist yet
     *         0L is returned and in case of an error a negative value.
     */
    public long getTotalSpace() {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VDiskFile) {
            VDiskFile otherFile = (VDiskFile) obj;

            return otherFile.disk.equals(this.disk)
                    && otherFile.pathname.equals(this.pathname);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return (this.disk.getHostLocation().toString() + this.pathname)
                .hashCode();
    }
}
