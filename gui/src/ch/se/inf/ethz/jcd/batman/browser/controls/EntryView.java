package ch.se.inf.ethz.jcd.batman.browser.controls;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import ch.se.inf.ethz.jcd.batman.browser.GuiState;
import ch.se.inf.ethz.jcd.batman.browser.TaskDialog;
import ch.se.inf.ethz.jcd.batman.model.Directory;
import ch.se.inf.ethz.jcd.batman.model.Entry;
import ch.se.inf.ethz.jcd.batman.model.File;

public class EntryView extends TableView<Entry> {
	
	private GuiState guiState;
	private Directory directory;
	private ObservableList<Entry> entryList = FXCollections.observableArrayList();
	
	public EntryView(GuiState guiState) {
		this.guiState = guiState;
		
		TableColumn<Entry, String> nameColumn = new TableColumn<Entry, String>("Name");
		nameColumn.setMinWidth(100);
		nameColumn.setCellValueFactory(new PropertyValueFactory<Entry, String>("name"));
		getColumns().add(nameColumn);
		
		TableColumn<Entry, Number> sizeColumn = new TableColumn<Entry, Number>("Size");
		sizeColumn.setMinWidth(100);
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
		getColumns().add(sizeColumn);
		
		TableColumn<Entry, String> dateColumn = new TableColumn<>("Last changed");
		dateColumn.setMinWidth(100);
		nameColumn.setCellValueFactory(new PropertyValueFactory<Entry, String>("name"));
		getColumns().add(dateColumn);
		setItems(entryList);
	}
	
	public void setDirectory(Directory directory) {
		this.directory = directory;
		entryList.clear();
		final Task<Entry[]> entriesTask = guiState.getController().createDirectoryEntriesTask(directory);
		new TaskDialog(guiState, entriesTask) {
			protected void succeeded(WorkerStateEvent event) {
				Entry[] entries = entriesTask.getValue();
				entryList.addAll(entries);
			}
		};
	}
	
	public Directory getDirectory() {
		return directory;
	}
	
}
