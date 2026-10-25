package guiExperience;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import entityClasses.ExperienceRecord;
import entityClasses.LessonLearned;

/** Holds the selected lesson, its experience records, and effort currently being entered. */
public class ModelExperience {
	/** Lesson whose experience records are being displayed. */
	protected static LessonLearned lesson;
	/** Experience records currently displayed for the signed-in Contributor. */
	protected static List<ExperienceRecord> experiences = new ArrayList<>();
	/** Record selected in the experience list, or null when creating a new record. */
	protected static ExperienceRecord selectedExperience;
	/** Team-member names mapped to effort hours currently being entered. */
	protected static final Map<String, Double> effortHoursByMember = new LinkedHashMap<>();

	/** Creates the model; the view uses only its static state. */
	public ModelExperience() { }
}
