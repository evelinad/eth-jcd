package ch.se.inf.ethz.jcd.batman.cli.command;

import java.io.IOException;

import ch.se.inf.ethz.jcd.batman.cli.CommandLineInterface;
import ch.se.inf.ethz.jcd.batman.io.VDiskFile;
import ch.se.inf.ethz.jcd.batman.util.PrioritizedObservable;
import ch.se.inf.ethz.jcd.batman.util.PrioritizedObserver;

public class QueryCommand implements PrioritizedObserver<String> {

    private static final String[] COMMAND_STRINGS = { "query" };

    @Override
    public void update(PrioritizedObservable<String> observable, String data) {
        assert observable instanceof CommandLineInterface;
        CommandLineInterface cli = (CommandLineInterface) observable;

        String[] lineParts = data.split(" ");
        for (String command : COMMAND_STRINGS) {
            if (lineParts[0].equalsIgnoreCase(command)) {
                cli.setHandled();
                
                VDiskFile currentLocation = cli.getCurrentLocation();
                if (currentLocation == null) {
                    cli.writeln("no disk loaded. command needs loaded disk.");
                    return;
                }

                if (lineParts.length == 2) {
                	try {
                		if (lineParts[1].equals("occupied")) {
                			cli.writeln(String.format("Occupied Space in bytes: %s", currentLocation.getDisk().getOccupiedSpace()));
                		} else if (lineParts[1].equals("free")) {
                			cli.writeln(String.format("Free Space in bytes: %s", currentLocation.getDisk().getFreeSpace()));
                		} else if (lineParts[1].equals("total")) {
                			cli.writeln(String.format("Total Space in bytes: %s", currentLocation.getDisk().getSize()));
                		} else {
                			cli.writeln("Incorrect use of command query. User query with argument free/occupied or total.");
                        }
                	} catch (IOException ex) {
                        cli.writeln(String.format(
                                "following exception occured: %s", ex.getMessage()));
                    }
                } else {
                	cli.writeln("Incorrect use of command query. User query with argument free/occupied or total.");
                }
            }
        }
    }

    @Override
    public int getPriority() {
        return 0;
    }

}
