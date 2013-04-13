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

	private ImageResource() {
		// nothing to do
	}

	public Image getArrowUp() {
		return new Image(getClass().getResourceAsStream("up.png"));
	}

	public Image getArrowLeft() {
		return new Image(getClass().getResourceAsStream("previous.png"));
	}

	public Image getArrowRight() {
		return new Image(getClass().getResourceAsStream("next.png"));
	}

	public Image getDelete() {
		return new Image(getClass().getResourceAsStream("cross.png"));
	}

	public Image getConnect() {
		return new Image(getClass().getResourceAsStream("connect.png"));
	}

	public Image getDisconnect() {
		return new Image(getClass().getResourceAsStream("disconnect.png"));
	}

}
