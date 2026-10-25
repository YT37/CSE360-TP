package entityTests;

import entityClasses.LessonLearned;

/**
 * <p>Title: LessonLearnedTests Class.</p>
 * <p>Description: Semi-automated tests for the lesson lock state that supports the Contributor
 * stories about locked lessons. Every test method names the requirement it exercises, explains how
 * the code satisfies that requirement, and explains how to read the console output.</p>
 *
 * <p>Requirement coverage: R4 (whole-lesson and individual-field lock state) with both positive
 * cases (a lock blocks the intended fields) and negative cases (clearing a lock or supplying an
 * unsupported field does not leave a false lock behind).</p>
 */
public class LessonLearnedTests {
	/** Creates the test runner. */
	public LessonLearnedTests() { }

	private static int passed;
	private static int failed;
	private static int testCaseNumber;

	/**
	 * <p>Runs every lock-state group and prints a pass/fail summary.</p>
	 *
	 * <p>Output: each line starts with the requirement ID and a numbered case. A line marked
	 * "***Success***" means the observed behavior matched the expectation in the method Javadoc; a
	 * "***Failure***" line identifies the broken rule. The final two totals must report a zero
	 * failure count, otherwise the process throws an AssertionError so an automated runner also sees
	 * the failure.</p>
	 *
	 * @param args not used
	 */
	public static void main(String[] args) {
		System.out.println("______________________________________\n\nLesson Learned Lock Test Automation");

		testNewLessonIsUnlocked();
		testFieldLockIsIndependent();
		testWholeLessonLockOverridesFields();
		testClearingWholeLessonLockPreservesFieldLocks();
		testUnlockField();
		testLockedFieldSetCannotBeBypassed();
		testUnknownFieldIsRejected();

		System.out.println("______________________________________\n");
		System.out.println("Number of tests passed: " + passed);
		System.out.println("Number of tests failed: " + failed);
		if (failed > 0) throw new AssertionError("Lesson lock tests failed: " + failed);
	}

	/**
	 * <p>Requirement tested: R4, positive case. A new lesson must not start locked.</p>
	 *
	 * <p>How the code satisfies it: {@link LessonLearned#LessonLearned()} leaves
	 * {@code lessonLocked} false and {@code lockedFields} empty, and
	 * {@link LessonLearned#isFieldLocked(String)} returns false until a setter is called.</p>
	 *
	 * <p>Reading the output: both "***Success***" lines mean the Contributor can edit a fresh
	 * lesson. A failure means a new lesson would wrongly appear read-only.</p>
	 */
	private static void testNewLessonIsUnlocked() {
		LessonLearned lesson = new LessonLearned();
		check("R4", "A new lesson is not locked as a whole", !lesson.isLessonLocked());
		check("R4", "A new lesson has no locked fields",
				!lesson.isFieldLocked("title") && !lesson.isFieldLocked("category"));
	}

	/**
	 * <p>Requirement tested: R4, positive and negative cases. Locking one field must not lock the
	 * rest, and the lock must be reported for persistence.</p>
	 *
	 * <p>How the code satisfies it: {@link LessonLearned#setFieldLocked(String, boolean)} adds a
	 * single field name to {@code lockedFields}; {@link LessonLearned#isFieldLocked(String)} checks
	 * only that field; {@link LessonLearned#getLockedFields()} exposes the stored set so the database
	 * can persist it.</p>
	 *
	 * <p>Reading the output: the first success means "title" is locked; the second success means
	 * "category" remains editable and the persisted lock list contains exactly "title".</p>
	 */
	private static void testFieldLockIsIndependent() {
		LessonLearned lesson = new LessonLearned();
		lesson.setFieldLocked("title", true);
		check("R4", "Locks the selected field without locking other fields",
				lesson.isFieldLocked("title") && !lesson.isFieldLocked("category"));
		check("R4", "Reports the explicitly locked field for persistence",
				lesson.getLockedFields().size() == 1 && lesson.getLockedFields().contains("title"));
	}

	/**
	 * <p>Requirement tested: R4, positive case. A whole-lesson lock must make every field
	 * unavailable.</p>
	 *
	 * <p>How the code satisfies it: {@link LessonLearned#isFieldLocked(String)} returns true when
	 * {@code lessonLocked} is true, regardless of the individual field set.</p>
	 *
	 * <p>Reading the output: success means the GUI and database will both treat every field as
	 * read-only while the whole lesson is locked.</p>
	 */
	private static void testWholeLessonLockOverridesFields() {
		LessonLearned lesson = new LessonLearned();
		lesson.setFieldLocked("title", true);
		lesson.setLessonLocked(true);
		check("R4", "A whole-lesson lock makes every field unavailable",
				lesson.isLessonLocked() && lesson.isFieldLocked("title")
						&& lesson.isFieldLocked("category"));
	}

