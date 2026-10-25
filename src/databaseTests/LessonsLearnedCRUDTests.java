package databaseTests;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import database.Database;
import entityClasses.ExperienceRecord;
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
		createLegacyLessonTable();
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
		LessonLearned firstRead = db.readLessonLearned(first.getLessonID());
		check(firstRead != null, "Reads a lesson by ID");
		check(firstRead != null && !firstRead.isLessonLocked() && firstRead.getLockedFields().isEmpty()
				&& !firstRead.isCuratorEdited() && firstRead.getEditedBy() == null
				&& firstRead.getSummary().isEmpty() && firstRead.getRootCause().isEmpty(),
				"Defaults lock and curator-edit metadata for a new lesson");
		check(!db.createLessonLearned(lesson("yogi", "h2 DATABASE")).isEmpty(), "Rejects duplicate title");
		LessonLearned second = lesson("bob", "Git workflow");
		second.setFieldLocked("title", true);
		db.createLessonLearned(second);
		LessonLearned duplicate = lesson("yogi", "Duplicate target");
		duplicate.setKeywords("duplicate");
		duplicate.setLessonLocked(true);
		duplicate.setCuratorEdited(true);
		duplicate.setEditedBy("curator_test");
		db.createLessonLearned(duplicate);
		LessonLearned curatorEditedLesson = db.readLessonLearned(duplicate.getLessonID());
		check(curatorEditedLesson != null && curatorEditedLesson.isCuratorEdited()
				&& curatorEditedLesson.getEditedBy().equals("curator_test"),
				"Reads curator-edited marker and editor identity after saving a lesson");
		check(db.readLessonsLearnedForContributor("bob").findByID(duplicate.getLessonID()) == null,
				"Keeps curator-edited lesson invisible to another Contributor");

		LessonLearned lockedTitleEdit = lesson("bob", "Changed Git workflow");
		lockedTitleEdit.setLessonID(second.getLessonID());
		String titleLockError = db.updateLessonLearned(lockedTitleEdit, "bob");
		check(titleLockError.toLowerCase().contains("title"),
				"Rejects editing a locked field with an error naming that field");

		LessonLearned unlockedFieldEdit = lesson("bob", "Git workflow");
		unlockedFieldEdit.setLessonID(second.getLessonID());
		unlockedFieldEdit.setSituation("The team recorded a new situation while preserving the locked title.");
		check(db.updateLessonLearned(unlockedFieldEdit, "bob").isEmpty(),
				"Allows updating an unlocked field when another field is locked");
		LessonLearned partiallyUpdated = db.readLessonLearned(second.getLessonID());
		check(partiallyUpdated != null && partiallyUpdated.getTitle().equals("Git workflow")
				&& partiallyUpdated.getSituation().equals(unlockedFieldEdit.getSituation()),
				"Preserves the locked title while saving an unlocked field change");

		LessonLearned wholeLessonEdit = lesson("yogi", "Duplicate target");
		wholeLessonEdit.setLessonID(duplicate.getLessonID());
		wholeLessonEdit.setKeywords(duplicate.getKeywords());
		wholeLessonEdit.setSituation("The contributor attempts to change a whole-lesson locked record.");
		String wholeLessonLockError = db.updateLessonLearned(wholeLessonEdit, "yogi");
		check(wholeLessonLockError.toLowerCase().contains("locked"),
				"Rejects an edit to any field when the whole lesson is locked");
		LessonLearned stillLocked = db.readLessonLearned(duplicate.getLessonID());
		check(stillLocked != null && !stillLocked.getSituation().equals(wholeLessonEdit.getSituation()),
				"Leaves whole-lesson-locked content unchanged after a rejected update");

		String fieldLockedDeleteError = db.deleteLessonLearned(second.getLessonID(), "bob");
		check(fieldLockedDeleteError.toLowerCase().contains("locked"),
				"Rejects deleting a lesson with a locked field");
		if (fieldLockedDeleteError.isEmpty()) {
			LessonLearned restoredSecond = lesson("bob", "Git workflow");
			restoredSecond.setFieldLocked("title", true);
			db.createLessonLearned(restoredSecond);
			second = restoredSecond;
		}
		check(db.readLessonLearned(second.getLessonID()) != null,
				"Retains a lesson after deletion is blocked by a field lock");
		String wholeLockedDeleteError = db.deleteLessonLearned(duplicate.getLessonID(), "yogi");
		check(wholeLockedDeleteError.toLowerCase().contains("locked"),
				"Rejects deleting a whole-lesson-locked record");
		if (wholeLockedDeleteError.isEmpty()) {
			LessonLearned restoredDuplicate = lesson("yogi", "Duplicate target");
			restoredDuplicate.setKeywords("duplicate");
			restoredDuplicate.setLessonLocked(true);
			db.createLessonLearned(restoredDuplicate);
			duplicate = restoredDuplicate;
		}
		check(db.readLessonLearned(duplicate.getLessonID()) != null,
				"Retains a lesson after deletion is blocked by a whole-lesson lock");

		ExperienceRecord missingParent = experience(999999, "yogi");
		check(db.createExperienceRecord(missingParent, "yogi").toLowerCase().contains("lesson"),
				"Rejects creating experience for a missing lesson");
		ExperienceRecord forgedOwner = experience(second.getLessonID(), "bob");
		check(db.createExperienceRecord(forgedOwner, "yogi").toLowerCase().contains("owner"),
				"Rejects adding experience to another Contributor's lesson");
		ExperienceRecord recordedExperience = experience(second.getLessonID(), "bob");
		check(db.createExperienceRecord(recordedExperience, "bob").isEmpty()
				&& recordedExperience.getExperienceID() > 0, "Creates an experience record with a database ID");
		ExperienceRecord readExperience = db.readExperienceRecord(recordedExperience.getExperienceID());
		check(readExperience != null && readExperience.getLessonID() == second.getLessonID()
				&& readExperience.getContributorUserName().equals("bob")
				&& readExperience.getWhatWasDone().equals(recordedExperience.getWhatWasDone())
				&& readExperience.getHow().equals(recordedExperience.getHow())
				&& readExperience.getDurationHours() == recordedExperience.getDurationHours()
				&& readExperience.getEffortHoursByMember().equals(recordedExperience.getEffortHoursByMember()),
				"Reads the experience fields and member efforts that were stored");
		check(db.readExperienceRecord(999999) == null, "Returns null when an experience ID is missing");
		List<ExperienceRecord> bobExperiences = db.readExperienceRecordsForLesson(second.getLessonID(), "bob");
		check(bobExperiences.size() == 1
				&& bobExperiences.get(0).getExperienceID() == recordedExperience.getExperienceID(),
				"Lists experiences for the owning Contributor's lesson");
		check(db.readExperienceRecordsForLesson(second.getLessonID(), "yogi").isEmpty(),
				"Does not list another Contributor's experience records");

		ExperienceRecord updatedExperience = experience(second.getLessonID(), "bob");
		updatedExperience.setExperienceID(recordedExperience.getExperienceID());
		updatedExperience.setWhatWasDone("The team completed a second integration attempt and recorded the outcome.");
		updatedExperience.setEffortHoursByMember(Map.of("Alice", 2.0));
		check(db.updateExperienceRecord(updatedExperience, "bob").isEmpty(),
				"Allows the experience owner to update the record");
		ExperienceRecord afterUpdate = db.readExperienceRecord(recordedExperience.getExperienceID());
		check(afterUpdate != null && afterUpdate.getWhatWasDone().equals(updatedExperience.getWhatWasDone())
				&& afterUpdate.getEffortHoursByMember().equals(updatedExperience.getEffortHoursByMember()),
				"Persists updated experience details and effort entries");
		ExperienceRecord unauthorizedUpdate = experience(second.getLessonID(), "bob");
		unauthorizedUpdate.setExperienceID(recordedExperience.getExperienceID());
		unauthorizedUpdate.setWhatWasDone("Another Contributor attempts to update this experience record.");
		check(db.updateExperienceRecord(unauthorizedUpdate, "yogi").toLowerCase().contains("owner"),
				"Rejects another Contributor's experience update");
		check(db.deleteExperienceRecord(recordedExperience.getExperienceID(), "yogi")
				.toLowerCase().contains("owner"), "Rejects another Contributor's experience deletion");
		check(db.readExperienceRecord(recordedExperience.getExperienceID()) != null,
				"Retains an experience after another Contributor's rejected delete");
		check(db.deleteExperienceRecord(recordedExperience.getExperienceID(), "bob").isEmpty(),
				"Allows the experience owner to delete the record");
		check(db.readExperienceRecord(recordedExperience.getExperienceID()) == null,
				"Removes an experience after its owner deletes it");

		ExperienceRecord bobAccountExperience = experience(second.getLessonID(), "bob");
		check(db.createExperienceRecord(bobAccountExperience, "bob").isEmpty(),
				"Creates an experience for the account-deletion cascade check");
		LessonLearned cascadeParent = lesson("bob", "Experience cascade parent");
		check(db.createLessonLearned(cascadeParent).isEmpty(), "Creates a lesson for the experience cascade check");
		ExperienceRecord cascadeExperience = experience(cascadeParent.getLessonID(), "bob");
		check(db.createExperienceRecord(cascadeExperience, "bob").isEmpty(),
				"Creates an experience for the lesson-deletion cascade check");
		check(db.deleteLessonLearned(cascadeParent.getLessonID(), "bob").isEmpty(),
				"Deletes the parent lesson for the experience cascade check");
		check(db.readExperienceRecord(cascadeExperience.getExperienceID()) == null,
				"Deletes experience records when their lesson is deleted");

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
		persistent.setLessonLocked(true);
		persistent.setFieldLocked("title", true);
		persistent.setFieldLocked("keywords", true);
		persistent.setSummary("The team verified the setup before starting integration.");
		persistent.setRootCause("The failure occurred because the initial environment configuration was incomplete.");
		check(db.createLessonLearned(persistent).isEmpty() && persistent.getLessonID() > 0,
				"Creates a lesson with whole-lesson and field lock metadata");
		ExperienceRecord renamedExperience = experience(persistent.getLessonID(), "yogi");
		check(db.createExperienceRecord(renamedExperience, "yogi").isEmpty(),
				"Creates an experience for the username-rename cascade check");
		db.closeConnection();
		Database reopened = new Database(URL);
		reopened.connectToDatabase();
		LessonLearned persistedLocks = reopened.readLessonLearned(persistent.getLessonID());
		check(persistedLocks != null, "Persists after reconnecting");
		check(persistedLocks != null && persistedLocks.isLessonLocked()
				&& persistedLocks.isFieldLocked("title") && persistedLocks.isFieldLocked("category"),
				"Persists the whole-lesson lock across reconnecting");
		check(persistedLocks != null && persistedLocks.getSummary().equals(persistent.getSummary())
				&& persistedLocks.getRootCause().equals(persistent.getRootCause()),
				"Persists summary and root-cause attributes after reconnecting");
		check(persistedLocks != null && persistedLocks.getLockedFields().contains("title")
				&& persistedLocks.getLockedFields().contains("keywords")
				&& !persistedLocks.getLockedFields().contains("category"),
				"Persists individual field locks separately from the whole-lesson lock");
		if (persistedLocks != null) persistedLocks.setLessonLocked(false);
		check(persistedLocks != null && persistedLocks.isFieldLocked("title")
				&& persistedLocks.isFieldLocked("keywords") && !persistedLocks.isFieldLocked("category"),
				"Restores effective field locks independently after reading from the database");
		ExperienceRecord persistedExperience = reopened.readExperienceRecord(bobAccountExperience.getExperienceID());
		check(persistedExperience != null
				&& persistedExperience.getEffortHoursByMember().equals(bobAccountExperience.getEffortHoursByMember()),
				"Persists the experience record and team-member effort data after reconnecting");
		LessonLearned curatorEditedAfterReconnect = reopened.readLessonLearned(duplicate.getLessonID());
		check(curatorEditedAfterReconnect != null && curatorEditedAfterReconnect.isCuratorEdited()
				&& curatorEditedAfterReconnect.getEditedBy().equals("curator_test"),
				"Persists curator-edited marker and editor identity after reconnecting");
		reopened.updateUsername("yogi", "yogi_renamed");
		check(reopened.readLessonsLearnedForContributor("yogi_renamed").size() == 502,
				"Migrates lesson ownership when the contributor renames their account");
		ExperienceRecord experienceAfterRename = reopened.readExperienceRecord(renamedExperience.getExperienceID());
		check(experienceAfterRename != null
				&& experienceAfterRename.getContributorUserName().equals("yogi_renamed"),
				"Migrates experience ownership when the Contributor renames their account");
		check(reopened.deleteLessonLearned(persistent.getLessonID(), "yogi_renamed").toLowerCase().contains("locked"),
				"Rejects deleting a renamed contributor's whole-lesson-locked record");
		check(reopened.readLessonLearned(persistent.getLessonID()) != null,
				"Retains a persisted locked lesson after a rejected delete");
		check(reopened.deleteUser("bob"), "Deletes a user account");
		check(reopened.readExperienceRecord(bobAccountExperience.getExperienceID()) == null,
				"Deletes a Contributor's experience records when their account is deleted");
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

	private static ExperienceRecord experience(int lessonID, String contributorUserName) {
		return new ExperienceRecord(lessonID, contributorUserName,
				"The team completed an integration attempt and recorded the result.",
				"The team followed the setup steps and recorded the actions taken.", 1.5,
				Map.of("Alice", 1.0, "Bob", 0.5));
	}

	private static void deleteTestDatabase() {
		String home = System.getProperty("user.home");
		new File(home, "HW2TestDatabase.mv.db").delete();
		new File(home, "HW2TestDatabase.trace.db").delete();
	}

	/** Creates the pre-lock schema so connectToDatabase must run the lock-column migration. */
	private static void createLegacyLessonTable() throws Exception {
		Class.forName("org.h2.Driver");
		String table = "CREATE TABLE lessonsLearnedDB ("
				+ "lessonID INT AUTO_INCREMENT PRIMARY KEY, "
				+ "contributorUserName VARCHAR(255) NOT NULL, "
				+ "title VARCHAR(80) NOT NULL, "
				+ "category VARCHAR(40) NOT NULL, "
				+ "situation VARCHAR(2000) NOT NULL, "
				+ "lessonText VARCHAR(2000) NOT NULL, "
				+ "recommendation VARCHAR(1000), "
				+ "keywords VARCHAR(200), "
				+ "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
				+ "updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
		try (Connection connection = DriverManager.getConnection(URL, "sa", "");
				Statement statement = connection.createStatement()) {
			statement.execute(table);
		}
	}

	private static void check(boolean condition, String description) {
		testCaseNumber++;
		System.out.println("Test case: " + testCaseNumber + " - " + description);
		if (condition) { passed++; System.out.println("Success: " + description); }
		else { failed++; System.out.println("Failure: " + description); }
	}
}
