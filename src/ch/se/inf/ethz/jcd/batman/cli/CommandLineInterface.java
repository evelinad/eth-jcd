package ch.se.inf.ethz.jcd.batman.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;

import ch.se.inf.ethz.jcd.batman.io.VDiskFile;

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
 */
public class CommandLineInterface implements CommandLine {
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

    /**
     * Output prefix for exceptions.
     */
    private static final String CLI_EXCEPTION_PREFIX = "!> ";

    /**
     * Format string for exception output. Should have three string place
     * holders. First placeholder is the {@link #CLI_EXCEPTION_PREFIX}, second
     * one the class name of the exception and the third one the exception
     * message itself.
     */
    private static final String CLI_EXCEPTION_FORMAT_STRING = "%s%s: %s";

    /**
     * String indicating the start of a stack trace.
     */
    private static final String CLI_STACKTRACE_BEGIN = "== STACK TRACE ==";

    /**
     * String indicating the end of a stack trace.
     */
    private static final String CLI_STACKTRACE_END = "== STACK END ==";

    /**
     * String used to separate parts of a user input
     */
    private static final String CLI_USER_INPUT_SEPARATOR = " ";

    private final BufferedReader in = new BufferedReader(new InputStreamReader(
            System.in));
    private final PrintWriter out = new PrintWriter(System.out);
    private HashMap<String, Command> aliasCommandMapping;
    private boolean running;
    private VDiskFile curLocation;

    public CommandLineInterface() {
        this.running = false;
        this.curLocation = null;
        this.aliasCommandMapping = new HashMap<String, Command>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.se.inf.ethz.jcd.batman.cli.CommandLine#start()
     */
    @Override
    public void start() throws IOException {
        this.running = true;

        while (this.running) {
            readCommand();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.se.inf.ethz.jcd.batman.cli.CommandLine#stop()
     */
    @Override
    public void stop() {
        this.running = false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * ch.se.inf.ethz.jcd.batman.cli.CommandLine#attachCommand(ch.se.inf.ethz
     * .jcd.batman.cli.Command)
     */
    @Override
    public void attachCommand(Command command) {
        String[] aliasList = command.getAliases();
        for (String alias : aliasList) {
            this.aliasCommandMapping.put(alias, command);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * ch.se.inf.ethz.jcd.batman.cli.CommandLine#detachCommand(ch.se.inf.ethz
     * .jcd.batman.cli.Command)
     */
    @Override
    public void detachCommand(Command command) {
        String[] aliasList = command.getAliases();
        for (String alias : aliasList) {
            if (this.aliasCommandMapping.get(alias) == command) {
                this.aliasCommandMapping.remove(alias);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.se.inf.ethz.jcd.batman.cli.CommandLine#write(java.lang.String)
     */
    @Override
    public void write(String text) {
        this.out.print(String.format("%s%s", CLI_OUTPUT_PREFIX, text));
        this.out.flush();
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.se.inf.ethz.jcd.batman.cli.CommandLine#write(java.lang.String,
     * java.lang.Object)
     */
    @Override
    public void write(String format, Object... args) {
        this.write(String.format(format, args));
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.se.inf.ethz.jcd.batman.cli.CommandLine#writeln(java.lang.String)
     */
    @Override
    public void writeln(String text) {
        this.write(text + System.lineSeparator());
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.se.inf.ethz.jcd.batman.cli.CommandLine#writeln(java.lang.String,
     * java.lang.Object)
     */
    @Override
    public void writeln(String format, Object... args) {
        this.write(format + System.lineSeparator(), args);
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.se.inf.ethz.jcd.batman.cli.CommandLine#write(java.lang.Exception)
     */
    @Override
    public void write(Exception ex) {
        out.println(String.format(CLI_EXCEPTION_FORMAT_STRING,
                CLI_EXCEPTION_PREFIX, ex.getClass().getName(), ex.getMessage()));

        out.println(CLI_STACKTRACE_BEGIN);
        ex.printStackTrace(out);
        out.println(CLI_STACKTRACE_END);
    }

    /**
     * Reads the input given by the user and calls all observers.
     * 
     * @throws IOException
     */
    private void readCommand() throws IOException {
        // write prefix depending on current state
        String inputPrefix = null;
        if (this.curLocation == null) {
            inputPrefix = String.format(CLI_INPUT_PREFIX_FORMAT_STR,
                    CLI_INPUT_PREFIX_NO_DISK, "");
        } else {
            String diskName = Paths
                    .get(this.curLocation.getDisk().getHostLocation())
                    .getFileName().toString();
            inputPrefix = String.format(CLI_INPUT_PREFIX_FORMAT_STR, diskName,
                    this.curLocation.getPath());
        }

        out.print(inputPrefix);
        out.flush();

        // read user input
        String userInputLine = in.readLine();

        // prepare read input and extract all needed parts.
        userInputLine = userInputLine.trim();
        String[] inputParts = userInputLine.split(CLI_USER_INPUT_SEPARATOR);
        String commandName = inputParts[0];
        String[] params = Arrays.copyOfRange(inputParts, 1, inputParts.length);

        // check if command is known. if not, inform user
        Command command = this.aliasCommandMapping.get(commandName);
        if (command == null) {
            this.writeln("given command '%s' not found.", commandName);
        } else {
            command.execute(this, commandName, params);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.se.inf.ethz.jcd.batman.cli.CommandLine#getCurrentLocation()
     */
    @Override
    public VDiskFile getCurrentLocation() {
        return this.curLocation;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * ch.se.inf.ethz.jcd.batman.cli.CommandLine#setCurrentLocation(ch.se.inf
     * .ethz.jcd.batman.io.VDiskFile)
     */
    @Override
    public void setCurrentLocation(VDiskFile newLoc) {
        this.curLocation = newLoc;
    }
}
