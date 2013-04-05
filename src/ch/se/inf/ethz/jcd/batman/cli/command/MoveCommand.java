package ch.se.inf.ethz.jcd.batman.cli.command;

import java.io.IOException;
import java.util.Arrays;

import ch.se.inf.ethz.jcd.batman.cli.CommandLineInterface;
import ch.se.inf.ethz.jcd.batman.io.VDiskFile;
import ch.se.inf.ethz.jcd.batman.util.PrioritizedObservable;
import ch.se.inf.ethz.jcd.batman.util.PrioritizedObserver;
import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualDisk;

public class MoveCommand implements PrioritizedObserver<String> {

    private static final String[] COMMAND_STRINGS = { "move", "mv" };

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
                    if (lineParts.length == 3) {
                        String oldPath = lineParts[1];
                        String newPath = lineParts[2];
                        
                        VDiskFile oldFile = getVDiskFile(oldPath, currentLocation);
                        VDiskFile newFile = getVDiskFile(newPath, currentLocation);
                        
                        if(!oldFile.renameTo(newFile)) {
                            cli.writeln("could not move file");
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

    private VDiskFile getVDiskFile(String path, VDiskFile currentLocation) throws IOException {
        if(path.startsWith(String.valueOf(IVirtualDisk.PATH_SEPARATOR))) {
            return new VDiskFile(path, currentLocation.getDisk());
        } else {
            return new VDiskFile(currentLocation, path);
        }
    }

    @Override
    public int getPriority() {
        return 0;
    }

}
