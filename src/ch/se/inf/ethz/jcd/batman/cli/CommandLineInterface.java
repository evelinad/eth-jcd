package ch.se.inf.ethz.jcd.batman.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Paths;

import ch.se.inf.ethz.jcd.batman.io.VDiskFile;
import ch.se.inf.ethz.jcd.batman.util.PrioritizedObservable;

/**
 * This class implements an interface for the command line (CLI).
 * 
 * This class is has a basic workflow implemented for a CLI. After starting this
 * CLI it asks the user for some input, after which it notifies it's observers
 * and allows them to react on the given user input. After notifying all
 * observers it will start again by asking the user for some input.
 * 
 * There are some additional features implemented, like a changeable prefix for
 * the input and a way to inform others that a given input was handled.
 * 
 * @see java.io.Console
 * @see ch.se.inf.ethz.jcd.batman.util.PrioritizedObserver
 */
public class CommandLineInterface extends PrioritizedObservable<String> {
    /**
     * String that is used as a format string for the input prefix. The format
     * string should always use two placeholder. The first one being the disk
     * identifier and the second one the current location on the disk.
     */
    private static final String CLI_INPUT_PREFIX_FORMAT_STR = "%s:%s> ";

    /**
     * String that is used if no location is available and therefore no disk.
     */
    private static final String CLI_INPUT_PREFIX_NO_DISK = "no-disk";

    /**
     * Output prefix. Each time the user gets some output from the application
     * it will start with this string followed by the specific output string.
     */
    private static final String CLI_OUTPUT_PREFIX = "=> ";

    private final BufferedReader in = new BufferedReader(new InputStreamReader(
            System.in));
    private final PrintWriter out = new PrintWriter(System.out);
    private boolean running;
    private VDiskFile curLocation;

    public CommandLineInterface() {
        running = false;
        curLocation = null;
    }

    /**
     * Starts the workflow described at class level. A call to stop() will stop
     * the workflow.
     * 
     * @throws IOException
     */
    public void start() throws IOException {
        running = true;

        while (running) {
            readCommand();
        }
    }

    /**
     * Stops the running workflow.
     */
    public void stop() {
        running = false;
    }

    /**
     * Writes the given text into the console. The visible text starts with the
     * CLI_OUTPUT_PREFIX followed by the given text.
     * 
     * @param text
     *            text to write into the console
     */
    public void write(String text) {
        out.print(String.format("%s%s", CLI_OUTPUT_PREFIX, text));
        out.flush();
    }

    /**
     * Same as write(String) except that it adds a newline at the end.
     * 
     * @param text
     *            text to write into the console followed by a newline
     */
    public void writeln(String text) {
        write(text + System.lineSeparator());
    }

    /**
     * Reads the input given by the user and calls all observers.
     * 
     * @throws IOException
     */
    private void readCommand() throws IOException {
        String inputPrefix = null;
        if(this.curLocation == null) {
            inputPrefix = String.format(CLI_INPUT_PREFIX_FORMAT_STR, CLI_INPUT_PREFIX_NO_DISK, "");
        } else {
            String diskName = Paths.get(this.curLocation.getDisk().getHostLocation()).getFileName().toString();
            inputPrefix = String.format(CLI_INPUT_PREFIX_FORMAT_STR, diskName, this.curLocation.getPath());
        }
        
        out.print(inputPrefix);

        String line = in.readLine();

        notifyAll(line.trim());
    }

    /**
     * Returns the current location of the CLI inside the loaded disk.
     * 
     * @return
     */
    public VDiskFile getCurrentLocation() {
        return this.curLocation;
    }

    /**
     * Sets a new location at which the CLI is currently.
     * 
     * A passed null value indicates that no disk is loaded and therefore no
     * location available. CommandLineInterface will not call any methods on
     * the old location / disk. The caller has to make sure to unload everything
     * the right way.
     * 
     * @param newLoc the new location or null if no disk is loaded.
     */
    public void setCurrentLocation(VDiskFile newLoc) {
        this.curLocation = newLoc;
    }
}
