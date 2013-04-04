package ch.se.inf.ethz.jcd.batman.cli.command;

import java.io.IOException;

import ch.se.inf.ethz.jcd.batman.cli.CommandLineInterface;
import ch.se.inf.ethz.jcd.batman.util.PrioritizedObservable;
import ch.se.inf.ethz.jcd.batman.util.PrioritizedObserver;
import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualDisk;

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
				
				IVirtualDisk disk = cli.getDisk();
				if(disk != null) {
				    try {
                        disk.close();
                    } catch (IOException ex) {
                        cli.writeln(String.format(
                                "following exception occured: %s",
                                ex.getMessage()));
                    }
				}
				
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
