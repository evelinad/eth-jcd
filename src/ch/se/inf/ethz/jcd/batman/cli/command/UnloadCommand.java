package ch.se.inf.ethz.jcd.batman.cli.command;

import ch.se.inf.ethz.jcd.batman.cli.CommandLineInterface;
import ch.se.inf.ethz.jcd.batman.util.PrioritizedObservable;
import ch.se.inf.ethz.jcd.batman.util.PrioritizedObserver;

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
					
				// TODO
				cli.writeln("command 'unload' called");
				
				cli.setInputPrefix(null);
			}
		}
	}

	@Override
	public int getPriority() {
		return 0;
	}

}
