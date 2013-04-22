package ch.se.inf.ethz.jcd.batman.browser;

import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

public class TaskDialog extends ModalDialog {

	protected static final long TIME_TO_WAIT_BEFORE_SHOW = 5L;

	private static final double TASK_DIALOG_MAX_WIDTH = 500;

	private final Task<?> task;

	public TaskDialog(GuiState guiState, final Task<?> task) {
		this.task = task;

		super.setMaxWidth(TASK_DIALOG_MAX_WIDTH);

		titleProperty().bind(task.titleProperty());

		Label label = new Label();
		label.setTextOverrun(OverrunStyle.LEADING_WORD_ELLIPSIS);
		label.textProperty().bind(task.messageProperty());
		getContainer().add(label, 0, 0);

		HBox progressBox = new HBox();
		ProgressBar progressBar = new ProgressBar();
		progressBar.progressProperty().bind(task.progressProperty());
		progressBox.getChildren().add(progressBar);
		getContainer().add(progressBox, 0, 1);
		progressBar.setMinWidth(super.getMinWidth());
		progressBox.setAlignment(Pos.CENTER);

		HBox buttonBox = new HBox();
		Button cancelButton = new Button("Stop Task");
		cancelButton.setCancelButton(true);
		cancelButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				task.cancel();
				setCloseReason(CloseReason.CANCEL);
			}
		});
		buttonBox.getChildren().add(cancelButton);
		getContainer().add(buttonBox, 0, 2);
		buttonBox.setAlignment(Pos.CENTER);

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
