package guiLessonsLearned;

import java.util.Optional;
import entityClasses.LessonLearned;
import entityClasses.LessonLearnedList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

/**
 * <p>Title: ControllerLessonsLearned Class.</p>
 * <p>Description: Handles Contributor actions for the Lessons Learned page. Database validation is
 * still authoritative so programmatic callers receive the same protection as the GUI.</p>
 */
public class ControllerLessonsLearned {
	private static boolean rebuildingList;

	/** Reads the current Contributor's lessons and applies the active search subset. */
	protected static void refreshList() {
		LessonLearnedList mine = ViewLessonsLearned.theDatabase
				.readLessonsLearnedForContributor(ViewLessonsLearned.theUser.getUserName());
		ModelLessonsLearned.displayedLessons = ModelLessonsLearned.currentSearch.isEmpty()
				? mine : mine.getSubsetByKeyword(ModelLessonsLearned.currentSearch);
		rebuildingList = true;
		ViewLessonsLearned.listView_Lessons.getItems().setAll(ModelLessonsLearned.displayedLessons.getAllLessons());
		rebuildingList = false;
		ViewLessonsLearned.label_ListCount.setText("Showing " + ModelLessonsLearned.displayedLessons.size()
				+ " of " + mine.size() + " lesson(s)");
		if (!ViewLessonsLearned.theDatabase.getLastLessonError().isEmpty())
			showMessage(ViewLessonsLearned.theDatabase.getLastLessonError(), true);
	}

	protected static void performSelect(LessonLearned lesson) {
		if (lesson == null || rebuildingList) return;
		ModelLessonsLearned.selectedLesson = lesson;
		ViewLessonsLearned.button_Experiences.setDisable(false);
		ViewLessonsLearned.text_Title.setText(lesson.getTitle());
		ViewLessonsLearned.combo_Category.setValue(lesson.getCategory());
		ViewLessonsLearned.text_Situation.setText(lesson.getSituation());
		ViewLessonsLearned.text_Lesson.setText(lesson.getLessonText());
		ViewLessonsLearned.text_Recommendation.setText(lesson.getRecommendation());
		ViewLessonsLearned.text_Keywords.setText(lesson.getKeywords());
		ViewLessonsLearned.applyLockState(lesson);
		if (lesson.isLessonLocked())
			showMessage("Lesson is locked: fields are read-only and Delete is disabled.", true);
		else if (!lesson.getLockedFields().isEmpty())
			showMessage("Locked fields are read-only; Delete is disabled.", true);
		else showMessage("Lesson #" + lesson.getLessonID() + " selected.", false);
	}

	protected static void performClear() {
		ModelLessonsLearned.selectedLesson = null;
		ViewLessonsLearned.button_Experiences.setDisable(true);
		ViewLessonsLearned.listView_Lessons.getSelectionModel().clearSelection();
		ViewLessonsLearned.text_Title.clear();
		ViewLessonsLearned.combo_Category.getSelectionModel().clearSelection();
		ViewLessonsLearned.text_Situation.clear();
		ViewLessonsLearned.text_Lesson.clear();
		ViewLessonsLearned.text_Recommendation.clear();
		ViewLessonsLearned.text_Keywords.clear();
		ViewLessonsLearned.clearLockState();
	}

	/** Opens experience history and entry for the selected lesson (R8). */
	protected static void performExperiences() {
		LessonLearned lesson = ModelLessonsLearned.selectedLesson;
		if (lesson == null) {
			showMessage("Select a lesson before opening its experiences.", true);
			return;
		}
		guiExperience.ViewExperience.displayExperience(ViewLessonsLearned.theStage,
				ViewLessonsLearned.theUser, lesson);
	}

	/** Creates a validated lesson and refreshes the persisted list. */
	protected static void performCreate() {
		LessonLearned lesson = lessonFromForm();
		String result = ViewLessonsLearned.theDatabase.createLessonLearned(lesson);
		if (!result.isEmpty()) { showMessage(result, true); return; }
		// A newly created lesson may not match the active filter, so show the full list before selecting it.
		ModelLessonsLearned.currentSearch = "";
		ViewLessonsLearned.text_Search.clear();
		refreshList();
		select(lesson.getLessonID());
		showMessage("Lesson #" + lesson.getLessonID() + " was created.", false);
	}

