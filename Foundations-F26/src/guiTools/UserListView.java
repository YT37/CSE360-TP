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

	public static class UserRow {
		private final String username, name, email, roles;
		public UserRow(String username, String name, String email, String roles) {
			this.username = username; this.name = name; this.email = email; this.roles = roles;
		}
		public String getUsername() { return username; }
		public String getName() { return name; }
		public String getEmail() { return email; }
		public String getRoles() { return roles; }
	}

	public static void show(Stage owner, Database theDatabase, User currentUser, boolean allowDelete) {
		Stage stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.initOwner(owner);
		stage.setTitle(allowDelete ? "Delete a User" : "All User Accounts");

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

		ObservableList<UserRow> rows = FXCollections.observableArrayList();
		for (String[] row : theDatabase.getAllUsersDetails()) {
			rows.add(new UserRow(row[0], row[1], row[2], row[3]));
		}
		table.setItems(rows);

		Label countLabel = new Label("Number of users: " + rows.size());

		VBox layout = new VBox(10);
		layout.setPadding(new Insets(15));
		layout.getChildren().addAll(countLabel, table);

		if (allowDelete) {
		    Button deleteButton = new Button("Delete Selected User");
		    deleteButton.setOnAction(e -> {
		        UserRow selected = table.getSelectionModel().getSelectedItem();
		        if (selected == null) {
		            new Alert(AlertType.WARNING, "Select a user to delete first.").showAndWait();
		            return;
		        }
		        if (selected.getUsername().equals(currentUser.getUserName())) {
		            new Alert(AlertType.WARNING,
		                    "An admin cannot delete their own account. Ask another admin to do it.")
		                    .showAndWait();
		            return;
		        }
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

		Button closeButton = new Button("Close");
		closeButton.setOnAction(e -> stage.close());
		VBox.setMargin(closeButton, new Insets(5, 0, 0, 0));
		layout.getChildren().add(closeButton);

		stage.setScene(new Scene(layout));
		stage.showAndWait();
	}
}