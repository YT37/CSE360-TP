package guiLessonsLearned;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import database.Database;
import entityClasses.LessonLearned;
import entityClasses.User;
import validators.LessonLearnedValidator;

/*******
 * <p> Title: ViewLessonsLearned Class. </p>
 *
 * <p> Description: The Java/FX-based Lessons Learned Page.  A Contributor reaches this page from
 * the Contributor Home page.  The left side lists the Contributor's own lessons or a keyword
 * subset of them; the right side is a form for one lesson with the Clear, Create, Update, Delete,
 * and Experiences buttons and a message area.</p>
 *
 * <p> The class follows the Foundations singleton View pattern: every use starts with
 * displayLessonsLearned, which builds the page once and then reloads it for the signed-in user.
 * Each button only calls a method in ControllerLessonsLearned.</p>
 *
 * <p> Operations: the Create, Update, and Delete buttons and the list support the create, read,
 * update, and delete stories (R1, R2, R3, R6); the search box, Search, and All Lessons give the
 * keyword subsets (R7); applyLockState marks locked fields read-only and disables Delete (R4,
 * R5); Experiences opens the experience records for the selected lesson (R8); label_Message
 * shows every validation error the database returns (R14).</p>
 *
 * <p> Requirement map: R1/R2/R3/R6 form actions, R4/R5 lock display, R7 search controls,
 * R8 experience navigation, and R14 validation messages.</p>
 *
 * <p> Copyright: Yug Thapar, Ishaan Arekar, Avishek Lodh, Shreetej Hadge © 2026 </p>
 *
 * @author Yug Thapar, Ishaan Arekar, Avishek Lodh, Shreetej Hadge
 *
 * @version 1.00		2026-10-24 Initial version for TP2
 * @version 1.01		2026-10-25 Foundations-style header; source and rationale for each attribute
 *
 */
public class ViewLessonsLearned {
	/** Shared application database.  Source: R11.  Rationale: the page reads and writes the same
	 * H2 database as the rest of the application. */
	protected static Database theDatabase = applicationMain.FoundationsMain.database;
	/** Stage used to display this page.  Source: Foundations View pattern.  Rationale: Return and
	 * Logout reuse the same window. */
	protected static Stage theStage;
	/** Signed-in Contributor.  Source: R12.  Rationale: the list and every change are limited to
	 * this user's lessons. */
	protected static User theUser;
	/** Singleton view instance.  Source: Foundations View pattern.  Rationale: the page is built
	 * once and reused. */
	private static ViewLessonsLearned theView;
	/** Scene containing the page.  Source: Foundations View pattern.  Rationale: lets the stage
	 * switch back to this page without rebuilding it. */
	private static Scene theScene;
	/** Shows who is signed in.  Source: TP1 page layout.  Rationale: the Contributor can see whose
	 * lessons are listed. */
	private static Label label_SignedIn = new Label();

	/** Shows how many lessons are displayed out of the total.  Source: R7.  Rationale: makes it
	 * clear when a search subset, including an empty one, is being shown. */
	protected static Label label_ListCount = new Label();
	/** Status or error message.  Source: R14.  Rationale: shows every validation, lock, and
	 * ownership message so the Contributor knows what to fix. */
	protected static Label label_Message = new Label();
	/** Search box.  Source: R7 keyword subsets.  Rationale: lets the Contributor narrow their
	 * lessons by keyword. */
	protected static TextField text_Search = new TextField();
	/** List of the Contributor's lessons.  Source: R2 and R12.  Rationale: the Contributor picks a
	 * lesson here to view, update, or delete it. */
	protected static ListView<LessonLearned> listView_Lessons = new ListView<>();
	/** Title label.  Source: R1 and R4.  Rationale: shows "(Locked)" when the title is locked. */
	protected static Label label_Title;
	/** Title input.  Source: R1 and R3.  Rationale: enters or edits the lesson title. */
	protected static TextField text_Title = new TextField();
	/** Category label.  Source: R1 and R4.  Rationale: shows "(Locked)" when the category is
	 * locked. */
	protected static Label label_Category;
	/** Category choice.  Source: R1 and R14.  Rationale: a drop-down offers only the categories the
	 * validator accepts. */
	protected static ComboBox<String> combo_Category = new ComboBox<>();
	/** What Happened label.  Source: R1 and R4.  Rationale: shows "(Locked)" when the field is
	 * locked. */
	protected static Label label_Situation;
	/** What Happened input.  Source: R1 and R3.  Rationale: enters or edits the situation. */
	protected static TextArea text_Situation = new TextArea();
	/** What I Learned label.  Source: R1 and R4.  Rationale: shows "(Locked)" when the field is
	 * locked. */
	protected static Label label_Lesson;
	/** What I Learned input.  Source: R1 and R3.  Rationale: enters or edits the lesson text. */
	protected static TextArea text_Lesson = new TextArea();
	/** Next Time label.  Source: R1 and R4.  Rationale: shows "(Locked)" when the field is
	 * locked. */
	protected static Label label_Recommendation;
	/** Next Time input.  Source: R1 and R3.  Rationale: enters or edits the optional
	 * recommendation. */
	protected static TextArea text_Recommendation = new TextArea();
	/** Keywords label.  Source: R1 and R4.  Rationale: shows "(Locked)" when the field is
	 * locked. */
	protected static Label label_Keywords;
	/** Keywords input.  Source: R7.  Rationale: enters the terms the keyword search matches. */
	protected static TextField text_Keywords = new TextField();
	/** Delete button.  Source: R6 and R5.  Rationale: deletes the selected lesson after a
	 * confirmation, and is disabled when any lock is set. */
	protected static Button button_Delete;
	/** Experiences button.  Source: R8.  Rationale: opens the experience records for the selected
	 * lesson; disabled until a lesson is selected. */
	protected static Button button_Experiences;

