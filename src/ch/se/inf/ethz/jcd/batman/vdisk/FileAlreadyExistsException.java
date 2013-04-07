package ch.se.inf.ethz.jcd.batman.vdisk;

/**
 * Checked exception thrown when an attempt is made to create a file or
 * directory and a file of that name already exists.
 */
public class FileAlreadyExistsException extends VirtualDiskException {

    private static final long serialVersionUID = -7734483892704484945L;

}
