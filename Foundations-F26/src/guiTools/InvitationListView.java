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

public class InvitationListView {

	public static class InvitationRow {
		private final String code, email, role, deadline;
		public InvitationRow(String code, String email, String role, String deadline) {
			this.code = code; this.email = email; this.role = role; this.deadline = deadline;
		}
		public String getCode() { return code; }
		public String getEmail() { return email; }
		public String getRole() { return role; }
		public String getDeadline() { return deadline; }
	}

	public static void show(Stage owner, Database theDatabase) {
		Stage stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.initOwner(owner);
		stage.setTitle("Manage Invitations");

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

		ObservableList<InvitationRow> rows = FXCollections.observableArrayList();
		for (String[] row : theDatabase.getAllInvitations()) {
			rows.add(new InvitationRow(row[0], row[1], row[2], row[3]));
		}
		table.setItems(rows);

		Label countLabel = new Label("Outstanding invitations: " + rows.size());

		VBox layout = new VBox(10);
		layout.setPadding(new Insets(15));
		layout.getChildren().addAll(countLabel, table);

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

		Button closeButton = new Button("Close");
		closeButton.setOnAction(e -> stage.close());
		VBox.setMargin(closeButton, new Insets(5, 0, 0, 0));
		layout.getChildren().add(closeButton);

		stage.setScene(new Scene(layout));
		stage.showAndWait();
	}
}