	/** Updates the selected owned lesson after the database repeats validation and ownership checks. */
	protected static void performUpdate() {
		if (ModelLessonsLearned.selectedLesson == null) {
			showMessage("Select a lesson before updating.", true);
			return;
		}
		LessonLearned lesson = lessonFromForm();
		lesson.setLessonID(ModelLessonsLearned.selectedLesson.getLessonID());
		String result = ViewLessonsLearned.theDatabase.updateLessonLearned(lesson,
				ViewLessonsLearned.theUser.getUserName());
		if (!result.isEmpty()) { showMessage(result, true); return; }
		refreshList();
		select(lesson.getLessonID());
		showMessage("Lesson was updated.", false);
	}

	/** Confirms and deletes the selected owned lesson. */
	protected static void performDelete() {
		LessonLearned selected = ModelLessonsLearned.selectedLesson;
		if (selected == null) { showMessage("Select a lesson before deleting.", true); return; }
		if (selected.isLessonLocked() || !selected.getLockedFields().isEmpty()) {
			showMessage("This lesson or one of its fields is locked; Delete is unavailable.", true);
			return;
		}
		Alert confirmation = new Alert(AlertType.CONFIRMATION);
		confirmation.setTitle("Delete Lesson");
		confirmation.setHeaderText("Delete lesson #" + selected.getLessonID() + "?");
		confirmation.setContentText("This action cannot be undone.");
		Optional<ButtonType> answer = confirmation.showAndWait();
		if (answer.isEmpty() || answer.get() != ButtonType.OK) return;
		String result = ViewLessonsLearned.theDatabase.deleteLessonLearned(selected.getLessonID(),
				ViewLessonsLearned.theUser.getUserName());
		if (!result.isEmpty()) { showMessage(result, true); return; }
		performClear();
		refreshList();
		showMessage("Lesson was deleted.", false);
	}

	protected static void performSearch() {
		String search = ViewLessonsLearned.text_Search.getText().trim();
		if (search.isEmpty()) {
			showMessage("Enter a keyword, or press All Lessons.", true);
			return;
		}
		ModelLessonsLearned.currentSearch = search;
		performClear();
		refreshList();
	}

	protected static void performShowAll() {
		ModelLessonsLearned.currentSearch = "";
		ViewLessonsLearned.text_Search.clear();
		performClear();
		refreshList();
	}

	protected static void performReturn() {
		guiContributor.ViewContributorHome.displayContributorHome(ViewLessonsLearned.theStage,
				ViewLessonsLearned.theUser);
	}

	protected static void performLogout() {
		guiUserLogin.ViewUserLogin.displayUserLogin(ViewLessonsLearned.theStage);
	}

	protected static void performQuit() { System.exit(0); }

	private static LessonLearned lessonFromForm() {
		return new LessonLearned(ViewLessonsLearned.theUser.getUserName(), ViewLessonsLearned.text_Title.getText(),
				ViewLessonsLearned.combo_Category.getValue(), ViewLessonsLearned.text_Situation.getText(),
				ViewLessonsLearned.text_Lesson.getText(), ViewLessonsLearned.text_Recommendation.getText(),
				ViewLessonsLearned.text_Keywords.getText());
	}

	private static void select(int id) {
		LessonLearned lesson = ModelLessonsLearned.displayedLessons.findByID(id);
		if (lesson != null) ViewLessonsLearned.listView_Lessons.getSelectionModel().select(lesson);
	}

	private static void showMessage(String message, boolean error) {
		ViewLessonsLearned.label_Message.setText(message);
		ViewLessonsLearned.label_Message.getStyleClass().removeAll("error-text", "success-text");
		ViewLessonsLearned.label_Message.getStyleClass().add(error ? "error-text" : "success-text");
	}
}
