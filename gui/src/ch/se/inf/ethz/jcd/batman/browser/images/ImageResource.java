package ch.se.inf.ethz.jcd.batman.browser.images;

import javafx.scene.image.Image;

public final class ImageResource {
	
	private static ImageResource instance;
	
	public static ImageResource getImageResource() {
		if (instance == null) {
			instance = new ImageResource();
		}

		return instance;
	}

	private Image up;
	private Image previous;
	private Image next;
	private Image cross;
	private Image connect;
	private Image disconnect;
	private Image folder;
	private Image file;
	
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

	public Image getArrowUp() {
		return up;
	}

	public Image getArrowLeft() {
		return previous;
	}

	public Image getArrowRight() {
		return next;
	}

	public Image getDelete() {
		return cross;
	}

	public Image getConnect() {
		return connect;
	}

	public Image getDisconnect() {
		return disconnect;
	}

	public Image getFolder() {
		return folder;
	}
	
	public Image getFile() {
		return file;
	}
	
}
