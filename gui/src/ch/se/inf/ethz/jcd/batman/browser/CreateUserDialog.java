package ch.se.inf.ethz.jcd.batman.browser;

import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

public class CreateUserDialog extends ModalDialog {

	private final TextField uriField;
	private final TextField userNameField;
	private final PasswordField passwordField;
	
	public CreateUserDialog() {
		super();
		setTitle("Create Directory");

		EventHandler<KeyEvent> closeEventHandler = new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.ENTER) {
					setCloseReason(CloseReason.OK);
					close();
				}
			}
		};
		
		//Create uri field
		Label uriLabel = new Label("Server URI:");
		getContainer().add(uriLabel, 0, 0);

		uriField = new TextField();
		uriField.setOnKeyPressed(closeEventHandler);
		getContainer().add(uriField, 1, 0);

		//Create userName field
		Label userNameLabel = new Label("Username:");
		getContainer().add(userNameLabel, 0, 1);

		userNameField = new TextField();
		userNameField.setOnKeyPressed(closeEventHandler);
		getContainer().add(userNameField, 1, 1);
		
		//Create password field
		Label passwordLabel = new Label("Password:");
		getContainer().add(passwordLabel, 0, 2);

		passwordField = new PasswordField();
		passwordField.setOnKeyPressed(closeEventHandler);
		getContainer().add(passwordField, 1, 2);
		
		Button okButton = new Button("Connect");
		okButton.setDefaultButton(true);
		okButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				setCloseReason(CloseReason.OK);
				close();
			}
		});
		getContainer().add(okButton, 0, 3);

		Button cancelButton = new Button("Cancel");
		cancelButton.setCancelButton(true);
		cancelButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				uriField.setText("");
				setCloseReason(CloseReason.CANCEL);
				close();
			}
		});
		getContainer().add(cancelButton, 1, 3);

		uriField.requestFocus();
	}

	public String getUri() {
		return uriField.getText();
	}
	
	public String getUserName() {
		return userNameField.getText();
	}
	
	public String getPassword() {
		return passwordField.getText();
	}
}