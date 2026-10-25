package guiLessonsLearned;

import entityClasses.LessonLearned;
import entityClasses.LessonLearnedList;

/**
 * <p>Title: ModelLessonsLearned Class.</p>
 * <p>Description: Holds the selected lesson and current Contributor search result. Keeping this
 * state outside JavaFX controls lets the Controller refresh the page from persisted data.</p>
 */
public class ModelLessonsLearned {
	/** Lessons currently displayed after applying the active keyword filter. */
	protected static LessonLearnedList displayedLessons = new LessonLearnedList();
	/** Lesson selected in the list, or null when no lesson is selected. */
	protected static LessonLearned selectedLesson;
	/** Active keyword filter; an empty string shows all lessons. */
	protected static String currentSearch = "";

	/** Creates the model; the view uses only its static state. */
	public ModelLessonsLearned() { }
}
