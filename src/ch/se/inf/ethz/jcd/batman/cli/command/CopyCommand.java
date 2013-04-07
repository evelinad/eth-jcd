package ch.se.inf.ethz.jcd.batman.cli.command;

import ch.se.inf.ethz.jcd.batman.cli.Command;
import ch.se.inf.ethz.jcd.batman.cli.CommandLine;
import ch.se.inf.ethz.jcd.batman.io.VDiskFile;

/**
 * Implements a command to copy files around on the virtual disk.
 * 
 * @see VDiskFile#copyTo(VDiskFile)
 */
public class CopyCommand implements Command {

    private static final String[] COMMAND_STRINGS = { "copy", "cp" };

    @Override
    public String[] getAliases() {
        return CopyCommand.COMMAND_STRINGS;
    }

    @Override
    public void execute(CommandLine caller, String alias, String... params) {
        if (caller.getCurrentLocation() == null) {
            caller.writeln("no disk loaded.");
            return;
        }

        if (params.length == 2) {
            VDiskFile source = CommandUtil.getFile(caller, params[0]);
            VDiskFile target = CommandUtil.getFile(caller, params[1]);

            if (!source.copyTo(target)) {
                caller.writeln("could not copy '%s' to '%s'", source.getPath(),
                        target.getPath());
            }
        } else {
            caller.writeln("expected two parameters, %s given.", params.length);
        }

    }

}
