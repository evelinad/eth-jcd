package ch.se.inf.ethz.jcd.batman.browser;

import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

public class GoOnlineDialog extends ModalDialog {

	private final PasswordField passwordField;

	public GoOnlineDialog() {
		super();
		setTitle("Go online");

		Label label = new Label("Password");
		getContainer().add(label, 0, 0);

		passwordField = new PasswordField();
		passwordField.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.ENTER) {
					setCloseReason(CloseReason.OK);
					close();
				}
			}
		});
		getContainer().add(passwordField, 1, 0);

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

		passwordField.requestFocus();
	}

	public String getPassword() {
		return passwordField.getText();
	}
}
