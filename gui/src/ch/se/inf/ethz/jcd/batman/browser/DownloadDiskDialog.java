package ch.se.inf.ethz.jcd.batman.browser;

import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

public class DownloadDiskDialog extends ModalDialog {

	private final TextField localDiskUriField;

	public DownloadDiskDialog() {
		super();
		setTitle("Download disk");

		Label label = new Label("Local disk URI:");
		getContainer().add(label, 0, 0);

		localDiskUriField = new TextField();
		localDiskUriField.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.ENTER) {
					setCloseReason(CloseReason.OK);
					close();
				}
			}
		});
		getContainer().add(localDiskUriField, 1, 0);

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
				setCloseReason(CloseReason.CANCEL);
				close();
			}
		});
		getContainer().add(cancelButton, 1, 1);

		localDiskUriField.requestFocus();
	}

	public String getLocalDiskUri() {
		return localDiskUriField.getText();
	}
}
