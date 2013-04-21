package ch.se.inf.ethz.jcd.batman.browser.util;

import java.io.IOException;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import ch.se.inf.ethz.jcd.batman.browser.ErrorDialog;
import ch.se.inf.ethz.jcd.batman.browser.GuiState;
import ch.se.inf.ethz.jcd.batman.browser.TaskDialog;
import ch.se.inf.ethz.jcd.batman.model.Entry;
import ch.se.inf.ethz.jcd.batman.model.File;

/**
 * Util methods that can be used to execute common work on the underlying
 * system.
 * 
 * 
 */
public class HostUtil {

	/**
	 * Opens the given file with the default application on the host system
	 * 
	 * @param file
	 *            file to open
	 */
	public static void openFile(File file, GuiState state) {
		try {
			final java.io.File hostFile = java.io.File.createTempFile(
					"open_disk_out", file.getPath().getName());
			hostFile.delete();

			Task<Void> task = state.getController().createExportTask(
					new Entry[] { file }, new String[] { hostFile.getPath() });

			new TaskDialog(state, task) {
				@Override
				protected void succeeded(WorkerStateEvent event) {
					String osName = System.getProperty("os.name").toLowerCase();
					if (osName.contains("mac")) {
						try {
							ProcessBuilder processBuilder = new ProcessBuilder(
									"open", hostFile.getPath());
							processBuilder.start();

						} catch (IOException e) {
							new ErrorDialog("Open File failed", e.getMessage())
									.showAndWait();
						}
					} else if (osName.contains("windows")) {
						try {
							ProcessBuilder processBuilder = new ProcessBuilder(
									"RUNDLL32.EXE SHELL32.DLL,OpenAs_RunDLL",
									hostFile.getPath());
							processBuilder.start();

						} catch (IOException e) {
							new ErrorDialog("Open File failed", e.getMessage())
									.showAndWait();
						}
					} else if (osName.contains("linux")) {
						try {
							ProcessBuilder processBuilder = new ProcessBuilder(
									"gnome-open", hostFile.getPath());
							processBuilder.start();

						} catch (IOException e) {
							new ErrorDialog("Open File failed", e.getMessage())
									.showAndWait();
						}
					}
				}
			};

		} catch (IOException e) {
			new ErrorDialog("Open File failed", e.getMessage()).showAndWait();
		}
	}
}
