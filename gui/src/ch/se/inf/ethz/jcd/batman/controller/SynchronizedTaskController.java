package ch.se.inf.ethz.jcd.batman.controller;

import java.net.URI;

public interface SynchronizedTaskController extends TaskController {

	SynchronizedTaskControllerState DEFAULT_STATE = SynchronizedTaskControllerState.DISCONNECTED;

	UpdateableTask<Void> createGoOfflineTask();

	UpdateableTask<Void> createLinkDiskTask(String server, String userName,
			String password, String diskName);

	UpdateableTask<Void> createGoOnlineTask(String password);

	UpdateableTask<Void> createDownloadDiskTask(URI localUri);

	void addStateListener(SynchronizedTaskControllerStateListener listener);

	void removeStateListener(SynchronizedTaskControllerStateListener listener);

	SynchronizedTaskControllerState getState();

}
