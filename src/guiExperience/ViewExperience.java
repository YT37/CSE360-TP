package guiExperience;

import database.Database;
import entityClasses.ExperienceRecord;
import entityClasses.LessonLearned;
import entityClasses.User;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * JavaFX page for creating, viewing, and deleting experience records for one Contributor lesson.
 * Requirement: R8, with R12 ownership enforced by the database when records are saved or removed.
 * Manual R8 check: add a valid record, select it to view the stored fields, then cancel and confirm
 * deletion; also attempt to save with a missing required field and verify the error is shown.
 */
public class ViewExperience {
	protected static Database theDatabase = applicationMain.FoundationsMain.database;
	protected static Stage theStage;
	protected static User theUser;
	private static ViewExperience theView;
	private static Scene theScene;

	protected static Label label_Lesson = new Label();
	protected static Label label_Message = new Label();
	protected static ListView<ExperienceRecord> listView_Experiences = new ListView<>();
	protected static TextArea text_WhatWasDone = new TextArea();
	protected static TextArea text_How = new TextArea();
	protected static TextField text_DurationHours = new TextField();
	protected static TextField text_TeamMember = new TextField();
	protected static TextField text_EffortHours = new TextField();
	protected static ListView<String> listView_Efforts = new ListView<>();
	protected static Button button_Add;
	protected static Button button_AddEffort;
	protected static Button button_Delete;
	protected static Button button_New;
	private static Button button_RemoveEffort;

	private ViewExperience() {
		BorderPane root = new BorderPane();
		root.getStyleClass().add("page-root");
		root.setPadding(new Insets(12));
		theScene = new Scene(root, applicationMain.FoundationsMain.WINDOW_WIDTH,
				applicationMain.FoundationsMain.WINDOW_HEIGHT);

		Label title = label("Experience Records", 28);
		title.getStyleClass().add("page-title");
		label_Lesson = label("", 14);
		label_Lesson.getStyleClass().add("subtitle");
		VBox header = new VBox(4, title, label_Lesson);
		header.getStyleClass().add("page-header");
		root.setTop(header);

		Label attempts = label("Previous Attempts", 18);
		attempts.getStyleClass().add("section-label");
		listView_Experiences.setMinHeight(140);
		VBox attemptsPanel = new VBox(8, attempts, listView_Experiences);
		attemptsPanel.getStyleClass().add("page-section");
		attemptsPanel.setMinWidth(150);
		VBox.setVgrow(listView_Experiences, Priority.ALWAYS);
		listView_Experiences.getSelectionModel().selectedItemProperty().addListener((o, oldValue, newValue) ->
				ControllerExperience.performSelect(newValue));

		text_WhatWasDone.setWrapText(true);
		text_How.setWrapText(true);
		text_WhatWasDone.setPrefRowCount(3);
		text_How.setPrefRowCount(3);
		VBox form = new VBox(10,
				formField("What Was Done", text_WhatWasDone),
				formField("How", text_How),
				formField("Duration (hours)", text_DurationHours));
		form.getStyleClass().add("form-section");

		Label effortLabel = label("Team Member Effort", 16);
		effortLabel.getStyleClass().add("section-label");
		VBox memberField = formField("Member name", text_TeamMember);
		VBox hoursField = formField("Hours", text_EffortHours);
		button_AddEffort = button("Add Effort", 110);
		button_AddEffort.setOnAction(e -> ControllerExperience.performAddEffort());
		FlowPane effortInputs = new FlowPane(8, 8, memberField, hoursField, button_AddEffort);
		effortInputs.setAlignment(Pos.BOTTOM_LEFT);
		listView_Efforts.setMinHeight(70);
		listView_Efforts.setPrefHeight(90);
		button_RemoveEffort = button("Remove Effort", 130);
		button_RemoveEffort.setOnAction(e -> ControllerExperience.performRemoveEffort());
		form.getChildren().addAll(effortLabel, effortInputs, listView_Efforts, button_RemoveEffort);
		ScrollPane formScroll = new ScrollPane(form);
		formScroll.setFitToWidth(true);
		formScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		formScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		formScroll.getStyleClass().add("form-scroll");
		formScroll.setMinWidth(250);

		SplitPane content = new SplitPane(attemptsPanel, formScroll);
		content.setOrientation(Orientation.HORIZONTAL);
		content.setDividerPositions(0.35);
		content.getStyleClass().add("page-content");
		root.setCenter(content);

		label_Message = label("", 13);
		label_Message.setWrapText(true);
		label_Message.setMaxWidth(Double.MAX_VALUE);
		button_New = button("New", 70);
		button_New.setOnAction(e -> ControllerExperience.performNew());
		button_Add = button("Add", 70);
		button_Add.getStyleClass().add("primary");
		button_Add.setOnAction(e -> ControllerExperience.performAdd());
		button_Delete = button("Delete", 70);
		button_Delete.setOnAction(e -> ControllerExperience.performDelete());
		Button back = button("Return", 70);
		back.setOnAction(e -> ControllerExperience.performReturn());
		Button logout = button("Logout", 70);
		logout.setOnAction(e -> ControllerExperience.performLogout());
		Button quit = button("Quit", 70);
		quit.setOnAction(e -> ControllerExperience.performQuit());
		FlowPane actions = new FlowPane(8, 8, button_New, button_Add, button_Delete, back, logout, quit);
		actions.setAlignment(Pos.CENTER_RIGHT);
		actions.getStyleClass().add("form-actions");
		VBox footer = new VBox(6, label_Message, actions);
		footer.getStyleClass().add("page-footer");
		root.setBottom(footer);
	}

	/** Displays experience records for the supplied lesson and current Contributor. */
	public static void displayExperience(Stage stage, User user, LessonLearned lesson) {
		theStage = stage;
		theUser = user;
		if (theView == null) theView = new ViewExperience();
		ModelExperience.lesson = lesson;
		label_Lesson.setText("Lesson #" + lesson.getLessonID() + ": " + lesson.getTitle());
		ControllerExperience.refreshList();
		ControllerExperience.performNew();
		stage.setMinWidth(520);
		stage.setMinHeight(440);
		applicationMain.Theme.apply(theScene);
		stage.setTitle("Lessons Learned System: Experience Records");
		stage.setScene(theScene);
		stage.show();
	}

	protected static void setEditing(boolean editing) {
		text_WhatWasDone.setEditable(editing);
		text_How.setEditable(editing);
		text_DurationHours.setEditable(editing);
		text_TeamMember.setDisable(!editing);
		text_EffortHours.setDisable(!editing);
		listView_Efforts.setDisable(!editing);
		button_AddEffort.setDisable(!editing);
		button_RemoveEffort.setDisable(!editing);
		button_Add.setDisable(!editing);
		button_Delete.setDisable(editing);
	}

	private static VBox formField(String text, javafx.scene.layout.Region control) {
		Label label = label(text, 13);
		label.getStyleClass().add("form-label");
		control.setMaxWidth(Double.MAX_VALUE);
		VBox field = new VBox(4, label, control);
		field.getStyleClass().add("form-field");
		return field;
	}

	private static Label label(String text, double size) {
		Label label = new Label(text);
		label.setFont(Font.font(applicationMain.Theme.FONT_FAMILY, size));
		return label;
	}

	private static Button button(String text, double minWidth) {
		Button button = new Button(text);
		button.setFont(Font.font(applicationMain.Theme.FONT_FAMILY, 14));
		button.setMinWidth(minWidth);
		return button;
	}
}
