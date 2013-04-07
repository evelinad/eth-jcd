package ch.se.inf.ethz.jcd.batman.cli.command;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.InvalidPathException;

import ch.se.inf.ethz.jcd.batman.cli.CommandLineInterface;
import ch.se.inf.ethz.jcd.batman.cli.util.PrioritizedObservable;
import ch.se.inf.ethz.jcd.batman.cli.util.PrioritizedObserver;
import ch.se.inf.ethz.jcd.batman.io.HostBridge;
import ch.se.inf.ethz.jcd.batman.io.VDiskFile;
import ch.se.inf.ethz.jcd.batman.io.VDiskFileOutputStream;
import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualDisk;

public class ImportCommand implements PrioritizedObserver<String> {
    private static final String[] COMMAND_STRINGS = { "import" };

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
                        // extract host path
                        File hostFile = null;
                        try {
                            hostFile = new File(lineParts[1]);

                            if (!hostFile.exists()) {
                                cli.writeln(String.format(
                                        "host file '%s' does not exist",
                                        hostFile.getAbsolutePath()));
                                return;
                            }

                            if (!hostFile.isFile()) {
                                cli.writeln(String.format(
                                        "host file '%s' is not a file",
                                        hostFile.getAbsolutePath()));
                                return;
                            }
                        } catch (InvalidPathException ex) {
                            cli.writeln(String.format(
                                    "provided path is not valid: %s",
                                    ex.getMessage()));
                            return;
                        }

                        // extract virtual path
                        VDiskFile currentLocation = cli.getCurrentLocation();
                        String virtualPathParam = lineParts[2];

                        VDiskFile virtualFile = null;

                        if (virtualPathParam.startsWith(String
                                .valueOf(IVirtualDisk.PATH_SEPARATOR))) {
                            virtualFile = new VDiskFile(virtualPathParam,
                                    currentLocation.getDisk());
                        } else {
                            virtualFile = new VDiskFile(currentLocation,
                                    virtualPathParam);
                        }

                        if (virtualFile.exists()) {
                            cli.writeln(String.format(
                                    "virtual file '%s' already exists",
                                    virtualFile.getPath()));
                            return;
                        }

                        // move it
                        HostBridge.importFile(hostFile, virtualFile);

                        cli.writeln(String.format("imported '%s' into '%s'",
                                hostFile.getAbsolutePath(),
                                virtualFile.getPath()));

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
