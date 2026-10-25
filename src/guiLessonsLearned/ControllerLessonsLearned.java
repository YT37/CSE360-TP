package guiLessonsLearned;

import java.util.Optional;
import entityClasses.LessonLearned;
import entityClasses.LessonLearnedList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

/*******
 * <p> Title: ControllerLessonsLearned Class. </p>
 *
 * <p> Description: The Java/FX-based Lessons Learned Page.  This class provides the controller
 * actions based on the user's use of the JavaFX GUI widgets defined by the View class.  The
 * database repeats every validation, lock, and ownership check, so a caller that skips this page
 * gets the same protection as the GUI.</p>
 *
 * <p> Operations: performCreate, performUpdate, and performDelete build a lesson from the form
 * and pass it to Database.createLessonLearned, updateLessonLearned, or deleteLessonLearned with
 * the signed-in username (R1, R3, R6, R12).  Any message the database returns is shown as an
 * error (R14, R5).  refreshList and performSelect read the Contributor's lessons and load one
 * into the form with its lock state (R2, R4).  performSearch and performShowAll set or clear the
 * keyword subset (R7).  performExperiences opens the Experience Records page (R8).  Delete asks
 * for confirmation before anything is removed.</p>
 *
 * <p> The methods are protected static and are called only by the View.  Do not change them to
 * public.</p>
 *
 * <p> Copyright: Yug Thapar, Ishaan Arekar, Avishek Lodh, Shreetej Hadge © 2026 </p>
 *
 * @author Yug Thapar, Ishaan Arekar, Avishek Lodh, Shreetej Hadge
 *
 * @version 1.00		2026-10-24 Initial version for TP2
 * @version 1.01		2026-10-25 Foundations-style header; source and rationale for the attribute
 *
 */
public class ControllerLessonsLearned {
	/** True while the list is being rebuilt.  Source: R2.  Rationale: rebuilding the ListView
	 * fires selection events, and this flag stops them from loading a lesson into the form by
	 * mistake. */
	private static boolean rebuildingList;

	/** Creates the controller; all actions are invoked statically by the view. */
	public ControllerLessonsLearned() { }

	/** R2/R7/R12: reads the current Contributor's lessons and applies the active keyword subset. */
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

	/** R2/R4: loads the selected lesson and shows which fields are locked.
	 *
	 * @param lesson the selected lesson, or null when the selection was cleared
	 */
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

	/** Clears the form and removes the current lesson selection. */
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

	/** R1/R14: creates a validated lesson and refreshes the persisted list. */
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

	/** R3/R5/R12: updates the selected owned lesson after database validation, lock, and ownership checks. */
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

	/** R6/R5/R12: confirms and deletes the selected owned lesson; locked lessons are refused. */
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

	/** R7: applies a keyword search to the Contributor's own lessons. */
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

	/** Clears the active keyword filter and shows all of the Contributor's lessons. */
	protected static void performShowAll() {
		ModelLessonsLearned.currentSearch = "";
		ViewLessonsLearned.text_Search.clear();
		performClear();
		refreshList();
	}

	/** Returns to the Contributor home page. */
	protected static void performReturn() {
		guiContributor.ViewContributorHome.displayContributorHome(ViewLessonsLearned.theStage,
				ViewLessonsLearned.theUser);
	}

	/** Logs out and returns to the login page. */
	protected static void performLogout() {
		guiUserLogin.ViewUserLogin.displayUserLogin(ViewLessonsLearned.theStage);
	}

	/** Exits the application. */
	protected static void performQuit() { System.exit(0); }

	/** Builds a lesson from the current form values.
	 *
	 * @return a lesson populated from the form
	 */
	private static LessonLearned lessonFromForm() {
		return new LessonLearned(ViewLessonsLearned.theUser.getUserName(), ViewLessonsLearned.text_Title.getText(),
				ViewLessonsLearned.combo_Category.getValue(), ViewLessonsLearned.text_Situation.getText(),
				ViewLessonsLearned.text_Lesson.getText(), ViewLessonsLearned.text_Recommendation.getText(),
				ViewLessonsLearned.text_Keywords.getText());
	}

	/** Selects a displayed lesson by ID.
	 *
	 * @param id persistent lesson ID
	 */
	private static void select(int id) {
		LessonLearned lesson = ModelLessonsLearned.displayedLessons.findByID(id);
		if (lesson != null) ViewLessonsLearned.listView_Lessons.getSelectionModel().select(lesson);
	}

	/** Displays a status or error message using the matching style class.
	 *
	 * @param message the message to display
	 * @param error true for an error style, false for success
	 */
	private static void showMessage(String message, boolean error) {
		ViewLessonsLearned.label_Message.setText(message);
		ViewLessonsLearned.label_Message.getStyleClass().removeAll("error-text", "success-text");
		ViewLessonsLearned.label_Message.getStyleClass().add(error ? "error-text" : "success-text");
	}
}
