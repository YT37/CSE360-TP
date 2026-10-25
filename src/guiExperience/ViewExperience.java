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

/*******
 * <p> Title: ViewExperience Class. </p>
 *
 * <p> Description: The Java/FX-based Experience Records Page.  A Contributor reaches it with the
 * Experiences button on the Lessons Learned page.  The left side lists the Contributor's earlier
 * attempts at the selected lesson; the right side is a form for what was done, how, the
 * duration, and each team member's effort.</p>
 *
 * <p> The class follows the Foundations singleton View pattern: every use starts with
 * displayExperience.  Each button only calls a method in ControllerExperience.</p>
 *
 * <p> Operations: Add saves a new record, selecting a record shows its stored values read-only,
 * and Delete removes it after a confirmation (R8).  The database checks that the signed-in
 * Contributor owns the lesson and the record (R12).</p>
 *
 * <p> Requirement map: R8, with R12 ownership enforced by the database.</p>
 *
 * <p> The manual R8 check for this page is M7 in guiTests.ContributorManualTests.</p>
 *
 * <p> Copyright: Yug Thapar, Ishaan Arekar, Avishek Lodh, Shreetej Hadge © 2026 </p>
 *
 * @author Yug Thapar, Ishaan Arekar, Avishek Lodh, Shreetej Hadge
 *
 * @version 1.00		2026-10-24 Initial version for TP2
 * @version 1.01		2026-10-25 Foundations-style header; source and rationale for each attribute;
 * 							manual check moved to ContributorManualTests M7
 *
 */
public class ViewExperience {
	/** Shared application database.  Source: R11.  Rationale: records are saved in the same H2
	 * database as the lessons they belong to. */
	protected static Database theDatabase = applicationMain.FoundationsMain.database;
	/** Stage used to display this page.  Source: Foundations View pattern.  Rationale: Return and
	 * Logout reuse the same window. */
	protected static Stage theStage;
	/** Signed-in Contributor.  Source: R12.  Rationale: only this user's records are listed and
	 * changed. */
	protected static User theUser;
	/** Singleton view instance.  Source: Foundations View pattern.  Rationale: the page is built
	 * once and reused. */
	private static ViewExperience theView;
	/** Scene containing the page.  Source: Foundations View pattern.  Rationale: lets the stage
	 * switch back to this page without rebuilding it. */
	private static Scene theScene;

	/** Shows which lesson the records belong to.  Source: R8.  Rationale: the Contributor can see
	 * which lesson they are recording attempts for. */
	protected static Label label_Lesson = new Label();
	/** Status or error message.  Source: R8 and R14.  Rationale: shows validation and ownership
	 * messages so the Contributor knows what to fix. */
	protected static Label label_Message = new Label();
	/** List of earlier attempts.  Source: R8 and R12.  Rationale: the Contributor picks a record
	 * here to view or delete it. */
	protected static ListView<ExperienceRecord> listView_Experiences = new ListView<>();
	/** What Was Done input.  Source: R8.  Rationale: describes the attempt. */
	protected static TextArea text_WhatWasDone = new TextArea();
	/** How input.  Source: R8.  Rationale: describes the method used. */
	protected static TextArea text_How = new TextArea();
	/** Duration input, in hours.  Source: R8.  Rationale: records how long the attempt took. */
	protected static TextField text_DurationHours = new TextField();
	/** Team member name input.  Source: R8.  Rationale: names the member whose effort is being
	 * added. */
	protected static TextField text_TeamMember = new TextField();
	/** Effort hours input.  Source: R8.  Rationale: the hours for the member named above. */
	protected static TextField text_EffortHours = new TextField();
	/** Effort entries for the current record.  Source: R8.  Rationale: shows each member's hours
	 * before the record is saved. */
	protected static ListView<String> listView_Efforts = new ListView<>();
	/** Add button.  Source: R8.  Rationale: saves the form as a new record. */
	protected static Button button_Add;
	/** Add Effort button.  Source: R8.  Rationale: adds one member's hours to the record. */
	protected static Button button_AddEffort;
	/** Delete button.  Source: R8.  Rationale: deletes the selected record after a
	 * confirmation. */
	protected static Button button_Delete;
	/** New button.  Source: R8.  Rationale: clears the form so a new record can be entered. */
	protected static Button button_New;
	/** Remove Effort button.  Source: R8.  Rationale: removes a member's hours entered by
	 * mistake. */
	private static Button button_RemoveEffort;

	/** Builds the page widgets once; only displayExperience calls this. */
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

	/** Displays experience records for the supplied lesson and current Contributor.
	 *
	 * @param stage the stage that displays the page
	 * @param user the signed-in Contributor
	 * @param lesson the lesson whose experiences are shown
	 */
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

	/** Enables or disables the form and action controls for the current mode.
	 *
	 * @param editing true when creating or editing a record
	 */
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

	/** Builds a labeled form field.
	 *
	 * @param text the field label
	 * @param control the input control
	 * @return a VBox containing the label and control
	 */
	private static VBox formField(String text, javafx.scene.layout.Region control) {
		Label label = label(text, 13);
		label.getStyleClass().add("form-label");
		control.setMaxWidth(Double.MAX_VALUE);
		VBox field = new VBox(4, label, control);
		field.getStyleClass().add("form-field");
		return field;
	}

	/** Creates a label using the application font.
	 *
	 * @param text the label text
	 * @param size the font size
	 * @return the configured label
	 */
	private static Label label(String text, double size) {
		Label label = new Label(text);
		label.setFont(Font.font(applicationMain.Theme.FONT_FAMILY, size));
		return label;
	}

	/** Creates a button using the application font.
	 *
	 * @param text the button text
	 * @param minWidth the minimum button width
	 * @return the configured button
	 */
	private static Button button(String text, double minWidth) {
		Button button = new Button(text);
		button.setFont(Font.font(applicationMain.Theme.FONT_FAMILY, 14));
		button.setMinWidth(minWidth);
		return button;
	}
}
