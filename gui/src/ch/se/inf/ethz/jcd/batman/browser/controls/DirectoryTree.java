package ch.se.inf.ethz.jcd.batman.browser.controls;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import ch.se.inf.ethz.jcd.batman.browser.DirectoryListener;
import ch.se.inf.ethz.jcd.batman.browser.DiskEntryListener;
import ch.se.inf.ethz.jcd.batman.browser.GuiState;
import ch.se.inf.ethz.jcd.batman.browser.State;
import ch.se.inf.ethz.jcd.batman.browser.StateListener;
import ch.se.inf.ethz.jcd.batman.browser.TaskDialog;
import ch.se.inf.ethz.jcd.batman.browser.images.ImageResource;
import ch.se.inf.ethz.jcd.batman.model.Directory;
import ch.se.inf.ethz.jcd.batman.model.Entry;
import ch.se.inf.ethz.jcd.batman.model.Path;

public class DirectoryTree extends TreeView<String> implements DiskEntryListener, StateListener, DirectoryListener {

	protected static String getPath(TreeItem<String> treeItem) {
		if (treeItem == null) {
			return "";
		}
    	TreeItem<String> parent = treeItem.getParent();
    	if (parent == null) {
        	return treeItem.getValue();	
    	} else {
    		String parentPath = getPath(parent);
    		if (!parentPath.endsWith(Path.SEPERATOR)) {
    			parentPath += Path.SEPERATOR;
    		}
    		return parentPath + treeItem.getValue();
    	}
    }
	
	public class DirectoryTreeItem extends TreeItem<String> {

		public DirectoryTreeItem(String value) {
			super(value, new ImageView(ImageResource.getImageResource().folderImage()));
		}
		
        private boolean hasChildrenLoaded = false;
        
        public boolean hasChildrenLoaded () {
        	return hasChildrenLoaded;
        }
        
        @Override
        public ObservableList<TreeItem<String>> getChildren() {
            if (!hasChildrenLoaded) {
                hasChildrenLoaded = true;
                super.getChildren().setAll(buildChildren(this));
            }
            return super.getChildren();
        }

        @Override
        public boolean isLeaf() {
            return (getChildren() == null) ? true : getChildren().isEmpty();
        }
        
        private ObservableList<TreeItem<String>> buildChildren(final TreeItem<String> treeItem) {
        	Path path = new Path(getPath(treeItem));
        	final Task<Entry[]> entriesTask = guiState.getController().createDirectoryEntriesTask(new Directory(path));
        	final ObservableList<TreeItem<String>> childDirectories = FXCollections.observableArrayList();
			new TaskDialog(guiState, entriesTask) {
        		protected void succeeded(WorkerStateEvent event) {
        			Entry[] entries = entriesTask.getValue();
        			if (entries != null) {
        				for (Entry entry : entries) {
        					if (entry instanceof Directory) {
        						addChild(DirectoryTreeItem.this, new DirectoryTreeItem(entry.getPath().getName()));
        					}
        				}
        			}
        		}
        	};
        	return childDirectories;
        }
        
	}
	
	private TreeItem<String> root;
	private GuiState guiState;
	private boolean selectionChangeListenerEnabled = true;
	
