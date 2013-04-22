package ch.se.inf.ethz.jcd.batman.browser.controls;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import ch.se.inf.ethz.jcd.batman.browser.GuiState;
import ch.se.inf.ethz.jcd.batman.browser.TaskDialog;
import ch.se.inf.ethz.jcd.batman.browser.images.ImageResource;
import ch.se.inf.ethz.jcd.batman.model.Directory;
import ch.se.inf.ethz.jcd.batman.model.Entry;
import ch.se.inf.ethz.jcd.batman.model.Path;

public class NameCell extends EntryCell<Entry, Entry> {

	private Entry editingItem;
	private TextField textField;
	
	public NameCell(GuiState guiState) {
		super(guiState);
	}
	
	private String getName(Entry item) {
		return (item == null) ? null : item.getPath().getName();
	}
	
    @Override
    public void startEdit() {
    	if (!isEmpty()) {
            editingItem = getItem();
    		super.startEdit();
            createTextField();
            setText(null);
            setGraphic(textField);
            textField.selectAll();
        }
    }
    
    @Override
    public void cancelEdit() {
    	super.cancelEdit();
        createContent(getItem());
    }

    private void createTextField() {
        textField = new TextField(getName(getItem()));
        textField.setMinWidth(this.getWidth() - this.getGraphicTextGap()* 2);
        textField.focusedProperty().addListener(new ChangeListener<Boolean>(){
            @Override
            public void changed(ObservableValue<? extends Boolean> arg0, 
                Boolean arg1, Boolean arg2) {
                if (!arg2) {
                	Entry clone = (Entry) editingItem.clone();
                    clone.getPath().changeName(textField.getText());
                	commitEdit(clone);
                }
            }
        });
    }
	
    @Override
    public void commitEdit(Entry newValue) {
    	super.commitEdit(newValue);
    	if (!newValue.getPath().equals(editingItem.getPath())) {
		    Entry[] sourceEntries = new Entry[] {editingItem};
			Path[] destinationPaths = new Path[] {newValue.getPath()};
			Task<Void> moveTask = guiState.getController().createMoveTask(sourceEntries, destinationPaths);
			new TaskDialog(guiState, moveTask);
    	}
    }
    
	@Override
	protected void updateItem(Entry item, boolean empty) {
		super.updateItem(item, empty);
		if (item == null) {
			setText(null);
			setGraphic(null);
		} else {
			setItem(item);
			if (isEditing()) {
                if (textField != null) {
                    textField.setText(getName(item));
                }
                setText(null);
                setGraphic(textField);
            } else {
            	createContent(item);
    		}
		}
	}
	
	private void createContent (Entry item) {
		setText(getName(item));
		
		ImageView imageview = new ImageView();
		imageview.setFitHeight(16);
		imageview.setFitWidth(16);
		if (item instanceof Directory) {
			imageview.setImage(ImageResource.getImageResource().folderImage());
		} else {
			imageview.setImage(ImageResource.getImageResource().fileImage()); 
		}

		setGraphic(imageview);

	}
	
	protected void handleMouseClick (MouseEvent event) {
		if (!event.isConsumed() && event.isControlDown() && event.getClickCount() == 2) {
			startEdit();
			event.consume();
		}
		super.handleMouseClick(event);
	}
}
