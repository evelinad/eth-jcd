package ch.se.inf.ethz.jcd.batman.browser;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ModalDialog extends Stage {

	public enum CloseReason {
		OK, CANCEL, UNDEFINED
	}

	private CloseReason closeReason;
	private final GridPane container;

	public ModalDialog() {
		super.initModality(Modality.APPLICATION_MODAL);
		super.setResizable(true);
		setMinWidth(300);
		container = new GridPane();
		container.setAlignment(Pos.CENTER);
		container.setHgap(10);
		container.setVgap(10);
		container.setPadding(new Insets(20, 20, 20, 20));

		super.setScene(new Scene(container));

		closeReason = CloseReason.UNDEFINED;
	}

	public GridPane getContainer() {
		return container;
	}

	public CloseReason getCloseReason() {
		return this.closeReason;
	}

	public void setCloseReason(CloseReason reason) {
		this.closeReason = reason;
	}
}
