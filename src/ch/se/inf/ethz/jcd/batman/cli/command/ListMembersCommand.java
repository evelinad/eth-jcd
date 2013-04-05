package ch.se.inf.ethz.jcd.batman.cli.command;

import java.io.IOException;
import java.util.Arrays;

import ch.se.inf.ethz.jcd.batman.cli.CommandLineInterface;
import ch.se.inf.ethz.jcd.batman.io.VDiskFile;
import ch.se.inf.ethz.jcd.batman.util.PrioritizedObservable;
import ch.se.inf.ethz.jcd.batman.util.PrioritizedObserver;
import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualDisk;

public class ListMembersCommand implements PrioritizedObserver<String> {

    private static final String[] COMMAND_STRINGS = { "list", "ls", "dir" };

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
                    VDiskFile listRoot = null;
                    if (lineParts.length == 1) {
                        listRoot = currentLocation;
                    } else if (lineParts.length == 2) {
                        String pathParam = lineParts[1];
                        if (pathParam.startsWith(String
                                .valueOf(IVirtualDisk.PATH_SEPARATOR))) {
                            listRoot = new VDiskFile(pathParam,
                                    currentLocation.getDisk());
                        } else {
                            listRoot = new VDiskFile(currentLocation,
                                    pathParam);
                        }
                    } else {
                        cli.writeln("not the right amount of parameters provided.");
                        return;
                    }

                    String[] list = listRoot.list();
                    cli.writeln(Arrays.toString(list));
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
