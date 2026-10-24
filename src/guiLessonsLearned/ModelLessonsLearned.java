package guiLessonsLearned;

import entityClasses.LessonLearned;
import entityClasses.LessonLearnedList;

/**
 * <p>Title: ModelLessonsLearned Class.</p>
 * <p>Description: Holds the selected lesson and current Contributor search result. Keeping this
 * state outside JavaFX controls lets the Controller refresh the page from persisted data.</p>
 */
public class ModelLessonsLearned {
	protected static LessonLearnedList displayedLessons = new LessonLearnedList();
	protected static LessonLearned selectedLesson;
	protected static String currentSearch = "";
}
