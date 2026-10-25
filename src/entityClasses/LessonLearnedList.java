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
	/** Backing list of lessons; order is preserved. */
	private final ArrayList<LessonLearned> lessons = new ArrayList<>();

	/** Creates an empty lesson list. */
	public LessonLearnedList() { }

	/** Adds a nonnull lesson to the list.
	 *
	 * @param lesson the lesson to add; null is ignored
	 */
	public void addLesson(LessonLearned lesson) {
		if (lesson != null) lessons.add(lesson);
	}

	/** Returns the number of lessons in this list.
	 *
	 * @return the list size
	 */
	public int size() { return lessons.size(); }

	/** Returns whether this list contains no lessons.
	 *
	 * @return true when the list is empty
	 */
	public boolean isEmpty() { return lessons.isEmpty(); }

	/** Returns a lesson by position, or null for an invalid position.
	 *
	 * @param index zero-based position in the list
	 * @return the lesson at index, or null when index is out of range
	 */
	public LessonLearned getLesson(int index) {
		if (index < 0 || index >= lessons.size()) return null;
		return lessons.get(index);
	}

	/** Finds a lesson by its persistent ID, or returns null when the ID is not in this list.
	 *
	 * @param lessonID persistent ID to find
	 * @return the matching lesson, or null when not present
	 */
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

	/** Shared ID lookup keeps find, update, and remove aligned on the same matching rule.
	 *
	 * @param lessonID persistent ID to find
	 * @return the list position, or -1 when not present
	 */
	private int indexOfID(int lessonID) {
		for (int i = 0; i < lessons.size(); i++)
			if (lessons.get(i).getLessonID() == lessonID) return i;
		return -1;
	}

	/** Returns a read-only view so callers cannot bypass list ownership.
	 *
	 * @return an unmodifiable list of all lessons
	 */
	public List<LessonLearned> getAllLessons() {
		return Collections.unmodifiableList(lessons);
	}

	/** R7 and R13: builds a new subset containing only lessons matching the keyword, including an
	 * empty subset when nothing matches.
	 *
	 * @param searchText keyword to search for
	 * @return a new list containing only matching lessons
	 */
	public LessonLearnedList getSubsetByKeyword(String searchText) {
		LessonLearnedList subset = new LessonLearnedList();
		for (LessonLearned lesson : lessons)
			if (lesson.matchesKeyword(searchText)) subset.addLesson(lesson);
		return subset;
	}
}
