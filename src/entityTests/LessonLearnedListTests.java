package entityTests;

import java.util.List;

import entityClasses.LessonLearned;
import entityClasses.LessonLearnedList;

/**
 * <p>Title: LessonLearnedListTests Class.</p>
 * <p>Description: Semi-automated tests for list CRUD and keyword subsets. The tests cover the list
 * operations required by the Contributor stories and the search behavior that produces a subset of
 * any size, including an empty one.</p>
 *
 * <p>Requirement coverage: R13 (list update and remove) and R7 (keyword subsets), each with
 * positive cases that must succeed and negative cases that must be rejected without corrupting the
 * list.</p>
 */
public class LessonLearnedListTests {
	private static int passed;
	private static int failed;
	private static int testCaseNumber;

	/**
	 * <p>Runs every list group and prints a pass/fail summary.</p>
	 *
	 * <p>Output: each line starts with the requirement ID and a numbered case. A line marked
	 * "***Success***" means the observed list state matched the expectation in the method Javadoc; a
	 * "***Failure***" line identifies the broken rule. The final failure total must be zero,
	 * otherwise the process throws an AssertionError for automated runners.</p>
	 *
	 * @param args not used
	 */
	public static void main(String[] args) {
		System.out.println("______________________________________\n\nLesson Learned List Test Automation");

		testEmptyList();
		testInvalidIndexes();
		testAddAndRead();
		testExposedListIsReadOnly();
		testUpdateExisting();
		testUpdateMissingAndNull();
		testRemoveExisting();
		testRemoveMissing();
		testKeywordSubset();

		System.out.println("______________________________________\n");
		System.out.println("Number of tests passed: " + passed);
		System.out.println("Number of tests failed: " + failed);
		if (failed > 0) throw new AssertionError("Lesson list tests failed: " + failed);
	}

	/**
	 * <p>Requirement tested: R13, positive case. A new list must start empty with size zero.</p>
	 *
	 * <p>How the code satisfies it: {@link LessonLearnedList} creates an empty
	 * {@link java.util.ArrayList}, and {@link LessonLearnedList#isEmpty()} and
	 * {@link LessonLearnedList#size()} report that state directly.</p>
	 *
	 * <p>Reading the output: success means create/search operations start from a predictable empty
	 * list rather than a partially initialized one.</p>
	 */
	private static void testEmptyList() {
		LessonLearnedList lessons = new LessonLearnedList();
		check("R13", "Starts empty with size zero", lessons.isEmpty() && lessons.size() == 0);
	}

	/**
	 * <p>Requirement tested: R13, negative case. Reading an invalid position must return null
	 * instead of throwing.</p>
	 *
	 * <p>How the code satisfies it: {@link LessonLearnedList#getLesson(int)} checks
	 * {@code index < 0 || index >= size} before calling {@code ArrayList.get}.</p>
	 *
	 * <p>Reading the output: both successes mean the GUI can safely clear its selection or ask for a
	 * position that no longer exists; a failure would mean an IndexOutOfBoundsException in the
	 * window.</p>
	 */
	private static void testInvalidIndexes() {
		LessonLearnedList lessons = new LessonLearnedList();
		check("R13", "Returns null for an index in an empty list", lessons.getLesson(0) == null);
		LessonLearned first = lesson(101, "First lesson");
		lessons.addLesson(first);
		check("R13", "Returns null for negative and out-of-range indexes",
				lessons.getLesson(-1) == null && lessons.getLesson(lessons.size()) == null);
	}

