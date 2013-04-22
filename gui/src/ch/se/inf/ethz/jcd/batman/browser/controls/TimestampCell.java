package ch.se.inf.ethz.jcd.batman.browser.controls;

import java.text.SimpleDateFormat;
import java.util.Date;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import ch.se.inf.ethz.jcd.batman.browser.GuiState;
import ch.se.inf.ethz.jcd.batman.model.Entry;

public class TimestampCell extends EntryCell<Entry, Long> {

	public TimestampCell(GuiState guiState) {
		super(guiState);
	}

	@Override
	protected void updateItem(Long item, boolean empty) {
		if(item!=null){                            
			HBox box= new HBox();
			box.setSpacing(10);
			box.setAlignment(Pos.CENTER_LEFT);
			Label nameLabel = new Label(SimpleDateFormat.getDateInstance().format(new Date(item)));
			box.getChildren().addAll(nameLabel); 
			setGraphic(box);
		}
	}
}
