package ch.se.inf.ethz.jcd.batman.browser;

import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

public class RemoteOpenDiskDialog extends ModalDialog {

	private final TextField inputField;

	public RemoteOpenDiskDialog() {
		super();
		setTitle("Open Disk");

		Label label = new Label("Disk URI:");
		getContainer().add(label, 0, 0);

		inputField = new TextField();
		inputField.setPromptText("URI to a virtual disk");
		inputField.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.ENTER) {
					setCloseReason(CloseReason.OK);
					close();
				}
			}
		});
		getContainer().add(inputField, 1, 0);

		Button okButton = new Button("Connect");
		okButton.setDefaultButton(true);
		okButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				setCloseReason(CloseReason.OK);
				close();
			}
		});
		getContainer().add(okButton, 0, 1);

		Button cancelButton = new Button("Cancel");
		cancelButton.setCancelButton(true);
		cancelButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				inputField.setText("");
				setCloseReason(CloseReason.CANCEL);
				close();
			}
		});
		getContainer().add(cancelButton, 1, 1);

		inputField.requestFocus();
	}

	public String getUserInput() {
		return inputField.getText();
	}
}
