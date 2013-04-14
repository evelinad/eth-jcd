package ch.se.inf.ethz.jcd.batman.controller;

import ch.se.inf.ethz.jcd.batman.model.Directory;
import ch.se.inf.ethz.jcd.batman.model.Entry;
import ch.se.inf.ethz.jcd.batman.model.File;
import javafx.concurrent.Task;

public interface TaskController {

	Task<File> createFileTask(File file);
	
	Task<Directory> createDirectoryTask(Directory directory);
	
	Task<Entry[]> getDirectoryEntrysTask(Directory directory);
	
	Task<Long> getFreeSpaceTask();
	
	Task<Long> getOccupiedSpaceTask();
	
	Task<Long> getUsedSpaceTask();
	
	void connect(boolean createNewIfNecessary) throws ConnectionException;
	
	boolean connected();
	
	void close();
	
}
