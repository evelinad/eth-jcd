package ch.se.inf.ethz.jcd.batman.browser;

import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

public class LinkDiskDialog extends ModalDialog {

	private final TextField hostField;
	private final TextField userNameField;
	private final PasswordField passwordField;
	private final TextField diskNameField;
	
	public LinkDiskDialog() {
		super();
		setTitle("Link Disk");

		EventHandler<KeyEvent> closeEventHandler = new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.ENTER) {
					setCloseReason(CloseReason.OK);
					close();
				}
			}
		};
		
		//Create host field
		Label uriLabel = new Label("Host");
		getContainer().add(uriLabel, 0, 0);

		hostField = new TextField();
		hostField.setOnKeyPressed(closeEventHandler);
		getContainer().add(hostField, 1, 0);

		//Create userNname field
		Label userNameLabel = new Label("Username");
		getContainer().add(userNameLabel, 0, 1);

		userNameField = new TextField();
		userNameField.setOnKeyPressed(closeEventHandler);
		getContainer().add(userNameField, 1, 1);
		
		//Create password field
		Label passwordLabel = new Label("Password");
		getContainer().add(passwordLabel, 0, 2);

		passwordField = new PasswordField();
		passwordField.setOnKeyPressed(closeEventHandler);
		getContainer().add(passwordField, 1, 2);
		
		//Create diskName field
		Label diskNameLabel = new Label("Diskname");
		getContainer().add(diskNameLabel, 0, 3);

		diskNameField = new TextField();
		diskNameField.setOnKeyPressed(closeEventHandler);
		getContainer().add(diskNameField, 1, 3);
		
		Button okButton = new Button("Connect");
		okButton.setDefaultButton(true);
		okButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				setCloseReason(CloseReason.OK);
				close();
			}
		});
		getContainer().add(okButton, 0, 4);

		Button cancelButton = new Button("Cancel");
		cancelButton.setCancelButton(true);
		cancelButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				setCloseReason(CloseReason.CANCEL);
				close();
			}
		});
		getContainer().add(cancelButton, 1, 4);

		hostField.requestFocus();
	}

	public String getHost() {
		return hostField.getText();
	}
	
	public String getUserName() {
		return userNameField.getText();
	}
	
	public String getPassword() {
		return passwordField.getText();
	}
	
	public String getDiskName() {
		return diskNameField.getText();
	}
}
