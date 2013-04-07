package ch.se.inf.ethz.jcd.batman.cli.command;

import java.io.IOException;

import ch.se.inf.ethz.jcd.batman.cli.CommandLineInterface;
import ch.se.inf.ethz.jcd.batman.cli.util.PrioritizedObservable;
import ch.se.inf.ethz.jcd.batman.cli.util.PrioritizedObserver;
import ch.se.inf.ethz.jcd.batman.io.VDiskFile;

/**
 * Provides a command to unload a loaded disk. Doesn't take any argument.
 *
 */
public class UnloadCommand implements PrioritizedObserver<String> {
	private static final String[] COMMAND_STRINGS = {"unload", "u"};
	
	@Override
	public void update(PrioritizedObservable<String> observable, String data) {
		assert observable instanceof CommandLineInterface;
		CommandLineInterface cli = (CommandLineInterface)observable;
		
		String[] lineParts = data.split(" ");
		for(String command : COMMAND_STRINGS) {
			if(lineParts[0].equalsIgnoreCase(command)) {
				cli.setHandled();
				
				
				VDiskFile curLocation = cli.getCurrentLocation();
				if(curLocation == null) {
                    cli.writeln("no disk is loaded.");
				} else {
				    try {
				        curLocation.getDisk().close();
				    } catch (IOException ex) {
				        cli.writeln(String.format(
				                "following exception occured: %s",
				                ex.getMessage()));
				    }
				    
				    cli.setCurrentLocation(null);
				    cli.writeln("disk unloaded.");
				}
			}
		}
	}

	@Override
	public int getPriority() {
		return 0;
	}

}
