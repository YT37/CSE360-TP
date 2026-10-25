package entityTests;

import entityClasses.LessonLearned;

/** R4 tests for whole-lesson and individual-field lock state. */
public class LessonLearnedTests {
	private static int passed;
	private static int failed;
	private static int testCaseNumber;

	/** Runs lock-state cases and exits non-zero if any expected behavior fails. */
	public static void main(String[] args) {
		LessonLearned lesson = new LessonLearned();
		check(!lesson.isLessonLocked(), "A new lesson is not locked as a whole");
		check(!lesson.isFieldLocked("title") && !lesson.isFieldLocked("category"),
				"A new lesson has no locked fields");

		lesson.setFieldLocked("title", true);
		check(lesson.isFieldLocked("title") && !lesson.isFieldLocked("category"),
				"Locks the selected field without locking other fields");
		check(lesson.getLockedFields().size() == 1 && lesson.getLockedFields().contains("title"),
				"Reports the explicitly locked field for persistence");

		lesson.setLessonLocked(true);
		check(lesson.isLessonLocked() && lesson.isFieldLocked("title")
				&& lesson.isFieldLocked("category"), "A whole-lesson lock makes every field unavailable");

		lesson.setLessonLocked(false);
		check(!lesson.isLessonLocked() && lesson.isFieldLocked("title")
				&& !lesson.isFieldLocked("category"),
				"Clearing a whole-lesson lock preserves independent field locks");

		lesson.setFieldLocked("title", false);
		check(!lesson.isFieldLocked("title") && lesson.getLockedFields().isEmpty(),
				"Unlocks an individually locked field");

		boolean lockViewReadOnly = false;
		try {
			lesson.getLockedFields().add("category");
		} catch (UnsupportedOperationException expected) {
			lockViewReadOnly = true;
		}
		check(lockViewReadOnly && lesson.getLockedFields().isEmpty(),
				"Prevents callers from bypassing the lock setter");

		System.out.println("Number of tests passed: " + passed);
		System.out.println("Number of tests failed: " + failed);
		if (failed > 0) throw new AssertionError("Lesson lock tests failed");
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
