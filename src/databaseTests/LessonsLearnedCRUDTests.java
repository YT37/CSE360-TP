package databaseTests;

import java.io.File;
import database.Database;
import entityClasses.LessonLearned;
import entityClasses.LessonLearnedList;
import entityClasses.User;

/**
 * <p>Title: LessonsLearnedCRUDTests Class.</p>
 * <p>Description: Semi-automated tests for lesson persistence, CRUD, ownership, and subsets. A
 * separate H2 file keeps destructive test operations away from the application's real data.</p>
 */
public class LessonsLearnedCRUDTests {
	private static int passed;
	private static int failed;
	private static int testCaseNumber;
	private static final String URL = "jdbc:h2:~/HW2TestDatabase";

	/** Runs every database test case and prints a pass/fail summary. */
	public static void main(String[] args) throws Exception {
		System.out.println("______________________________________\n\nLessons Learned CRUD Test Automation");
		deleteTestDatabase();
		Database db = new Database(URL);
		db.connectToDatabase();
		db.register(new User("yogi", "password", "Yogi", "", "Tester", "Yogi", "yogi@example.com", false, true, false));
		db.register(new User("bob", "password", "Bob", "", "Tester", "Bob", "bob@example.com", false, true, false));
		check(db.readAllLessonsLearned().isEmpty(), "Reads an empty lesson database");
		check(db.createLessonLearned(new LessonLearned("yogi", "", "Technical", "A valid situation with enough detail.",
				"A valid lesson with enough detail.", "A valid recommendation.", "test")).contains("Title"),
				"Rejects invalid creation with a title error");
		LessonLearned first = lesson("yogi", "H2 database");
		check(db.createLessonLearned(first).isEmpty() && first.getLessonID() > 0, "Creates a lesson");
		check(db.readLessonLearned(first.getLessonID()) != null, "Reads a lesson by ID");
		check(!db.createLessonLearned(lesson("yogi", "h2 DATABASE")).isEmpty(), "Rejects duplicate title");
		LessonLearned second = lesson("bob", "Git workflow");
		db.createLessonLearned(second);
		LessonLearned duplicate = lesson("yogi", "Duplicate target");
		duplicate.setKeywords("duplicate");
		db.createLessonLearned(duplicate);
		check(db.readLessonsLearnedForContributor("yogi").size() == 2, "Reads only the contributor's lessons");
		LessonLearnedList all = db.readAllLessonsLearned();
		check(all.size() == 3, "Reads all lessons");
		check(all.getSubsetByKeyword("database").size() == 1, "Creates a matching subset");
		check(all.getSubsetByKeyword("missing term").isEmpty(), "Supports an empty subset");
		for (int i = 1; i <= 500; i++) db.createLessonLearned(lesson("yogi", "Practice lesson " + i));
		check(db.readLessonsLearnedForContributor("yogi").size() == 502,
				"Supports a large contributor list without a fixed limit");

		LessonLearned missing = lesson("yogi", "Missing update");
		missing.setLessonID(999999);
		check(db.updateLessonLearned(missing, "yogi").contains("not found"),
				"Rejects updating a missing lesson with a not-found error");
		first.setTitle("Duplicate target");
		check(db.updateLessonLearned(first, "yogi").contains("another lesson"),
				"Rejects an update to a duplicate title");
		first.setTitle("Updated H2 database");
		check(db.updateLessonLearned(first, "yogi").isEmpty(), "Updates an owned lesson");
		check(db.updateLessonLearned(first, "bob").contains("belongs"),
				"Rejects an unauthorized update with an ownership error");
		check(db.deleteLessonLearned(first.getLessonID(), "yogi").isEmpty(), "Deletes an owned lesson");
		check(db.deleteLessonLearned(first.getLessonID(), "yogi").contains("not found"),
				"Rejects deleting a missing lesson with a not-found error");

		LessonLearned persistent = lesson("yogi", "Persistence test");
		db.createLessonLearned(persistent);
		db.closeConnection();
		Database reopened = new Database(URL);
		reopened.connectToDatabase();
		check(reopened.readLessonLearned(persistent.getLessonID()) != null, "Persists after reconnecting");
		reopened.updateUsername("yogi", "yogi_renamed");
		check(reopened.readLessonsLearnedForContributor("yogi_renamed").size() == 502,
				"Migrates lesson ownership when the contributor renames their account");
		check(reopened.deleteLessonLearned(persistent.getLessonID(), "yogi_renamed").isEmpty(),
				"Allows the renamed contributor to delete their lesson");
		check(reopened.deleteUser("bob"), "Deletes a user account");
		reopened.register(new User("bob", "password", "Bob", "", "Recreated", "Bob", "bob2@example.com", false, true, false));
		check(reopened.readLessonsLearnedForContributor("bob").isEmpty(),
				"Prevents a recreated username from inheriting deleted user's lessons");
		reopened.closeConnection();
		System.out.println("Number of tests passed: " + passed);
		System.out.println("Number of tests failed: " + failed);
		if (failed > 0) throw new AssertionError("Lesson CRUD tests failed");
	}

	private static LessonLearned lesson(String user, String title) {
		String keywords = title.equals("Git workflow") ? "git, workflow" : "database, testing";
		return new LessonLearned(user, title, "Technical",
				"The team encountered a specific problem while using the application.",
				"The team learned to verify the setup before starting work.",
				"Check the setup before repeating the work.", keywords);
	}

	private static void deleteTestDatabase() {
		String home = System.getProperty("user.home");
		new File(home, "HW2TestDatabase.mv.db").delete();
		new File(home, "HW2TestDatabase.trace.db").delete();
	}

	private static void check(boolean condition, String description) {
		testCaseNumber++;
		System.out.println("Test case: " + testCaseNumber + " - " + description);
		if (condition) { passed++; System.out.println("Success: " + description); }
		else { failed++; System.out.println("Failure: " + description); }
	}
}
