package ch.se.inf.ethz.jcd.batman.cli;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;

import ch.se.inf.ethz.jcd.batman.util.PrioritizedObservable;
import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualDisk;

/**
 * This class implements an interface for the command line (CLI).
 * 
 * This class is has a basic workflow implemented for a CLI.
 * After starting this CLI it asks the user for some input,
 * after which it notifies it's observers and allows them to
 * react on the given user input. After notifying all observers
 * it will start again by asking the user for some input. 
 * 
 * There are some additional features implemented, like a changeable
 * prefix for the input and a way to inform others that a given input
 * was handled.
 * 
 * <b>Important:</b> If an instance of this class is used inside some
 * IDEs (i.e. Eclipse) it won't work as those terminals are not correctly
 * recognized as a console and therefore System.console() returns null.
 * An excpetion is thrown in such a case.
 * 
 * @see java.io.Console
 * @see ch.se.inf.ethz.jcd.batman.util.PrioritizedObserver
 */
public class CommandLineInterface extends PrioritizedObservable<String> {
	/**
	 * String that is appended to the end of the input prefix
	 */
	private static final String CLI_INPUT_PREFIX_END = "> ";
	
	/**
	 * Default input prefix. Each time the user is asked to give some input
	 * the line will start with "[input prefix][CLI_INPUT_PREFIX_END]".
	 */
	private static final String CLI_INPUT_PREFIX_NO_DISK = "no-disk";
	
	/**
	 * Output prefix. Each time the user gets some output from the
	 * application it will start with this string followed by the
	 * specific output string.
	 */
	private static final String CLI_OUTPUT_PREFIX = "=> ";
	
	private final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	private String inputPrefix;
	private boolean running;
	private IVirtualDisk disk;
	
	public CommandLineInterface() 	{		
		inputPrefix = CLI_INPUT_PREFIX_NO_DISK;
		running = false;
		disk = null;
	}
	
	/**
	 * Starts the workflow described at class level. A call to stop() will
	 * stop the workflow.
	 * @throws IOException 
	 */
	public void start() throws IOException {
		running = true;
		
		while(running) {
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
	 * Writes the given text into the console. The visible text starts
	 * with the CLI_OUTPUT_PREFIX followed by the given text.
	 * 
	 * @param text text to write into the console
	 */
	public void write(String text) {
	    System.out.print(String.format("%s%s", CLI_OUTPUT_PREFIX, text));
		System.out.flush();
	}
	
	/**
	 * Same as write(String) except that it adds a newline at the end.
	 * 
	 * @param text text to write into the console followed by a newline
	 */
	public void writeln(String text) {
		write(text + System.lineSeparator());
	}
	
	/**
	 * Sets the prefix used for user input. If the given
	 * prefix is null, the input prefix will be reset to it's
	 * default.
	 * 
	 * @see #getInputPrefix
	 * @see #CLI_INPUT_PREFIX_NO_DISK
	 * @param prefix new input prefix to use
	 */
	public void setInputPrefix(String prefix) {
		if(prefix == null) {
			inputPrefix = CLI_INPUT_PREFIX_NO_DISK;
		} else {
			inputPrefix = prefix;
		}
	}
	
	/**
	 * Returns the current input prefix used for user input
	 * 
	 * @return current input prefix
	 */
	public String getInputPrefix() {
		return inputPrefix;
	}
	
	/**
	 * Reads the input given by the user and calls all observers.
	 * @throws IOException 
	 */
	private void readCommand() throws IOException {
	    write(String.format("%s%s", inputPrefix, CLI_INPUT_PREFIX_END));
		String line = in.readLine();
		
		notifyAll(line.trim());
	}
	
	public IVirtualDisk getDisk() {
	    return disk;
	}
	
	public void setDisk(IVirtualDisk disk) {
	    this.disk = disk;
	}
}
