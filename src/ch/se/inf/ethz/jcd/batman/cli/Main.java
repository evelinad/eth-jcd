package ch.se.inf.ethz.jcd.batman.cli;

import java.io.IOException;

import ch.se.inf.ethz.jcd.batman.cli.command.ChangeDirectoryCommand;
import ch.se.inf.ethz.jcd.batman.cli.command.CreateCommand;
import ch.se.inf.ethz.jcd.batman.cli.command.CreateDirectoryCommand;
import ch.se.inf.ethz.jcd.batman.cli.command.DeleteCommand;
import ch.se.inf.ethz.jcd.batman.cli.command.DestroyCommand;
import ch.se.inf.ethz.jcd.batman.cli.command.ImportCommand;
import ch.se.inf.ethz.jcd.batman.cli.command.ListMembersCommand;
import ch.se.inf.ethz.jcd.batman.cli.command.LoadCommand;
import ch.se.inf.ethz.jcd.batman.cli.command.QueryCommand;
import ch.se.inf.ethz.jcd.batman.cli.command.SizeCommand;
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
		cli.attachCommand(new CreateCommand());
		cli.attachCommand(new ChangeDirectoryCommand());
		cli.attachCommand(new CreateDirectoryCommand());
		cli.attachCommand(new ListMembersCommand());
		cli.attachCommand(new DeleteCommand());
		cli.attachCommand(new DestroyCommand());
		cli.attachCommand(new SizeCommand());
		cli.attachCommand(new QueryCommand());
		cli.attachCommand(new ImportCommand());
		
		// start command line interface
		cli.start();
	}

}
