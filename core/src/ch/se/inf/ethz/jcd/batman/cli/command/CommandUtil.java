package ch.se.inf.ethz.jcd.batman.cli.command;

import java.io.IOException;

import ch.se.inf.ethz.jcd.batman.cli.CommandLine;
import ch.se.inf.ethz.jcd.batman.io.VDiskFile;
import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualDisk;

/**
 * Class providing utility methods used by multiple CLI command implementations.
 * 
 */
public class CommandUtil {
	/**
	 * Creates a VDiskFile for the given path string that.
	 * 
	 * @param pathParam
	 *            a string representing a path
	 * @return a VDiskFile instance or null in case of an error.
	 */
	public static VDiskFile getFile(CommandLine cli, String pathParam) {
		VDiskFile file = null;
		VDiskFile curLocation = cli.getCurrentLocation();

		if (curLocation == null) {
			return null;
		}

		try {
			if (pathParam.startsWith(String
					.valueOf(IVirtualDisk.PATH_SEPARATOR))) {
				file = new VDiskFile(pathParam, curLocation.getDisk());
			} else {
				file = new VDiskFile(curLocation, pathParam);
			}
		} catch (IOException e) {
			cli.write(e);
		}

		return file;
	}
}
