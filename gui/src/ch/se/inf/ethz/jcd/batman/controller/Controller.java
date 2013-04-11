package ch.se.inf.ethz.jcd.batman.controller;

import ch.se.inf.ethz.jcd.batman.model.Directory;
import ch.se.inf.ethz.jcd.batman.model.Entry;

public interface Controller {

	void getEntrys(Directory directory, AsyncCallback<Entry[]> callback);
	
	void getFreeSpace(AsyncCallback<Long> callback);
	
	void getOccupiedSpace(AsyncCallback<Long> callback);
	
	void getUsedSpace(AsyncCallback<Long> callback);
	
	void close();
	
}
