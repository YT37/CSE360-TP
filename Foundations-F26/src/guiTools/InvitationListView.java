package guiTools;

import java.util.Optional;

import database.Database;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/*******
 * <p> Title: InvitationListView Class. </p>
 *
 * <p> Description: A modal table window showing every outstanding invitation code.  It is used by
 * Admin Home's "Manage Invitations" button.  Expired invitations are removed before the table is
 * filled, and the admin may cancel a selected invitation after confirming.</p>
 */
public class InvitationListView {

	/**********
	 * <p> Title: InvitationRow Class. </p>
	 *
	 * <p> Description: One row of the invitation table: the invitation code, the email address it
	 * was sent to, the role it grants, and its deadline.  The getters are used by the table
	 * columns to display each value.</p>
	 */
	public static class InvitationRow {
		private final String code, email, role, deadline;

		/**********
		 * <p> Method: InvitationRow(String code, String email, String role, String deadline) </p>
		 *
		 * <p> Description: This constructor establishes one row of the invitation table. </p>
		 *
		 * @param code specifies the invitation code
		 *
		 * @param email specifies the email address the invitation was sent to
		 *
		 * @param role specifies the role the invitation grants
		 *
		 * @param deadline specifies when the invitation expires
		 */
		public InvitationRow(String code, String email, String role, String deadline) {
			this.code = code; this.email = email; this.role = role; this.deadline = deadline;
		}

		/** @return the invitation code */
		public String getCode() { return code; }

		/** @return the email address the invitation was sent to */
		public String getEmail() { return email; }

		/** @return the role the invitation grants */
		public String getRole() { return role; }

		/** @return when the invitation expires */
		public String getDeadline() { return deadline; }
	}

	/**********
	 * <p> Method: show(Stage owner, Database theDatabase) </p>
	 *
	 * <p> Description: This method builds and displays the Manage Invitations window.  It lists
	 * the code, email address, role, and deadline of every outstanding invitation and provides a
	 * button to cancel the selected invitation and a button to close the window.  The method
	 * does not return until the window is closed.</p>
	 *
	 * @param owner specifies the Stage that owns this modal window
	 *
	 * @param theDatabase specifies the Database that holds the invitations
	 */
	public static void show(Stage owner, Database theDatabase) {
		// Create a modal window so the admin must close it before using the Admin Home page
		Stage stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.initOwner(owner);
		stage.setTitle("Manage Invitations");

		// Establish the table and one column for each attribute of an invitation
		TableView<InvitationRow> table = new TableView<>();
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		TableColumn<InvitationRow, String> colCode = new TableColumn<>("Code");
		colCode.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCode()));

		TableColumn<InvitationRow, String> colEmail = new TableColumn<>("Email");
		colEmail.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));

		TableColumn<InvitationRow, String> colRole = new TableColumn<>("Role");
		colRole.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRole()));

		TableColumn<InvitationRow, String> colDeadline = new TableColumn<>("Deadline");
		colDeadline.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDeadline()));

		table.getColumns().addAll(colCode, colEmail, colRole, colDeadline);
		table.setPrefSize(600, 320);

		// Fill the table with the outstanding invitations (expired ones are removed first)
		ObservableList<InvitationRow> rows = FXCollections.observableArrayList();
		for (String[] row : theDatabase.getAllInvitations()) {
			rows.add(new InvitationRow(row[0], row[1], row[2], row[3]));
		}
		table.setItems(rows);

		// Show how many invitations are outstanding above the table
		Label countLabel = new Label("Outstanding invitations: " + rows.size());

		VBox layout = new VBox(10);
		layout.setPadding(new Insets(15));
		layout.getChildren().addAll(countLabel, table);

		// Cancel the selected invitation, but only after the admin confirms it
		Button cancelButton = new Button("Cancel Selected Invitation");
		cancelButton.setOnAction(e -> {
			InvitationRow selected = table.getSelectionModel().getSelectedItem();
			if (selected == null) {
				new Alert(AlertType.WARNING, "Select an invitation to cancel first.").showAndWait();
				return;
			}
			Alert confirm = new Alert(AlertType.CONFIRMATION,
					"Cancel the invitation for \"" + selected.getEmail() + "\"?");
			confirm.setHeaderText(null);
			Optional<ButtonType> result = confirm.showAndWait();
			if (result.isPresent() && result.get() == ButtonType.OK) {
				theDatabase.cancelInvitation(selected.getCode());
				rows.remove(selected);
				countLabel.setText("Outstanding invitations: " + rows.size());
			}
		});
		VBox.setMargin(cancelButton, new Insets(5, 0, 0, 0));
		layout.getChildren().add(cancelButton);

		// Close the window and return to the Admin Home page
		Button closeButton = new Button("Close");
		closeButton.setOnAction(e -> stage.close());
		VBox.setMargin(closeButton, new Insets(5, 0, 0, 0));
		layout.getChildren().add(closeButton);

		// Display the window and wait for the admin to close it
		stage.setScene(new Scene(layout));
		stage.showAndWait();
	}
}