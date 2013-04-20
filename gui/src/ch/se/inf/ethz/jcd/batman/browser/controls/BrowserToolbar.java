package ch.se.inf.ethz.jcd.batman.browser.controls;

import java.net.URI;

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
import javafx.scene.input.MouseEvent;
import ch.se.inf.ethz.jcd.batman.browser.ErrorDialog;
import ch.se.inf.ethz.jcd.batman.browser.GuiState;
import ch.se.inf.ethz.jcd.batman.browser.ModalDialog.CloseReason;
import ch.se.inf.ethz.jcd.batman.browser.RemoteOpenDiskDialog;
import ch.se.inf.ethz.jcd.batman.browser.State;
import ch.se.inf.ethz.jcd.batman.browser.StateListener;
import ch.se.inf.ethz.jcd.batman.browser.TaskDialog;
import ch.se.inf.ethz.jcd.batman.browser.images.ImageResource;
import ch.se.inf.ethz.jcd.batman.controller.TaskController;
import ch.se.inf.ethz.jcd.batman.controller.TaskControllerFactory;
import ch.se.inf.ethz.jcd.batman.model.Directory;
import ch.se.inf.ethz.jcd.batman.model.Path;

public class BrowserToolbar extends ToolBar implements StateListener {
	
	private GuiState guiState;
	
	private Button connectButton;
	private Button disconnectButton;
	private Button toParentDirButton;
	private Button goBackButton;
	private Button goForewardButton;
	private Button deleteButton;
	private Button importButton;
	private Button exportButton;
	private TextField search;
	
	public BrowserToolbar(final GuiState guiState) {
		this.guiState = guiState;
		guiState.addStateListener(this);
		
		// connect button
		Image connectImage = ImageResource.getImageResource().getConnect();
		connectButton = new Button("", new ImageView(connectImage));
		super.getItems().add(connectButton);

		connectButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				connect();
			}
		});

		// disconnect button
		Image disconnectImage = ImageResource.getImageResource()
				.getDisconnect();
		disconnectButton = new Button("", new ImageView(disconnectImage));
		disconnectButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				disconnect();
			}
		});
		super.getItems().add(disconnectButton);

		// separator
		super.getItems().add(new Separator(Orientation.VERTICAL));

		// go to parent dir button
		Image toParentDirImage = ImageResource.getImageResource().getArrowUp();
		toParentDirButton = new Button("", new ImageView(
				toParentDirImage));
		super.getItems().add(toParentDirButton);

		// go back button
		Image goBackDirImage = ImageResource.getImageResource().getArrowLeft();
		goBackButton = new Button("", new ImageView(goBackDirImage));
		super.getItems().add(goBackButton);

		// go foreward buttin
		Image goForewardDirImage = ImageResource.getImageResource()
				.getArrowRight();
		goForewardButton = new Button("", new ImageView(
				goForewardDirImage));
		super.getItems().add(goForewardButton);

		// delete element button
		Image deleteImage = ImageResource.getImageResource().getDelete();
		deleteButton = new Button("", new ImageView(deleteImage));
		super.getItems().add(deleteButton);

		// import button
		importButton = new Button("import");
		super.getItems().add(importButton);

		// export button
		exportButton = new Button("export");
		super.getItems().add(exportButton);

		// search field
		search = new TextField();
		search.setPromptText("Search");
		super.getItems().add(search);
		stateChanged(null, guiState.getState());
	}

	protected void connect () {
		String uri = getUserInputOnDiskLocation();
		if (uri != null) {
			try {
				final TaskController controller = TaskControllerFactory.getController(new URI(uri));
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
	
	protected void disconnect () {
		Task<Void> disconnectTask = guiState.getController().createDisconnectTask();
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

	@Override
	public void stateChanged(State oldState, State newState) {
		if (newState == State.DISCONNECTED) {
			connectButton.setDisable(false);
			disconnectButton.setDisable(true);
			toParentDirButton.setDisable(true);
			goBackButton.setDisable(true);
			goForewardButton.setDisable(true);
			deleteButton.setDisable(true);
			importButton.setDisable(true);
			exportButton.setDisable(true);
			search.setDisable(true);
		} else if (newState == State.CONNECTED) {
			connectButton.setDisable(true);
			disconnectButton.setDisable(false);
			toParentDirButton.setDisable(false);
			goBackButton.setDisable(false);
			goForewardButton.setDisable(false);
			deleteButton.setDisable(false);
			importButton.setDisable(false);
			exportButton.setDisable(false);
			search.setDisable(false);
		}
	}
}
