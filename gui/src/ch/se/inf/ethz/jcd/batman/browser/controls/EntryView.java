package ch.se.inf.ethz.jcd.batman.browser.controls;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import javafx.util.Callback;
import ch.se.inf.ethz.jcd.batman.browser.DirectoryListener;
import ch.se.inf.ethz.jcd.batman.browser.DiskEntryListener;
import ch.se.inf.ethz.jcd.batman.browser.GuiState;
import ch.se.inf.ethz.jcd.batman.browser.TaskDialog;
import ch.se.inf.ethz.jcd.batman.browser.util.HostUtil;
import ch.se.inf.ethz.jcd.batman.model.Directory;
import ch.se.inf.ethz.jcd.batman.model.Entry;
import ch.se.inf.ethz.jcd.batman.model.File;
import ch.se.inf.ethz.jcd.batman.model.SearchDirectory;

public class EntryView extends TableView<Entry> implements DirectoryListener,
		DiskEntryListener {

	private static final Text NO_DISK_LOADED_TEXT = new Text("No Disk Loaded.");
	private static final Text NO_ENTRIES_TEXT = new Text("Is Empty.");

	private GuiState guiState;
	private Directory directory;
	private ObservableList<Entry> entryList = FXCollections
			.observableArrayList();

	public EntryView(final GuiState guiState) {
		this.guiState = guiState;
		guiState.addDirectoryListener(this);
		guiState.addDiskEntryListener(this);
		guiState.setActiveEntryView(this);

		this.setPlaceholder(NO_DISK_LOADED_TEXT);

		getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		TableColumn<Entry, Entry> nameColumn = new TableColumn<Entry, Entry>(
				"Name");
		nameColumn.setPrefWidth(300);
		nameColumn
				.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Entry, Entry>, ObservableValue<Entry>>() {

					@Override
					public ObservableValue<Entry> call(
							CellDataFeatures<Entry, Entry> param) {
						return new SimpleObjectProperty<>(param.getValue());
					}
				});
		nameColumn
				.setCellFactory(new Callback<TableColumn<Entry, Entry>, TableCell<Entry, Entry>>() {

					@Override
					public TableCell<Entry, Entry> call(
							TableColumn<Entry, Entry> param) {
						return new NameCell(guiState);
					}
				});

		getColumns().add(nameColumn);

		TableColumn<Entry, String> dateColumn = new TableColumn<>(
				"Last changed");
		dateColumn.setPrefWidth(100);
		dateColumn.setCellValueFactory(new PropertyValueFactory<Entry, String>(
				"timestamp"));
		dateColumn
				.setCellFactory(new Callback<TableColumn<Entry, String>, TableCell<Entry, String>>() {

					@Override
					public TableCell<Entry, String> call(
							TableColumn<Entry, String> param) {
						return new EntryCell<Entry, String>(guiState);
					}
				});
		getColumns().add(dateColumn);

		TableColumn<Entry, Number> sizeColumn = new TableColumn<Entry, Number>(
				"Size");
		sizeColumn.setPrefWidth(100);
		sizeColumn
				.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Entry, Number>, ObservableValue<Number>>() {

					@Override
					public ObservableValue<Number> call(
							CellDataFeatures<Entry, Number> param) {
						Entry entry = param.getValue();
						if (entry instanceof File) {
							return ((File) entry).sizeProperty();
						}
						return null;
					}
				});
		sizeColumn
				.setCellFactory(new Callback<TableColumn<Entry, Number>, TableCell<Entry, Number>>() {

					@Override
					public TableCell<Entry, Number> call(
							TableColumn<Entry, Number> param) {
						return new EntryCell<Entry, Number>(guiState);
					}
				});
		getColumns().add(sizeColumn);

		setOnKeyPressed(new EventHandler<KeyEvent>() {

			@Override
			public void handle(KeyEvent event) {
				Entry[] selected = getSelectedEntries();

				// go inside a directory
				if (event.getCode() == KeyCode.ENTER
						|| event.getCode() == KeyCode.RIGHT) {
					if (selected.length == 1
							&& selected[0] instanceof Directory) {
						event.consume();
						guiState.setCurrentDirectory((Directory) selected[0]);
					}
				}

				// go back in history
				if (event.getCode() == KeyCode.LEFT) {
					event.consume();
					guiState.backToPreviousDirectory();
				}

				// open a file
				if (event.getCode() == KeyCode.ENTER) {
					for (Entry entry : selected) {
						if (entry instanceof File) {
							event.consume();
							HostUtil.openFile((File) entry, guiState);
						}
					}
				}

				// delete a file
				if (event.getCode() == KeyCode.DELETE) {
					event.consume();
					guiState.delete();
				}
			}
		});

		setItems(entryList);
	}

	protected void clear() {
		entryList.clear();
	}

	protected void setEntries(Entry[] entries) {
		entryList.addAll(entries);
		// TODO sort
	}

	public void setDirectory(Directory directory) {
		this.directory = directory;
		clear();
		if (directory != null) {
			if (directory instanceof SearchDirectory) {
				SearchDirectory search = (SearchDirectory) directory;
				final Task<Entry[]> searchTask = guiState.getController().createSearchTask(
						search.getTerm(), search.isRegex(), search.isCheckFiles(), search.isCheckFolders(), 
						search.isCaseSensitive(), search.isCheckChildren(), new Directory(search.getPath()));
				new TaskDialog(guiState, searchTask) {
					protected void succeeded(WorkerStateEvent event) {
						setEntries(searchTask.getValue());
					}
				};
			} else {
				final Task<Entry[]> entriesTask = guiState.getController()
						.createDirectoryEntriesTask(directory);
				new TaskDialog(guiState, entriesTask) {
					protected void succeeded(WorkerStateEvent event) {
						setEntries(entriesTask.getValue());
					}
				};
			}
		}

		if (entryList.size() <= 0) {
			this.setPlaceholder(NO_ENTRIES_TEXT);
		}
	}

	public Directory getDirectory() {
		return directory;
	}

	@Override
	public void directoryChanged(Directory directory) {
		setDirectory(directory);
	}

	@Override
	public void entryAdded(Entry entry) {
		if (entry.getPath().getParentPath().pathEquals(directory.getPath())) {
			entryList.add(entry);
		}
	}

	@Override
	public void entryDeleted(Entry entry) {
		if (entryList.contains(entry)) {
			entryList.remove(entry);
		}
	}

	@Override
	public void entryChanged(Entry oldEntry, Entry newEntry) {
		entryDeleted(oldEntry);
		entryAdded(newEntry);

	}

	public Entry[] getSelectedEntries() {
		ObservableList<Entry> selectedItems = getSelectionModel()
				.getSelectedItems();
		return selectedItems.toArray(new Entry[selectedItems.size()]);
	}

}
