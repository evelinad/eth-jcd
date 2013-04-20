package ch.se.inf.ethz.jcd.batman.browser;

import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;

public class ErrorDialog extends ModalDialog {

	public ErrorDialog(String title, String message) {
		setTitle(title);
		
		Label label = new Label();
		label.setText(message);
		getContainer().add(label, 0, 0);
		
		Button okButton = new Button("OK");
		okButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				setCloseReason(CloseReason.OK);
				close();
			}
		});
		getContainer().add(okButton, 0, 1);
		setResizable(true);
	}
	
}
