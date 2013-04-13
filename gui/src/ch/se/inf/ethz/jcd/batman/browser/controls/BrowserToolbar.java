package ch.se.inf.ethz.jcd.batman.browser.controls;

import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.jmx.MXNodeAlgorithm;
import com.sun.javafx.jmx.MXNodeAlgorithmContext;
import com.sun.javafx.sg.PGNode;

import ch.se.inf.ethz.jcd.batman.browser.RemoteOpenDiskDialog;
import ch.se.inf.ethz.jcd.batman.browser.ModalDialog.CloseReason;
import ch.se.inf.ethz.jcd.batman.browser.images.ImageResource;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

public class BrowserToolbar extends ToolBar {

	public BrowserToolbar() {
		// connect button
		Image connectImage = ImageResource.getImageResource().getConnect();
		Button connectButton = new Button("", new ImageView(connectImage));
		super.getItems().add(connectButton);

		connectButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				getUserInputOnDiskLocation();
			}
		});

		// disconnect button
		Image disconnectImage = ImageResource.getImageResource()
				.getDisconnect();
		Button disconnectButton = new Button("", new ImageView(disconnectImage));
		super.getItems().add(disconnectButton);

		// separator
		super.getItems().add(new Separator(Orientation.VERTICAL));

		// go to parent dir button
		Image toParentDirImage = ImageResource.getImageResource().getArrowUp();
		Button toParentDirButton = new Button("", new ImageView(
				toParentDirImage));
		super.getItems().add(toParentDirButton);

		// go back button
		Image goBackDirImage = ImageResource.getImageResource().getArrowLeft();
		Button goBackButton = new Button("", new ImageView(goBackDirImage));
		super.getItems().add(goBackButton);

		// go foreward buttin
		Image goForewardDirImage = ImageResource.getImageResource()
				.getArrowRight();
		Button goForewardButton = new Button("", new ImageView(
				goForewardDirImage));
		super.getItems().add(goForewardButton);

		// delete element button
		Image deleteImage = ImageResource.getImageResource().getDelete();
		Button deleteButton = new Button("", new ImageView(deleteImage));
		super.getItems().add(deleteButton);

		// import button
		Button importButton = new Button("import");
		super.getItems().add(importButton);

		// export button
		Button exportButton = new Button("export");
		super.getItems().add(exportButton);

		// search field
		TextField search = new TextField();
		search.setPromptText("Search");
		super.getItems().add(search);

	}

	protected void getUserInputOnDiskLocation() {
		RemoteOpenDiskDialog dialog = new RemoteOpenDiskDialog();
		dialog.showAndWait();

		if (dialog.getCloseReason() == CloseReason.OK) {
			System.out.println(dialog.getUserInput());
		}
	}
}