	/** Builds the page widgets once; only displayLessonsLearned calls this. */
	private ViewLessonsLearned() {
		BorderPane root = new BorderPane();
		root.getStyleClass().add("page-root");
		root.setPadding(new Insets(12));
		theScene = new Scene(root, applicationMain.FoundationsMain.WINDOW_WIDTH,
				applicationMain.FoundationsMain.WINDOW_HEIGHT);
		Label title = label("Lessons Learned", 28);
		title.getStyleClass().add("page-title");
		label_SignedIn = label("", 14);
		label_SignedIn.getStyleClass().add("subtitle");
		VBox header = new VBox(4, title, label_SignedIn);
		header.getStyleClass().add("page-header");
		root.setTop(header);

		Label listTitle = label("My Lessons", 18);
		listTitle.getStyleClass().add("section-label");
		text_Search.setPromptText("Search your lessons");
		Button search = button("Search", 80);
		search.setOnAction(e -> ControllerLessonsLearned.performSearch());
		HBox searchRow = new HBox(6, text_Search, search);
		HBox.setHgrow(text_Search, Priority.ALWAYS);
		Button all = button("All Lessons", 120);
		all.setMaxWidth(Double.MAX_VALUE);
		all.setOnAction(e -> ControllerLessonsLearned.performShowAll());
		listView_Lessons.setMinHeight(120);
		listView_Lessons.getSelectionModel().selectedItemProperty().addListener((o, oldValue, newValue) ->
				ControllerLessonsLearned.performSelect(newValue));
		VBox left = new VBox(8, listTitle, searchRow, all, listView_Lessons, label_ListCount);
		left.getStyleClass().add("page-section");
		left.setMinWidth(170);
		VBox.setVgrow(listView_Lessons, Priority.ALWAYS);

		label_Title = fieldLabel("Title");
		label_Category = fieldLabel("Category");
		combo_Category.setItems(FXCollections.observableArrayList(LessonLearnedValidator.CATEGORIES));
		text_Situation.setWrapText(true);
		text_Lesson.setWrapText(true);
		text_Recommendation.setWrapText(true);
		text_Situation.setPrefRowCount(2);
		text_Lesson.setPrefRowCount(2);
		text_Recommendation.setPrefRowCount(2);
		label_Situation = fieldLabel("What Happened");
		label_Lesson = fieldLabel("What I Learned");
		label_Recommendation = fieldLabel("Next Time (optional)");
		label_Keywords = fieldLabel("Keywords (comma-separated)");
		VBox form = new VBox(10,
				label("Lesson Details", 18),
				formField(label_Title, text_Title),
				formField(label_Category, combo_Category),
				formField(label_Situation, text_Situation),
				formField(label_Lesson, text_Lesson),
				formField(label_Recommendation, text_Recommendation),
				formField(label_Keywords, text_Keywords));
		form.getStyleClass().add("form-section");
		ScrollPane formScroll = new ScrollPane(form);
		formScroll.setFitToWidth(true);
		formScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		formScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		formScroll.getStyleClass().add("form-scroll");
		formScroll.setMinWidth(260);
		SplitPane content = new SplitPane(left, formScroll);
		content.setDividerPositions(0.36);
		content.getStyleClass().add("page-content");
		root.setCenter(content);

		Button clear = button("Clear", 78);
		clear.setOnAction(e -> ControllerLessonsLearned.performClear());
		Button create = button("Create", 78);
		create.getStyleClass().add("primary");
		create.setOnAction(e -> ControllerLessonsLearned.performCreate());
		Button update = button("Update", 78);
		update.setOnAction(e -> ControllerLessonsLearned.performUpdate());
		button_Delete = button("Delete", 78);
		button_Delete.setOnAction(e -> ControllerLessonsLearned.performDelete());
		button_Experiences = button("Experiences", 110);
		button_Experiences.setDisable(true);
		button_Experiences.setOnAction(e -> ControllerLessonsLearned.performExperiences());
		label_Message.setWrapText(true);
		label_Message.setMaxWidth(Double.MAX_VALUE);
		FlowPane actions = new FlowPane(8, 8, clear, create, update, button_Delete, button_Experiences);
		actions.getStyleClass().add("form-actions");
		form.getChildren().addAll(actions, label_Message);

		Button back = button("Return", 110);
		back.setOnAction(e -> ControllerLessonsLearned.performReturn());
		Button logout = button("Logout", 110);
		logout.setOnAction(e -> ControllerLessonsLearned.performLogout());
		Button quit = button("Quit", 110);
		quit.setOnAction(e -> ControllerLessonsLearned.performQuit());
		FlowPane navigation = new FlowPane(8, 8, back, logout, quit);
		navigation.setAlignment(Pos.CENTER_RIGHT);
		navigation.getStyleClass().add("form-actions");
		root.setBottom(navigation);
	}

