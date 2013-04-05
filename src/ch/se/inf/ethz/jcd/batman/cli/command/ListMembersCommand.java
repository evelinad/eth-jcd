package ch.se.inf.ethz.jcd.batman.cli.command;

import java.io.IOException;
import java.util.Arrays;

import ch.se.inf.ethz.jcd.batman.cli.CommandLineInterface;
import ch.se.inf.ethz.jcd.batman.io.VDiskFile;
import ch.se.inf.ethz.jcd.batman.util.PrioritizedObservable;
import ch.se.inf.ethz.jcd.batman.util.PrioritizedObserver;
import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualDisk;

public class ListMembersCommand implements PrioritizedObserver<String> {

    private static final String[] COMMAND_STRINGS = { "list", "ls", "dir" };

    @Override
    public void update(PrioritizedObservable<String> observable, String data) {
        assert observable instanceof CommandLineInterface;
        CommandLineInterface cli = (CommandLineInterface) observable;

        String[] lineParts = data.split(" ");
        for (String command : COMMAND_STRINGS) {
            if (lineParts[0].equalsIgnoreCase(command)) {
                cli.setHandled();

                IVirtualDisk disk = cli.getDisk();
                if (disk == null) {
                    cli.writeln("no disk loaded. command needs loaded disk.");
                    return;
                }

                if (lineParts.length == 2) {
                    VDiskFile file = new VDiskFile(lineParts[1], disk);
                    try {
                        String[] list = file.list();
                        cli.writeln(Arrays.toString(list));
                    } catch (IOException ex) {
                        cli.writeln(String.format(
                                "following exception occured: %s",
                                ex.getMessage()));
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
