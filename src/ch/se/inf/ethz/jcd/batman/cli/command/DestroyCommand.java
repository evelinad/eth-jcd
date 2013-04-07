/**
 * 
 */
package ch.se.inf.ethz.jcd.batman.cli.command;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.FileSystems;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Arrays;

import ch.se.inf.ethz.jcd.batman.cli.CommandLineInterface;
import ch.se.inf.ethz.jcd.batman.cli.util.PrioritizedObservable;
import ch.se.inf.ethz.jcd.batman.cli.util.PrioritizedObserver;
import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualDisk;

/**
 * Provides a CLI command to destroy a virtual disk
 * 
 */
public class DestroyCommand implements PrioritizedObserver<String> {
    private static final String[] COMMAND_STRINGS = { "destroy" };

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

                    // check for magic number
                    byte[] readMagicNumber = new byte[IVirtualDisk.MAGIC_NUMBER.length];
                    try {
                        File hostFile = hostPath.toFile();
                        FileInputStream reader = new FileInputStream(hostFile.getAbsolutePath());
                        reader.read(readMagicNumber);
                        reader.close();
                        
                        if(Arrays.equals(IVirtualDisk.MAGIC_NUMBER, readMagicNumber)) {
                            if(hostFile.delete()) {
                                cli.writeln("virtual disk deleted");
                            } else {
                                cli.writeln("could not delete virtual disk");
                            }
                        } else {
                            cli.writeln(String.format("file '%s' is not a virtual disk. command ignored.", hostPath));
                        }
                    } catch (Exception e) {
                        cli.writeln(String.format("following exception occured: %s", e.getMessage()));
                    }
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
