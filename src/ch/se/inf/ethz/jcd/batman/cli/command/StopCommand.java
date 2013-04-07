package ch.se.inf.ethz.jcd.batman.cli.command;

import java.io.IOException;

import ch.se.inf.ethz.jcd.batman.cli.CommandLineInterface;
import ch.se.inf.ethz.jcd.batman.cli.util.PrioritizedObservable;
import ch.se.inf.ethz.jcd.batman.cli.util.PrioritizedObserver;
import ch.se.inf.ethz.jcd.batman.io.VDiskFile;

/**
 * Implements a quit command that can be used to end the CLI.
 */
public class StopCommand implements PrioritizedObserver<String> {
    /**
     * Accepted commands. Case will be ignored for the check.
     */
    private static final String[] COMMAND_STRINGS = { "quit", "q", "exit",
            "stop" };

    @Override
    public void update(PrioritizedObservable<String> obs, String line) {
        assert obs instanceof CommandLineInterface;
        CommandLineInterface cli = (CommandLineInterface) obs;

        for (String commandStr : COMMAND_STRINGS) {

            if (line.equalsIgnoreCase(commandStr)) {
                cli.setHandled();
                cli.writeln("exiting...");

                VDiskFile curLocation = cli.getCurrentLocation();
                if (curLocation != null) {
                    try {
                        curLocation.getDisk().close();
                    } catch (IOException ex) {
                        cli.writeln(String.format(
                                "following exception occured: %s",
                                ex.getMessage()));
                    }
                }

                cli.stop();
            }
        }
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE - 1;
    }

}
