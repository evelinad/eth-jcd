package ch.se.inf.ethz.jcd.batman.cli.command;

import ch.se.inf.ethz.jcd.batman.cli.CommandLineInterface;
import ch.se.inf.ethz.jcd.batman.cli.util.PrioritizedObservable;
import ch.se.inf.ethz.jcd.batman.cli.util.PrioritizedObserver;

/**
 * As a last resort an instance of this class will inform the user
 * that the given string doesn't belong to any known command.
 * 
 * Accordingly it uses the largest value as it's priority. Accordingly
 * all other commands should have a priority less than Integer.MAX_VALUE.
 */
public class CommandNotFound implements PrioritizedObserver<String> {

	@Override
	public void update(PrioritizedObservable<String> obs, String data) {
		if(!obs.isHandled()) {
			assert obs instanceof CommandLineInterface;
			CommandLineInterface cli = (CommandLineInterface) obs;

			cli.writeln(String.format("command '%s' not found.", data));
		}
	}

	@Override
	public int getPriority() {
		return Integer.MAX_VALUE;
	}

}
