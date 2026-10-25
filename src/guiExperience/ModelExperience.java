package guiExperience;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import entityClasses.ExperienceRecord;
import entityClasses.LessonLearned;

/*******
 * <p> Title: ModelExperience Class. </p>
 *
 * <p> Description: The Experience Records Page Model.  It holds the page state that is not a
 * JavaFX widget: the lesson being worked on, its records, the selected record, and the effort
 * entries typed so far.</p>
 *
 * <p> Role in the MVC split: the View owns the widgets, the Controller reacts to the buttons, and
 * this Model remembers what the Contributor is working on between button presses.  The class is
 * thin on purpose.  The rules for a record live in ExperienceRecord, LessonLearnedValidator, and
 * Database, which the tests also use, so this Model only needs to hold the current state.  The
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
public class ModelExperience {
	/** Lesson whose experience records are shown.  Source: R8.  Rationale: every new record is
	 * saved against this lesson's ID. */
	protected static LessonLearned lesson;
	/** Records shown for the signed-in Contributor.  Source: R8 and R12.  Rationale: the
	 * Controller finds the new record here to select it after Add. */
	protected static List<ExperienceRecord> experiences = new ArrayList<>();
	/** Record selected in the list, or null when entering a new one.  Source: R8.  Rationale:
	 * Delete acts on this record. */
	protected static ExperienceRecord selectedExperience;
	/** Team-member names mapped to the effort hours entered so far.  Source: R8.  Rationale: effort
	 * is added one member at a time, so it is kept here until Add saves the record.  A
	 * LinkedHashMap keeps the order the members were entered. */
	protected static final Map<String, Double> effortHoursByMember = new LinkedHashMap<>();

	/** Creates the model; the view uses only its static state. */
	public ModelExperience() { }
}
