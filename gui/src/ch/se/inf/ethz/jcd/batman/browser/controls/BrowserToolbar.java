package ch.se.inf.ethz.jcd.batman.browser.controls;

import java.io.File;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import ch.se.inf.ethz.jcd.batman.browser.ErrorDialog;
import ch.se.inf.ethz.jcd.batman.browser.GuiState;
import ch.se.inf.ethz.jcd.batman.browser.ModalDialog.CloseReason;
import ch.se.inf.ethz.jcd.batman.browser.RemoteOpenDiskDialog;
import ch.se.inf.ethz.jcd.batman.browser.SearchDialog;
import ch.se.inf.ethz.jcd.batman.browser.State;
import ch.se.inf.ethz.jcd.batman.browser.StateListener;
import ch.se.inf.ethz.jcd.batman.browser.TaskDialog;
import ch.se.inf.ethz.jcd.batman.browser.images.ImageResource;
import ch.se.inf.ethz.jcd.batman.controller.TaskController;
import ch.se.inf.ethz.jcd.batman.controller.TaskControllerFactory;
import ch.se.inf.ethz.jcd.batman.model.Directory;
import ch.se.inf.ethz.jcd.batman.model.Entry;
import ch.se.inf.ethz.jcd.batman.model.Path;
import ch.se.inf.ethz.jcd.batman.model.SearchDirectory;

public class BrowserToolbar extends ToolBar implements StateListener {

	private GuiState guiState;

	private Button connectButton;
	private Button disconnectButton;
	private Button toParentDirButton;
	private Button goBackButton;
	private Button goForewardButton;
	private Button deleteButton;
	private Button copyButton;
	private Button cutButton;
	private Button pasteButton;
	private Button importFilesButton;
	private Button importDirectoryButton;
	private Button exportButton;
	private TextField search;

	private Button advancedSearchButton;

