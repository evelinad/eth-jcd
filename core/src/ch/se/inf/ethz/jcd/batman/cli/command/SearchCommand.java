package ch.se.inf.ethz.jcd.batman.cli.command;

import java.io.IOException;
import java.util.List;

import ch.se.inf.ethz.jcd.batman.cli.Command;
import ch.se.inf.ethz.jcd.batman.cli.CommandLine;
import ch.se.inf.ethz.jcd.batman.io.VDiskFile;
import ch.se.inf.ethz.jcd.batman.vdisk.search.VirtualDiskSearch;

/**
 * Implements a simple search command.
 * 
 * This command allows to search inside file and directory names for a given
 * string. The matching is case insensitive.
 * 
 */
public class SearchCommand implements Command {

	private static final String[] COMMAND_STRINGS = { "search" };

	private final VirtualDiskSearch.Settings searchSettings;

	public SearchCommand() {
		searchSettings = new VirtualDiskSearch.Settings();
		searchSettings.setCaseSensitive(false);
		searchSettings.setCheckFiles(true);
		searchSettings.setCheckFolders(true);
		searchSettings.setCheckSubFolders(true);
	}

	@Override
	public String[] getAliases() {
		return COMMAND_STRINGS;
	}

	@Override
	public void execute(CommandLine caller, String alias, String... params) {
		if (params.length == 1) {
			try {
				List<VDiskFile> results = VirtualDiskSearch.searchName(
						searchSettings, params[0], caller.getCurrentLocation());

				for (VDiskFile entry : results) {
					caller.writeln("%s", entry.getPath());
				}
			} catch (IOException e) {
				caller.write(e);
			}
		} else {
			caller.writeln("expected one parameter, %s given", params.length);
		}
	}

}
