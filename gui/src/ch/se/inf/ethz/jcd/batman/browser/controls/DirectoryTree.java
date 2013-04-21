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
import ch.se.inf.ethz.jcd.batman.browser.DiskEntryListener;
import ch.se.inf.ethz.jcd.batman.browser.GuiState;
import ch.se.inf.ethz.jcd.batman.browser.State;
import ch.se.inf.ethz.jcd.batman.browser.StateListener;
import ch.se.inf.ethz.jcd.batman.browser.TaskDialog;
import ch.se.inf.ethz.jcd.batman.browser.images.ImageResource;
import ch.se.inf.ethz.jcd.batman.model.Directory;
import ch.se.inf.ethz.jcd.batman.model.Entry;
import ch.se.inf.ethz.jcd.batman.model.Path;

public class DirectoryTree extends TreeView<String> implements DiskEntryListener, StateListener {

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
		
        private boolean isFirstTimeChildren = true;
         
        @Override
        public ObservableList<TreeItem<String>> getChildren() {
            if (isFirstTimeChildren) {
                isFirstTimeChildren = false;
                super.getChildren().setAll(buildChildren(this));
            }
            return super.getChildren();
        }

        @Override
        public boolean isLeaf() {
            return (getChildren() == null) ? true : getChildren().isEmpty();
        }
        
        private ObservableList<TreeItem<String>> buildChildren(TreeItem<String> treeItem) {
        	Path path = new Path(getPath(treeItem));
        	final Task<Entry[]> entriesTask = guiState.getController().createDirectoryEntriesTask(new Directory(path));
        	final ObservableList<TreeItem<String>> childDirectories = FXCollections.observableArrayList();
			new TaskDialog(guiState, entriesTask) {
        		protected void succeeded(WorkerStateEvent event) {
        			Entry[] entries = entriesTask.getValue();
        			if (entries != null) {
        				for (Entry entry : entries) {
        					if (entry instanceof Directory) {
        						getChildren().add(new DirectoryTreeItem(entry.getPath().getName()));
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
	
	public DirectoryTree(final GuiState guiState) {
		this.guiState = guiState;
		guiState.addStateListener(this);
		guiState.addDiskEntryListener(this);
		
		getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<String>>() {

			@Override
			public void changed(
					ObservableValue<? extends TreeItem<String>> observable,
					TreeItem<String> oldValue, TreeItem<String> newValue) {
				if (newValue != null) {
					Directory currentDirectory = guiState.getCurrentDirectory();
					Path itemPath = new Path(getPath(newValue));
					if (!currentDirectory.getPath().equals(itemPath)) {
						guiState.setCurrentDirectory(new Directory(itemPath));
					}
				}
			}
        });
	}

	@Override
	public void entryAdded(Entry entry) {
		// TODO Auto-generated method stub
	}

	@Override
	public void entryDeleted(Entry entry) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void entryChanged(Entry oldEntry, Entry newEntry) {
		// TODO Auto-generated method stub
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
			setRoot(root);
			root.setExpanded(true);
		}
	}
	
}
