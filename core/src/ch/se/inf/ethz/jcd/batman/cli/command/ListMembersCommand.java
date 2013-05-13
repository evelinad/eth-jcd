package ch.se.inf.ethz.jcd.batman.cli.command;

import java.io.IOException;

import ch.se.inf.ethz.jcd.batman.cli.Command;
import ch.se.inf.ethz.jcd.batman.cli.CommandLine;
import ch.se.inf.ethz.jcd.batman.io.VDiskFile;

/**
 * Implements a ls / dir like command for the CLI
 * 
 */
public class ListMembersCommand implements Command {

	private static final String[] COMMAND_STRINGS = { "list", "ls", "dir" };

	@Override
	public String[] getAliases() {
		return ListMembersCommand.COMMAND_STRINGS;
	}

	@Override
	public void execute(CommandLine caller, String alias, String... params) {
		VDiskFile curLocation = caller.getCurrentLocation();
		if (curLocation == null) {
			caller.writeln("no disk loaded.");
			return;
		}

		VDiskFile listRoot = null;
		if (params.length == 1) {
			listRoot = CommandUtil.getFile(caller, params[0]);
		} else {
			listRoot = curLocation;
		}

		try {
			for (VDiskFile child : listRoot.listFiles()) {
				if (child.isDirectory()) {
					caller.writeln("%s [D]", child.getName());
				} else if (child.isFile()) {
					caller.writeln("%s [F]", child.getName());
				} else {
					caller.writeln("%s [?]", child.getName());
				}
			}
		} catch (IOException e) {
			caller.write(e);
		}
	}

}
