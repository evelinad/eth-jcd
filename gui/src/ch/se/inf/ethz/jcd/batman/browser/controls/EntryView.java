package ch.se.inf.ethz.jcd.batman.browser.controls;

import java.util.Comparator;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableColumn.SortType;
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

	private TableColumn<Entry, Entry> nameColumn;
	private GuiState guiState;
	private Directory directory;
	
	public EntryView(final GuiState guiState) {
		this.guiState = guiState;
		guiState.addDirectoryListener(this);
		guiState.addDiskEntryListener(this);
		guiState.setActiveEntryView(this);

		this.setPlaceholder(NO_DISK_LOADED_TEXT);

		getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		nameColumn = new TableColumn<Entry, Entry>("Name");
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
		nameColumn.setComparator(new Comparator<Entry>() {
			@Override
			public int compare(Entry o1, Entry o2) {
				return o1.getPath().getName().toLowerCase()
						.compareTo(o2.getPath().getName().toLowerCase());
			}
		});
		nameColumn.setEditable(true);
		getColumns().add(nameColumn);

		TableColumn<Entry, Long> dateColumn = new TableColumn<>("Last changed");
		dateColumn.setPrefWidth(100);
		dateColumn.setCellValueFactory(new PropertyValueFactory<Entry, Long>(
				"timestamp"));
		dateColumn
				.setCellFactory(new Callback<TableColumn<Entry, Long>, TableCell<Entry, Long>>() {

					@Override
					public TableCell<Entry, Long> call(
							TableColumn<Entry, Long> param) {
						return new TimestampCell(guiState);
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
						return new SizeCell(guiState);
					}
				});
		getColumns().add(sizeColumn);

		setOnKeyPressed(new EventHandler<KeyEvent>() {

			@Override
			public void handle(KeyEvent event) {
				Entry[] selected = getSelectedEntries();

				if (!event.getTarget().equals(EntryView.this)) {
					return;
				}

				if (event.getCode() == KeyCode.R && event.isControlDown()) {
					editSelected();
				}

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
		getSortOrder().add(nameColumn);
		nameColumn.setSortType(SortType.ASCENDING);
	}
	
	public void editSelected () {
		Entry[] selected = getSelectedEntries();
		if (selected.length > 0) {
			setEditable(true);
			edit(getSelectionModel().getSelectedIndex(), nameColumn);
			setEditable(false);
		}

	}
	
	protected void clear() {
		getItems().clear();
	}

	protected void setEntries(Entry[] entries) {
		TableColumn<Entry, ?> sortcolumn = null;
		SortType st = null;
		if (getSortOrder().size() > 0) {
			sortcolumn = (TableColumn<Entry, ?>) getSortOrder().get(0);
			st = sortcolumn.getSortType();
		}
		getItems().addAll(entries);
		if (sortcolumn != null) {
			getSortOrder().add(sortcolumn);
			sortcolumn.setSortType(st);
			sortcolumn.setSortable(true); // This performs a sort
		}
	}

	public void setDirectory(Directory directory) {
		if (directory == null) {
			setPlaceholder(NO_DISK_LOADED_TEXT);
		} else {
			setPlaceholder(NO_ENTRIES_TEXT);
		}
		this.directory = directory;
		clear();
		if (directory != null) {
			if (directory instanceof SearchDirectory) {
				SearchDirectory search = (SearchDirectory) directory;
				final Task<Entry[]> searchTask = guiState.getController()
						.createSearchTask(search.getTerm(), search.isRegex(),
								search.isCheckFiles(), search.isCheckFolders(),
								search.isCaseSensitive(),
								search.isCheckChildren(),
								new Directory(search.getPath()));
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
	}

	public Directory getDirectory() {
		return directory;
	}

	@Override
	public void directoryChanged(Directory directory) {
		setDirectory(directory);
	}

	private boolean entryAddedImpl(Entry entry) {
		if (entry.getPath().getParentPath().pathEquals(directory.getPath())) {
			// The observable list needs to be resorted after inserting
			// A better solution will be available in a newer version
			// ->
			// http://stackoverflow.com/questions/13409350/javafx-tableview-insert-in-observablelist-when-column-sorting-is-active
			TableColumn<Entry, ?> sortcolumn = null;
			SortType st = null;
			if (getSortOrder().size() > 0) {
				sortcolumn = (TableColumn<Entry, ?>) getSortOrder().get(0);
				st = sortcolumn.getSortType();
			}
			getItems().add(entry);
			if (sortcolumn != null) {
				getSortOrder().add(sortcolumn);
				sortcolumn.setSortType(st);
				sortcolumn.setSortable(true); // This performs a sort
			}
			return true;
		}
		return false;
	}

	@Override
	public void entryAdded(Entry entry) {
		entryAddedImpl(entry);
	}

	private boolean entryDeletedImpl(Entry entry) {
		if (getItems().contains(entry)) {
			getItems().remove(entry);
			return true;
		}
		return false;
	}

	@Override
	public void entryDeleted(Entry entry) {
		entryDeletedImpl(entry);
	}

	@Override
	public void entryChanged(final Entry oldEntry, final Entry newEntry) {
		Entry[] selectedEntries = getSelectedEntries();
		boolean selected = getSelectionModel().getSelectedItems().contains(
				oldEntry);
		boolean deleted = entryDeletedImpl(oldEntry);
		boolean added = entryAddedImpl(newEntry);
		getSelectionModel().clearSelection();
		for (Entry entry : selectedEntries) {
			if (!entry.equals(oldEntry)) {
				getSelectionModel().select(entry);
			}
		}
		if (selected && added && deleted) {
			getSelectionModel().select(newEntry);
		}
	}

	public Entry[] getSelectedEntries() {
		ObservableList<Entry> selectedItems = getSelectionModel()
				.getSelectedItems();
		return selectedItems.toArray(new Entry[selectedItems.size()]);
	}

}
