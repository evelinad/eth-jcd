package ch.se.inf.ethz.jcd.batman.controller.remote;

import java.net.URI;

import ch.se.inf.ethz.jcd.batman.controller.AsyncCallback;
import ch.se.inf.ethz.jcd.batman.controller.Controller;
import ch.se.inf.ethz.jcd.batman.model.Directory;
import ch.se.inf.ethz.jcd.batman.model.Entry;

public class RemoteController implements Controller {

	public RemoteController(URI uri) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void getEntrys(Directory directory,
			AsyncCallback<Entry[]> callback) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getFreeSpace(AsyncCallback<Long> callback) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getOccupiedSpace(AsyncCallback<Long> callback) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getUsedSpace(AsyncCallback<Long> callback) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

}
