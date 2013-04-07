package ch.se.inf.ethz.jcd.batman.cli.command;

import ch.se.inf.ethz.jcd.batman.cli.Command;
import ch.se.inf.ethz.jcd.batman.cli.CommandLine;
import ch.se.inf.ethz.jcd.batman.io.VDiskFile;

/**
 * Implements a move command that allows to move (and therefore rename)
 * different members of a virtual disk.
 * 
 * @see VDiskFile#renameTo(VDiskFile)
 * 
 */
public class MoveCommand implements Command {

    private static final String[] COMMAND_STRINGS = { "move", "mv" };

    @Override
    public String[] getAliases() {
        return MoveCommand.COMMAND_STRINGS;
    }

    @Override
    public void execute(CommandLine caller, String alias, String... params) {
        if (caller.getCurrentLocation() == null) {
            caller.writeln("no disk loaded.");
            return;
        }

        if (params.length == 2) {
            VDiskFile oldFile = CommandUtil.getFile(caller, params[0]);
            VDiskFile newFile = CommandUtil.getFile(caller, params[1]);

            if (!oldFile.renameTo(newFile)) {
                caller.writeln("could not move file");
            }
        } else {
            caller.writeln("expected two parameters, %s given", params.length);
        }
    }

}