	public BrowserToolbar(final GuiState guiState) {
		this.guiState = guiState;
		guiState.addStateListener(this);

		// connect button
		Image connectImage = ImageResource.getImageResource().connectImage();
		connectButton = new Button("", new ImageView(connectImage));
		super.getItems().add(connectButton);

		connectButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				event.consume();
				connect();
			}
		});

		// disconnect button
		Image disconnectImage = ImageResource.getImageResource()
				.disconnectImage();
		disconnectButton = new Button("", new ImageView(disconnectImage));
		disconnectButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				event.consume();
				disconnect();
			}
		});
		super.getItems().add(disconnectButton);

		// separator
		super.getItems().add(new Separator(Orientation.VERTICAL));

		// go to parent dir button
		Image toParentDirImage = ImageResource.getImageResource()
				.goToParentImage();
		toParentDirButton = new Button("", new ImageView(toParentDirImage));
		toParentDirButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				event.consume();
				toParentDir();
			}
		});
		super.getItems().add(toParentDirButton);

		// go back button
		Image goBackDirImage = ImageResource.getImageResource().goBackImage();
		goBackButton = new Button("", new ImageView(goBackDirImage));
		goBackButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				event.consume();
				back();
			}
		});
		super.getItems().add(goBackButton);

		// go foreward button
		Image goForewardDirImage = ImageResource.getImageResource()
				.goForwardImage();
		goForewardButton = new Button("", new ImageView(goForewardDirImage));
		goForewardButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				event.consume();
				forward();
			}
		});
		super.getItems().add(goForewardButton);

		// delete element button
		Image deleteImage = ImageResource.getImageResource().deleteImage();
		deleteButton = new Button("", new ImageView(deleteImage));
		deleteButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				event.consume();
				guiState.delete();
			}
		});
		super.getItems().add(deleteButton);

		// copy element button
		copyButton = new Button("copy");
		copyButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				event.consume();
				guiState.copy();
			}
		});
		super.getItems().add(copyButton);

		// cut element button
		cutButton = new Button("cut");
		cutButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				event.consume();
				guiState.cut();
			}
		});
		super.getItems().add(cutButton);

		// paste element button
		pasteButton = new Button("paste");
		pasteButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				event.consume();
				guiState.paste();
			}
		});
		super.getItems().add(pasteButton);

		// import files button
		importFilesButton = new Button("import files");
		importFilesButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				event.consume();
				importFiles();
			}
		});
		super.getItems().add(importFilesButton);

		// import files button
		importDirectoryButton = new Button("import directory");
		importDirectoryButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				event.consume();
				importDirectory();
			}
		});
		super.getItems().add(importDirectoryButton);

		// export button
		exportButton = new Button("export");
		exportButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				export();
			}
		});
		super.getItems().add(exportButton);

		// search field
		search = new TextField();
		search.setPromptText("Search");
		search.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				// start search
				if (event.getCode() == KeyCode.ENTER) {
					event.consume();
					search(search.getText(), false, true, true, false, true);
				}
			}
		});
		super.getItems().add(search);

		// advanced search button
		advancedSearchButton = new Button("Advanced Search");
		advancedSearchButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				event.consume();
				advancedSearch();
			}
		});
		super.getItems().add(advancedSearchButton);

		// add shortcuts for toolbar buttons
		guiState.getPrimaryStage()
				.getScene()
				.addEventFilter(KeyEvent.KEY_RELEASED,
						new EventHandler<KeyEvent>() {
							@Override
							public void handle(KeyEvent event) {
								if (event.isControlDown()) {
									if (event.getCode() == KeyCode.F
											&& !advancedSearchButton
													.isDisabled()) {
										event.consume();
										advancedSearch();
									}

									if (event.getCode() == KeyCode.O
											&& !connectButton.isDisabled()) {
										event.consume();
										connect();
									}

									if (event.getCode() == KeyCode.D
											&& !disconnectButton.isDisabled()) {
										event.consume();
										disconnect();
									}

									if (event.getCode() == KeyCode.C
											&& !copyButton.isDisabled()) {
										event.consume();
										guiState.copy();
									}

									if (event.getCode() == KeyCode.X
											&& !cutButton.isDisabled()) {
										event.consume();
										guiState.cut();
									}

									if (event.getCode() == KeyCode.V
											&& !pasteButton.isDisabled()) {
										event.consume();
										guiState.paste();
									}
								}

								if (event.isAltDown()) {
									if (event.getCode() == KeyCode.LEFT
											&& !goBackButton.isDisabled()) {
										event.consume();
										back();
									}

									if (event.getCode() == KeyCode.RIGHT
											&& !goForewardButton.isDisabled()) {
										event.consume();
										forward();
									}

									if (event.getCode() == KeyCode.UP
											&& !toParentDirButton.isDisabled()) {
										event.consume();
										toParentDir();
									}
								}
							}
						});

		stateChanged(null, guiState.getState());
	}

	protected void export() {
		Entry[] selectedEntries = guiState.getSelectedEntries();
		if (selectedEntries != null && selectedEntries.length > 0) {
			DirectoryChooser directoryChooser = new DirectoryChooser();
			directoryChooser.setTitle("Choose export directory");
			File exportDirectory = directoryChooser.showDialog(guiState
					.getPrimaryStage());
			if (exportDirectory != null) {
				String[] destinationPaths = new String[selectedEntries.length];
				for (int i = 0; i < selectedEntries.length; i++) {
					destinationPaths[i] = exportDirectory.getAbsolutePath()
							+ java.io.File.separator
							+ selectedEntries[i].getPath().getName();
				}
				Task<Void> exportTask = guiState.getController()
						.createExportTask(selectedEntries, destinationPaths);
				new TaskDialog(guiState, exportTask);
			}

		}
	}

	protected void importFiles() {
		FileChooser fileChooser = new FileChooser();
		List<File> importFiles = fileChooser.showOpenMultipleDialog(guiState
				.getPrimaryStage());
		if (importFiles != null && !importFiles.isEmpty()) {
			List<String> sourcePaths = new LinkedList<String>();
			List<Path> destinationPath = new LinkedList<Path>();
			for (File file : importFiles) {
				sourcePaths.add(file.getAbsolutePath());
				destinationPath.add(new Path(guiState.getCurrentDirectory()
						.getPath(), file.getName()));
			}
			Task<Void> importTask = guiState.getController().createImportTask(
					sourcePaths.toArray(new String[sourcePaths.size()]),
					destinationPath.toArray(new Path[destinationPath.size()]));
			new TaskDialog(guiState, importTask);
		}
	}

	protected void importDirectory() {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		File importFiles = directoryChooser.showDialog(guiState
				.getPrimaryStage());
		if (importFiles != null) {
			Task<Void> importTask = guiState.getController().createImportTask(
					new String[] { importFiles.getAbsolutePath() },
					new Path[] { new Path(guiState.getCurrentDirectory()
							.getPath(), importFiles.getName()) });
			new TaskDialog(guiState, importTask);
		}
	}

	protected void forward() {
		guiState.forwardToNextDirectoy();
	}

	protected void back() {
		guiState.backToPreviousDirectory();
	}

	protected void toParentDir() {
		Path parentPath = guiState.getCurrentDirectory().getPath()
				.getParentPath();
		if (parentPath != null) {
			guiState.setCurrentDirectory(new Directory(parentPath));
		}
	}

	protected void connect() {
		String uri = getUserInputOnDiskLocation();
		if (uri != null) {
			try {
				final TaskController controller = TaskControllerFactory
						.getController(new URI(uri));
				Task<Void> connectTask = controller.createConnectTask(true);
				new TaskDialog(guiState, connectTask) {
					protected void succeeded(WorkerStateEvent event) {
						guiState.setController(controller);
						guiState.setState(State.CONNECTED);
						guiState.setCurrentDirectory(new Directory(new Path()));
					}
				};
			} catch (Exception e) {
				new ErrorDialog("Error", e.getMessage()).showAndWait();
				return;
			}
		}
	}

	protected void disconnect() {
		Task<Void> disconnectTask = guiState.getController()
				.createDisconnectTask();
		new TaskDialog(guiState, disconnectTask) {
			@Override
			protected void succeeded(WorkerStateEvent event) {
				guiState.setController(null);
				guiState.setCurrentDirectory(null);
				guiState.setState(State.DISCONNECTED);
			}
		};
	}

	protected String getUserInputOnDiskLocation() {
		RemoteOpenDiskDialog dialog = new RemoteOpenDiskDialog();
		dialog.showAndWait();

		if (dialog.getCloseReason() == CloseReason.OK) {
			return dialog.getUserInput();
		}
		return null;
	}

	protected void advancedSearch() {
		final SearchDialog dialog = new SearchDialog();
		dialog.showAndWait();

		if (dialog.getCloseReason() == CloseReason.OK) {
			search(dialog.getSearchTerm(), dialog.isRegex(),
					dialog.checkFiles(), dialog.checkFolders(),
					dialog.isCaseSensitive(), dialog.checkSubFolders());
		}
	}

	protected void search(final String term, final boolean isRegex,
			final boolean checkFiles, final boolean checkFolders,
			final boolean isCaseSensitive, final boolean checkChildren) {
		SearchDirectory search = new SearchDirectory(guiState
				.getCurrentDirectory().getPath(), term, isRegex, checkFiles,
				checkFolders, isCaseSensitive, checkChildren);
		guiState.setCurrentDirectory(search);
	}

	@Override
	public void stateChanged(State oldState, State newState) {
		if (newState == State.DISCONNECTED) {
			connectButton.setDisable(false);
			disconnectButton.setDisable(true);
			toParentDirButton.setDisable(true);
			goBackButton.setDisable(true);
			goForewardButton.setDisable(true);
			deleteButton.setDisable(true);
			copyButton.setDisable(true);
			cutButton.setDisable(true);
			pasteButton.setDisable(true);
			importFilesButton.setDisable(true);
			importDirectoryButton.setDisable(true);
			exportButton.setDisable(true);
			search.setDisable(true);
			advancedSearchButton.setDisable(true);
		} else if (newState == State.CONNECTED) {
			connectButton.setDisable(true);
			disconnectButton.setDisable(false);
			toParentDirButton.setDisable(false);
			goBackButton.setDisable(false);
			goForewardButton.setDisable(false);
			deleteButton.setDisable(false);
			copyButton.setDisable(false);
			cutButton.setDisable(false);
			pasteButton.setDisable(false);
			importFilesButton.setDisable(false);
			importDirectoryButton.setDisable(false);
			exportButton.setDisable(false);
			search.setDisable(false);
			advancedSearchButton.setDisable(false);
		}
	}
}
