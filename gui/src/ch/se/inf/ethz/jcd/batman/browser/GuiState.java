package ch.se.inf.ethz.jcd.batman.browser;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javafx.concurrent.Task;

import ch.se.inf.ethz.jcd.batman.browser.controls.EntryView;
import ch.se.inf.ethz.jcd.batman.controller.TaskController;
import ch.se.inf.ethz.jcd.batman.model.Directory;

public class GuiState {

	private static final int THREAD_POOL_SIZE = 1;
	
	private TaskController controller;
	private State state;
	private List<StateListener> stateListener = new LinkedList<StateListener>();
	private List<DirectoryListener> directoryListener = new LinkedList<DirectoryListener>();
	private int directoryIndex = -1;
	private LinkedList<Directory> directoryHistory = new LinkedList<Directory>();
	private ScheduledExecutorService scheduler;
	
	public GuiState() {
		state = State.DISCONNECTED;
		scheduler = Executors.newScheduledThreadPool(THREAD_POOL_SIZE);
	}
	
	public TaskController getController() {
		return controller;
	}

	public void setController(TaskController controller) {
		directoryHistory.clear();
		directoryIndex = -1;
		this.controller = controller;
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
	
	@Override
	protected void finalize() throws Throwable {
		scheduler.shutdownNow();
		super.finalize();
	}
	
}
