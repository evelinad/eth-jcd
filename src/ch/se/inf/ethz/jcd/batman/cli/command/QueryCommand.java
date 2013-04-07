package ch.se.inf.ethz.jcd.batman.cli.command;

import java.io.IOException;

import ch.se.inf.ethz.jcd.batman.cli.Command;
import ch.se.inf.ethz.jcd.batman.cli.CommandLine;
import ch.se.inf.ethz.jcd.batman.io.VDiskFile;

/**
 * Implements a query command for the CLI.
 * 
 * The query command can query multiple informations. For example: occupied,
 * free, total
 * 
 */
public class QueryCommand implements Command {

    private static final String[] COMMAND_STRINGS = { "query" };

    private static final String OCCUPIED_PARAM = "occupied";
    private static final String FREE_PARAM = "free";
    private static final String TOTAL_PARAM = "total";

    @Override
    public String[] getAliases() {
        return QueryCommand.COMMAND_STRINGS;
    }

    @Override
    public void execute(CommandLine caller, String alias, String... params) {
        VDiskFile curLocation = caller.getCurrentLocation();
        if (curLocation == null) {
            caller.write("no disk loaded.");
            return;
        }

        if (params.length == 1) {
            try {
                if (params[0].equalsIgnoreCase(OCCUPIED_PARAM)) {
                    caller.writeln("%s bytes occupied", curLocation.getDisk()
                            .getOccupiedSpace());
                } else if (params[0].equalsIgnoreCase(FREE_PARAM)) {
                    caller.writeln("%s bytes free pre-allocated space",
                            curLocation.getDisk().getFreeSpace());
                } else if (params[0].equalsIgnoreCase(TOTAL_PARAM)) {
                    caller.writeln("%s bytes in total", curLocation.getDisk()
                            .getSize());
                } else {
                    caller.writeln("unknown query command '%s'", params[0]);
                }
            } catch (IOException e) {
                caller.write(e);
            }
        } else {
            caller.writeln("expected one parameter, %s given", params.length);
        }
    }

}
