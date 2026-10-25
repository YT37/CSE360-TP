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

/**
 * <p>Title: ViewLessonsLearned Class.</p>
 * <p>Description: JavaFX page for Contributor lesson CRUD and keyword subsets. The page follows
 * the TP1 singleton View pattern and delegates actions to ControllerLessonsLearned.</p>
 */
public class ViewLessonsLearned {
	protected static Database theDatabase = applicationMain.FoundationsMain.database;
	protected static Stage theStage;
	protected static User theUser;
	private static ViewLessonsLearned theView;
	private static Scene theScene;
	private static Label label_SignedIn = new Label();

	protected static Label label_ListCount = new Label();
	protected static Label label_Message = new Label();
	protected static TextField text_Search = new TextField();
	protected static ListView<LessonLearned> listView_Lessons = new ListView<>();
	protected static Label label_Title;
	protected static TextField text_Title = new TextField();
	protected static Label label_Category;
	protected static ComboBox<String> combo_Category = new ComboBox<>();
	protected static Label label_Situation;
	protected static TextArea text_Situation = new TextArea();
	protected static Label label_Lesson;
	protected static TextArea text_Lesson = new TextArea();
	protected static Label label_Recommendation;
	protected static TextArea text_Recommendation = new TextArea();
	protected static Label label_Keywords;
	protected static TextField text_Keywords = new TextField();
	protected static Button button_Delete;
	protected static Button button_Experiences;

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

	/** Displays field lock indicators and disables deletion when any lock is present (R4, R5). */
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

	private static void setTextLock(Label label, String text, javafx.scene.control.TextInputControl control,
			LessonLearned lesson, String field) {
		boolean locked = lesson != null && lesson.isFieldLocked(field);
		setLabelLock(label, text, locked);
		control.setEditable(!locked);
	}

	private static void setLabelLock(Label label, String text, boolean locked) {
		label.setText(locked ? text + " (Locked)" : text);
		label.getStyleClass().remove("locked-label");
		if (locked) label.getStyleClass().add("locked-label");
	}

	/** Displays the page for the logged-in Contributor and reloads persisted data. */
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

	private static Label fieldLabel(String text) {
		Label label = label(text, 14);
		label.getStyleClass().add("form-label");
		return label;
	}

	private static VBox formField(Label label, javafx.scene.layout.Region control) {
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
