package ch.se.inf.ethz.jcd.batman.cli.command;

import java.io.File;

import java.io.IOException;

import ch.se.inf.ethz.jcd.batman.cli.Command;
import ch.se.inf.ethz.jcd.batman.cli.CommandLine;
import ch.se.inf.ethz.jcd.batman.io.HostBridge;
import ch.se.inf.ethz.jcd.batman.io.VDiskFile;

/**
 * Implements an export command that can be used to export a file on the virtual
 * disk into a file on the host's disk.
 * 
 * @see HostBridge#exportFile(VDiskFile, File)
 */
public class ExportCommand implements Command {
    private static final String[] COMMAND_STRINGS = { "export" };

    @Override
    public String[] getAliases() {
        return ExportCommand.COMMAND_STRINGS;
    }

    @Override
    public void execute(CommandLine caller, String alias, String... params) {
        if(caller.getCurrentLocation() == null) {
            caller.writeln("no disk loaded.");
            return;
        }
        
        if (params.length == 2) {
            // extract virtual file
            VDiskFile virtualFile = CommandUtil.getFile(caller, params[0]);
            if (virtualFile == null) {
                caller.writeln("given virtual file path '%s' not valid",
                        params[1]);
                return;
            }

            if (!virtualFile.exists()) {
                caller.writeln("given virtual file '%s' does NOT exist",
                        virtualFile.getPath());
                return;
            }

            // extract host file
            File hostFile = new File(params[1]);

            if (hostFile.exists()) {
                caller.writeln("given host file '%s' already exists",
                        hostFile.getPath());
                return;
            }

            // export it
            try {
                HostBridge.exportFile(virtualFile, hostFile);
                caller.writeln("exported '%s' into '%s'",
                        virtualFile.getPath(), hostFile.getPath());
            } catch (IOException e) {
                caller.write(e);
            }
        } else {
            caller.writeln("expected two parameters, %s given", params.length);
        }
    }
}
