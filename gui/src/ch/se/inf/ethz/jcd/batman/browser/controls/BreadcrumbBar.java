package ch.se.inf.ethz.jcd.batman.browser.controls;

import java.util.LinkedList;

import ch.se.inf.ethz.jcd.batman.browser.DirectoryListener;
import ch.se.inf.ethz.jcd.batman.browser.GuiState;
import ch.se.inf.ethz.jcd.batman.model.Directory;
import ch.se.inf.ethz.jcd.batman.model.Path;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

public class BreadcrumbBar extends HBox implements DirectoryListener {
	
	private class Breadcrumb {
		public String name;
		public Path path;
	}
	
	private GuiState guiState;
	private Path curPath;
	
	public BreadcrumbBar(GuiState guiState) {
		this.guiState = guiState;
		curPath = new Path(Path.SEPERATOR);
		guiState.addDirectoryListener(this);
	}
	
	public void setPath(Path path) {
		curPath = path;
		refreshBreadcrumbs();
	}

	public Path getPath() {
		return curPath;
	}
	
	private void refreshBreadcrumbs() {
		LinkedList<Breadcrumb> crumbs = new LinkedList<Breadcrumb>();
		
		// build list of crumbs
		Path current = curPath;
		while(current != null) {
			Breadcrumb breadcrumb = new Breadcrumb();
			breadcrumb.name = current.getName();
			breadcrumb.path = current;
			
			crumbs.addFirst(breadcrumb);
			current = current.getParentPath();
		};
		
		// build buttons
		getChildren().clear();
		
		for(final Breadcrumb crumb : crumbs) {
			Button button = new Button(crumb.name);
			button.setOnMouseClicked(new EventHandler<MouseEvent>() {

				@Override
				public void handle(MouseEvent event) {
					guiState.setCurrentDirectory(new Directory(crumb.path));
				}
			});
			getChildren().add(button);
		}
	}

	@Override
	public void directoryChanged(Directory directory) {
		setPath((directory == null) ? null : directory.getPath());
	}
}
