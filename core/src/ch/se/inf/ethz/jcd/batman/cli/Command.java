package ch.se.inf.ethz.jcd.batman.cli;

/**
 * Interface for CLI commands.
 * 
 * @see CommandLineInterface
 */
public interface Command {
	/**
	 * Returns all aliases for the command.
	 * 
	 * An alias is a string that represents a call to a command. This allows
	 * commands to have more than one defining string sequence that will execute
	 * the command.
	 * 
	 * @return an array of aliases for the command
	 */
	String[] getAliases();

	/**
	 * This method starts the execution of the command.
	 * 
	 * It provides the command with the alias used to invoke the command and a
	 * list of parameters (may be empty) that the user provided.
	 * 
	 * @param caller
	 *            the caller of the command
	 * @param alias
	 *            the alias used to invoke the command
	 * @param params
	 *            provided parameters for the command. May be empty.
	 */
	void execute(CommandLine caller, String alias, String... params);
}
