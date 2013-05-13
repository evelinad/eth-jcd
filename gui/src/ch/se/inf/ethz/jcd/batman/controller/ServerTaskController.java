package ch.se.inf.ethz.jcd.batman.controller;

public interface ServerTaskController {

	UpdateableTask<Void> createNewUserTask(String userName, String password);

}
