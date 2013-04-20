package ch.se.inf.ethz.jcd.batman.browser.controls;

import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import ch.se.inf.ethz.jcd.batman.browser.GuiState;
import ch.se.inf.ethz.jcd.batman.browser.images.ImageResource;
import ch.se.inf.ethz.jcd.batman.model.Directory;
import ch.se.inf.ethz.jcd.batman.model.Entry;

public class NameCell extends EntryCell<Entry, Entry> {

	public NameCell(GuiState guiState) {
		super(guiState);
	}

	@Override
	protected void updateItem(Entry item, boolean empty) {
		if(item!=null){                            
			HBox box= new HBox();
			box.setSpacing(10) ;
			Label nameLabel = new Label(item.getPath().getName());
			
			ImageView imageview = new ImageView();
			imageview.setFitHeight(16);
			imageview.setFitWidth(16);
			if (item instanceof Directory) {
				imageview.setImage(ImageResource.getImageResource().folderImage());
			} else {
				imageview.setImage(ImageResource.getImageResource().fileImage()); 
			}

			box.getChildren().addAll(imageview, nameLabel); 
			setGraphic(box);
		}
	}
	
}
