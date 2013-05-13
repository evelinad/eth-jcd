package ch.se.inf.ethz.jcd.batman.cli.command;

import ch.se.inf.ethz.jcd.batman.cli.Command;
import ch.se.inf.ethz.jcd.batman.cli.CommandLine;
import ch.se.inf.ethz.jcd.batman.io.VDiskFile;

/**
 * Implements a delete command for the CLI to delete members
 * 
 */
public class DeleteCommand implements Command {

	private static final String[] COMMAND_STRINGS = { "delete", "del" };

	@Override
	public String[] getAliases() {
		return DeleteCommand.COMMAND_STRINGS;
	}

	@Override
	public void execute(CommandLine caller, String alias, String... params) {
		VDiskFile curLocation = caller.getCurrentLocation();
		if (curLocation == null) {
			caller.writeln("no disk loaded.");
			return;
		}

		VDiskFile commandRoot = null;

		if (params.length == 1) {
			commandRoot = CommandUtil.getFile(caller, params[0]);
		} else {
			commandRoot = curLocation;
		}

		if (commandRoot != null) {
			if (commandRoot.delete()) {
				caller.writeln("deleted '%s'", commandRoot.getPath());
			} else {
				caller.writeln("could not delete '%s'", commandRoot.getPath());
			}
		}
	}

}
