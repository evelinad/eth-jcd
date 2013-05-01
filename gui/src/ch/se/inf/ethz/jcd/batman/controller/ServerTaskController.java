package ch.se.inf.ethz.jcd.batman.controller;

import javafx.concurrent.Task;

public interface ServerTaskController {

	Task<Void> createNewUserTask (String userName, String password);
	
}
