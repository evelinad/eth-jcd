package ch.se.inf.ethz.jcd.batman.browser;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;

public class RemoteOpenDiskDialog extends ModalDialog {

	private TextField inputField;

	public RemoteOpenDiskDialog() {
		super();

		GridPane container = new GridPane();
		container.setAlignment(Pos.CENTER);
		container.setHgap(10);
		container.setVgap(10);
		container.setPadding(new Insets(25, 25, 25, 25));
		super.setScene(new Scene(container));

		Label label = new Label("Disk URI:");
		container.add(label, 0, 0);

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
		container.add(inputField, 1, 0);

		Button okButton = new Button("OK");
		okButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				setCloseReason(CloseReason.OK);
				close();
			}
		});
		container.add(okButton, 0, 1);

		Button cancelButton = new Button("Cancel");
		cancelButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				inputField.setText("");
				setCloseReason(CloseReason.CANCEL);
				close();
			}
		});
		container.add(cancelButton, 1, 1);
	}

	public String getUserInput() {
		return inputField.getText();
	}
}
