package ch.se.inf.ethz.jcd.batman.vdisk.impl;

import java.io.IOException;

import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualDisk;
import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualDiskEntry;
import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualDiskSpace;
import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualFile;
import ch.se.inf.ethz.jcd.batman.vdisk.VirtualDiskException;

/**
 * Implementation of {@link IVirtualFile}.
 * 
 * The file contains two separate {@link IVirtualDiskSpace} on stores the meta data of
 * the file and one stores the data of the file.
 * 
 * The {@link IVirtualDiskSpace} which handles the meta data is structured as
 * follows:
 * 
 * 0x00 1 Entry Type 0x01 8 Time stamp 0x09 8 Offset position of the next
 * directory/file which is stored in the same directory as this directory 0x11 8
 * Offset position of the {@link IVirtualDiskSpace} of the data 0x19 n File name
 */
public final class VirtualFile extends VirtualDiskEntry implements IVirtualFile {

    /**
     * Loads a File located at the offset position given by position.
     * 
     * @param disk
     *            the disk on which the file is stored
     * @param position
     *            the offset position in bytes of the file
     * @return the loaded File
     * @throws IOException
     *             if an I/O error occurs
     */
    public static IVirtualFile load(IVirtualDisk disk, long position)
            throws IOException {
        VirtualFile virtualFile = new VirtualFile(disk);
        virtualFile.load(VirtualDiskSpace.load(disk, position));
        return virtualFile;
    }

    /**
     * Loads a File from the virtual disk, the meta data is located in the
     * {@link IVirtualDiskSpace} given by the parameter space.
     * 
     * @param disk
     *            the disk on which the file is stored
     * @param space
     *            contains the meta data of the file
     * @return the loaded File
     * @throws IOException
     *             if an I/O error occurs
     */
    protected static IVirtualFile load(IVirtualDisk disk,
            IVirtualDiskSpace space) throws IOException {
        VirtualFile virtualFile = new VirtualFile(disk);
        virtualFile.load(space);
        return virtualFile;
    }

    /**
     * Creates a file with the given name and size on the {@link IVirtualDisk}.
     * 
     * @param disk
     *            the disk on which the file should be stored
     * @param name
     *            the name of the newly created file
     * @param size
     *            the starting size of the newly created file
     * @return the newly created file
     * @throws IOException
     *             if an I/O error occurs
     */
    public static IVirtualFile create(IVirtualDisk disk, String name, long size)
            throws IOException {
        VirtualFile virtualFile = new VirtualFile(disk);
        virtualFile.create(name, size);
        return virtualFile;
    }

    protected static boolean isFile(IVirtualDiskSpace space) throws IOException {
        return space.read(ENTRY_TYPE_POS) == FILE_ENTRY;
    }

    private static final int ENTRY_TYP_SIZE = 1;
    private static final int TIMESTAMP_SIZE = 8;
    private static final int NEXT_ENTRY_SIZE = 8;
    private static final int DATA_LOC_SIZE = 8;

    private static final int ENTRY_TYPE_POS = 0;
    private static final int TIMESTAMP_POS = ENTRY_TYP_SIZE;
    private static final int NEXT_ENTRY_POS = TIMESTAMP_POS + TIMESTAMP_SIZE;
    private static final int DATA_LOC_POS = NEXT_ENTRY_POS + NEXT_ENTRY_SIZE;
    private static final int NAME_POS = DATA_LOC_POS + DATA_LOC_SIZE;

    private static final int DEFAULT_SIZE = NAME_POS;

    private static final byte FILE_ENTRY = 2;

    private IVirtualDiskSpace space;
    private IVirtualDiskSpace dataSpace;
    private boolean dataSpaceLoaded;

    private VirtualFile(IVirtualDisk disk) throws IOException {
        super(disk);
    }

