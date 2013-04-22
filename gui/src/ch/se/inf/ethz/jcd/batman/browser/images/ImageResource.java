package ch.se.inf.ethz.jcd.batman.browser.images;

import javafx.scene.image.Image;

/**
 * Class holding images used by the view.
 * 
 * This class implements the Singleton Design Pattern. This way all images
 * are loaded only once.
 *
 */
public final class ImageResource {

	private static ImageResource instance;

	public static ImageResource getImageResource() {
		if (instance == null) {
			instance = new ImageResource();
		}

		return instance;
	}

	private final Image up;
	private final Image previous;
	private final Image next;
	private final Image cross;
	private final Image connect;
	private final Image disconnect;
	private final Image folder;
	private final Image file;
	private final Image cut;
	private final Image createFolder;
	private final Image copy;
	private final Image paste;
	private final Image export;
	private final Image importDir;
	private final Image importFile;
	private final Image search;

	private ImageResource() {
		up = new Image(getClass().getResourceAsStream("up.png"));
		previous = new Image(getClass().getResourceAsStream("previous.png"));
		next = new Image(getClass().getResourceAsStream("next.png"));
		cross = new Image(getClass().getResourceAsStream("cross.png"));
		connect = new Image(getClass().getResourceAsStream("connect.png"));
		disconnect = new Image(getClass().getResourceAsStream("disconnect.png"));
		folder = new Image(getClass().getResourceAsStream("folder.png"));
		file = new Image(getClass().getResourceAsStream("file.png"));
		cut = new Image(getClass().getResourceAsStream("cut.png"));
		createFolder = new Image(getClass().getResourceAsStream("folder_add.png"));
		copy = new Image(getClass().getResourceAsStream("page_white_copy.png"));
		paste = new Image(getClass().getResourceAsStream("page_white_paste.png"));
		export = new Image(getClass().getResourceAsStream("folder_go.png"));
		importDir = new Image(getClass().getResourceAsStream("folder_page.png"));
		importFile = new Image(getClass().getResourceAsStream("page_white_put.png"));
		search = new Image(getClass().getResourceAsStream("magnifier.png"));
	}

	public Image goToParentImage() {
		return up;
	}

	public Image goBackImage() {
		return previous;
	}

	public Image goForwardImage() {
		return next;
	}

	public Image deleteImage() {
		return cross;
	}

	public Image connectImage() {
		return connect;
	}

	public Image disconnectImage() {
		return disconnect;
	}

	public Image folderImage() {
		return folder;
	}

	public Image fileImage() {
		return file;
	}
	
	public Image cutImage() {
		return cut;
	}
	
	public Image createFolderImage() {
		return createFolder;
	}
	
	public Image copyImage() {
		return copy;
	}
	
	public Image pasteImage() {
		return paste;
	}

	public Image importDirectory() {
		return importDir;
	}
	
	public Image importFile() {
		return importFile;
	}
	
	public Image exportImage() {
		return export;
	}
	
	public Image magnifier() {
		return search;
	}
}
