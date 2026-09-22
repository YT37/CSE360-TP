package guiTools;

import java.util.List;
import java.util.Optional;

import database.Database;
import entityClasses.User;
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
 * <p> Title: UserListView Class. </p>
 *
 * <p> Description: A reusable table window showing every user account. Used by Admin Home's
 * "List All Users" button (allowDelete = false) and "Delete a User" button (allowDelete = true,
 * which adds a Delete button that removes the selected row after confirming).</p>
 */
public class UserListView {

	/**********
	 * <p> Title: UserRow Class. </p>
	 *
	 * <p> Description: One row of the user table: the username, full name, email address, and
	 * roles of a user account.  The getters are used by the table columns to display each
	 * value.</p>
	 */
	public static class UserRow {
		private final String username, name, email, roles;

		/**********
		 * <p> Method: UserRow(String username, String name, String email, String roles) </p>
		 *
		 * <p> Description: This constructor establishes one row of the user table. </p>
		 *
		 * @param username specifies the account username
		 *
		 * @param name specifies the user's first and last name
		 *
		 * @param email specifies the user's email address
		 *
		 * @param roles specifies the roles this user plays
		 */
		public UserRow(String username, String name, String email, String roles) {
			this.username = username; this.name = name; this.email = email; this.roles = roles;
		}

		/** @return the account username */
		public String getUsername() { return username; }

		/** @return the user's first and last name */
		public String getName() { return name; }

		/** @return the user's email address */
		public String getEmail() { return email; }

		/** @return the roles this user plays */
		public String getRoles() { return roles; }
	}

	/**********
	 * <p> Method: show(Stage owner, Database theDatabase, User currentUser, boolean allowDelete)
	 * </p>
	 *
	 * <p> Description: This method builds and displays a table of every user account with the
	 * username, name, email address, and roles of each user.  When allowDelete is true, a Delete
	 * button is added.  The admin must confirm before a user is deleted, and an admin cannot
	 * delete their own account.  The method does not return until the window is closed.</p>
	 *
	 * @param owner specifies the Stage that owns this modal window
	 *
	 * @param theDatabase specifies the Database that holds the user accounts
	 *
	 * @param currentUser specifies the admin who is using this window
	 *
	 * @param allowDelete specifies whether the Delete button is shown
	 */
	public static void show(Stage owner, Database theDatabase, User currentUser, boolean allowDelete) {
		// Create a modal window so the admin must close it before using the Admin Home page
		Stage stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.initOwner(owner);
		stage.setTitle(allowDelete ? "Delete a User" : "All User Accounts");

		// Establish the table and one column for each attribute of a user
		TableView<UserRow> table = new TableView<>();

		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		TableColumn<UserRow, String> colUsername = new TableColumn<>("Username");
		colUsername.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUsername()));

		TableColumn<UserRow, String> colName = new TableColumn<>("Name");
		colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));

		TableColumn<UserRow, String> colEmail = new TableColumn<>("Email");
		colEmail.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));

		TableColumn<UserRow, String> colRoles = new TableColumn<>("Roles");
		colRoles.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRoles()));

		table.getColumns().addAll(colUsername, colName, colEmail, colRoles);
		table.setPrefSize(560, 320);

		// Fill the table with the details of every user account
		ObservableList<UserRow> rows = FXCollections.observableArrayList();
		for (String[] row : theDatabase.getAllUsersDetails()) {
			rows.add(new UserRow(row[0], row[1], row[2], row[3]));
		}
		table.setItems(rows);

		// Show how many users there are above the table
		Label countLabel = new Label("Number of users: " + rows.size());

		VBox layout = new VBox(10);
		layout.setPadding(new Insets(15));
		layout.getChildren().addAll(countLabel, table);

		// When deleting is allowed, add a button that deletes the selected user after confirming
		if (allowDelete) {
			Button deleteButton = new Button("Delete Selected User");
			deleteButton.setOnAction(e -> {

				// A user must be selected before anything can be deleted
				UserRow selected = table.getSelectionModel().getSelectedItem();
				if (selected == null) {
					new Alert(AlertType.WARNING, "Select a user to delete first.").showAndWait();
					return;
				}

				// An admin cannot delete their own account
				if (selected.getUsername().equals(currentUser.getUserName())) {
					new Alert(AlertType.WARNING,
							"An admin cannot delete their own account. Ask another admin to do it.")
							.showAndWait();
					return;
				}

				// Ask "Are you sure?" and delete the user only if the admin confirms
				Alert confirm = new Alert(AlertType.CONFIRMATION,
						"Are you sure you want to delete \"" + selected.getUsername() + "\"?");
				confirm.setHeaderText(null);
				Optional<ButtonType> result = confirm.showAndWait();
				if (result.isPresent() && result.get() == ButtonType.OK) {
					theDatabase.deleteUser(selected.getUsername());
					rows.remove(selected);
					countLabel.setText("Number of users: " + rows.size());
				}
			});
			VBox.setMargin(deleteButton, new Insets(5, 0, 0, 0));
			layout.getChildren().add(deleteButton);
		}

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