package ch.se.inf.ethz.jcd.batman.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Implements an Observable as part of the Observer Design-Pattern.
 * 
 * This observable allows to have observers which are called in a
 * specific order. Observing observers are called in ascending order
 * of their priority returned by getPriority().
 *
 * @see ch.se.inf.ethz.jcd.batman.util.PrioritizedObserver
 * @param <T> type of the data object used in notifyAll
 */
public class PrioritizedObservable<T> {
	private List<PrioritizedObserver<T>> observers;
	private boolean handled;
	private ObserverComperator comperator;
	
	/**
	 * Comparator implementation to compare integers and sort them in ascending order.
	 */
	private class ObserverComperator implements Comparator<PrioritizedObserver<T>> {

		@Override
		public int compare(PrioritizedObserver<T> o1, PrioritizedObserver<T> o2) {
			int prioObj1 = o1.getPriority();
			int prioObj2 = o2.getPriority();
			
			if(prioObj1 < prioObj2) {
				return -1;
			} else if(prioObj1 > prioObj2) {
				return 1;
			} else {
				return 0;
			}
		}
		
	}
	
	public PrioritizedObservable() {
		observers = new ArrayList<PrioritizedObserver<T>>();
		comperator = new ObserverComperator();
	}
	
	/**
	 * Adds an observer to the list of observers.
	 * 
	 * @param observer PrioritizedObserver to add
	 */
	public void addObserver(PrioritizedObserver<T> observer) {
		observers.add(observer);
		Collections.sort(observers, comperator);
	}
	
	/**
	 * Removed an observer from the list of observers.
	 * 
	 * @param observer PrioritizedObserver to remove
	 */
	public void removeObserver(PrioritizedObserver<T> observer) {
		observers.remove(observer);
	}
	
	/**
	 * Notifies all observers currently attached to the observable.
	 * isHandled() will return false for the first observer as it is set
	 * back to false.
	 * 
	 * @param data data of type T to pass on to the observers.
	 */
	public void notifyAll(T data) {
		handled = false;
		for(PrioritizedObserver<T> observer : observers) {
			observer.update(this, data);
		}
	}
	
	/**
	 * Indicates that the current change, about which all observers
	 * are being notified, was handled by at least one of the observers.
	 * 
	 * @see isHandled
	 */
	public void setHandled() {
		handled = true;
	}
	
	/**
	 * Indicates if the current change, about which all observers are
	 * being notified, was handled by at least one of the observers. In
	 * such a case true is returned, otherwise false.
	 * 
	 * @return returns true if the current change was handled, otherwise false
	 */
	public boolean isHandled() {
		return handled;
	}
}
