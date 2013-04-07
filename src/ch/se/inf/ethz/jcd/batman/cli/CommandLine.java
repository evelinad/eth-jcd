package ch.se.inf.ethz.jcd.batman.cli;

import java.io.IOException;

import ch.se.inf.ethz.jcd.batman.io.VDiskFile;

/**
 * Interface for a command line.
 * 
 * @see CommandLineInterface
 * @see Command
 * 
 */
public interface CommandLine {

    /**
     * Starts the workflow described at class level. A call to stop() will stop
     * the workflow.
     * 
     * @throws IOException
     */
    void start() throws IOException;

    /**
     * Stops the running workflow.
     */
    void stop();

    /**
     * Attaches the given command to the CLI.
     * 
     * @param command
     *            CLI command to attach
     */
    void attachCommand(Command command);

    /**
     * Removes the given command from the CLI.
     * 
     * @param command
     *            CLI command to detach
     */
    void detachCommand(Command command);

    /**
     * Writes the given text into the console. The visible text starts with the
     * CLI_OUTPUT_PREFIX followed by the given text.
     * 
     * @param text
     *            text to write into the console
     */
    void write(String text);

    /**
     * Wrapper around {@link #write(String)} with a
     * {@link String#format(String, Object...)} like interface.
     * 
     * It's just a wrapper around {@link String#format(String, Object...)}, so
     * it can be used the exact same way.
     * 
     * @see String#format(String, Object...)
     * @param format
     *            {@link String#format(String, Object...)}
     * @param args
     *            {@link String#format(String, Object...)}
     */
    void write(String format, Object... args);

    /**
     * Same as {@link #write(String)} except that it adds a newline at the end.
     * 
     * @param text
     *            text to write into the console followed by a newline
     */
    void writeln(String text);

    /**
     * Wrapper around {@link #writeln(String)} with a
     * {@link String#format(String, Object...)} like interface.
     * 
     * It's just a wrapper around {@link String#format(String, Object...)}, so
     * it can be used the exact same way.
     * 
     * @param format
     *            {@link String#format(String, Object...)}
     * @param args
     *            {@link String#format(String, Object...)}
     */
    void writeln(String format, Object... args);

    /**
     * Writes the given exception into the console. This can be used to inform
     * the user about an exception.
     * 
     * @param ex
     *            Exception to show the user in the console
     */
    void write(Exception ex);

    /**
     * Returns the current location of the CLI inside the loaded disk.
     * 
     * @return the location at which the CLI currently is or null if no disk is
     *         loaded.
     */
    VDiskFile getCurrentLocation();

    /**
     * Sets a new location at which the CLI is currently.
     * 
     * A passed null value indicates that no disk is loaded and therefore no
     * location available. CommandLineInterface will not call any methods on the
     * old location / disk. The caller has to make sure to unload everything the
     * right way.
     * 
     * @param newLoc
     *            the new location or null if no disk is loaded.
     */
    void setCurrentLocation(VDiskFile newLoc);

}
