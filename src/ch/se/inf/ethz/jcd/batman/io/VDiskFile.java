package ch.se.inf.ethz.jcd.batman.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import ch.se.inf.ethz.jcd.batman.vdisk.FileAlreadyExistsException;
import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualDirectory;
import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualDisk;
import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualDiskEntry;
import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualFile;
import ch.se.inf.ethz.jcd.batman.vdisk.VirtualDiskException;
import ch.se.inf.ethz.jcd.batman.vdisk.util.VirtualDiskUtil;

/**
 * Representation of a virtual disk entry (file or directory).
 * 
 * This class provides everything needed to navigate inside a
 * {@link IVirtualDisk}.
 * 
 */
public class VDiskFile {

    private static final String PATH_SEPARATOR = String
            .valueOf(IVirtualDisk.PATH_SEPARATOR);

    private static final int BUFFER_SIZE = 1024 * 1024; // 1 MiB

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
        if (!isValidPath(this.pathname)) {
            throw new VirtualDiskException("Invalid pathname");
        }
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
        if (!isValidPath(this.pathname)) {
            throw new VirtualDiskException("Invalid pathname");
        }
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

    private boolean isValidPath(String path) {
        // split path into entry names
        String[] pathParts = pathname.split(PATH_SEPARATOR);
        if (pathParts.length == 0) {
            return path.equals(PATH_SEPARATOR);
        }

        // check that the path starts with the root
        if (!pathParts[0].isEmpty()) {
            return false;
        }
        return true;
    }

    /**
     * Returns a IVirtualDiskEntry for the given path.
     * 
     * @param path
     *            path for which to return a IVirtualDiskEntry
     * @return the corresponding IVirtualDiskEntry or null. Null is returned if
     *         the path is not valid or no disk entry exists yet
     */
    private IVirtualDiskEntry getDiskEntry(String path) {
        // split path into entry names
        String[] pathParts = path.split(PATH_SEPARATOR);
        if (pathParts.length == 0) {
            return disk.getRootDirectory();
        }

        /*
         * go down to the second to last entry name (the should all be
         * directories)
         */
        IVirtualDiskEntry currentEntry = disk.getRootDirectory();
        for (int curPartIndex = 1; curPartIndex < (pathParts.length - 1); curPartIndex++) {
            String pathPartName = pathParts[curPartIndex];

            // go one step deeper
            try {
                currentEntry = VirtualDiskUtil.getDirectoryMember(
                        (IVirtualDirectory) currentEntry, pathPartName);
            } catch (IOException e) {
                return null;
            }

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
        try {
            currentEntry = VirtualDiskUtil.getDirectoryMember(
                    (IVirtualDirectory) currentEntry, lastPart);
        } catch (IOException e) {
            return null;
        }

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
        if (exists() && pathDiskEntry instanceof IVirtualDirectory) {
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
        this.pathDiskEntry = getDiskEntry(this.pathname);
        return pathDiskEntry != null;
    }

    /**
     * Returns an indicator if the VDiskFile is a directory.
     * 
     * @return true if VDiskFile is directory, otherwise false
     */
    public boolean isDirectory() {
        if (exists()) {
            return pathDiskEntry instanceof IVirtualDirectory;
        }
        return false;
    }

    /**
     * Returns an indicator if the VDiskFile is a file.
     * 
     * @return true if VDiskFile is a file, otherwise false
     */
    public boolean isFile() {
        if (exists()) {
            return pathDiskEntry instanceof IVirtualFile;
        }
        return false;
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
     * This method will only create a directory if the parent already exists.
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
     * Copies the VDiskFile to the given targetFile.
     * 
     * Note that only files can be copied, not directories or other virtual disk
     * objects.
     * 
     * @param targetFile
     *            target for the file
     * @return true if copy was created successfully, otherwise false TODO:
     *         implement copyTo for directories
     */
    public boolean copyTo(VDiskFile targetFile) {
        if (targetFile.getDisk() != this.disk) {
            // we do not support a copy over different disks
            return false;
        }

        if (targetFile.exists()) {
            // we do not overwrite existing data
            return false;
        }

        if (!this.exists()) {
            // file to copy does not exist
            return false;
        }

        if (this.isFile()) {
            try {
                targetFile.createNewFile(((IVirtualFile) this.pathDiskEntry)
                        .getSize());

                InputStream reader = new VDiskFileInputStream(this);
                OutputStream writer = new VDiskFileOutputStream(targetFile,
                        false);

                byte[] buffer = new byte[BUFFER_SIZE];
                int readAmount = 0;
                do {
                    readAmount = reader.read(buffer);
                    if (readAmount > 0) {
                        writer.write(buffer, 0, readAmount);
                    }
                } while (readAmount > 0);

                reader.close();
                writer.close();

                return true;
            } catch (IOException e) {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Creates a new, empty file of minimal size.
     * 
     * @see #createNewFile(long)
     * @return true if file could be created in all other cases false
     * @throws IOException
     *             TODO
     */
    public boolean createNewFile() throws IOException {
        return this.createNewFile(1);
    }

    /**
     * Creates a new, empty file of given size.
     * 
     * @param size
     *            the size to reserve in advance. Must be greater than zero.
     * @return true if file could be created in all other cases false
     * @throws IOException
     *             TODO
     */
    public boolean createNewFile(long size) throws IOException {
        if (size < 1L) {
            return false;
        }

        if (this.exists()) {
            return false;
        }

        VDiskFile parent = this.getParentFile();
        if (!parent.exists() || !parent.isDirectory()) {
            return false;
        }

        try {
            this.pathDiskEntry = this.disk.createFile(
                    (IVirtualDirectory) parent.getDiskEntry(), this.getName(),
                    size);
        } catch (FileAlreadyExistsException e) {
            return false;
        }
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
     * Note: For a directory the returned value does not include the files
     * inside it and their size. It will only return the size of the structure
     * used to represent the directory.
     * 
     * @return size in bytes of the represented object. If the object does not
     *         exist yet 0L is returned and in case of an error a negative
     *         value.
     */
    public long getTotalSpace() {
        if (this.exists()) {
            try {
                return this.pathDiskEntry.getTotalSize();
            } catch (IOException e) {
                return -1L;
            }
        } else {
            return 0L;
        }
    }

    /**
     * Returns the IVirtualDiskEntry belonging to the represented object.
     * 
     * @return the IVirtualDiskEntry representing the VDiskFile or null if
     *         object does not exist on disk
     */
    public IVirtualDiskEntry getDiskEntry() {
        return this.pathDiskEntry;
    }

    /**
     * Deletes the file or directory denoted by this object. If this object
     * denotes a directory, then the directory must be empty in order to be
     * deleted.
     * 
     * @return true if the object was successfully deleted, false otherwise.
     */
    public boolean delete() {
        if (exists()) {
            try {
                if (isDirectory()) {
                    // Check if directory is empty
                    IVirtualDirectory directory = (IVirtualDirectory) this.pathDiskEntry;
                    if (directory.getFirstMember() != null) {
                        return false;
                    }
                }
                this.pathDiskEntry.delete();
                this.pathDiskEntry = null;
                return true;
            } catch (IOException e) {
                return false;
            }
        }
        return false;
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
        /*
         * PMD shows a UselessParentheses violation, although it's obviously not
         * as I need the hash of the whole string containing the URI and
         * pathname
         */
        return (this.disk.getHostLocation().toString() + this.pathname)
                .hashCode();
    }
}
