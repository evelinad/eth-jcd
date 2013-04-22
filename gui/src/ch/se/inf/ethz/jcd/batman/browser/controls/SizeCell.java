package ch.se.inf.ethz.jcd.batman.browser.controls;

import javafx.geometry.Pos;
import ch.se.inf.ethz.jcd.batman.browser.GuiState;
import ch.se.inf.ethz.jcd.batman.model.Entry;

public class SizeCell extends EntryCell<Entry, Number> {

	//Based on http://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java
	private static String humanReadableByteCount(long bytes, boolean si) {
	    int unit = si ? 1000 : 1024;
	    if (bytes < unit) return bytes + " B";
	    int exp = (int) (Math.log(bytes) / Math.log(unit));
	    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
	    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
	
	public SizeCell(GuiState guiState) {
		super(guiState);
		setAlignment(Pos.CENTER_RIGHT);
	}
	
	@Override
	protected void updateItem(Number item, boolean empty) {
		if(item!=null){
			setText(humanReadableByteCount(item.longValue(), true));
		} else {
			setText(null);
		}
	}
}