	public DirectoryTree(final GuiState guiState) {
		this.guiState = guiState;
		guiState.addStateListener(this);
		guiState.addDiskEntryListener(this);
		guiState.addDirectoryListener(this);
		
		getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<String>>() {

			@Override
			public void changed(
					ObservableValue<? extends TreeItem<String>> observable,
					TreeItem<String> oldValue, TreeItem<String> newValue) {
				if (selectionChangeListenerEnabled) {
					if (newValue != null) {
						Directory currentDirectory = guiState.getCurrentDirectory();
						Path itemPath = new Path(getPath(newValue));
						if (currentDirectory != null && !currentDirectory.getPath().equals(itemPath)) {
							guiState.setCurrentDirectory(new Directory(itemPath));
						}
					}
				}
			}
        });
	}

	public void addEntry (Entry entry, DirectoryTreeItem newEntry) {
		if (!(entry instanceof Directory)) {
			return;
		}
		String[] split = entry.getPath().split();
		if (split == null || split.length < 2) {
			return;
		}
		
		TreeItem<String> currentItem = root;
		for (int i = 1; i < split.length - 1; i++) {
			boolean found = false;
			if (currentItem != null && ((DirectoryTreeItem) currentItem).hasChildrenLoaded()) {
				for (TreeItem<String> child : currentItem.getChildren()) {
					if (child.getValue().equals(split[i])) {
						currentItem = child;
						found = true;
					}
				}
			}
			if (!found) {
				currentItem = null;
			}
		}
		if (currentItem != null) {
			addChild(currentItem, newEntry);
		}
	}
	
	private void addChild (TreeItem<String> parent, TreeItem<String> child) {
		//Because the selection changes for some reason if a new child is added,
		//the current selection has to be stored and set back after the child was added.
		TreeItem<String> selectedItem = getSelectionModel().getSelectedItem();
		selectionChangeListenerEnabled = false;
		parent.getChildren().add(child);
		getSelectionModel().select(selectedItem);
		selectionChangeListenerEnabled = true;
	}
	
	private void removeChild (TreeItem<String> parent, TreeItem<String> child) {
		//Because the selection changes for some reason if a child is removed,
		//the current selection has to be stored and set back after the child was removed.
		TreeItem<String> selectedItem = getSelectionModel().getSelectedItem();
		selectionChangeListenerEnabled = false;
		parent.getChildren().remove(child);
		if (selectedItem.equals(child)) {
			selectionChangeListenerEnabled = true;
			getSelectionModel().select(parent);
		} else {
			getSelectionModel().select(selectedItem);
			selectionChangeListenerEnabled = true;
		}
	}
	
	@Override
	public void entryAdded(Entry entry) {
		addEntry(entry, new DirectoryTreeItem(entry.getPath().getName()));
	}

	private TreeItem<String> deleteEntry (Entry entry) {
		if (!(entry instanceof Directory)) {
			return null;
		}
		String[] split = entry.getPath().split();
		if (split == null || split.length < 2) {
			return null;
		}
		
		TreeItem<String> currentItem = root;
		for (int i = 1; i < split.length; i++) {
			boolean found = false;
			if (currentItem != null && ((DirectoryTreeItem) currentItem).hasChildrenLoaded()) {
				for (TreeItem<String> child : currentItem.getChildren()) {
					if (child.getValue().equals(split[i])) {
						currentItem = child;
						found = true;
					}
				}
			}
			if (!found) {
				currentItem = null;
			}
		}
		if (currentItem != null) {
			removeChild(currentItem.getParent(), currentItem);
		}
		return currentItem;
	}
	
	@Override
	public void entryDeleted(Entry entry) {
		deleteEntry(entry);
	}

	@Override
	public void entryChanged(Entry oldEntry, Entry newEntry) {
		TreeItem<String> deletedEntry = deleteEntry(oldEntry);
		if (deletedEntry == null) {
			addEntry(newEntry, new DirectoryTreeItem(newEntry.getPath().getName()));
		} else {
			deletedEntry.setValue(newEntry.getPath().getName());
			addEntry(newEntry, (DirectoryTreeItem) deletedEntry);
		}
	}

	private DirectoryTreeItem createRoot() {
		return new DirectoryTreeItem(Path.SEPERATOR);
	}
	
	@Override
	public void stateChanged(State oldState, State newState) {
		if (newState == State.DISCONNECTED) {
			root = null;
			setRoot(null);
		} else if (newState == State.CONNECTED) {
			root = createRoot();
			root.setExpanded(true);
			setRoot(root);
		}
	}

	public void setSelected(TreeItem<String> item) {
		getSelectionModel().select(item);
	}
	
	@Override
	public void directoryChanged(Directory directory) {
		if (directory != null) {
			String[] split = directory.getPath().split();
			if (split == null || split.length < 2) {
				setSelected(root);
			}
			TreeItem<String> currentItem = root;
			for (int i = 1; i < split.length; i++) {
				boolean found = false;
				for (TreeItem<String> treeItem : currentItem.getChildren()) {
					if (split[i].equals(treeItem.getValue())) {
						currentItem = treeItem;
						found = true;
						break;
					}
				}
				if (!found) {
					currentItem = null;
					break;
				}
			}
			if (currentItem != null) {
				setSelected(currentItem);
			}
		}
	}
	
}
