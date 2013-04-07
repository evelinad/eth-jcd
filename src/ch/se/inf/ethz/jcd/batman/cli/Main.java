package ch.se.inf.ethz.jcd.batman.cli;

import java.io.IOException;

import ch.se.inf.ethz.jcd.batman.cli.command.LoadCommand;

/**
 * Starts the CLI interface.
 *
 */
public class Main {

	public static void main(String[] args) throws IOException {
		CommandLine cli = new CommandLineInterface();
		
		// register commands
		cli.attachCommand(new LoadCommand());
		
		// start command line interface
		cli.start();
	}

}
