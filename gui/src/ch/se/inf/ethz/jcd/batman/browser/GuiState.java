package ch.se.inf.ethz.jcd.batman.browser;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.scene.control.TreeItem;
import javafx.stage.Stage;
import ch.se.inf.ethz.jcd.batman.browser.controls.DirectoryTree;
import ch.se.inf.ethz.jcd.batman.browser.controls.EntryView;
import ch.se.inf.ethz.jcd.batman.controller.TaskController;
import ch.se.inf.ethz.jcd.batman.model.Directory;
import ch.se.inf.ethz.jcd.batman.model.Entry;
import ch.se.inf.ethz.jcd.batman.model.Path;

public class GuiState {

	private enum LastAction {
		COPY, CUT
	}

	private static final int THREAD_POOL_SIZE = 1;

	private final Stage primaryStage;
	private TaskController controller;
	private State state;
	private final List<StateListener> stateListener = new LinkedList<StateListener>();
	private final List<DirectoryListener> directoryListener = new LinkedList<DirectoryListener>();
	private final List<DiskEntryListener> diskEntryListener = new LinkedList<DiskEntryListener>();
	/*
	 * regarding the PMD warning: We use thr removeLast() method and therefore
	 * it must be of type LinkedList<T> and not one of the implemented
	 * interfaces
	 */
	private final LinkedList<Directory> directoryHistory = new LinkedList<Directory>();
	private int directoryIndex = -1;
	private final ScheduledExecutorService scheduler;
	private Entry[] copiedCutEntries;
	private LastAction lastAction;

	private EntryView activeEntryView;
	private DirectoryTree activeTreeView;
	private boolean lastFocusEntryView;

	public GuiState(Stage primaryStage) {
		this.primaryStage = primaryStage;
		lastFocusEntryView = true;
		state = State.DISCONNECTED;
		scheduler = Executors.newScheduledThreadPool(THREAD_POOL_SIZE);
	}

	public Stage getPrimaryStage() {
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

	public void setActiveEntryView(EntryView entryView) {
		this.activeEntryView = entryView;
		this.activeEntryView.getFocusModel().focusedItemProperty()
				.addListener(new ChangeListener<Entry>() {
					@Override
					public void changed(
							ObservableValue<? extends Entry> observable,
							Entry oldValue, Entry newValue) {
						lastFocusEntryView = true;
					}
				});
	}

	public EntryView getActiveEntryView() {
		return this.activeEntryView;
	}

	public void addDirectoryTree(DirectoryTree directoryTree) {
		this.activeTreeView = directoryTree;
		this.activeTreeView.getFocusModel().focusedItemProperty()
				.addListener(new ChangeListener<TreeItem<String>>() {
					@Override
					public void changed(
							ObservableValue<? extends TreeItem<String>> observable,
							TreeItem<String> oldValue, TreeItem<String> newValue) {
						lastFocusEntryView = false;
					}
				});
	}

	public Entry[] getSelectedEntries() {
		if (activeTreeView != null && !lastFocusEntryView) {
			Path path = new Path(DirectoryTree.getPath(activeTreeView
					.getSelectionModel().getSelectedItem()));
			Directory dir = new Directory(path);
			return new Entry[] { dir };
		} else if (activeEntryView != null) {
			return activeEntryView.getSelectedEntries();
		}

		return new Entry[0];
	}

	public void addStateListener(StateListener listener) {
		if (!stateListener.contains(listener)) {
			stateListener.add(listener);
		}
	}

	public void removeStateListener(StateListener listener) {
		stateListener.remove(listener);
	}

	public boolean hasPreviousDirectory() {
		return directoryIndex > 0;
	}

	public void backToPreviousDirectory() {
		if (hasPreviousDirectory()) {
			directoryIndex--;
			callDirectoryListener(getCurrentDirectory());
		}
	}

	public void forwardToNextDirectoy() {
		if (hasNextDirectory()) {
			directoryIndex++;
			callDirectoryListener(getCurrentDirectory());
		}
	}

	public boolean hasNextDirectory() {
		return directoryIndex < directoryHistory.size() - 1;
	}

	public void setCurrentDirectory(Directory currentDirectory) {
		int removableDirectories = directoryHistory.size() - 1 - directoryIndex;
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

	public Directory getCurrentDirectory() {
		return directoryIndex < 0 ? null : directoryHistory.get(directoryIndex);
	}

	public void addDirectoryListener(DirectoryListener listener) {
		if (!directoryListener.contains(listener)) {
			directoryListener.add(listener);
		}
	}

	public void removeDirectoryListener(DirectoryListener listener) {
		directoryListener.remove(listener);
	}

	public State getState() {
		return state;
	}

	public void setState(State newState) {
		if (newState != state) {
			State oldState = state;
			state = newState;
			for (StateListener listener : stateListener) {
				listener.stateChanged(oldState, newState);
			}
		}
	}

	public void submitTask(Task<?> task) {
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
		Task<Void> deleteEntriesTask = getController().createDeleteEntriesTask(
				selectedEntries);
		new TaskDialog(this, deleteEntriesTask);
	}

	public void destroy() {
		if (controller == null) {
			scheduler.shutdownNow();
		} else {
			Task<Void> disconnectTask = getController().createDisconnectTask();
			new TaskDialog(this, disconnectTask) {
				private void shutdownScheduler() {
					Platform.runLater(new Runnable() {

						@Override
						public void run() {
							scheduler.shutdownNow();
						}
					});
				}

				protected void succeeded(WorkerStateEvent event) {
					shutdownScheduler();
				}

				@Override
				protected void failed(WorkerStateEvent event) {
					super.failed(event);
					shutdownScheduler();
				}
			};
			controller = null;
		}
	}

	@Override
	protected void finalize() throws Throwable {
		destroy();
		super.finalize();
	}

	public void copy() {
		lastAction = LastAction.COPY;
		copiedCutEntries = getSelectedEntries();
	}

	public void cut() {
		lastAction = LastAction.CUT;
		copiedCutEntries = getSelectedEntries();
	}

	public void paste() {
		if (copiedCutEntries != null && copiedCutEntries.length > 0) {
			Path[] destinationPaths = new Path[copiedCutEntries.length];
			Path currentDirectoryPath = getCurrentDirectory().getPath();
			for (int i = 0; i < copiedCutEntries.length; i++) {
				destinationPaths[i] = new Path(currentDirectoryPath,
						copiedCutEntries[i].getPath().getName());
			}
			Task<Void> task = null;
			if (LastAction.COPY == lastAction) {
				task = getController().createCopyTask(copiedCutEntries,
						destinationPaths);
			} else if (LastAction.CUT == lastAction) {
				task = getController().createMoveTask(copiedCutEntries,
						destinationPaths);
				lastAction = null;
				copiedCutEntries = null;
			}
			if (task != null) {
				new TaskDialog(this, task);
			}
		}
	}

}
