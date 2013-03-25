package ch.se.inf.ethz.jcd.batman.vdisk;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;



/*
 * DirectoryEntry 64 bytes
 * 0x00 1  Entry Type
 * 0x01 1  Continuous Entries
 * 0x02 8  Time Stamp
 * 0x0A 8  BlockNr of next File/Directory in this Directory
 * 0x12 8  Starting Directory/File of this directory
 * 0x1A 38 Directory name (last byte reserved for block end if no continuous entries)
 * 
 * FileEntry 64 bytes
 * 0x00  1 Entry Type
 * 0x01  1 Continuous Entries
 * 0x02  8 Time Stamp
 * 0x0A  8 Starting Block of File
 * 0x12  8 BlockNr of next File/Directory
 * 0x1A 38 FileName (last byte reserved for block end if no continuous entries)
 * 
 * Continuous Entry 
 * 0x00 64 File/Directory name extended (last byte reserved for block end if last continuous entries)
 * 
 * FreeBlock Start 64 bytes
 * 0x00 8  Size and free identifier
 * 0x00 56 Free
 * 
 * FreeBlock Middle 64 bytes
 * 0x00 64 Free
 * 
 * FreeBlock End 64 bytes
 * 0x00 63 Free
 * 0xFF 1  Size and free identifier
 * 
 * DataBlock Start 64 bytes
 * 0x00 8  Size and free identifier
 * 0x08 8  Block Number of next block
 * 0x00 48 Data
 * 
 * DataBlock Middle 64 bytes
 * 0x00 64 Data
 * 
 * DataBlock End 64 bytes
 * 0x00 63 Data
 * 0xFF 1  Size and free/data block identifier 
 */
public class MixedPlacementStrategy implements IPlacementStrategy {

	private static final int BLOCK_SIZE = 64;
	private static final int METADATA_SIZE = 128;
	private static final int DIRECTORY_SIZE = BLOCK_SIZE;
	private static final int BLOCK_ENTRY = 8;
	private static final int FREE_LIST_SIZE = METADATA_SIZE - BLOCK_ENTRY;
	
	private IVirtualDisk virtualDisk;
	private RandomAccessFile file;
	private IVirtualDirectory rootDirectory;
	
	public MixedPlacementStrategy(VirtualDisk virtualDisk, RandomAccessFile file) {
		this.virtualDisk = virtualDisk;
		this.file = file;
		try {
			file.seek(virtualDisk.getSuperblockSize());
			/*
			 * Uses 64 bytes to store the root directory and the free lists
			 * 0x00 8byte root directory block number
			 * 0x08 120bytes free lists
			 */
			file.setLength(virtualDisk.getSuperblockSize() + METADATA_SIZE + DIRECTORY_SIZE);
			createFreeLists();
			createRootDirectory();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * Initialize the segregated free lists
	 * There is a class for each two power size
	 * 1 : 1-2
	 * 2 : 3-4
	 * 3 : 5-8
	 * ....
	 * 13 : 4097-8192
	 * 14 : 8193-16384
	 * 15 : 16385-infinity
	 */
	private void createFreeLists () throws IOException {
		file.seek(virtualDisk.getSuperblockSize() + BLOCK_ENTRY);
		for (int i = 0; i < FREE_LIST_SIZE; i++) {
			file.write(0);
		}
	}
	
	private void createRootDirectory () {
		rootDirectory = createDirectory("root", new Date().getTime());
	}
	
	private IVirtualDirectory createDirectory (String name, long timeStamp) {
		//TODO
		return null;
	}
	
	@Override
	public IVirtualDirectory getRootDirectory() {
		return rootDirectory;
	}

}
