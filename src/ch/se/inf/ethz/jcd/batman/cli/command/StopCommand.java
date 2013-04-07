package ch.se.inf.ethz.jcd.batman.cli.command;

import java.io.IOException;

import ch.se.inf.ethz.jcd.batman.cli.Command;
import ch.se.inf.ethz.jcd.batman.cli.CommandLine;
import ch.se.inf.ethz.jcd.batman.io.VDiskFile;

/**
 * Implements a quit command that can be used to end the CLI.
 */
public class StopCommand implements Command {
    /**
     * Accepted commands. Case will be ignored for the check.
     */
    private static final String[] COMMAND_STRINGS = { "quit", "q", "exit",
            "stop" };

    @Override
    public String[] getAliases() {
        return StopCommand.COMMAND_STRINGS;
    }

    @Override
    public void execute(CommandLine caller, String alias, String... params) {
        VDiskFile curLocation = caller.getCurrentLocation();
        if (curLocation != null) {
            try {
                curLocation.getDisk().close();
            } catch (IOException e) {
                caller.write(e);
            }
        }

        caller.stop();
    }

}
