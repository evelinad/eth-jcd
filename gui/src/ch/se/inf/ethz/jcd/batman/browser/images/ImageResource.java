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

	private ImageResource() {
		up = new Image(getClass().getResourceAsStream("up.png"));
		previous = new Image(getClass().getResourceAsStream("previous.png"));
		next = new Image(getClass().getResourceAsStream("next.png"));
		cross = new Image(getClass().getResourceAsStream("cross.png"));
		connect = new Image(getClass().getResourceAsStream("connect.png"));
		disconnect = new Image(getClass().getResourceAsStream("disconnect.png"));
		folder = new Image(getClass().getResourceAsStream("folder.png"));
		file = new Image(getClass().getResourceAsStream("file.png"));
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

}
