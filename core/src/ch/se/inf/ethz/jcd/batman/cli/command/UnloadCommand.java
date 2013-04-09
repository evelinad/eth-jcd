package ch.se.inf.ethz.jcd.batman.cli.command;

import java.io.IOException;

import ch.se.inf.ethz.jcd.batman.cli.Command;
import ch.se.inf.ethz.jcd.batman.cli.CommandLine;
import ch.se.inf.ethz.jcd.batman.io.VDiskFile;

/**
 * Provides a command to unload a loaded disk. Doesn't take any argument.
 * 
 */
public class UnloadCommand implements Command {
    private static final String[] COMMAND_STRINGS = { "unload", "u" };

    @Override
    public String[] getAliases() {
        return UnloadCommand.COMMAND_STRINGS;
    }

    @Override
    public void execute(CommandLine caller, String alias, String... params) {
        VDiskFile curLocation = caller.getCurrentLocation();

        if (curLocation == null) {
            caller.writeln("no disk loaded.");
        } else {
            try {
                curLocation.getDisk().close();
            } catch (IOException e) {
                caller.write(e);
            }

            caller.setCurrentLocation(null);
        }
    }

}