	/** Displays field lock indicators and disables deletion when any lock is present (R4, R5).
	 *
	 * @param lesson the lesson whose lock state is displayed
	 */
	protected static void applyLockState(LessonLearned lesson) {
		setTextLock(label_Title, "Title", text_Title, lesson, "title");
		setLabelLock(label_Category, "Category", lesson.isFieldLocked("category"));
		combo_Category.setDisable(lesson.isFieldLocked("category"));
		setTextLock(label_Situation, "What Happened", text_Situation, lesson, "situation");
		setTextLock(label_Lesson, "What I Learned", text_Lesson, lesson, "lessonText");
		setTextLock(label_Recommendation, "Next Time (optional)", text_Recommendation, lesson, "recommendation");
		setTextLock(label_Keywords, "Keywords (comma-separated)", text_Keywords, lesson, "keywords");
		button_Delete.setDisable(lesson.isLessonLocked() || !lesson.getLockedFields().isEmpty());
	}

	/** Restores the form's normal editing controls after clearing a selection. */
	protected static void clearLockState() {
		setTextLock(label_Title, "Title", text_Title, null, "title");
		setLabelLock(label_Category, "Category", false);
		combo_Category.setDisable(false);
		setTextLock(label_Situation, "What Happened", text_Situation, null, "situation");
		setTextLock(label_Lesson, "What I Learned", text_Lesson, null, "lessonText");
		setTextLock(label_Recommendation, "Next Time (optional)", text_Recommendation, null, "recommendation");
		setTextLock(label_Keywords, "Keywords (comma-separated)", text_Keywords, null, "keywords");
		button_Delete.setDisable(false);
	}

	/** Applies a field lock indicator and editability state to a text control.
	 *
	 * @param label the field label
	 * @param text the unlocked label text
	 * @param control the input control
	 * @param lesson the lesson providing lock state, or null to clear locks
	 * @param field the lockable field name
	 */
	private static void setTextLock(Label label, String text, javafx.scene.control.TextInputControl control,
			LessonLearned lesson, String field) {
		boolean locked = lesson != null && lesson.isFieldLocked(field);
		setLabelLock(label, text, locked);
		control.setEditable(!locked);
	}

	/** Updates one field label to show its lock state.
	 *
	 * @param label the field label
	 * @param text the unlocked label text
	 * @param locked true to append the locked marker
	 */
	private static void setLabelLock(Label label, String text, boolean locked) {
		label.setText(locked ? text + " (Locked)" : text);
		label.getStyleClass().remove("locked-label");
		if (locked) label.getStyleClass().add("locked-label");
	}

	/** Displays the page for the logged-in Contributor and reloads persisted data.
	 *
	 * @param stage the stage that displays the page
	 * @param user the signed-in Contributor
	 */
	public static void displayLessonsLearned(Stage stage, User user) {
		theStage = stage;
		theUser = user;
		if (theView == null) theView = new ViewLessonsLearned();
		label_SignedIn.setText("Signed in as " + user.getUserName());
		ModelLessonsLearned.currentSearch = "";
		text_Search.clear();
		ControllerLessonsLearned.performClear();
		ControllerLessonsLearned.refreshList();
		stage.setMinWidth(560);
		stage.setMinHeight(440);
		applicationMain.Theme.apply(theScene);
		stage.setTitle("Lessons Learned System: Lessons Learned");
		stage.setScene(theScene);
		stage.show();
	}

	/** Creates a form field label.
	 *
	 * @param text the label text
	 * @return the configured label
	 */
	private static Label fieldLabel(String text) {
		Label label = label(text, 14);
		label.getStyleClass().add("form-label");
		return label;
	}

	/** Builds a labeled form field.
	 *
	 * @param label the field label
	 * @param control the input control
	 * @return a VBox containing the label and control
	 */
	private static VBox formField(Label label, javafx.scene.layout.Region control) {
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
