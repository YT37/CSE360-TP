package guiLessonsLearned;

import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
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
	private static final double width = applicationMain.FoundationsMain.WINDOW_WIDTH;
	private static final double height = applicationMain.FoundationsMain.WINDOW_HEIGHT;
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
	protected static TextField text_Title = new TextField();
	protected static ComboBox<String> combo_Category = new ComboBox<>();
	protected static TextArea text_Situation = new TextArea();
	protected static TextArea text_Lesson = new TextArea();
	protected static TextArea text_Recommendation = new TextArea();
	protected static TextField text_Keywords = new TextField();

	private ViewLessonsLearned() {
		Pane root = new Pane();
		theScene = new Scene(root, width, height);
		Label title = label("Lessons Learned", 28, 20, 12, 300);
		label_SignedIn = label("", 14, 20, 60, 300);
		Label listTitle = label("My Lessons", 20, 20, 115, 250);
		Label formTitle = label("Lesson Details", 20, 330, 115, 250);
		text_Search.setPromptText("Search your lessons");
		place(text_Search, 20, 145, 180, 14);
		Button search = button("Search", 210, 145, 80, 14);
		search.setOnAction(e -> ControllerLessonsLearned.performSearch());
		Button all = button("All Lessons", 20, 180, 270, 14);
		all.setOnAction(e -> ControllerLessonsLearned.performShowAll());
		place(listView_Lessons, 20, 215, 280, 245);
		listView_Lessons.getSelectionModel().selectedItemProperty().addListener((o, oldValue, newValue) ->
				ControllerLessonsLearned.performSelect(newValue));
		place(label_ListCount, 20, 470, 280, 14);

		int x = 330;
		Label label_Title = label("Title", 14, x, 145, 100);
		place(text_Title, x, 165, 450, 14);
		Label label_Category = label("Category", 14, x, 205, 100);
		combo_Category.setItems(FXCollections.observableArrayList(LessonLearnedValidator.CATEGORIES));
		place(combo_Category, x + 110, 198, 340, 14);
		Label label_Situation = label("What Happened", 14, x, 240, 180);
		place(text_Situation, x, 260, 450, 52);
		Label label_Lesson = label("What I Learned", 14, x, 320, 180);
		place(text_Lesson, x, 340, 450, 52);
		Label label_Recommendation = label("Next Time (optional)", 14, x, 400, 180);
		place(text_Recommendation, x, 420, 450, 45);
		Label label_Keywords = label("Keywords (comma-separated)", 14, x, 475, 220);
		place(text_Keywords, x + 225, 468, 225, 14);
		label_Title.getStyleClass().add("form-label");
		label_Category.getStyleClass().add("form-label");
		label_Situation.getStyleClass().add("form-label");
		label_Lesson.getStyleClass().add("form-label");
		label_Recommendation.getStyleClass().add("form-label");
		label_Keywords.getStyleClass().add("form-label");

		Button clear = button("Clear", x, 515, 95, 14);
		clear.setOnAction(e -> ControllerLessonsLearned.performClear());
		Button create = button("Create", x + 105, 515, 95, 14);
		create.getStyleClass().add("primary");
		create.setOnAction(e -> ControllerLessonsLearned.performCreate());
		Button update = button("Update", x + 210, 515, 95, 14);
		update.setOnAction(e -> ControllerLessonsLearned.performUpdate());
		Button delete = button("Delete", x + 315, 515, 95, 14);
		delete.setOnAction(e -> ControllerLessonsLearned.performDelete());
		place(label_Message, 20, 500, 280, 14);
		Button back = button("Return", 20, 550, 150, 14);
		back.setOnAction(e -> ControllerLessonsLearned.performReturn());
		Button logout = button("Logout", 180, 550, 150, 14);
		logout.setOnAction(e -> ControllerLessonsLearned.performLogout());
		Button quit = button("Quit", 340, 550, 150, 14);
		quit.setOnAction(e -> ControllerLessonsLearned.performQuit());
		root.getChildren().addAll(title, label_SignedIn, listTitle, formTitle, text_Search, search, all, listView_Lessons,
				label_ListCount, label_Title, text_Title, label_Category, combo_Category, label_Situation, text_Situation,
				label_Lesson, text_Lesson, label_Recommendation, text_Recommendation, label_Keywords, text_Keywords,
				label_Message, clear, create, update, delete, back, logout, quit);
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
		applicationMain.Theme.apply(theScene);
		stage.setTitle("Lessons Learned System: Lessons Learned");
		stage.setScene(theScene);
		stage.show();
	}

	private static Label label(String text, double size, double x, double y, double width) {
		Label label = new Label(text);
		label.setFont(Font.font(applicationMain.Theme.FONT_FAMILY, size));
		label.setLayoutX(x); label.setLayoutY(y); label.setMinWidth(width);
		return label;
	}

	private static Button button(String text, double x, double y, double width, double size) {
		Button button = new Button(text);
		button.setFont(Font.font(applicationMain.Theme.FONT_FAMILY, size));
		button.setLayoutX(x); button.setLayoutY(y); button.setMinWidth(width);
		return button;
	}

	private static void place(javafx.scene.layout.Region node, double x, double y, double width, double height) {
		node.setLayoutX(x); node.setLayoutY(y); node.setMinWidth(width); node.setPrefWidth(width);
		if (node instanceof TextArea area) area.setPrefHeight(height);
	}
}
