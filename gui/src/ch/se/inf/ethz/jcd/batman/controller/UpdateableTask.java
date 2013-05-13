package ch.se.inf.ethz.jcd.batman.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;

public abstract class UpdateableTask<V> extends Task<V> {

	public static final EventType<WorkerStateEvent> FINISHED_EVENT = new EventType<>(
			WorkerStateEvent.ANY, "finished");

	private EventHandler<WorkerStateEvent> handler;

	public void setOnFinished(EventHandler<WorkerStateEvent> handler) {
		this.handler = handler;
	}

	/**
	 * Called whenever the task stops. This could be if the task finished
	 * successfully, the task failed or the task was cancelled.
	 */
	public void finished() {
		if (handler != null) {
			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					handler.handle(new WorkerStateEvent(UpdateableTask.this,
							FINISHED_EVENT));
				}
			});
		}
	}

	// As this should have the same interface as Task.call, it throws the same
	// exception
	@Override
	protected V call() throws Exception {
		try {
			V returnValue = callImpl();
			return returnValue;
		} finally {
			finished();
		}

	}

	// Regarding the PMD warning, as this should have the same interface as
	// Task.call, it throws the same exception
	protected abstract V callImpl() throws Exception;

	/*
	 * Regarding the PMD warning, the super method is protected and the "new"
	 * implementation makes this methods public, which allows other methods to
	 * update the task.
	 */
	@Override
	public void updateMessage(String message) {
		super.updateMessage(message);
	}

	@Override
	public void updateProgress(double workDone, double max) {
		super.updateProgress(workDone, max);
	}

	@Override
	public void updateProgress(long workDone, long max) {
		super.updateProgress(workDone, max);
	}

	@Override
	public void updateTitle(String title) {
		super.updateTitle(title);
	}

}
