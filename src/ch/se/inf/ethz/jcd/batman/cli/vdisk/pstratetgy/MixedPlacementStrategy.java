package ch.se.inf.ethz.jcd.batman.cli.vdisk.pstratetgy;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;

import ch.se.inf.ethz.jcd.batman.cli.vdisk.IVirtualDisk;
import ch.se.inf.ethz.jcd.batman.cli.vdisk.VirtualDisk;
import ch.se.inf.ethz.jcd.batman.cli.vdisk.directory.IDirectory;


/*
 * DirectoryEntry 64 bytes
 * 0x00 1  Entry Type
 * 0x01 1  Continuous Entries
 * 0x02 8  Time Stamp
 * 0x0A 8  BlockNr of next File/Directory in this Directory
 * 0x12 46 Directory name
 * 
 * FileEntry 64 bytes
 * 0x00  1 Entry Type
 * 0x01  1 Continuous Entries
 * 0x02  8 Time Stamp
 * 0x0A  8 Starting Block of File
 * 0x12  8 End Block of File
 * 0x1A  8 BlockNr of next File/Directory
 * 0x22 30 FileName
 * 
 * Continuous Entry
 * 0x00 64 File/Directory name extended
 * 
 * FreeBlock Start 64 bytes
 * 0x00 8  Identification Number
 * 0x00 
 * 
 */
public class MixedPlacementStrategy implements IPlacementStrategy {

	private static final int BLOCK_SIZE = 64;
	private static final int METADATA_SIZE = 128;
	private static final int DIRECTORY_SIZE = BLOCK_SIZE;
	private static final int BLOCK_ENTRY = 8;
	private static final int FREE_LIST_SIZE = METADATA_SIZE - BLOCK_ENTRY;
	
	private IVirtualDisk virtualDisk;
	private RandomAccessFile file;
	private IDirectory rootDirectory;
	
	public MixedPlacementStrategy(VirtualDisk virtualDisk, RandomAccessFile file) {
		this.virtualDisk = virtualDisk;
		this.file = file;
		try {
			file.seek(virtualDisk.getSuperblockSize());
			/*
			 * Uses 64 bytes to store the rootdir and the freelists
			 * 0x00 8byte rootdirectory block nr
			 * 0x08 56bytes freelists
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
	 * Initialise the segregated free lists
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
	
	private IDirectory createDirectory (String name, long timeStamp) {
		//TODO
		return null;
	}
	
	@Override
	public IDirectory getRootDirectory() {
		return rootDirectory;
	}

}
