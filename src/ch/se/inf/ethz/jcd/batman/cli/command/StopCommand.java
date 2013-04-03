package ch.se.inf.ethz.jcd.batman.cli.command;

import ch.se.inf.ethz.jcd.batman.cli.CommandLineInterface;
import ch.se.inf.ethz.jcd.batman.util.PrioritizedObservable;
import ch.se.inf.ethz.jcd.batman.util.PrioritizedObserver;

/**
 * Implements a quit command that can be used to end the CLI.
 */
public class StopCommand implements PrioritizedObserver<String> {
	/**
	 * Accepted commands. Case will be ignored for the check.
	 */
	private static final String[] COMMAND_STRINGS = {"quit", "q", "exit", "stop"};

	@Override
	public void update(PrioritizedObservable<String> obs, String line) {
		assert obs instanceof CommandLineInterface;
		CommandLineInterface cli = (CommandLineInterface)obs;
		
		for(String commandStr : COMMAND_STRINGS) {
			if(line.equalsIgnoreCase(commandStr)) {
				cli.writeln("exiting...");
				cli.stop();
				cli.setHandled();
			}
		}
	}

	@Override
	public int getPriority() {
		return Integer.MAX_VALUE - 1;
	}

}
