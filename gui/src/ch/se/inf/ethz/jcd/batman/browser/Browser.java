package ch.se.inf.ethz.jcd.batman.browser;

import ch.se.inf.ethz.jcd.batman.browser.controls.BreadcrumbBar;
import ch.se.inf.ethz.jcd.batman.browser.controls.BrowserToolbar;
import ch.se.inf.ethz.jcd.batman.browser.controls.EntryView;
import ch.se.inf.ethz.jcd.batman.model.Path;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class Browser extends BorderPane {
	
	private GuiState guiState;
	
	// top parts
	private VBox topBox;
	private ToolBar toolBar;

	// left parts
	private TreeView<String> dirTree;
	private TreeItem<String> dirRoot;

	// center parts
	private EntryView entryView;
	
	// bottom parts
	private BreadcrumbBar breadcrumbs;

	public Browser() {
		guiState = new GuiState();
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
		breadcrumbs.setPath(new Path("/hallo/welt/spiegel"));
	}

	private void initializeCenter() {
		entryView = new EntryView(guiState);
	}

	private void initializeLeft() {
		dirRoot = new TreeItem<String>("/");
		dirTree = new TreeView<String>(dirRoot);
	}

	private void initializeTop() {
		// create container for elements of the top part
		topBox = new VBox();

		// create toolbar
		toolBar = new BrowserToolbar(guiState);
		topBox.getChildren().add(toolBar);
	}
}