	/**
	 * <p>Requirement tested: R13, positive case. Adding lessons must preserve insertion order so the
	 * GUI list matches the database order.</p>
	 *
	 * <p>How the code satisfies it: {@link LessonLearnedList#addLesson(LessonLearned)} appends to
	 * the backing {@code ArrayList}, and {@link LessonLearnedList#getLesson(int)} returns the object
	 * at that position.</p>
	 *
	 * <p>Reading the output: success means one and then two lessons are stored in the order they
	 * were added.</p>
	 */
	private static void testAddAndRead() {
		LessonLearnedList lessons = new LessonLearnedList();
		LessonLearned first = lesson(101, "First lesson");
		lessons.addLesson(first);
		check("R13", "Stores and reads one lesson",
				!lessons.isEmpty() && lessons.size() == 1 && lessons.getLesson(0) == first);
		LessonLearned second = lesson(202, "Second lesson");
		lessons.addLesson(second);
		check("R13", "Stores and reads multiple lessons in order",
				lessons.size() == 2 && lessons.getLesson(1) == second);
	}

	/**
	 * <p>Requirement tested: R13, negative case. A caller must not mutate the list through the
	 * collection returned for display.</p>
	 *
	 * <p>How the code satisfies it: {@link LessonLearnedList#getAllLessons()} returns
	 * {@link java.util.Collections#unmodifiableList(java.util.List)}, so adding through the result
	 * throws {@link UnsupportedOperationException}.</p>
	 *
	 * <p>Reading the output: success means the displayed list cannot be changed behind the list
	 * class's back; a failure would allow an unpersisted lesson to appear in the GUI.</p>
	 */
	private static void testExposedListIsReadOnly() {
		LessonLearnedList lessons = new LessonLearnedList();
		lessons.addLesson(lesson(101, "First lesson"));
		lessons.addLesson(lesson(202, "Second lesson"));
		List<LessonLearned> exposedLessons = lessons.getAllLessons();
		boolean readOnly = false;
		try {
			exposedLessons.add(lesson(303, "External lesson"));
		} catch (UnsupportedOperationException expected) {
			readOnly = true;
		}
		check("R13", "Prevents callers from mutating the exposed list", readOnly && lessons.size() == 2);
	}

	/**
	 * <p>Requirement tested: R13, positive case. Updating an existing ID must replace the lesson
	 * without moving it.</p>
	 *
	 * <p>How the code satisfies it: {@link LessonLearnedList#updateLesson(LessonLearned)} finds the
	 * position with the shared {@code indexOfID} helper and calls {@code ArrayList.set}, so the
	 * replacement keeps the original position.</p>
	 *
	 * <p>Reading the output: both successes mean the refreshed lesson is found by ID and by its
	 * original position, and the list still has two entries.</p>
	 */
	private static void testUpdateExisting() {
		LessonLearnedList lessons = new LessonLearnedList();
		lessons.addLesson(lesson(101, "First lesson"));
		lessons.addLesson(lesson(202, "Second lesson"));
		LessonLearned replacement = lesson(101, "Updated first lesson");
		check("R13", "Updates a lesson with an existing ID", lessons.updateLesson(replacement));
		check("R13", "Keeps the updated lesson in its original position",
				lessons.findByID(101) == replacement && lessons.getLesson(0) == replacement
						&& lessons.size() == 2);
	}

	/**
	 * <p>Requirement tested: R13, negative cases. A missing ID or a null replacement must be
	 * rejected without changing the list.</p>
	 *
	 * <p>How the code satisfies it: {@link LessonLearnedList#updateLesson(LessonLearned)} returns
	 * false for null, an unpersisted ID ({@code <= 0}), or an ID that {@code indexOfID} does not
	 * find; it never calls {@code set} in those cases.</p>
	 *
	 * <p>Reading the output: all three successes mean the list is unchanged after each rejected
	 * update, so a failed edit cannot silently delete or reorder lessons.</p>
	 */
	private static void testUpdateMissingAndNull() {
		LessonLearnedList lessons = new LessonLearnedList();
		LessonLearned replacement = lesson(101, "Updated first lesson");
		lessons.addLesson(replacement);
		LessonLearned missing = lesson(999, "Missing lesson");
		check("R13", "Rejects updating a missing lesson ID", !lessons.updateLesson(missing));
		check("R13", "Leaves the list unchanged after a missing-ID update",
				lessons.size() == 1 && lessons.findByID(101) == replacement
						&& lessons.findByID(999) == null);
		check("R13", "Rejects a null lesson update", !lessons.updateLesson(null));
	}

