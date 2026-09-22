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

	/**********
	 * <p> Method: Optional&lt;String&gt; showAndWait(Window owner, String title, String headerText)
	 * </p>
	 * 
	 * <p> Description: This method displays a modal window with one password field and a
	 * PasswordStatusPanel that shows which password requirements are met as the user types.  The
	 * Save button is enabled only when every requirement is met.  The method does not return
	 * until the window is closed.</p>
	 * 
	 * @param owner specifies the Window that owns this modal window
	 * 
	 * @param title specifies the title of the window
	 * 
	 * @param headerText specifies the instruction shown above the password field
	 * 
	 * @return the password if the user pressed Save, or an empty Optional if the user cancelled
	 */
	public static Optional<String> showAndWait(Window owner, String title, String headerText) {
		// Create a modal window so the user must finish with it before doing anything else
		Stage stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.initOwner(owner);
		stage.setTitle(title);

		// Establish the instruction, the password field, and the live requirement feedback
		Label header = new Label(headerText);
		PasswordField passwordField = new PasswordField();
		passwordField.setPromptText("Enter password");

		PasswordStatusPanel statusPanel = new PasswordStatusPanel(false, true);
		// Single password field dialog - no confirm field, so match requirement is always satisfied
		statusPanel.updateMatch(true);

		// Save stays disabled until the password meets every requirement
		Button saveButton = new Button("Save");
		Button cancelButton = new Button("Cancel");
		saveButton.setDisable(true);

		// Holds the password when Save is pressed; it stays null if the user cancels
		final String[] savedValue = { null };

		// Update the feedback and the Save button on every keystroke
		passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
			statusPanel.update(newVal);
			boolean allGood = statusPanel.isFullyValidWithMaxLength();
			saveButton.setDisable(!allGood);
		});

		// Save keeps the password and closes the window; Cancel just closes it
		saveButton.setOnAction(e -> {
			savedValue[0] = passwordField.getText();
			stage.close();
		});
		cancelButton.setOnAction(e -> stage.close());

		// Lay out the widgets, display the window, and wait for the user to close it
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