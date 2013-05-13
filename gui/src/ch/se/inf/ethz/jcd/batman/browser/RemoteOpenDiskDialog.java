package ch.se.inf.ethz.jcd.batman.browser;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import ch.se.inf.ethz.jcd.batman.controller.TaskControllerFactory;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

public class RemoteOpenDiskDialog extends ModalDialog {

	private final static String LOCAL = "Local";
	private final static String SERVER = "Server";
	
	private final ToggleGroup group;

	private final List<Node> localNodes = new LinkedList<>();
	private final Label localHostLabel = new Label("Host");
	private final Label localDiskPathLabel = new Label("Path");
	private final TextField localHostField = new TextField();;
	private final TextField localDiskPathField = new TextField();;

	private final List<Node> serverNodes = new LinkedList<>();
	private final Label serverHostLabel = new Label("Server");
	private final Label serverUserNameLabel = new Label("Username");
	private final Label serverPasswordLabel = new Label("Password");
	private final Label serverDiskNameLabel = new Label("Diskname");
	private final TextField serverHostField = new TextField();
	private final TextField serverUserNameField = new TextField();;
	private final PasswordField serverPasswordField = new PasswordField();;
	private final TextField serverDiskNameField = new TextField();;

	public RemoteOpenDiskDialog() {
		super();
		setTitle("Open Disk");

		group = new ToggleGroup();
		group.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
		    public void changed(ObservableValue<? extends Toggle> ov,
		        Toggle oldToggle, Toggle newToggle) {
		            if (group.getSelectedToggle() != null) {
		            	setToggle(group.getSelectedToggle().getUserData().toString());
		            }                
		        }
		});
		
		RadioButton localRadioButton = new RadioButton(LOCAL);
		localRadioButton.setUserData(LOCAL);
		localRadioButton.setToggleGroup(group);
		RadioButton serverRadioButton = new RadioButton(SERVER);
		serverRadioButton.setUserData(SERVER);
		serverRadioButton.setToggleGroup(group);
		getContainer().addRow(0, localRadioButton, serverRadioButton);
		
		localNodes.add(localHostLabel);
		localNodes.add(localHostField);
		localNodes.add(localDiskPathLabel);
		localNodes.add(localDiskPathField);
		
		serverNodes.add(serverHostLabel);
		serverNodes.add(serverHostField);
		serverNodes.add(serverUserNameLabel);
		serverNodes.add(serverUserNameField);
		serverNodes.add(serverPasswordLabel);
		serverNodes.add(serverPasswordField);
		serverNodes.add(serverDiskNameLabel);
		serverNodes.add(serverDiskNameField);
		
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
		serverHostField.setOnKeyPressed(closeEventHandler);
		serverUserNameField.setOnKeyPressed(closeEventHandler);
		serverPasswordField.setOnKeyPressed(closeEventHandler);
		serverDiskNameField.setOnKeyPressed(closeEventHandler);
		
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
		getContainer().addRow(5, okButton, cancelButton);

		localRadioButton.setSelected(true);
	}

	private void setToggle(String option) {
		if (LOCAL.equals(option)) {
			getContainer().getChildren().removeAll(serverNodes);
			getContainer().addRow(1, localHostLabel, localHostField);
			getContainer().addRow(2, localDiskPathLabel, localDiskPathField);
			localHostField.requestFocus();
		} else if (SERVER.equals(option)) {
			getContainer().getChildren().removeAll(localNodes);
			getContainer().addRow(1, serverHostLabel, serverHostField);
			getContainer().addRow(2, serverUserNameLabel, serverUserNameField);
			getContainer().addRow(3, serverPasswordLabel, serverPasswordField);
			getContainer().addRow(4, serverDiskNameLabel, serverDiskNameField);
			serverHostField.requestFocus();
		}
	}
	
	public URI getUri () throws URISyntaxException {
		String toggle = group.getSelectedToggle().getUserData().toString();
		if (LOCAL.equals(toggle)) {
			return new URI(TaskControllerFactory.REMOTE_SCHEME + "://" + localHostField.getText() + "?" + localDiskPathField.getText());
		} else if (SERVER.equals(toggle)) {
			return new URI(TaskControllerFactory.REMOTE_SCHEME + "://" + serverUserNameField.getText() + ":" + serverPasswordField.getText() + "@" + serverHostField.getText() + "?" + serverDiskNameField.getText());
		}
		return new URI("");
	}
	
}
