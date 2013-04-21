package ch.se.inf.ethz.jcd.batman.browser;

import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseEvent;

public class TaskDialog extends ModalDialog {

	protected static final long TIME_TO_WAIT_BEFORE_SHOW = 5L;
	
	private final Task<?> task;

	public TaskDialog(GuiState guiState, final Task<?> task) {
		this.task = task;

		titleProperty().bind(task.titleProperty());

		Label label = new Label();
		label.setTextOverrun(OverrunStyle.LEADING_WORD_ELLIPSIS);
		label.textProperty().bind(task.messageProperty());
		getContainer().add(label, 0, 0);

		ProgressBar progressBar = new ProgressBar();
		progressBar.progressProperty().bind(task.progressProperty());
		getContainer().add(progressBar, 0, 1);

		Button cancelButton = new Button("Cancel");
		cancelButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				task.cancel();
				setCloseReason(CloseReason.CANCEL);
			}
		});
		getContainer().add(cancelButton, 0, 2);

		task.setOnCancelled(new EventHandler<WorkerStateEvent>() {

			@Override
			public void handle(WorkerStateEvent event) {
				close();
			}
		});
		task.setOnRunning(new EventHandler<WorkerStateEvent>() {

			@Override
			public void handle(WorkerStateEvent event) {
				final Timer timer = new Timer();
				timer.schedule(new TimerTask() {

					@Override
					public void run() {
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								if (!task.isDone()) {
									showAndWait();
								}
							}
						});

						timer.cancel();
					}
				}, TIME_TO_WAIT_BEFORE_SHOW);
			}
		});
		task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

			@Override
			public void handle(WorkerStateEvent event) {
				close();
				succeeded(event);
			}
		});
		task.setOnFailed(new EventHandler<WorkerStateEvent>() {

			@Override
			public void handle(WorkerStateEvent event) {
				close();
				failed(event);
			}
		});
		guiState.submitTask(task);
	}

	protected void succeeded(WorkerStateEvent event) {
	}

	protected void failed(WorkerStateEvent event) {
		showErrorDialog(event);
	}

	protected void showErrorDialog(WorkerStateEvent event) {
		Throwable exception = event.getSource().getException();
		new ErrorDialog("Error", exception.getClass() + ": "
				+ exception.getMessage()).showAndWait();
	}

	@Override
	public void showAndWait() {
		if (task.isRunning()) {
			super.showAndWait();
		}
	}

}
