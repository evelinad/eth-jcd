package ch.se.inf.ethz.jcd.batman.browser;

import java.net.URI;
import java.net.URISyntaxException;

import ch.se.inf.ethz.jcd.batman.controller.TaskControllerFactory;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

public class DownloadDiskDialog extends ModalDialog {

	private final Label localHostLabel = new Label("Host");
	private final Label localDiskPathLabel = new Label("Path");
	private final TextField localHostField = new TextField();;
	private final TextField localDiskPathField = new TextField();;
	
	public DownloadDiskDialog() {
		super();
		setTitle("Download disk");
		
		EventHandler<KeyEvent> closeEventHandler = new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.ENTER) {
					setCloseReason(CloseReason.OK);
					close();
				}
			}
		};
		localHostField.setOnKeyPressed(closeEventHandler);
		localDiskPathField.setOnKeyPressed(closeEventHandler);
		
		getContainer().addRow(0, localHostLabel, localHostField);
		getContainer().addRow(1, localDiskPathLabel, localDiskPathField);
		
		Button okButton = new Button("Connect");
		okButton.setDefaultButton(true);
		okButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				setCloseReason(CloseReason.OK);
				close();
			}
		});

		Button cancelButton = new Button("Cancel");
		cancelButton.setCancelButton(true);
		cancelButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				setCloseReason(CloseReason.CANCEL);
				close();
			}
		});
		getContainer().addRow(2, okButton, cancelButton);

		localHostField.requestFocus();
	}

	public URI getLocalDiskUri() throws URISyntaxException {
		return new URI(TaskControllerFactory.REMOTE_SCHEME + "://" + localHostField.getText() + "?" + localDiskPathField.getText());
	}
}
