package ch.se.inf.ethz.jcd.batman.cli.command;

import java.io.File;

import java.io.IOException;

import ch.se.inf.ethz.jcd.batman.cli.Command;
import ch.se.inf.ethz.jcd.batman.cli.CommandLine;
import ch.se.inf.ethz.jcd.batman.io.VDiskFile;
import ch.se.inf.ethz.jcd.batman.io.util.DefaultMover;
import ch.se.inf.ethz.jcd.batman.io.util.HostBridge;

/**
 * Implements an import command that can be used to import host files into the
 * virtual disk.
 * 
 * @see HostBridge#importFile(File, VDiskFile,
 *      ch.se.inf.ethz.jcd.batman.io.util.DataMover)
 */
public class ImportCommand implements Command {
    private static final String[] COMMAND_STRINGS = { "import" };

    @Override
    public String[] getAliases() {
        return ImportCommand.COMMAND_STRINGS;
    }

    @Override
    public void execute(CommandLine caller, String alias, String... params) {
        if (caller.getCurrentLocation() == null) {
            caller.writeln("no disk loaded.");
            return;
        }

        if (params.length == 2) {
            // extract host file
            File hostFile = new File(params[0]);

            if (!hostFile.exists()) {
                caller.writeln("given host file '%s' does not exist",
                        hostFile.getPath());
                return;
            }

            // extract virtual file
            VDiskFile virtualFile = CommandUtil.getFile(caller, params[1]);
            if (virtualFile == null) {
                caller.writeln("given virtual file path '%s' not valid",
                        params[1]);
                return;
            }

            if (virtualFile.exists()) {
                caller.writeln("given virtual file '%s' already exists",
                        virtualFile.getPath());
                return;
            }

            // import it
            try {
                HostBridge
                        .importFile(hostFile, virtualFile, new DefaultMover());
                caller.writeln("imported '%s' into '%s'", hostFile.getPath(),
                        virtualFile.getPath());
            } catch (IOException e) {
                caller.write(e);
            }

        } else {
            caller.writeln("expected two parameters, %s given", params.length);
        }
    }

}
