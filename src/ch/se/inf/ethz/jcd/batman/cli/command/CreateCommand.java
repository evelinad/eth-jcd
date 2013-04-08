/**
 * 
 */
package ch.se.inf.ethz.jcd.batman.cli.command;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import ch.se.inf.ethz.jcd.batman.cli.Command;
import ch.se.inf.ethz.jcd.batman.cli.CommandLine;
import ch.se.inf.ethz.jcd.batman.vdisk.impl.VirtualDisk;

/**
 * Provides a CLI command to create a new virtual disk.
 * 
 * The command to create a disk the following command can be used:
 * <code>create /some/host/path/to/a/file</code>
 * 
 */
public class CreateCommand implements Command {
    private static final String[] COMMAND_STRINGS = { "create", "c" };

    @Override
    public String[] getAliases() {
        return CreateCommand.COMMAND_STRINGS;
    }

    @Override
    public void execute(CommandLine caller, String alias, String... params) {
        if (params.length == 1) {
            try {
                Path hostPath = FileSystems.getDefault().getPath(params[0])
                        .toAbsolutePath();
                VirtualDisk.create(hostPath.toString()).close();
                caller.writeln(
                        "disk created at '%s'. Use load command to load the disk",
                        hostPath);
            } catch (IOException e) {
                caller.write(e);
            }
        } else {
            caller.writeln("expected one parameter, %s given", params.length);
        }
    }

}