	/**
	 * <p>Requirement tested: R4, negative case. Clearing the whole-lesson lock must not erase the
	 * independent field locks.</p>
	 *
	 * <p>How the code satisfies it: the whole-lesson flag and the field set are stored separately,
	 * and {@link LessonLearned#setLessonLocked(boolean)} changes only the flag.</p>
	 *
	 * <p>Reading the output: success means "title" stays locked after the whole-lesson lock is
	 * cleared, while "category" becomes editable again.</p>
	 */
	private static void testClearingWholeLessonLockPreservesFieldLocks() {
		LessonLearned lesson = new LessonLearned();
		lesson.setFieldLocked("title", true);
		lesson.setLessonLocked(true);
		lesson.setLessonLocked(false);
		check("R4", "Clearing a whole-lesson lock preserves independent field locks",
				!lesson.isLessonLocked() && lesson.isFieldLocked("title")
						&& !lesson.isFieldLocked("category"));
	}

	/**
	 * <p>Requirement tested: R4, positive case. An explicit unlock must remove only that field.</p>
	 *
	 * <p>How the code satisfies it: {@link LessonLearned#setFieldLocked(String, boolean)} removes
	 * the field name from {@code lockedFields} when the boolean is false.</p>
	 *
	 * <p>Reading the output: success means the unlocked lesson reports no locked fields, so the
	 * Contributor can edit and delete it.</p>
	 */
	private static void testUnlockField() {
		LessonLearned lesson = new LessonLearned();
		lesson.setFieldLocked("title", true);
		lesson.setFieldLocked("title", false);
		check("R4", "Unlocks an individually locked field",
				!lesson.isFieldLocked("title") && lesson.getLockedFields().isEmpty());
	}

	/**
	 * <p>Requirement tested: R4, negative case. Callers must not bypass the lock setter by
	 * modifying the returned set.</p>
	 *
	 * <p>How the code satisfies it: {@link LessonLearned#getLockedFields()} returns
	 * {@link java.util.Collections#unmodifiableSet(java.util.Set)}, so an attempted add throws
	 * {@link UnsupportedOperationException} and leaves the entity unchanged.</p>
	 *
	 * <p>Reading the output: success means the GUI cannot silently inject a lock that the database
	 * never stored; a failure would mean lock state could be corrupted outside the setter.</p>
	 */
	private static void testLockedFieldSetCannotBeBypassed() {
		LessonLearned lesson = new LessonLearned();
		boolean lockViewReadOnly = false;
		try {
			lesson.getLockedFields().add("category");
		} catch (UnsupportedOperationException expected) {
			lockViewReadOnly = true;
		}
		check("R4", "Prevents callers from bypassing the lock setter",
				lockViewReadOnly && lesson.getLockedFields().isEmpty());
	}

	/**
	 * <p>Requirement tested: R4, negative case. An unsupported field name must be rejected rather
	 * than stored as a meaningless lock.</p>
	 *
	 * <p>How the code satisfies it: {@link LessonLearned#isFieldLocked(String)} and
	 * {@link LessonLearned#setFieldLocked(String, boolean)} call {@code validateLockableField},
	 * which throws {@link IllegalArgumentException} for names outside the documented lockable
	 * fields.</p>
	 *
	 * <p>Reading the output: success means both calls rejected the bad name. A failure would allow a
	 * typo such as "titel" to appear successful while the real field stayed editable.</p>
	 */
	private static void testUnknownFieldIsRejected() {
		LessonLearned lesson = new LessonLearned();
		boolean setterRejected = false;
		boolean getterRejected = false;
		try {
			lesson.setFieldLocked("titel", true);
		} catch (IllegalArgumentException expected) {
			setterRejected = true;
		}
		try {
			lesson.isFieldLocked("titel");
		} catch (IllegalArgumentException expected) {
			getterRejected = true;
		}
		check("R4", "Rejects an unsupported lockable field name", setterRejected && getterRejected);
	}

	/**
	 * <p>Records one numbered result and prints the requirement tag and description.</p>
	 *
	 * <p>Reading the output: "***Success***" means the assertion matched the expectation described
	 * in the calling method's Javadoc; "***Failure***" means it did not and the final failure total
	 * becomes non-zero.</p>
	 *
	 * @param requirement the requirement identifier exercised by this assertion
	 * @param description what the assertion checks
	 * @param condition the observed result
	 */
	private static void check(String requirement, String description, boolean condition) {
		testCaseNumber++;
		System.out.println("Test case " + testCaseNumber + " [" + requirement + "]: " + description);
		if (condition) {
			passed++;
			System.out.println("***Success*** " + description);
		} else {
			failed++;
			System.out.println("***Failure*** " + description);
		}
	}
}
