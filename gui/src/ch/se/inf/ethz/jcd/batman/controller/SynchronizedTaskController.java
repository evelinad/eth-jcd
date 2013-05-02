package ch.se.inf.ethz.jcd.batman.controller;

import java.net.URI;

import javafx.concurrent.Task;

public interface SynchronizedTaskController extends TaskController {
	
	SynchronizedTaskControllerState DEFAULT_STATE = SynchronizedTaskControllerState.DISCONNECTED;
	
	Task<Void> createGoOfflineTask();
	
	Task<Void> createGoOnlineTask(URI serverUri);
	
	Task<Void> createDownloadDiskTask(URI localUri);
	
	void addStateListener(SynchronizedTaskControllerStateListener listener);
	
	void removeStateListener(SynchronizedTaskControllerStateListener listener);

	SynchronizedTaskControllerState getState ();
	
}
