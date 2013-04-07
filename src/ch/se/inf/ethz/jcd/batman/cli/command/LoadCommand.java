package ch.se.inf.ethz.jcd.batman.cli.command;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

import ch.se.inf.ethz.jcd.batman.cli.CommandLineInterface;
import ch.se.inf.ethz.jcd.batman.cli.util.PrioritizedObservable;
import ch.se.inf.ethz.jcd.batman.cli.util.PrioritizedObserver;
import ch.se.inf.ethz.jcd.batman.io.VDiskFile;
import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualDisk;
import ch.se.inf.ethz.jcd.batman.vdisk.VirtualDisk;

/**
 * Provides a command to load a disk.
 * 
 * The command takes a host path which should be a loadable
 * disk.
 * 
 * Example:
 * <code>load /some/path/on/to/a/file</code>
 * 
 *
 */
public class LoadCommand implements PrioritizedObserver<String> {
	private static final String[] COMMAND_STRINGS = {"load", "l"};
	
	@Override
	public void update(PrioritizedObservable<String> observable, String data) {
		assert observable instanceof CommandLineInterface;
		CommandLineInterface cli = (CommandLineInterface)observable;
		
		String[] lineParts = data.split(" ");
		for(String command : COMMAND_STRINGS) {
			if(lineParts[0].equalsIgnoreCase(command)) {
				cli.setHandled();
				
				// parse parameters
				if(lineParts.length == 2) {
					// extract path
					Path hostPath = null;
					try {
						hostPath = FileSystems.getDefault().getPath(lineParts[1]).toAbsolutePath();
					} catch (InvalidPathException ex) {
						cli.writeln(String.format("provided path is not valid: %s", ex.getMessage()));
						return;
					}
					
					if(cli.getCurrentLocation() != null) {
					    cli.writeln("a disk is still loaded. unload first.");
					    return;
					}
					
					try {
					    IVirtualDisk disk = VirtualDisk.load(hostPath.toString());
                        VDiskFile rootDir = new VDiskFile("/", disk);
                        
                        cli.setCurrentLocation(rootDir);
                        
                    } catch (IOException ex) {
                        cli.writeln(String.format(
                                "following exception occured: %s",
                                ex.getMessage()));
                        return;
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
