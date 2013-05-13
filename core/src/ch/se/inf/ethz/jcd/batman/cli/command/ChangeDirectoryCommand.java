package ch.se.inf.ethz.jcd.batman.cli.command;

import ch.se.inf.ethz.jcd.batman.cli.Command;
import ch.se.inf.ethz.jcd.batman.cli.CommandLine;
import ch.se.inf.ethz.jcd.batman.io.VDiskFile;

/**
 * Implements a change directory command to move around on the virtual disk.
 * 
 */
public class ChangeDirectoryCommand implements Command {

	private static final String[] COMMAND_STRINGS = { "cd" };

	@Override
	public String[] getAliases() {
		return ChangeDirectoryCommand.COMMAND_STRINGS;
	}

	@Override
	public void execute(CommandLine caller, String alias, String... params) {
		VDiskFile curLocation = caller.getCurrentLocation();
		if (curLocation == null) {
			caller.writeln("no disk loaded.");
			return;
		}

		if (params.length == 1) {
			curLocation = CommandUtil.getFile(caller, params[0]);

			if (curLocation.isDirectory()) {
				caller.setCurrentLocation(curLocation);
			} else {
				caller.writeln(
						"given path '%s' is not a directory or does not exist",
						curLocation.getPath());
			}
		} else {
			caller.writeln("expected one parameter, %s given", params.length);
		}
	}

}
