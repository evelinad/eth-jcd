/**
 * 
 */
package ch.se.inf.ethz.jcd.batman.cli.command;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

import ch.se.inf.ethz.jcd.batman.cli.CommandLineInterface;
import ch.se.inf.ethz.jcd.batman.cli.util.PrioritizedObservable;
import ch.se.inf.ethz.jcd.batman.cli.util.PrioritizedObserver;
import ch.se.inf.ethz.jcd.batman.vdisk.VirtualDisk;

/**
 * Provides a CLI command to create a new virtual disk.
 * 
 * The command to create a disk of a size of 1 MByte the following command can
 * be used: <code>create /some/host/path/to/a/file 1048576</code>
 * 
 */
public class CreateCommand implements PrioritizedObserver<String> {
    private static final String[] COMMAND_STRINGS = { "create", "cr" };

    @Override
    public void update(PrioritizedObservable<String> observable, String data) {
        assert observable instanceof CommandLineInterface;
        CommandLineInterface cli = (CommandLineInterface) observable;

        String[] lineParts = data.split(" ");
        for (String command : COMMAND_STRINGS) {
            if (lineParts[0].equalsIgnoreCase(command)) {
                cli.setHandled();

                // parse parameters
                if (lineParts.length == 2) {
                    // extract path
                    Path hostPath = null;
                    try {
                        hostPath = FileSystems.getDefault().getPath(
                                lineParts[1]).toAbsolutePath();
                    } catch (InvalidPathException ex) {
                        cli.writeln(String.format(
                                "provided path is not valid: %s",
                                ex.getMessage()));
                        return;
                    }

                    try {
                        VirtualDisk.create(hostPath.toString()).close();
                    } catch (IOException ex) {
                        cli.writeln(String.format(
                                "following exception occured: %s",
                                ex.getMessage()));
                        return;
                    }

                    cli.writeln(String
                            .format("disk created at '%s', use load command to load the disk",
                                    hostPath));
                } else {
                    cli.writeln("not the right amount of parameters provided.");
                }
            }
        }
    }

    @Override
    public int getPriority() {
        return 0;
    }

}
