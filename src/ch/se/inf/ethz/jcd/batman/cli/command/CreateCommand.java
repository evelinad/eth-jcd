/**
 * 
 */
package ch.se.inf.ethz.jcd.batman.cli.command;

import java.nio.file.FileSystems;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

import ch.se.inf.ethz.jcd.batman.cli.CommandLineInterface;
import ch.se.inf.ethz.jcd.batman.util.PrioritizedObservable;
import ch.se.inf.ethz.jcd.batman.util.PrioritizedObserver;

/**
 * Provides a CLI command to create a new virtual disk.
 * 
 * The command to create a disk of a size of 1 MByte the following
 * command can be used:
 * <code>create /some/host/path/to/a/file 1048576</code>
 *
 */
public class CreateCommand implements PrioritizedObserver<String> {
	private static final String[] COMMAND_STRINGS = {"create", "cr"};
	
	@Override
	public void update(PrioritizedObservable<String> observable, String data) {
		assert observable instanceof CommandLineInterface;
		CommandLineInterface cli = (CommandLineInterface)observable;
		
		String[] lineParts = data.split(" ");
		for(String command : COMMAND_STRINGS) {
			if(lineParts[0].equalsIgnoreCase(command)) {
				cli.setHandled();
				
				// parse parameters
				if(lineParts.length == 3) {
					// extract path
					Path hostPath = null;
					try {
						hostPath = FileSystems.getDefault().getPath(lineParts[1]);
					} catch (InvalidPathException ex) {
						cli.writeln(String.format("provided path is not valid: %s", ex.getMessage()));
						return;
					}
					
					// extract size
					long size = 0L;
					try {
						size = Long.parseLong(lineParts[2]);
					} catch (NumberFormatException ex) {
						cli.writeln(String.format("provided size is not valid: %s", ex.getMessage()));
						return;
					}
					
					// TODO
					cli.writeln(String.format("command 'create' called for path '%s' and size '%s'", hostPath, size));
				} else {
					cli.writeln("not the right amount of parameters provided.");
				}
			}
		}
	}
	
	@Override
	public int getPriority() {
		return 0;
	}

}