	/**
	 * <p>Requirement tested: R13, positive case. Removing an existing ID must remove only that
	 * lesson.</p>
	 *
	 * <p>How the code satisfies it: {@link LessonLearnedList#removeLesson(int)} finds the position
	 * through {@code indexOfID} and removes exactly one entry.</p>
	 *
	 * <p>Reading the output: both successes mean the removed lesson is gone and the other lesson is
	 * still present; a failure would mean delete removed the wrong record.</p>
	 */
	private static void testRemoveExisting() {
		LessonLearnedList lessons = new LessonLearnedList();
		LessonLearned replacement = lesson(101, "Updated first lesson");
		lessons.addLesson(replacement);
		lessons.addLesson(lesson(202, "Second lesson"));
		check("R13", "Removes a lesson with an existing ID", lessons.removeLesson(202));
		check("R13", "Retains other lessons after removal",
				lessons.size() == 1 && lessons.findByID(202) == null
						&& lessons.findByID(101) == replacement);
	}

	/**
	 * <p>Requirement tested: R13, negative case. Removing a missing ID must return false and leave
	 * the list untouched.</p>
	 *
	 * <p>How the code satisfies it: {@link LessonLearnedList#removeLesson(int)} returns false for
	 * {@code lessonID <= 0} and when {@code indexOfID} returns -1.</p>
	 *
	 * <p>Reading the output: both successes mean a delete of an already-removed record cannot remove
	 * a neighboring lesson.</p>
	 */
	private static void testRemoveMissing() {
		LessonLearnedList lessons = new LessonLearnedList();
		LessonLearned replacement = lesson(101, "Updated first lesson");
		lessons.addLesson(replacement);
		check("R13", "Rejects removing a missing lesson ID", !lessons.removeLesson(999));
		check("R13", "Leaves the list unchanged after a missing-ID removal",
				lessons.size() == 1 && lessons.findByID(101) == replacement);
	}

	/**
	 * <p>Requirement tested: R7, positive and negative cases. A keyword search must return a new
	 * subset containing only matches, and an unmatched term must produce an empty subset.</p>
	 *
	 * <p>How the code satisfies it: {@link LessonLearnedList#getSubsetByKeyword(String)} builds a
	 * new list and adds only lessons whose {@link LessonLearned#matchesKeyword(String)} returns true
	 * for a case-insensitive match in any searchable field.</p>
	 *
	 * <p>Reading the output: the first success means the matching lesson is found by a keyword in
	 * its keywords field; the second success means an unknown term returns zero lessons instead of
	 * the full list or null.</p>
	 */
	private static void testKeywordSubset() {
		LessonLearnedList lessons = new LessonLearnedList();
		lessons.addLesson(lesson(101, "First lesson"));
		lessons.addLesson(lesson(202, "Second lesson"));
		check("R7", "Creates a matching subset", lessons.getSubsetByKeyword("testing").size() == 2);
		check("R7", "Supports an empty subset", lessons.getSubsetByKeyword("missing term").isEmpty());
	}

	/**
	 * <p>Builds a lesson with a distinctive title and the shared test keywords.</p>
	 *
	 * @param id the persistent ID assigned by the test
	 * @param title the lesson title
	 * @return a populated lesson
	 */
	private static LessonLearned lesson(int id, String title) {
		LessonLearned lesson = new LessonLearned("contributor", title, "Technical",
				"The team encountered a specific problem during the project work.",
				"The team learned to verify the behavior before relying on it.",
				"Verify the behavior before using the result again.", "testing, list");
		lesson.setLessonID(id);
		return lesson;
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
