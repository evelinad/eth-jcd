package ch.se.inf.ethz.jcd.batman.cli.command;

import java.nio.file.FileSystems;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

import ch.se.inf.ethz.jcd.batman.cli.CommandLineInterface;
import ch.se.inf.ethz.jcd.batman.util.PrioritizedObservable;
import ch.se.inf.ethz.jcd.batman.util.PrioritizedObserver;

/**
 * Provides a command to load a disk.
 * 
 * The command takes a host path which should be a loadable
 * disk.
 * 
 * Example:
 * <code>load /some/path/on/to/a/file</code>
 * 
 *
 */
public class LoadCommand implements PrioritizedObserver<String> {
	private static final String[] COMMAND_STRINGS = {"load", "l"};
	
	@Override
	public void update(PrioritizedObservable<String> observable, String data) {
		assert(observable instanceof CommandLineInterface);
		CommandLineInterface cli = (CommandLineInterface)observable;
		
		String[] lineParts = data.split(" ");
		for(String command : COMMAND_STRINGS) {
			if(lineParts[0].equalsIgnoreCase(command)) {
				cli.setHandled();
				
				// parse parameters
				if(lineParts.length != 2) {
					cli.writeln("not the right amount of parameters provided.");
				} else {
					// extract path
					Path hostPath = null;
					try {
						hostPath = FileSystems.getDefault().getPath(lineParts[1]);
					} catch (InvalidPathException ex) {
						cli.writeln(String.format("provided path is not valid: %s", ex.getMessage()));
						return;
					}
					
					// TODO
					cli.writeln(String.format("command 'load' called for path '%s'", hostPath));
				}
			}
		}
	}

	@Override
	public int getPriority() {
		return 0;
	}

}
