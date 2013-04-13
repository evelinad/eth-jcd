package ch.se.inf.ethz.jcd.batman.browser.controls;

import java.util.LinkedList;

import ch.se.inf.ethz.jcd.batman.model.Path;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

public class BreadcrumbBar extends HBox {
	
	private class Breadcrumb {
		public String name;
		public Path path;
	}
	
	private Path curPath;
	
	public BreadcrumbBar() {
		curPath = new Path(Path.SEPERATOR);
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
		do {
			Breadcrumb breadcrumb = new Breadcrumb();
			breadcrumb.name = current.getName();
			breadcrumb.path = current;
			
			crumbs.addFirst(breadcrumb);
			current = current.getParentPath();
		} while(current != null);
		
		// build buttons
		getChildren().clear();
		
		for(Breadcrumb crumb : crumbs) {
			Button button = new Button(crumb.name);
			getChildren().add(button);
		}
	}
}
