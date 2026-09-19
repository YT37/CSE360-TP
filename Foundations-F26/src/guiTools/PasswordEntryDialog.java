package guiTools;

import java.util.Optional;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import validators.PasswordValidator;

/*******
 * <p> Title: PasswordEntryDialog Class. </p>
 * 
 * <p> Description: A password-entry dialog with live pass/fail feedback per requirement as
 * the user types, matching the pattern in PasswordEvaluationTestbed-F26's Model.updatePassword().
 * Save stays disabled until every requirement is satisfied. </p>
 */
public class PasswordEntryDialog {

	public static Optional<String> showAndWait(Window owner, String title, String headerText) {
		Stage stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.initOwner(owner);
		stage.setTitle(title);

		Label header = new Label(headerText);
		PasswordField passwordField = new PasswordField();
		passwordField.setPromptText("Enter password");

		PasswordStatusPanel statusPanel = new PasswordStatusPanel(false, true);
		// Single password field dialog - no confirm field, so match requirement is always satisfied
		statusPanel.updateMatch(true);

		Button saveButton = new Button("Save");
		Button cancelButton = new Button("Cancel");
		saveButton.setDisable(true);

		final String[] savedValue = { null };

		passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
			statusPanel.update(newVal);
			boolean allGood = statusPanel.isFullyValidWithMaxLength();
			saveButton.setDisable(!allGood);
		});

		saveButton.setOnAction(e -> {
			savedValue[0] = passwordField.getText();
			stage.close();
		});
		cancelButton.setOnAction(e -> stage.close());

		VBox layout = new VBox(8);
		layout.setPadding(new Insets(15));
		layout.getChildren().addAll(header, passwordField, statusPanel, new HBox(10, saveButton, cancelButton));

		stage.setScene(new Scene(layout, 460, 330));
		stage.setMinWidth(460);
		stage.setMinHeight(330);
		stage.showAndWait();

		return Optional.ofNullable(savedValue[0]);
	}
}