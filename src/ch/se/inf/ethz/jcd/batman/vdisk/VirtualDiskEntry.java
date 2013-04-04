package ch.se.inf.ethz.jcd.batman.vdisk;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public abstract class VirtualDiskEntry implements IVirtualDiskEntry {

	public static IVirtualDiskEntry load (IVirtualDisk disk, long position) throws IOException {
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
	
	private IVirtualDisk disk;
	private IVirtualDirectory parent;
	private IVirtualDiskEntry previous;
	private IVirtualDiskEntry next;
	private boolean nextEntryLoaded;
	private String name;
	private long timestamp;
	private FileState state;
	
	public VirtualDiskEntry (IVirtualDisk disk) throws IOException {
		this.disk = disk;
		state = FileState.CREATED;
	}
	
	protected void create (String name) throws IOException {
		this.name = name;
		nextEntryLoaded = true;
	}
	
	protected void load () throws IOException {
		this.name = loadName();
		nextEntryLoaded = false;
	}
	
	protected abstract String loadName() throws IOException;
	
	/**
	 * Checks if the name is already in use in the given directory. If so an exception is thrown.
	 * 
	 * @param directory the directory to check
	 * @param name the name to check
	 * @throws VirtualDiskException if the name is already in use
	 */
	protected void checkNameFree (IVirtualDirectory parent, String name) throws IOException {
		if (parent != null) {
			IVirtualDiskEntry[] directoryEntrys = VirtualDiskUtil.getDirectoryEntrys(parent);
			for (IVirtualDiskEntry entry : directoryEntrys) {
				if (entry.getName().equals(name)) {
					throw new VirtualDiskException("Name already in use");
				}
			}
		}
	}
	
	@Override
	public void setName(String name) throws IOException {
		checkNameFree(parent, name);
		this.name = name;
		updateName();
	}
	
	protected abstract void updateName () throws IOException;
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public IVirtualDirectory getParent() {
		return parent;
	}

	@Override
	public void setParent(IVirtualDirectory parent) throws IOException {
		this.parent = parent;
	}
	
	@Override
	public IVirtualDiskEntry getPreviousEntry() {
		return previous;
	}

	@Override
	public void setPreviousEntry(IVirtualDiskEntry previous) {
		this.previous = previous;
	}

	protected abstract IVirtualDiskEntry loadNextEntry() throws IOException;
	
	@Override
	public IVirtualDiskEntry getNextEntry() throws IOException {
		if (!nextEntryLoaded) {
			next = loadNextEntry();
			next.setParent(this.getParent());
			next.setPreviousEntry(this);
			nextEntryLoaded = true;
		}
		return next;
	}
	
	@Override
	public void setNextEntry(IVirtualDiskEntry next) throws IOException {
		this.next = next;
		nextEntryLoaded = true;
		updateNextEntry();
	}
	
	protected abstract void updateNextEntry() throws IOException;
	
	@Override
	public long getTimestamp() {
		return timestamp;
	}

	@Override
	public void setTimestamp(long timestamp) throws IOException {
		this.timestamp = timestamp;
		updateTimestamp();
	}
	
	protected abstract void updateTimestamp () throws IOException;
	
	public IVirtualDisk getDisk () {
		return disk;
	}
	
	@Override
	public void delete () throws IOException {
		state = FileState.DELETED;
	}

	@Override
	public boolean exists () {
		return state == FileState.CREATED;
	}
	
	protected void saveString (IVirtualDiskSpace space, long position, String string) throws IOException {
		byte[] encodedString = string.getBytes(CHARSET_NAME);
		space.write(position, encodedString);
		space.write(position + encodedString.length, String.valueOf('\0').getBytes(CHARSET_NAME));
	}
	
	protected String loadString (IVirtualDiskSpace space, long position) throws IOException {
		ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
		byte lastByteRead;
		while ((lastByteRead = space.read(position++)) != '\0') {
			byteArray.write(lastByteRead);
		}
		return new String(byteArray.toByteArray(), CHARSET_NAME);
	}
	
	protected long calculateStringSpace (String string) throws IOException {
		return (string != null) ? string.getBytes(CHARSET_NAME).length : 0 +
				String.valueOf('\0').getBytes(CHARSET_NAME).length;
	}
}
