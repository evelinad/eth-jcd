package ch.se.inf.ethz.jcd.batman.cli;

import ch.se.inf.ethz.jcd.batman.cli.command.CommandNotFound;
import ch.se.inf.ethz.jcd.batman.cli.command.CreateCommand;
import ch.se.inf.ethz.jcd.batman.cli.command.StopCommand;

/**
 * Starts the CLI interface.
 *
 */
public class Main {

	public static void main(String[] args) {
		CommandLineInterface cli = new CommandLineInterface();
		
		// register commands
		cli.addObserver(new StopCommand());
		cli.addObserver(new CommandNotFound());
		cli.addObserver(new CreateCommand());
		
		// start command line interface
		cli.start();
	}

}
