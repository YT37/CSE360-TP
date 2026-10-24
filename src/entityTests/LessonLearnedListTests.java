package entityTests;

import java.util.List;

import entityClasses.LessonLearned;
import entityClasses.LessonLearnedList;

/**
 * R13 tests for list CRUD and collection behavior. Each case reports its result, and the process
 * exits non-zero if any case fails so automated runs cannot mistake a failure for success.
 */
public class LessonLearnedListTests {
	private static int passed;
	private static int failed;
	private static int testCaseNumber;

	/** Runs the R13 list cases and prints a pass/fail summary. */
	public static void main(String[] args) {
		LessonLearnedList lessons = new LessonLearnedList();
		check(lessons.isEmpty() && lessons.size() == 0, "Starts empty with size zero");
		check(lessons.getLesson(0) == null, "Returns null for an index in an empty list");

		LessonLearned first = lesson(101, "First lesson");
		lessons.addLesson(first);
		check(!lessons.isEmpty() && lessons.size() == 1 && lessons.getLesson(0) == first,
				"Stores and reads one lesson");

		LessonLearned second = lesson(202, "Second lesson");
		lessons.addLesson(second);
		check(lessons.size() == 2 && lessons.getLesson(1) == second,
				"Stores and reads multiple lessons in order");
		check(lessons.getLesson(-1) == null && lessons.getLesson(lessons.size()) == null,
				"Returns null for negative and out-of-range indexes");

		List<LessonLearned> exposedLessons = lessons.getAllLessons();
		boolean readOnly = false;
		try {
			exposedLessons.add(lesson(303, "External lesson"));
		} catch (UnsupportedOperationException expected) {
			readOnly = true;
		}
		check(readOnly && lessons.size() == 2, "Prevents callers from mutating the exposed list");

		LessonLearned replacement = lesson(101, "Updated first lesson");
		check(lessons.updateLesson(replacement), "Updates a lesson with an existing ID");
		check(lessons.findByID(101) == replacement && lessons.getLesson(0) == replacement
				&& lessons.size() == 2, "Keeps the updated lesson in its original position");

		LessonLearned missing = lesson(999, "Missing lesson");
		check(!lessons.updateLesson(missing), "Rejects updating a missing lesson ID");
		check(lessons.size() == 2 && lessons.findByID(101) == replacement
				&& lessons.findByID(999) == null, "Leaves the list unchanged after a missing-ID update");
		check(!lessons.updateLesson(null), "Rejects a null lesson update");

		check(lessons.removeLesson(202), "Removes a lesson with an existing ID");
		check(lessons.size() == 1 && lessons.findByID(202) == null
				&& lessons.findByID(101) == replacement, "Retains other lessons after removal");
		check(!lessons.removeLesson(999), "Rejects removing a missing lesson ID");
		check(lessons.size() == 1 && lessons.findByID(101) == replacement,
				"Leaves the list unchanged after a missing-ID removal");

		System.out.println("Number of tests passed: " + passed);
		System.out.println("Number of tests failed: " + failed);
		if (failed > 0) throw new AssertionError("Lesson list tests failed");
	}

	private static LessonLearned lesson(int id, String title) {
		LessonLearned lesson = new LessonLearned("contributor", title, "Technical",
				"The team encountered a specific problem during the project work.",
				"The team learned to verify the behavior before relying on it.",
				"Verify the behavior before using the result again.", "testing, list");
		lesson.setLessonID(id);
		return lesson;
	}

	private static void check(boolean condition, String description) {
		testCaseNumber++;
		System.out.println("Test case: " + testCaseNumber + " - " + description);
		if (condition) {
			passed++;
			System.out.println("Success: " + description);
		} else {
			failed++;
			System.out.println("Failure: " + description);
		}
	}
}
