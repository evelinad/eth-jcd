package ch.se.inf.ethz.jcd.batman.browser;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javafx.concurrent.Task;
import javafx.stage.Stage;
import ch.se.inf.ethz.jcd.batman.browser.controls.EntryView;
import ch.se.inf.ethz.jcd.batman.controller.TaskController;
import ch.se.inf.ethz.jcd.batman.model.Directory;
import ch.se.inf.ethz.jcd.batman.model.Entry;

public class GuiState {

	private static final int THREAD_POOL_SIZE = 1;
	
	private Stage primaryStage;
	private TaskController controller;
	private State state;
	private List<StateListener> stateListener = new LinkedList<StateListener>();
	private List<DirectoryListener> directoryListener = new LinkedList<DirectoryListener>();
	private List<DiskEntryListener> diskEntryListener = new LinkedList<DiskEntryListener>();
	private LinkedList<Directory> directoryHistory = new LinkedList<Directory>();
	private int directoryIndex = -1;
	private ScheduledExecutorService scheduler;

	private EntryView activeEntryView;
	
	public GuiState(Stage primaryStage) {
		this.primaryStage = primaryStage;
		state = State.DISCONNECTED;
		scheduler = Executors.newScheduledThreadPool(THREAD_POOL_SIZE);
	}
	
	public Stage getPrimaryStage () {
		return primaryStage;
	}
	
	public TaskController getController() {
		return controller;
	}

	public void setController(TaskController controller) {
		if (this.controller != null) {
			for (DiskEntryListener listener : diskEntryListener) {
				this.controller.removeDiskEntryListener(listener);
			}
		}
		directoryHistory.clear();
		directoryIndex = -1;
		this.controller = controller;
		if (this.controller != null) {
			for (DiskEntryListener listener : diskEntryListener) {
				this.controller.addDiskEntryListener(listener);
			}
		}
	}
	
	public void setActiveEntryView (EntryView entryView) {
		this.activeEntryView = entryView;
	}
	
	public Entry[] getSelectedEntries () {
		if (activeEntryView != null) {
			return activeEntryView.getSelectedEntries();
		}
		return null;
	}
	
	public void addStateListener(StateListener listener) {
		if (!stateListener.contains(listener)) {
			stateListener.add(listener);
		}
	}
	
	public void removeStateListener(StateListener listener) {
		stateListener.remove(listener);
	}
	
	public boolean hasPreviousDirectory () {
		return directoryIndex > 0;
	}
	
	public void backToPreviousDirectory () {
		if (hasPreviousDirectory()) {
			directoryIndex--;
			callDirectoryListener(getCurrentDirectory());
		}
	}
	
	public void forwardToNextDirectoy () {
		if (hasNextDirectory()) {
			directoryIndex++;
			callDirectoryListener(getCurrentDirectory());
		}
	}
	
	public boolean hasNextDirectory () {
		return directoryIndex < directoryHistory.size()-1;
	}
	
	public void setCurrentDirectory (Directory currentDirectory) {
		int removableDirectories = directoryHistory.size()-1 - directoryIndex;
		for (int i = 0; i < removableDirectories; i++) {
			directoryHistory.removeLast();
		}
		directoryHistory.add(currentDirectory);
		directoryIndex++;
		callDirectoryListener(currentDirectory);
	}
	
	private void callDirectoryListener(Directory currentDirectory) {
		for (DirectoryListener listener : directoryListener) {
			listener.directoryChanged(currentDirectory);
		}
	}
	
	public Directory getCurrentDirectory () {
		return (directoryIndex < 0 ) ? null : directoryHistory.get(directoryIndex);
	}
	
	public void addDirectoryListener(DirectoryListener listener) {
		if (!directoryListener.contains(listener)) {
			directoryListener.add(listener);
		}
	}
	
	public void removeDirectoryListener(DirectoryListener listener) {
		directoryListener.remove(listener);
	}
	
	public State getState () {
		return state;
	}
	
	public void setState (State newState) {
		if (newState != state) {
			State oldState = state;
			state = newState;
			for (StateListener listener : stateListener) {
				listener.stateChanged(oldState, newState);
			}
		}
	}
	
	public void submitTask (Task<?> task) {
		scheduler.submit(task);
	}
	

	public void addDiskEntryListener(DiskEntryListener listener) {
		if (!diskEntryListener.contains(listener)) {
			diskEntryListener.add(listener);
			if (controller != null) {
				controller.addDiskEntryListener(listener);
			}
		}
	}

	public void removeDiskEntryListener(DiskEntryListener listener) {
		diskEntryListener.remove(listener);
		if (controller != null) {
			controller.addDiskEntryListener(listener);
		}
	}
	
	public void delete() {
		Entry[] selectedEntries = getSelectedEntries();
		Task<Void> deleteEntriesTask = getController()
				.createDeleteEntriesTask(selectedEntries);
		new TaskDialog(this, deleteEntriesTask);
	}
	
	@Override
	protected void finalize() throws Throwable {
		scheduler.shutdownNow();
		super.finalize();
	}
	
}
