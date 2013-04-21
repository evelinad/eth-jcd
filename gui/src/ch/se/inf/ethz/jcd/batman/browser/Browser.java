package ch.se.inf.ethz.jcd.batman.browser;

import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ch.se.inf.ethz.jcd.batman.browser.controls.BreadcrumbBar;
import ch.se.inf.ethz.jcd.batman.browser.controls.BrowserToolbar;
import ch.se.inf.ethz.jcd.batman.browser.controls.DirectoryTree;
import ch.se.inf.ethz.jcd.batman.browser.controls.EntryView;

public class Browser extends BorderPane {
	
	private GuiState guiState;
	
	// top parts
	private VBox topBox;
	private ToolBar toolBar;

	// left parts
	private TreeView<String> dirTree;

	// center parts
	private EntryView entryView;
	
	// bottom parts
	private BreadcrumbBar breadcrumbs;

	public Browser(Stage primaryStage) {
		guiState = new GuiState(primaryStage);
		initializeTop();
		initializeLeft();
		initializeCenter();
		initializeBottom();

		super.setTop(topBox);
		super.setLeft(dirTree);
		super.setCenter(entryView);
		super.setBottom(breadcrumbs);
	}

	private void initializeBottom() {
		breadcrumbs = new BreadcrumbBar(guiState);
	}

	private void initializeCenter() {
		entryView = new EntryView(guiState);
	}

	private void initializeLeft() {
		dirTree = new DirectoryTree(guiState);
	}

	private void initializeTop() {
		// create container for elements of the top part
		topBox = new VBox();

		// create toolbar
		toolBar = new BrowserToolbar(guiState);
		topBox.getChildren().add(toolBar);
	}
}
