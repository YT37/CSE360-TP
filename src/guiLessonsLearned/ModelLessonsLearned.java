package guiLessonsLearned;

import entityClasses.LessonLearned;
import entityClasses.LessonLearnedList;

/*******
 * <p> Title: ModelLessonsLearned Class. </p>
 *
 * <p> Description: The Lessons Learned Page Model.  It holds the page state that is not a JavaFX
 * widget: the lessons on display, the selected lesson, and the active search.  Keeping this
 * state outside the controls lets the Controller rebuild the page from the database at any
 * time.</p>
 *
 * <p> Role in the MVC split: the View owns the widgets, the Controller reacts to the buttons, and
 * this Model remembers what the Contributor is looking at.  The class is thin on purpose.  The
 * real data rules live in LessonLearned, LessonLearnedValidator, and Database, which other pages
 * and the tests also use, so there is no lesson logic left for this Model to hold.  The
 * Foundations Models are thin for the same reason.</p>
 *
 * <p> Copyright: Yug Thapar, Ishaan Arekar, Avishek Lodh, Shreetej Hadge © 2026 </p>
 *
 * @author Yug Thapar, Ishaan Arekar, Avishek Lodh, Shreetej Hadge
 *
 * @version 1.00		2026-10-24 Initial version for TP2
 * @version 1.01		2026-10-25 Foundations-style header; MVC role; source and rationale for
 * 							each attribute
 *
 */
public class ModelLessonsLearned {
	/** Lessons currently displayed after applying the active keyword filter.  Source: R2 and R7.
	 * Rationale: the Controller finds the lesson to reselect after a create or update here. */
	protected static LessonLearnedList displayedLessons = new LessonLearnedList();
	/** Lesson selected in the list, or null.  Source: R3 and R6.  Rationale: Update and Delete act
	 * on this lesson's ID, and its lock state decides whether Delete is allowed. */
	protected static LessonLearned selectedLesson;
	/** Active keyword filter; an empty string shows all lessons.  Source: R7.  Rationale: the
	 * search is applied again on every refresh, so an update or delete keeps the subset on
	 * screen. */
	protected static String currentSearch = "";

	/** Creates the model; the view uses only its static state. */
	public ModelLessonsLearned() { }
}
