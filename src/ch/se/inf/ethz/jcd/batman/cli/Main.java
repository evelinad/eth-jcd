package ch.se.inf.ethz.jcd.batman.cli;

import java.io.IOException;

import ch.se.inf.ethz.jcd.batman.cli.command.LoadCommand;
import ch.se.inf.ethz.jcd.batman.cli.command.StopCommand;
import ch.se.inf.ethz.jcd.batman.cli.command.UnloadCommand;

/**
 * Starts the CLI interface.
 *
 */
public class Main {

	public static void main(String[] args) throws IOException {
		CommandLine cli = new CommandLineInterface();
		
		// register commands
		cli.attachCommand(new LoadCommand());
		cli.attachCommand(new StopCommand());
		cli.attachCommand(new UnloadCommand());
		
		// start command line interface
		cli.start();
	}

}
