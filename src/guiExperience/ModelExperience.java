package guiExperience;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import entityClasses.ExperienceRecord;
import entityClasses.LessonLearned;

/** Holds the selected lesson, its experience records, and effort currently being entered. */
public class ModelExperience {
	protected static LessonLearned lesson;
	protected static List<ExperienceRecord> experiences = new ArrayList<>();
	protected static ExperienceRecord selectedExperience;
	protected static final Map<String, Double> effortHoursByMember = new LinkedHashMap<>();
}
