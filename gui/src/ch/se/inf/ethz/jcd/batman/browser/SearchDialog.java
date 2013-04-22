package ch.se.inf.ethz.jcd.batman.browser;

import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

public class SearchDialog extends ModalDialog {

	private TextField searchTermField;
	private CheckBox regexCheckBox;
	private CheckBox filesCheckBox;
	private CheckBox foldersCheckBox;
	private CheckBox caseSensitiveCheckBox;
	private CheckBox subFoldersCheckBox;
	private Button searchButton;
	private Button cancelButton;

	public SearchDialog() {
		super();
		setTitle("Search");

		Label termLabel = new Label("Search term:");
		getContainer().add(termLabel, 0, 0);

		searchTermField = new TextField();
		searchTermField.setPromptText("search term");
		getContainer().add(searchTermField, 1, 0);

		regexCheckBox = new CheckBox("is RegEx");
		getContainer().add(regexCheckBox, 1, 1);

		filesCheckBox = new CheckBox("check files");
		filesCheckBox.setSelected(true);
		getContainer().add(filesCheckBox, 1, 2);

		foldersCheckBox = new CheckBox("check folders");
		foldersCheckBox.setSelected(true);
		getContainer().add(foldersCheckBox, 1, 3);

		caseSensitiveCheckBox = new CheckBox("case sensitive");
		getContainer().add(caseSensitiveCheckBox, 1, 4);

		subFoldersCheckBox = new CheckBox("check sub folders");
		subFoldersCheckBox.setSelected(true);
		getContainer().add(subFoldersCheckBox, 1, 5);

		searchButton = new Button("Search");
		searchButton.setDefaultButton(true);
		searchButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				setCloseReason(CloseReason.OK);
				close();
			}
		});
		getContainer().add(searchButton, 0, 6);

		cancelButton = new Button("Cancel");
		cancelButton.setCancelButton(true);
		cancelButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				setCloseReason(CloseReason.CANCEL);
				close();
			}
		});
		getContainer().add(cancelButton, 1, 6);
	}

	public String getSearchTerm() {
		return searchTermField.getText();
	}

	public boolean isRegex() {
		return regexCheckBox.isSelected();
	}

	public boolean checkFiles() {
		return filesCheckBox.isSelected();
	}

	public boolean checkFolders() {
		return foldersCheckBox.isSelected();
	}

	public boolean isCaseSensitive() {
		return caseSensitiveCheckBox.isSelected();
	}

	public boolean checkSubFolders() {
		return subFoldersCheckBox.isSelected();
	}
}
