package ch.se.inf.ethz.jcd.batman.browser.controls;

import ch.se.inf.ethz.jcd.batman.browser.GuiState;
import ch.se.inf.ethz.jcd.batman.model.Directory;
import ch.se.inf.ethz.jcd.batman.model.Entry;
import javafx.event.EventHandler;
import javafx.scene.control.TableCell;
import javafx.scene.input.MouseEvent;

public class EntryCell<T, S> extends TableCell<T, S>{

	protected final GuiState guiState;
	
	public EntryCell(final GuiState guiState) {
		this.guiState = guiState;
		setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				handleMouseClick(event);
			}
		});
	}
	
	protected void handleMouseClick (MouseEvent event) {
		if (!event.isConsumed() && event.getClickCount() == 2) {
			Entry entry = (Entry) getTableRow().getItem();
			if (entry instanceof Directory) {
				guiState.setCurrentDirectory((Directory) entry);
			}
			event.consume();
		}
	}
	
}
