package ch.se.inf.ethz.jcd.batman.cli.command;

import java.io.IOException;

import ch.se.inf.ethz.jcd.batman.cli.CommandLineInterface;
import ch.se.inf.ethz.jcd.batman.cli.util.PrioritizedObservable;
import ch.se.inf.ethz.jcd.batman.cli.util.PrioritizedObserver;
import ch.se.inf.ethz.jcd.batman.io.VDiskFile;
import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualDisk;

public class DeleteCommand implements PrioritizedObserver<String> {

    private static final String[] COMMAND_STRINGS = { "delete" };

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

                    if (commandRoot.delete()) {
                    	cli.writeln(String.format("%s was successfully deleted", commandRoot.getPath()));
                    } else {
                    	cli.writeln(String.format("%s could not be deleted", commandRoot.getPath()));
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