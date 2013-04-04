package ch.se.inf.ethz.jcd.batman.vdisk;

import java.io.IOException;

public class VirtualFile extends VirtualDiskEntry implements IVirtualFile {

	public static IVirtualFile load (IVirtualDisk disk, long position) throws IOException {
		VirtualFile virtualFile = new VirtualFile(disk);
		virtualFile.load(VirtualDiskSpace.load(disk, position));
		return virtualFile;
	}
	
	protected static IVirtualFile load (IVirtualDisk disk, IVirtualDiskSpace space) throws IOException {
		VirtualFile virtualFile = new VirtualFile(disk);
		virtualFile.load(space);
		return virtualFile;
	}
	
	public static IVirtualFile create (IVirtualDisk disk, String name, long size) throws IOException {
		VirtualFile virtualFile = new VirtualFile(disk);
		virtualFile.create(name, size);
		return virtualFile;
	}
	
	protected static boolean isFile (IVirtualDiskSpace space) throws IOException {
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
	
	public VirtualFile(IVirtualDisk disk) throws IOException {
		super(disk);
	}

	protected void create (String name, long size) throws IOException {
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
		space.write(getDataPosition());
	}
	
	protected void load (IVirtualDiskSpace space) throws IOException {
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
		dataSpace = VirtualDiskSpace.load(getDisk(), dataLocPos);
		dataSpaceLoaded = true;
	}
	
	protected void checkEntryType() throws IOException {
		space.seek(ENTRY_TYPE_POS);
		byte directoryEntry = space.read();
		if (directoryEntry != FILE_ENTRY) {
			throw new VirtualDiskException("Can't load file, invalid entry type");
		}
	}
	
	protected void loadTimestamp() throws IOException {
		space.seek(TIMESTAMP_POS);
		setTimestamp(space.readLong());
	}
	
	protected IVirtualDiskEntry loadNextEntry() throws IOException {
		space.seek(NEXT_ENTRY_POS);
		long nextEntry = space.readLong();
		return VirtualDiskEntry.load(getDisk(), nextEntry);
	}

	private long calculateSize (String name) throws IOException {
		return DEFAULT_SIZE + calculateStringSpace(name);
	}
	
	@Override
	public long getPosition() {
		return space.getVirtualDiskPosition();
	}

	@Override
	public long getDataPosition() throws IOException {
		return getDataSpace().getVirtualDiskPosition();
	}

	@Override
	public void seek(long position) throws IOException {
		getDataSpace().seek(position);
	}

	@Override
	public int read(byte[] b) throws IOException {
		return getDataSpace().read(b);
	}

	@Override
	public byte read() throws IOException {
		return getDataSpace().read();
	}

	@Override
	public void write(byte b) throws IOException {
		getDataSpace().write(b);
	}

	@Override
	public void write(byte[] b) throws IOException {
		getDataSpace().write(b);
	}

	@Override
	public void setSize(long size) throws IOException {
		getDataSpace().changeSize(size);
	}

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
		space.write((next == null) ? 0 : next.getPosition());
	}

	@Override
	protected void updateTimestamp() throws IOException {
		space.seek(TIMESTAMP_POS);
		space.write(getTimestamp());
	}

	private IVirtualDiskSpace getDataSpace () throws IOException {
		if (!dataSpaceLoaded) {
			loadDataSpace();
		}
		return dataSpace;
	}

	@Override
	public long getDiskSize() throws IOException {
		return getDataSpace().getDiskSize();
	}
	
}
