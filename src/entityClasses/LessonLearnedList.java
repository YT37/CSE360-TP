package entityClasses;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>Title: LessonLearnedList Class.</p>
 * <p>Description: Holds all lessons or any subset of lessons without a fixed size limit. A new
 * list is returned for searches so the complete database result remains available.</p>
 */
public class LessonLearnedList {
	private final ArrayList<LessonLearned> lessons = new ArrayList<>();

	/** Adds a nonnull lesson to the list. */
	public void addLesson(LessonLearned lesson) {
		if (lesson != null) lessons.add(lesson);
	}

	public int size() { return lessons.size(); }
	public boolean isEmpty() { return lessons.isEmpty(); }

	/** Returns a lesson by position, or null for an invalid position. */
	public LessonLearned getLesson(int index) {
		if (index < 0 || index >= lessons.size()) return null;
		return lessons.get(index);
	}

	/** Finds a lesson by its persistent ID, or returns null when the ID is not in this list. */
	public LessonLearned findByID(int lessonID) {
		int index = indexOfID(lessonID);
		return index < 0 ? null : lessons.get(index);
	}

	/** Replaces a lesson by its persistent ID without changing its position in the list (R13).
	 *
	 * @param lesson replacement lesson; its ID selects the list entry to replace
	 * @return true if a lesson with that ID was replaced; false for null, unpersisted, or missing IDs
	 */
	public boolean updateLesson(LessonLearned lesson) {
		if (lesson == null || lesson.getLessonID() <= 0) return false;
		int index = indexOfID(lesson.getLessonID());
		if (index < 0) return false;
		lessons.set(index, lesson);
		return true;
	}

	/** Removes a lesson by persistent ID (R13).
	 *
	 * @param lessonID persistent ID of the lesson to remove
	 * @return true if a lesson was removed; false for an invalid or missing ID
	 */
	public boolean removeLesson(int lessonID) {
		if (lessonID <= 0) return false;
		int index = indexOfID(lessonID);
		if (index < 0) return false;
		lessons.remove(index);
		return true;
	}

	/** Shared ID lookup keeps find, update, and remove aligned on the same matching rule. */
	private int indexOfID(int lessonID) {
		for (int i = 0; i < lessons.size(); i++)
			if (lessons.get(i).getLessonID() == lessonID) return i;
		return -1;
	}

	/** Returns a read-only view so callers cannot bypass list ownership. */
	public List<LessonLearned> getAllLessons() {
		return Collections.unmodifiableList(lessons);
	}

	/** R7 and R13: builds a new subset containing only lessons matching the keyword, including an
	 * empty subset when nothing matches. */
	public LessonLearnedList getSubsetByKeyword(String searchText) {
		LessonLearnedList subset = new LessonLearnedList();
		for (LessonLearned lesson : lessons)
			if (lesson.matchesKeyword(searchText)) subset.addLesson(lesson);
		return subset;
	}
}
