package ch.se.inf.ethz.jcd.batman.browser.controls;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
		setEditable(true);
	}
	
	private String getName(Entry item) {
		return (item == null) ? null : item.getPath().getName();
	}
	
    @Override
    public void startEdit() {
    	if (!isEmpty()) {
    		editingItem = getItem();
            createTextField();
    		super.startEdit();
            setText(null);
            setGraphic(textField);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    textField.requestFocus();
                }
            });
        }
    }
    
    @Override
    public void cancelEdit() {
    	textField = null;
    	super.cancelEdit();
        createContent(getItem());
    }

    private void createTextField() {
    	textField = new TextField(getName(getItem()));
        textField.setMinWidth(this.getWidth() - this.getGraphicTextGap()* 2);
        textField.setOnKeyPressed(new EventHandler<KeyEvent>() {

			@Override
			public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.ENTER && textField != null) {
					Entry clone = (Entry) editingItem.clone();
	                clone.getPath().changeName(textField.getText());
	                commitEdit(clone);
	            	event.consume();
				} else if (event.getCode() == KeyCode.ESCAPE && textField != null) {
                    cancelEdit();
				}
			}
		});
        textField.focusedProperty().addListener(new ChangeListener<Boolean>(){
            @Override
            public void changed(ObservableValue<? extends Boolean>  observable, Boolean oldValue, Boolean newValue) {
            	if (!newValue && textField != null) {
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
        textField = null;
    	if (!newValue.getPath().equals(editingItem.getPath())) {
		    Entry[] sourceEntries = new Entry[] {editingItem};
			Path[] destinationPaths = new Path[] {newValue.getPath()};
			Task<Void> moveTask = guiState.getController().createMoveTask(sourceEntries, destinationPaths);
			new TaskDialog(guiState, moveTask);
    	}
    	createContent(editingItem);
    }
    
	@Override
	protected void updateItem(Entry item, boolean empty) {
		super.updateItem(item, empty);
		if (item == null) {
			setText(null);
			setGraphic(null);
		} else {
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
}
