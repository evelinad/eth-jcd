package ch.se.inf.ethz.jcd.batman.cli.command;

import java.nio.file.FileSystems;
import java.nio.file.Path;

import ch.se.inf.ethz.jcd.batman.cli.Command;
import ch.se.inf.ethz.jcd.batman.cli.CommandLine;
import ch.se.inf.ethz.jcd.batman.io.VDiskFile;
import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualDisk;
import ch.se.inf.ethz.jcd.batman.vdisk.VirtualDisk;

/**
 * Provides a command to load a disk.
 * 
 * The command takes a host path which should be a loadable disk.
 * 
 * Example: <code>load /some/path/on/to/a/file</code>
 * 
 * 
 */
public class LoadCommand implements Command {
    private static final String[] COMMAND_STRINGS = { "load", "l" };

    @Override
    public String[] getAliases() {
        return LoadCommand.COMMAND_STRINGS;
    }

    @Override
    public void execute(CommandLine caller, String alias, String... params) {
        if (caller.getCurrentLocation() != null) {
            caller.writeln("a disk is already loaded. unload first.");
            return;
        }

        if (params.length == 1) {
            try {
                Path hostPath = FileSystems.getDefault().getPath(params[0])
                        .toAbsolutePath();

                IVirtualDisk disk = VirtualDisk.load(hostPath.toString());
                VDiskFile rootDir = new VDiskFile(
                        String.valueOf(IVirtualDisk.PATH_SEPARATOR), disk);
                
                caller.setCurrentLocation(rootDir);

            } catch (Exception e) {
                caller.write(e);
            }
        } else {
            caller.writeln("expected one parameter, %s given", params.length);
        }
    }

}
