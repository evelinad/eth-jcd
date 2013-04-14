package ch.se.inf.ethz.jcd.batman.controller.remote;

import javafx.concurrent.Task;
import ch.se.inf.ethz.jcd.batman.controller.DirectoryEntriesService;
import ch.se.inf.ethz.jcd.batman.model.Entry;

public class RemoteDirectoryEntriesService extends DirectoryEntriesService {

	private final RemoteWorkerController controller;
	
	public RemoteDirectoryEntriesService(RemoteWorkerController controller) {
		this.controller = controller;
	}
	
	@Override
	protected Task<Entry[]> createTask() {
		return new Task<Entry[]>() {

			@Override
			protected Entry[] call() throws Exception {
				return controller.getRemoteDisk().getEntrys(controller.getDiskId(), getDirectory());
			}
			
		};
	}


}
