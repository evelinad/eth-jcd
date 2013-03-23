package ch.se.inf.ethz.jcd.batman.util;

/**
 * Interface which an observer for a PrioritizedObservable instance has
 * to implement.
 *
 * @see ch.se.inf.ethz.jcd.batman.util.PrioritizedObservable
 * @param <T> type of the data received by update()
 */
public interface PrioritizedObserver<T> {
	/**
	 * Will be called by {@link PrioritizedObservable.notifyAll}, informing
	 * the observer about some change.
	 * 
	 * @param observable the PrioritizedObservable instance calling the observer
	 * @param data additional data provided by the caller
	 */
	void update(PrioritizedObservable<T> observable, T data);
	
	/**
	 * Returns the priority of the observer. Observers are called by ascending
	 * order of their priority.
	 * 
	 * @return priority of the observer
	 */
	int getPriority();
}

