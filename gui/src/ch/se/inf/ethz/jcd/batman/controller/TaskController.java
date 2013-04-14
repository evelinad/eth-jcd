package ch.se.inf.ethz.jcd.batman.controller;

import ch.se.inf.ethz.jcd.batman.model.Directory;
import ch.se.inf.ethz.jcd.batman.model.Entry;
import ch.se.inf.ethz.jcd.batman.model.File;
import javafx.concurrent.Task;

public interface TaskController {

	Task<Void> createImportTask(String sourcePath, Entry destination);
	
	Task<Void> createExportTask(Entry sourceEntry, String destinationPath);
	
	Task<Void> createRenameTask(Entry source, Entry destination);
	
	Task<Void> createFileTask(File file);
	
	Task<Void> createDirectoryTask(Directory directory);
	
	Task<Entry[]> createDirectoryEntrysTask(Directory directory);
	
	Task<Void> createDeleteEntryTask(Entry entry);
	
	Task<Long> createFreeSpaceTask();
	
	Task<Long> createOccupiedSpaceTask();
	
	Task<Long> createUsedSpaceTask();
	
	Task<Void> createConnectTask(boolean createNewIfNecessary);
	
	boolean isConnected();
	
	void close();
	
}
