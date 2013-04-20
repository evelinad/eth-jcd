package ch.se.inf.ethz.jcd.batman.browser.controls;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import ch.se.inf.ethz.jcd.batman.browser.DirectoryListener;
import ch.se.inf.ethz.jcd.batman.browser.GuiState;
import ch.se.inf.ethz.jcd.batman.browser.TaskDialog;
import ch.se.inf.ethz.jcd.batman.model.Directory;
import ch.se.inf.ethz.jcd.batman.model.Entry;
import ch.se.inf.ethz.jcd.batman.model.File;

public class EntryView extends TableView<Entry> implements DirectoryListener {
	
	private GuiState guiState;
	private Directory directory;
	private ObservableList<Entry> entryList = FXCollections.observableArrayList();
	
	public EntryView(final GuiState guiState) {
		this.guiState = guiState;
		guiState.addDirectoryListener(this);
		
		TableColumn<Entry, Entry> nameColumn = new TableColumn<Entry, Entry>("Name");
		nameColumn.setPrefWidth(300);
		nameColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Entry,Entry>, ObservableValue<Entry>>() {

			@Override
			public ObservableValue<Entry> call(CellDataFeatures<Entry, Entry> param) {
				return new SimpleObjectProperty<>(param.getValue());
			}
		});
		nameColumn.setCellFactory(new Callback<TableColumn<Entry, Entry>, TableCell<Entry, Entry>>() {

			@Override
			public TableCell<Entry, Entry> call(TableColumn<Entry, Entry> param) {
				return new NameCell(guiState);
			}
		});
				
		getColumns().add(nameColumn);
		
		TableColumn<Entry, String> dateColumn = new TableColumn<>("Last changed");
		dateColumn.setPrefWidth(100);
		dateColumn.setCellValueFactory(new PropertyValueFactory<Entry, String>("timestamp"));
		dateColumn.setCellFactory(new Callback<TableColumn<Entry,String>, TableCell<Entry,String>>() {

			@Override
			public TableCell<Entry, String> call(TableColumn<Entry, String> param) {
				return new EntryCell<Entry, String>(guiState);
			}
		});
		getColumns().add(dateColumn);

		TableColumn<Entry, Number> sizeColumn = new TableColumn<Entry, Number>("Size");
		sizeColumn.setPrefWidth(100);
		sizeColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Entry,Number>, ObservableValue<Number>>() {

			@Override
			public ObservableValue<Number> call(CellDataFeatures<Entry, Number> param) {
				Entry entry = param.getValue();
				if (entry instanceof File) {
					return ((File) entry).sizeProperty();
				}
				return null;
			}
		});
		sizeColumn.setCellFactory(new Callback<TableColumn<Entry, Number>, TableCell<Entry,Number>>() {

			@Override
			public TableCell<Entry, Number> call(TableColumn<Entry, Number> param) {
				return new EntryCell<Entry, Number>(guiState);
			}
		});
		getColumns().add(sizeColumn);
		
		
		setItems(entryList);
	}
	
	public void setDirectory(Directory directory) {
		this.directory = directory;
		entryList.clear();
		if (directory != null) {
			final Task<Entry[]> entriesTask = guiState.getController().createDirectoryEntriesTask(directory);
			new TaskDialog(guiState, entriesTask) {
				protected void succeeded(WorkerStateEvent event) {
					Entry[] entries = entriesTask.getValue();
					entryList.addAll(entries);
				}
			};
		}
	}
	
	public Directory getDirectory() {
		return directory;
	}

	@Override
	public void directoryChanged(Directory directory) {
		setDirectory(directory);
	}
	
}
