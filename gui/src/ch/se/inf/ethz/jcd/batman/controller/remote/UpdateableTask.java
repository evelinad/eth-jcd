package ch.se.inf.ethz.jcd.batman.controller.remote;

import javafx.concurrent.Task;

public abstract class UpdateableTask<V> extends Task<V> {

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
