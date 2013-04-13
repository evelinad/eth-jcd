package ch.se.inf.ethz.jcd.batman.browser;

import javafx.stage.Modality;
import javafx.stage.Stage;

public class ModalDialog extends Stage {

	public enum CloseReason {
		OK, CANCEL, UNDEFINED
	}

	private CloseReason closeReason;

	public ModalDialog() {
		super.initModality(Modality.APPLICATION_MODAL);
		super.setResizable(false);

		closeReason = CloseReason.UNDEFINED;
	}

	public CloseReason getCloseReason() {
		return this.closeReason;
	}

	public void setCloseReason(CloseReason reason) {
		this.closeReason = reason;
	}
}
