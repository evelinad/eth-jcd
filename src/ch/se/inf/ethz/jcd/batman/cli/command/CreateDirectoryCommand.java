package ch.se.inf.ethz.jcd.batman.cli.command;

import java.io.IOException;
import java.util.Arrays;

import ch.se.inf.ethz.jcd.batman.cli.CommandLineInterface;
import ch.se.inf.ethz.jcd.batman.io.VDiskFile;
import ch.se.inf.ethz.jcd.batman.util.PrioritizedObservable;
import ch.se.inf.ethz.jcd.batman.util.PrioritizedObserver;
import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualDisk;

public class CreateDirectoryCommand implements PrioritizedObserver<String> {

    private static final String[] COMMAND_STRINGS = { "mkdir" };

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

                try {
                    VDiskFile newDir = null;
                    if (lineParts.length == 2) {
                        String pathParam = lineParts[1];
                        if (pathParam.startsWith(String
                                .valueOf(IVirtualDisk.PATH_SEPARATOR))) {
                            // absolute path

                            newDir = new VDiskFile(pathParam,
                                    currentLocation.getDisk());
                        } else {
                            newDir = new VDiskFile(currentLocation,
                                    pathParam);
                        }

                        if (newDir.exists()) {
                            cli.writeln(String.format(
                                    "location '%s' already exists", pathParam));
                        } else {
                            if (!newDir.mkdir()) {
                                cli.writeln(String.format(
                                        "could not create directory at '%s'",
                                        pathParam));
                            }
                        }

                    } else {
                        cli.writeln("not the right amount of parameters provided.");
                    }
                } catch (IOException ex) {
                    cli.writeln(String.format(
                            "following exception occured: %s", ex.getMessage()));
                }
            }
        }
    }

    @Override
    public int getPriority() {
        return 0;
    }

}
