package ch.se.inf.ethz.jcd.batman.cli.command;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Arrays;

import ch.se.inf.ethz.jcd.batman.cli.Command;
import ch.se.inf.ethz.jcd.batman.cli.CommandLine;
import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualDisk;

/**
 * Provides a CLI command to destroy a virtual disk
 * 
 */
public class DestroyCommand implements Command {
    private static final String[] COMMAND_STRINGS = { "destroy" };

    @Override
    public String[] getAliases() {
        return DestroyCommand.COMMAND_STRINGS;
    }

    @Override
    public void execute(CommandLine caller, String alias, String... params) {
        if (params.length == 1) {
            try {
                // extract path
                Path hostPath = FileSystems.getDefault().getPath(params[0])
                        .toAbsolutePath();

                // heck for magic number
                byte[] readMagicNumber = new byte[IVirtualDisk.MAGIC_NUMBER.length];

                File hostFile = hostPath.toFile();
                FileInputStream reader = new FileInputStream(
                        hostFile.getAbsolutePath());
                reader.read(readMagicNumber);
                reader.close();

                if (Arrays.equals(IVirtualDisk.MAGIC_NUMBER, readMagicNumber)) {
                    if (hostFile.delete()) {
                        caller.writeln("virtual disk deleted");
                    } else {
                        caller.writeln("could not delete virtual disk");
                    }
                } else {
                    caller.writeln(
                            "file '%s' is not a virtual disk. command ignored.",
                            hostPath);
                }
            } catch (Exception e) {
                caller.write(e);
            }
        } else {
            caller.writeln("expected one parameter, %s given", params.length);
        }
    }

}