    protected void create(String name, long size) throws IOException {
        space = VirtualDiskSpace.create(getDisk(), calculateSize(name));
        super.create(name);
        dataSpace = VirtualDiskSpace.create(getDisk(), size);
        dataSpaceLoaded = true;
        updateAll();
    }

    private void updateAll() throws IOException {
        updateEntryType();
        updateTimestamp();
        updateNextEntry();
        updateDataPosition();
        updateName();
    }

    protected void updateEntryType() throws IOException {
        space.seek(ENTRY_TYPE_POS);
        space.write(FILE_ENTRY);
    }

    protected void updateDataPosition() throws IOException {
        space.seek(DATA_LOC_POS);
        space.writeLong(getDataPosition());
    }

    protected void load(IVirtualDiskSpace space) throws IOException {
        this.space = space;
        checkEntryType();
        super.load();
        loadTimestamp();
        loadName();
        dataSpaceLoaded = false;
    }

    protected void loadDataSpace() throws IOException {
        space.seek(DATA_LOC_POS);
        long dataLocPos = space.readLong();
        if (dataLocPos == 0) {
        	dataSpace = VirtualDiskSpace.create(getDisk(), 0);
        } else {
        	dataSpace = VirtualDiskSpace.load(getDisk(), dataLocPos);
        }
        dataSpaceLoaded = true;
    }

    protected void checkEntryType() throws IOException {
        space.seek(ENTRY_TYPE_POS);
        byte directoryEntry = space.read();
        if (directoryEntry != FILE_ENTRY) {
            throw new VirtualDiskException(
                    "Can't load file, invalid entry type");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete() throws IOException {
        super.delete();
        if (getParent() != null) {
            getParent().removeMember(this);
        }
        getDataSpace().free();
        space.free();
    }

    protected void loadTimestamp() throws IOException {
        space.seek(TIMESTAMP_POS);
        setTimestamp(space.readLong());
    }

    protected IVirtualDiskEntry loadNextEntry() throws IOException {
        space.seek(NEXT_ENTRY_POS);
        long nextEntry = space.readLong();
        if (nextEntry == 0) {
            return null;
        } else {
            return VirtualDiskEntry.load(getDisk(), nextEntry);
        }
    }

    private long calculateSize(String name) throws IOException {
        return DEFAULT_SIZE + calculateStringSpace(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getPosition() {
        return space.getVirtualDiskPosition();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getDataPosition() throws IOException {
        return getDataSpace().getVirtualDiskPosition();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void seek(long position) throws IOException {
        getDataSpace().seek(position);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read(byte[] b) throws IOException {
        return getDataSpace().read(b);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte read() throws IOException {
        return getDataSpace().read();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(byte b) throws IOException {
        getDataSpace().write(b);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(byte[] b) throws IOException {
        getDataSpace().write(b);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSize(long size) throws IOException {
        getDataSpace().changeSize(size);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getSize() throws IOException {
        return getDataSpace().getSize();
    }

    @Override
    protected String loadName() throws IOException {
        return loadString(space, NAME_POS);
    }

    @Override
    protected void updateName() throws IOException {
        space.changeSize(calculateSize(getName()));
        saveString(space, NAME_POS, getName());
    }

    @Override
    protected void updateNextEntry() throws IOException {
        space.seek(NEXT_ENTRY_POS);
        IVirtualDiskEntry next = getNextEntry();
        space.writeLong((next == null) ? 0 : next.getPosition());
    }

    @Override
    protected void updateTimestamp() throws IOException {
        space.seek(TIMESTAMP_POS);
        space.writeLong(getTimestamp());
    }

    private IVirtualDiskSpace getDataSpace() throws IOException {
        if (!dataSpaceLoaded) {
            loadDataSpace();
        }
        return dataSpace;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getDataDiskSize() throws IOException {
        return getDataSpace().getDiskSize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getTotalSize() throws IOException {
        return getDataSpace().getDiskSize() + space.getDiskSize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getFilePointer() throws IOException {
        return getDataSpace().getPosition();
    }

}
