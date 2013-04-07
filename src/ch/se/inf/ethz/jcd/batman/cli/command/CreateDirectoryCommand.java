package ch.se.inf.ethz.jcd.batman.cli.command;

import ch.se.inf.ethz.jcd.batman.cli.Command;
import ch.se.inf.ethz.jcd.batman.cli.CommandLine;
import ch.se.inf.ethz.jcd.batman.io.VDiskFile;

/**
 * Implements a create directory command for the CLI.
 *
 */
public class CreateDirectoryCommand implements Command {

    private static final String[] COMMAND_STRINGS = { "mkdir" };
    private static final String CREATE_PARENTS_FLAG = "-p";

    @Override
    public String[] getAliases() {
        return CreateDirectoryCommand.COMMAND_STRINGS;
    }

    @Override
    public void execute(CommandLine caller, String alias, String... params) {
        VDiskFile curLocation = caller.getCurrentLocation();
        if(curLocation == null) {
            caller.writeln("no disk loaded.");
            return;
        }
        
        if (params.length == 2) {
            boolean createParents = params[0]
                    .equalsIgnoreCase(CREATE_PARENTS_FLAG);
            VDiskFile dirFile = CommandUtil.getFile(caller, params[1]);

            createDir(caller, dirFile, createParents);
        } else if (params.length == 1) {
            VDiskFile dirFile = CommandUtil.getFile(caller, params[0]);

            createDir(caller, dirFile, false);
        } else {
            caller.write("expected at least one parameter, got %s",
                    params.length);
        }
    }

    private void createDir(CommandLine cli, VDiskFile dirFile,
            boolean createParents) {

        if (dirFile.exists()) {
            cli.writeln("location '%s' already exists", dirFile.getPath());
        } else {
            if (createParents) {
                if (!dirFile.mkdirs()) {
                    cli.writeln("could not create directory at '%s'",
                            dirFile.getPath());
                }
            } else {
                if (!dirFile.mkdir()) {
                    cli.writeln("could not create directory at '%s'",
                            dirFile.getPath());
                }
            }
        }
    }
}
