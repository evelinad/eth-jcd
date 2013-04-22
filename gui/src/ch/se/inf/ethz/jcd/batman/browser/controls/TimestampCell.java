package ch.se.inf.ethz.jcd.batman.browser.controls;

import java.text.SimpleDateFormat;
import java.util.Date;

import ch.se.inf.ethz.jcd.batman.browser.GuiState;
import ch.se.inf.ethz.jcd.batman.model.Entry;

public class TimestampCell extends EntryCell<Entry, Long> {

	public TimestampCell(GuiState guiState) {
		super(guiState);
	}

	@Override
	protected void updateItem(Long item, boolean empty) {
		if (item != null) {
			setText(SimpleDateFormat.getDateInstance().format(new Date(item)));
		} else {
			setText(null);
		}
	}
}
