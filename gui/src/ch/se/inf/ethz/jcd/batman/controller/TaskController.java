package ch.se.inf.ethz.jcd.batman.controller;

import ch.se.inf.ethz.jcd.batman.browser.DiskEntryListener;
import ch.se.inf.ethz.jcd.batman.model.Directory;
import ch.se.inf.ethz.jcd.batman.model.Entry;
import ch.se.inf.ethz.jcd.batman.model.File;
import ch.se.inf.ethz.jcd.batman.model.Path;

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
	UpdateableTask<Void> createImportTask(String[] sourcePaths, Path[] destinationPath);

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
	UpdateableTask<Void> createExportTask(Entry[] sourceEntries, String[] destinationPaths);

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
	UpdateableTask<Void> createMoveTask(Entry[] sourceEntries, Path[] destinationPaths);

	/**
	 * Creates a task to copy entries.
	 * 
	 * @param sourceEntries
	 *            an array of entries which should be copied
	 * @param destinationPaths
	 *            an array of destinations for the copies
	 * @return the task to execute the copy
	 */
	UpdateableTask<Void> createCopyTask(Entry[] sourceEntries, Path[] destinationPaths);

	/**
	 * Creates a task that deletes the given entries.
	 * 
	 * @param entries
	 *            an array of entries to delete
	 * @return the task to execute the deletion
	 */
	UpdateableTask<Void> createDeleteEntriesTask(Entry[] entries);

	/**
	 * Creates a task to create a new file.
	 * 
	 * @param file
	 *            the file to create
	 * @return task to execute the creation
	 */
	UpdateableTask<Void> createFileTask(File file);

	/**
	 * Creates a task to create a directory.
	 * 
	 * @param directory
	 *            the directory to create
	 * @return task to execute the creation
	 */
	UpdateableTask<Void> createDirectoryTask(Directory directory);

	/**
	 * Creates a task to get the children of a directory.
	 * 
	 * @param directory
	 *            the parent of the returned child entries
	 * @return task to execute the query
	 */
	UpdateableTask<Entry[]> createDirectoryEntriesTask(Directory directory);

	/**
	 * Creates a task to retrieve the amount of free space.
	 * 
	 * @return task to execute the query
	 */
	UpdateableTask<Long> createFreeSpaceTask();

	/**
	 * Creates a task to retrieve the amount of occupied space.
	 * 
	 * @return task to execute the query
	 */
	UpdateableTask<Long> createOccupiedSpaceTask();

	/**
	 * Creates a task to retrieve the amount of used space.
	 * 
	 * @return task to execute the query
	 */
	UpdateableTask<Long> createUsedSpaceTask();

	/**
	 * Creates a task to connect to virtual disk
	 * 
	 * @param createNewIfNecessary
	 *            indicates if a new disk should be created, if it does not
	 *            exist. True if a new one should be created, otherwise false
	 * 
	 * @return task to execute the connection
	 */
	UpdateableTask<Void> createConnectTask(boolean createNewIfNecessary);

	/**
	 * Creates a task to disconnect to the virtual disk
	 * 
	 * @return task to execute the disconnect
	 */
	UpdateableTask<Void> createDisconnectTask();

	/**
	 * Creates a task to search for the given term inside the given parents.
	 * 
	 * @param term
	 *            the search term
	 * @param isRegex
	 *            true if term is a regex, otherwise false
	 * @param checkFiles
	 *            true if files should be checked
	 * @param checkFolders
	 *            true if folders should be checked
	 * @param isCaseSensitive
	 *            true if term is case sensitive
	 * @param checkChildren
	 *            true if children and subfolders should be checked
	 * @param parents
	 *            list of parents to search in
	 * @return task to execute the search
	 */
	UpdateableTask<Entry[]> createSearchTask(String term, boolean isRegex,
			boolean checkFiles, boolean checkFolders, boolean isCaseSensitive,
			boolean checkChildren, Entry... parents);
	
	UpdateableTask<Void> createDeleteDiskTask();
}
