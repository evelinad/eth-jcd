package ch.se.inf.ethz.jcd.batman.browser;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javafx.concurrent.Task;

import ch.se.inf.ethz.jcd.batman.controller.TaskController;

public class GuiState {

	private static final int THREAD_POOL_SIZE = 1;
	
	private TaskController controller;
	private State state;
	private List<StateListener> stateListener = new LinkedList<StateListener>();
	private ScheduledExecutorService scheduler;
	
	public GuiState() {
		state = State.DISCONNECTED;
		scheduler = Executors.newScheduledThreadPool(THREAD_POOL_SIZE);
	}
	
	public TaskController getController() {
		return controller;
	}

	public void setController(TaskController controller) {
		this.controller = controller;
	}
	
	public void addStateListener(StateListener listener) {
		if (!stateListener.contains(listener)) {
			stateListener.add(listener);
		}
	}
	
	public void removeStateListener(StateListener listener) {
		stateListener.remove(listener);
	}
	
	public State getState () {
		return state;
	}
	
	public void setState (State newState) {
		if (newState != state) {
			State oldState = state;
			state = newState;
			for (StateListener listener : stateListener) {
				listener.stateChanged(oldState, newState);
			}
		}
	}
	
	public void submitTask (Task<?> task) {
		scheduler.submit(task);
	}
	
	@Override
	protected void finalize() throws Throwable {
		scheduler.shutdownNow();
		super.finalize();
	}
	
}
