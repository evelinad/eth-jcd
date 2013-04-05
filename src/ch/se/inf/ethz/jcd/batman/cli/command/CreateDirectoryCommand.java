package ch.se.inf.ethz.jcd.batman.cli.command;

import java.io.IOException;

import ch.se.inf.ethz.jcd.batman.cli.CommandLineInterface;
import ch.se.inf.ethz.jcd.batman.io.VDiskFile;
import ch.se.inf.ethz.jcd.batman.util.PrioritizedObservable;
import ch.se.inf.ethz.jcd.batman.util.PrioritizedObserver;
import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualDisk;

public class CreateDirectoryCommand implements PrioritizedObserver<String> {

    private static final String[] COMMAND_STRINGS = { "mkdir" };
    private static final String CREATE_PARENTS_FLAG = "-p";

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
                        String pathParam = lineParts[2];
                        if (lineParts[1].equalsIgnoreCase(CREATE_PARENTS_FLAG)) {
                            createDir(cli, currentLocation, pathParam, true);
                        } else {
                            cli.writeln(String.format("unknown option '%s'",
                                    lineParts[1]));
                        }

                    } else if (lineParts.length == 2) {
                        String pathParam = lineParts[1];
                        createDir(cli, currentLocation, pathParam, false);

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

    private void createDir(CommandLineInterface cli, VDiskFile currentLocation,
            String pathParam, boolean createParents) throws IOException {
        VDiskFile newDir;

        // check if provided path is absolute or relative
        if (pathParam.startsWith(String.valueOf(IVirtualDisk.PATH_SEPARATOR))) {
            newDir = new VDiskFile(pathParam, currentLocation.getDisk());
        } else {
            newDir = new VDiskFile(currentLocation, pathParam);
        }

        if (newDir.exists()) {
            cli.writeln(String
                    .format("location '%s' already exists", newDir.getPath()));
        } else {
            if (createParents) {
                if (!newDir.mkdirs()) {
                    cli.writeln(String.format(
                            "could not create directory at '%s'", newDir.getPath()));
                }
            } else {
                if (!newDir.mkdir()) {
                    cli.writeln(String.format(
                            "could not create directory at '%s'", newDir.getPath()));
                }
            }
        }
    }

    @Override
    public int getPriority() {
        return 0;
    }

}
