package ch.se.inf.ethz.jcd.batman.browser.controls;

import java.io.File;
import java.net.URI;
import java.util.Date;
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
import ch.se.inf.ethz.jcd.batman.browser.CreateDirectoryDialog;
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
	
	private final static String EMPTY_STRING = "";

	private final GuiState guiState;

	private final Button connectButton;
	private final Button disconnectButton;
	private final Button toParentDirButton;
	private final Button goBackButton;
	private final Button goForewardButton;
	private final Button deleteButton;
	private final Button copyButton;
	private final Button cutButton;
	private final Button pasteButton;
	private final Button importFilesButton;
	private final Button importDirectoryButton;
	private final Button exportButton;
	private final Button createDirButton;
	private final Button renameButton;
	private final TextField searchField;
	private final Button advancedSearchButton;

	public BrowserToolbar(final GuiState guiState) {
		this.guiState = guiState;
		guiState.addStateListener(this);

		// connect button
		Image connectImage = ImageResource.getImageResource().connectImage();
		connectButton = new Button(EMPTY_STRING, new ImageView(connectImage));
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
		disconnectButton = new Button(EMPTY_STRING, new ImageView(disconnectImage));
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
		toParentDirButton = new Button(EMPTY_STRING, new ImageView(toParentDirImage));
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
		goBackButton = new Button(EMPTY_STRING, new ImageView(goBackDirImage));
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
		goForewardButton = new Button(EMPTY_STRING, new ImageView(goForewardDirImage));
		goForewardButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				event.consume();
				forward();
			}
		});
		super.getItems().add(goForewardButton);

		// rename button
		renameButton = new Button(EMPTY_STRING, new ImageView(ImageResource
				.getImageResource().editImage()));
		renameButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				guiState.getActiveEntryView().editSelected();
			}
		});
		super.getItems().add(renameButton);

		// delete element button
		Image deleteImage = ImageResource.getImageResource().deleteImage();
		deleteButton = new Button(EMPTY_STRING, new ImageView(deleteImage));
		deleteButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				event.consume();
				guiState.delete();
			}
		});
		super.getItems().add(deleteButton);

		// copy element button
		copyButton = new Button(EMPTY_STRING, new ImageView(ImageResource
				.getImageResource().copyImage()));
		copyButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				event.consume();
				guiState.copy();
			}
		});
		super.getItems().add(copyButton);

		// cut element button
		cutButton = new Button(EMPTY_STRING, new ImageView(ImageResource
				.getImageResource().cutImage()));
		cutButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				event.consume();
				guiState.cut();
			}
		});
		super.getItems().add(cutButton);

		// paste element button
		pasteButton = new Button(EMPTY_STRING, new ImageView(ImageResource
				.getImageResource().pasteImage()));
		pasteButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				event.consume();
				guiState.paste();
			}
		});
		super.getItems().add(pasteButton);

		// create directory button
		createDirButton = new Button(EMPTY_STRING, new ImageView(ImageResource
				.getImageResource().createFolderImage()));
		createDirButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				event.consume();
				createFolder();
			}
		});
		super.getItems().add(createDirButton);

		// import files button
		importFilesButton = new Button("import file", new ImageView(
				ImageResource.getImageResource().importFileImage()));
		importFilesButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				event.consume();
				importFiles();
			}
		});
		super.getItems().add(importFilesButton);

		// import files button
		importDirectoryButton = new Button("import directory", new ImageView(
				ImageResource.getImageResource().importDirectoryImage()));
		importDirectoryButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				event.consume();
				importDirectory();
			}
		});
		super.getItems().add(importDirectoryButton);

		// export button
		exportButton = new Button("export", new ImageView(ImageResource
				.getImageResource().exportImage()));
		exportButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				export();
			}
		});
		super.getItems().add(exportButton);

		// search field
		searchField = new TextField();
		searchField.setPromptText("Search");
		searchField.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				// start search
				if (event.getCode() == KeyCode.ENTER) {
					event.consume();
					search(searchField.getText(), false, true, true, false, true);
				}
			}
		});
		super.getItems().add(searchField);

		// advanced search button
		advancedSearchButton = new Button("advanced search", new ImageView(
				ImageResource.getImageResource().magnifierImage()));
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

	protected void createFolder() {
		CreateDirectoryDialog dialog = new CreateDirectoryDialog();
		dialog.showAndWait();

		if (dialog.getCloseReason() == CloseReason.OK) {
			Directory target = new Directory(new Path(guiState
					.getCurrentDirectory().getPath(), dialog.getUserInput()));
			target.setTimestamp(new Date().getTime());

			Task<Void> task = guiState.getController().createDirectoryTask(
					target);
			new TaskDialog(guiState, task);
		}
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
							+ File.separator
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
			searchField.setDisable(true);
			advancedSearchButton.setDisable(true);
			createDirButton.setDisable(true);
			renameButton.setDisable(true);
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
			searchField.setDisable(false);
			advancedSearchButton.setDisable(false);
			createDirButton.setDisable(false);
			renameButton.setDisable(false);
		}
	}
}
