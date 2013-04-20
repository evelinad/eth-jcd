package ch.se.inf.ethz.jcd.batman.controller;

import ch.se.inf.ethz.jcd.batman.browser.DiskEntryListener;
import ch.se.inf.ethz.jcd.batman.model.Directory;
import ch.se.inf.ethz.jcd.batman.model.Entry;
import ch.se.inf.ethz.jcd.batman.model.File;
import ch.se.inf.ethz.jcd.batman.model.Path;
import javafx.concurrent.Task;

/**
 * Controller (as in MVC-Pattern) interface for a virtual disk file browser.
 * 
 * This interface is based around {@link Task} to allow concurrent executions.
 * In other words: every command executable on a virtual disk is wrapped inside
 * a {@link Task} instance.
 * 
 */
public interface TaskController {

	/**
	 * Indicates if the controller is connected to a virtual disk.
	 * 
	 * @return true if the controller is connected to a disk, otherwise false
	 */
	boolean isConnected();

	/**
	 * Adds an observer to the controller.
	 * 
	 * @param listener
	 *            the observer to add
	 */
	void addDiskEntryListener(DiskEntryListener listener);

	/**
	 * Removes an observer from the controller.
	 * 
	 * @param listener
	 *            the observer to remove
	 */
	void removeDiskEntryListener(DiskEntryListener listener);

	/**
	 * Creates a task to import the given paths into the given destinations.
	 * 
	 * @param sourcePaths
	 *            an array of source paths on the current system
	 * @param destinationPath
	 *            an array of destinations inside the virtual disk
	 * @return the task to execute the import
	 */
	Task<Void> createImportTask(String[] sourcePaths, Path[] destinationPath);

	/**
	 * Creates a task to export the given entries into the given destinations.
	 * 
	 * @param sourceEntries
	 *            an array of entries inside the virtual disk to export
	 * @param destinationPaths
	 *            an array of paths on the current system representing the
	 *            target of an export
	 * @return the task to execute the export
	 */
	Task<Void> createExportTask(Entry[] sourceEntries, String[] destinationPaths);

	/**
	 * Creates a task to move entries to new locations.
	 * 
	 * This task can also be used to rename entries.
	 * 
	 * @param sourceEntries
	 *            an array of entries inside the virtual disk which should be
	 *            moved to a new location
	 * @param destinationPaths
	 *            an array of paths for the virtual disk, representing the
	 *            destinations of the move
	 * @return the task to execute the move
	 */
	Task<Void> createMoveTask(Entry[] sourceEntries, Path[] destinationPaths);

	/**
	 * Creates a task to copy entries.
	 * 
	 * @param sourceEntries
	 *            an array of entries which should be copied
	 * @param destinationPaths
	 *            an array of destinations for the copies
	 * @return the task to execute the copy
	 */
	Task<Void> createCopyTask(Entry[] sourceEntries, Path[] destinationPaths);

	/**
	 * Creates a task that deletes the given entries.
	 * 
	 * @param entries
	 *            an array of entries to delete
	 * @return the task to execute the deletion
	 */
	Task<Void> createDeleteEntriesTask(Entry[] entries);

	/**
	 * Creates a task to create a new file.
	 * 
	 * @param file
	 *            the file to create
	 * @return task to execute the creation
	 */
	Task<Void> createFileTask(File file);

	/**
	 * Creates a task to create a directory.
	 * 
	 * @param directory
	 *            the directory to create
	 * @return task to execute the creation
	 */
	Task<Void> createDirectoryTask(Directory directory);

	/**
	 * Creates a task to get the children of a directory.
	 * 
	 * @param directory
	 *            the parent of the returned child entries
	 * @return task to execute the query
	 */
	Task<Entry[]> createDirectoryEntriesTask(Directory directory);

	/**
	 * Creates a task to retrieve the amount of free space.
	 * 
	 * @return task to execute the query
	 */
	Task<Long> createFreeSpaceTask();

	/**
	 * Creates a task to retrieve the amount of occupied space.
	 * 
	 * @return task to execute the query
	 */
	Task<Long> createOccupiedSpaceTask();

	/**
	 * Creates a task to retrieve the amount of used space.
	 * 
	 * @return task to execute the query
	 */
	Task<Long> createUsedSpaceTask();

	/**
	 * Creates a task to connect to virtual disk
	 * 
	 * @param createNewIfNecessary
	 *            indicates if a new disk should be created, if it does not
	 *            exist. True if a new one should be created, otherwise false
	 * 
	 * @return task to execute the connection
	 */
	Task<Void> createConnectTask(boolean createNewIfNecessary);

	/**
	 * Creates a task to disconnect to the virtual disk
	 * 
	 * @return task to execute the disconnect
	 */
	Task<Void> createDisconnectTask();
}
