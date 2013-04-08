package ch.se.inf.ethz.jcd.batman.vdisk.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import ch.se.inf.ethz.jcd.batman.vdisk.FileAlreadyExistsException;
import ch.se.inf.ethz.jcd.batman.vdisk.FileState;
import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualDirectory;
import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualDisk;
import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualDiskEntry;
import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualDiskSpace;
import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualFile;
import ch.se.inf.ethz.jcd.batman.vdisk.VirtualDiskException;
import ch.se.inf.ethz.jcd.batman.vdisk.util.VirtualDiskUtil;
import ch.se.inf.ethz.jcd.batman.vdisk.util.VirtualEntryIterator;

/**
 * Implementation of {@link IVirtualDiskEntry}.
 */
public abstract class VirtualDiskEntry implements IVirtualDiskEntry {

    /**
     * Loads a {@link IVirtualDiskEntry} located at the offset position given by
     * position. The returned {@link IVirtualDiskEntry} is either an instance of
     * {@link IVirtualDirectory} or {@link IVirtualFile}.
     * 
     * @param disk
     *            the disk on which the entry is stored
     * @param position
     *            the offset position of the entry
     * @return the loaded entry
     * @throws IOException
     *             if an I/O error occurs
     */
    public static IVirtualDiskEntry load(IVirtualDisk disk, long position)
            throws IOException {
        IVirtualDiskSpace space = VirtualDiskSpace.load(disk, position);
        if (VirtualDirectory.isDirectory(space)) {
            return VirtualDirectory.load(disk, space);
        } else if (VirtualFile.isFile(space)) {
            return VirtualFile.load(disk, space);
        } else {
            throw new VirtualDiskException("Unsupported disk entry");
        }
    }

    private static final String CHARSET_NAME = "UTF-8";

    private final IVirtualDisk disk;
    private IVirtualDirectory parent;
    private IVirtualDiskEntry previous;
    private IVirtualDiskEntry next;
    private boolean nextEntryLoaded;
    private String name;
    private long timestamp;
    private FileState state;

    protected VirtualDiskEntry(IVirtualDisk disk) throws IOException {
        this.disk = disk;
        state = FileState.CREATED;
    }

    protected void create(String name) throws IOException {
        checkNameValid(name);
        this.name = name;
        nextEntryLoaded = true;
    }

    protected void load() throws IOException {
        this.name = loadName();
        nextEntryLoaded = false;
    }

    protected abstract String loadName() throws IOException;

    protected void checkNameValid(String name) throws VirtualDiskException {
        if (name == null || name.contains("" + IVirtualDisk.PATH_SEPARATOR)) {
            throw new VirtualDiskException("Invalid name");
        }
    }

    /**
     * Checks if the name is already in use in the given directory. If so an
     * exception is thrown.
     * 
     * @param parent
     *            the directory to check
     * @param name
     *            the name to check
     * @throws VirtualDiskException
     *             if the name is already in use
     */
    protected void checkNameFree(IVirtualDirectory parent, String name)
            throws IOException {
        if (parent != null) {
            Collection<IVirtualDiskEntry> directoryEntrys = VirtualDiskUtil
                    .getDirectoryMembers(parent);
            for (IVirtualDiskEntry entry : directoryEntrys) {
                if (entry.getName().equals(name)) {
                    throw new FileAlreadyExistsException();
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setName(String name) throws IOException {
        checkNameValid(name);
        checkNameFree(parent, name);
        this.name = name;
        updateName();
    }

    protected abstract void updateName() throws IOException;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IVirtualDirectory getParent() {
        return parent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setParent(IVirtualDirectory parent) throws IOException {
        this.parent = parent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IVirtualDiskEntry getPreviousEntry() {
        return previous;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPreviousEntry(IVirtualDiskEntry previous) {
        this.previous = previous;
    }

    protected abstract IVirtualDiskEntry loadNextEntry() throws IOException;

    /**
     * {@inheritDoc}
     */
    @Override
    public IVirtualDiskEntry getNextEntry() throws IOException {
        if (!nextEntryLoaded) {
            next = loadNextEntry();
            if (next != null) {
                next.setParent(this.getParent());
                next.setPreviousEntry(this);
                nextEntryLoaded = true;
            }
        }
        return next;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNextEntry(IVirtualDiskEntry next) throws IOException {
        this.next = next;
        nextEntryLoaded = true;
        updateNextEntry();
    }

    protected abstract void updateNextEntry() throws IOException;

    /**
     * {@inheritDoc}
     */
    @Override
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTimestamp(long timestamp) throws IOException {
        this.timestamp = timestamp;
        updateTimestamp();
    }

    protected abstract void updateTimestamp() throws IOException;

    /**
     * Return the disk on which this entry is stored.
     * 
     * @return the disk on which this entry is stored.
     */
    public IVirtualDisk getDisk() {
        return disk;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete() throws IOException {
        state = FileState.DELETED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean exists() {
        return state == FileState.CREATED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<IVirtualDiskEntry> iterator() {
        return new VirtualEntryIterator(this);
    }

    protected void saveString(IVirtualDiskSpace space, long position,
            String string) throws IOException {
        byte[] encodedString = string.getBytes(CHARSET_NAME);
        space.write(position, encodedString);
        space.write(position + encodedString.length, String.valueOf('\0')
                .getBytes(CHARSET_NAME));
    }

    protected String loadString(IVirtualDiskSpace space, long position)
            throws IOException {
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        byte lastByteRead;
        long currentPosition = position;
        while ((lastByteRead = space.read(currentPosition++)) != '\0') {
            byteArray.write(lastByteRead);
        }
        return new String(byteArray.toByteArray(), CHARSET_NAME);
    }

    protected long calculateStringSpace(String string) throws IOException {
        long terminatorLength = String.valueOf('\0').getBytes(CHARSET_NAME).length;
        return (string == null) ? terminatorLength : terminatorLength
                + string.getBytes(CHARSET_NAME).length;
    }
}
