package ch.se.inf.ethz.jcd.batman.browser.controls;

import ch.se.inf.ethz.jcd.batman.browser.GuiState;
import ch.se.inf.ethz.jcd.batman.model.Directory;
import ch.se.inf.ethz.jcd.batman.model.Entry;
import javafx.event.EventHandler;
import javafx.scene.control.TableCell;
import javafx.scene.input.MouseEvent;

public class EntryCell<T, S> extends TableCell<T, S>{

	public EntryCell(final GuiState guiState) {
		setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				if (event.getClickCount() == 2) {
					Entry entry = (Entry) getTableRow().getItem();
					if (entry instanceof Directory) {
						guiState.setCurrentDirectory((Directory) entry);
					}
				}
			}
		});
	}
	
}
