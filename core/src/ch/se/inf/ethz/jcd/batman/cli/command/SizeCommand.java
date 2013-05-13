package ch.se.inf.ethz.jcd.batman.cli.command;

import ch.se.inf.ethz.jcd.batman.cli.Command;
import ch.se.inf.ethz.jcd.batman.cli.CommandLine;
import ch.se.inf.ethz.jcd.batman.io.VDiskFile;

/**
 * Implements a command to query the disk size of a file or directory.
 * 
 * This command allways returns the size on the disk, which may be greater than
 * the size of the "useful" data of a file.
 */
public class SizeCommand implements Command {

	private static final String[] COMMAND_STRINGS = { "size" };

	@Override
	public String[] getAliases() {
		return SizeCommand.COMMAND_STRINGS;
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

		if (commandRoot == null) {
			caller.write("unknown path '%s'", params[0]);
			return;
		}

		caller.writeln("%s: %s bytes", commandRoot.getPath(),
				commandRoot.getTotalSpace());
	}

}
