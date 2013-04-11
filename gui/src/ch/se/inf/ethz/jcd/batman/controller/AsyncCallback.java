package ch.se.inf.ethz.jcd.batman.controller;

public interface AsyncCallback<T> {

	void onSuccess(T object);

	void onFailure(Throwable caught);
	
}
