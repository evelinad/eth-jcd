package ch.se.inf.ethz.jcd.batman.controller;

import ch.se.inf.ethz.jcd.batman.model.Directory;
import ch.se.inf.ethz.jcd.batman.model.Entry;
import ch.se.inf.ethz.jcd.batman.model.File;
import ch.se.inf.ethz.jcd.batman.model.Path;
import javafx.concurrent.Task;

public interface TaskController {

	Task<Void> createImportTask(String[] sourcePaths, Path[] destinationPath);
	
	Task<Void> createExportTask(Entry[] sourceEntries, String[] destinationPaths);
	
	Task<Void> createMoveTask(Entry[] sourceEntries, Path[] destinationPaths);
	
	Task<Void> createCopyTask(Entry[] sourceEntries, Path[] destinationPaths);
	
	Task<Void> createDeleteEntriesTask(Entry[] entries);
	
	Task<Void> createFileTask(File file);
	
	Task<Void> createDirectoryTask(Directory directory);
	
	Task<Entry[]> createDirectoryEntriesTask(Directory directory);
	
	Task<Long> createFreeSpaceTask();
	
	Task<Long> createOccupiedSpaceTask();
	
	Task<Long> createUsedSpaceTask();
	
	Task<Void> createConnectTask(boolean createNewIfNecessary);
	
	boolean isConnected();
	
	void close();
	
}
