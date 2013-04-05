package ch.se.inf.ethz.jcd.batman.cli.command;

import java.io.IOException;

import ch.se.inf.ethz.jcd.batman.cli.CommandLineInterface;
import ch.se.inf.ethz.jcd.batman.io.VDiskFile;
import ch.se.inf.ethz.jcd.batman.util.PrioritizedObservable;
import ch.se.inf.ethz.jcd.batman.util.PrioritizedObserver;
import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualDisk;

public class SizeCommand implements PrioritizedObserver<String> {

    private static final String[] COMMAND_STRINGS = { "size" };

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
                    VDiskFile commandRoot = null;
                    if (lineParts.length == 1) {
                        commandRoot = currentLocation;
                    } else if (lineParts.length == 2) {
                        String pathParam = lineParts[1];
                        
                        if (pathParam.startsWith(String
                                .valueOf(IVirtualDisk.PATH_SEPARATOR))) {
                            commandRoot = new VDiskFile(pathParam,
                                    currentLocation.getDisk());
                        } else {
                            commandRoot = new VDiskFile(currentLocation,
                                    pathParam);
                        }
                    } else {
                        cli.writeln("not the right amount of parameters provided.");
                        return;
                    }

                    cli.writeln(String.format("%s: %s", commandRoot.getPath(), commandRoot.getTotalSpace()));
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
