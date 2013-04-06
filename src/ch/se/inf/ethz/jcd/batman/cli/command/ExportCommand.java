package ch.se.inf.ethz.jcd.batman.cli.command;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.InvalidPathException;

import ch.se.inf.ethz.jcd.batman.cli.CommandLineInterface;
import ch.se.inf.ethz.jcd.batman.io.HostBridge;
import ch.se.inf.ethz.jcd.batman.io.VDiskFile;
import ch.se.inf.ethz.jcd.batman.io.VDiskFileInputStream;
import ch.se.inf.ethz.jcd.batman.util.PrioritizedObservable;
import ch.se.inf.ethz.jcd.batman.util.PrioritizedObserver;
import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualDisk;

public class ExportCommand implements PrioritizedObserver<String> {
    private static final String[] COMMAND_STRINGS = { "export" };

    @Override
    public void update(PrioritizedObservable<String> observable, String data) {
        assert observable instanceof CommandLineInterface;
        CommandLineInterface cli = (CommandLineInterface) observable;

        String[] lineParts = data.split(" ");
        for (String command : COMMAND_STRINGS) {
            if (lineParts[0].equalsIgnoreCase(command)) {
                cli.setHandled();

                // parse parameters
                try {
                    if (lineParts.length == 3) {
                        // extract virtual path
                        VDiskFile currentLocation = cli.getCurrentLocation();
                        String virtualPathParam = lineParts[1];

                        VDiskFile virtualFile = null;

                        if (virtualPathParam.startsWith(String
                                .valueOf(IVirtualDisk.PATH_SEPARATOR))) {
                            virtualFile = new VDiskFile(virtualPathParam,
                                    currentLocation.getDisk());
                        } else {
                            virtualFile = new VDiskFile(currentLocation,
                                    virtualPathParam);
                        }

                        if (!virtualFile.exists()) {
                            cli.writeln(String.format(
                                    "virtual file '%s' does not exist",
                                    virtualFile.getPath()));
                            return;
                        }

                        if (!virtualFile.isFile()) {
                            cli.writeln(String.format(
                                    "virtual file '%s' is not file",
                                    virtualFile.getPath()));
                            return;
                        }

                        // extract host path
                        File hostFile = null;
                        try {
                            hostFile = new File(lineParts[2]);

                            if (hostFile.exists()) {
                                cli.writeln(String.format(
                                        "host file '%s' already exists",
                                        hostFile.getAbsolutePath()));
                                return;
                            }
                        } catch (InvalidPathException ex) {
                            cli.writeln(String.format(
                                    "provided path is not valid: %s",
                                    ex.getMessage()));
                            return;
                        }

                        // export
                        HostBridge.exportFile(virtualFile, hostFile);

                        cli.writeln(String.format("exported '%s' into '%s'",
                                virtualFile.getPath(),
                                hostFile.getAbsolutePath()));

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
