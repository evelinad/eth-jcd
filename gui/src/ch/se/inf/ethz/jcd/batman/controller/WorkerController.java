package ch.se.inf.ethz.jcd.batman.controller;

import javafx.concurrent.Service;

public interface WorkerController {

	DirectoryEntriesService getDirectoryEntrysService();
	
	Service<Long> getFreeSpaceService();
	
	Service<Long> getOccupiedSpaceService();
	
	Service<Long> getUsedSpaceService();
	
	boolean connect(boolean createNewIfNecessary);
	
	boolean connected();
	
	void close();
	
}
