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
 * <p>Description: Semi-automated tests for lesson persistence, CRUD, ownership, locking, search
 * subsets, experience records, reconnection, and account lifecycle. A separate H2 file keeps these
 * destructive operations away from the application's real data.</p>
 *
 * <p>Requirement catalog used by this suite: R1 create a lesson, R2 read lessons, R3 update a
 * lesson, R4 lock state, R5 lock enforcement, R6 delete a lesson, R7 keyword subsets, R8 experience
 * records, R9 curator provenance, R10 future coaching attributes, R11 persistence, R12 Contributor
 * isolation, R13 list CRUD, and R14 input validation. Every group below names the requirements it
 * exercises and contains positive cases that must succeed plus negative cases that must be
 * rejected.</p>
 *
 * <p>Each test group documents how the code satisfies its requirements and how to read the output.
 * Every assertion prints its requirement tag and description, then "***Success***" or
 * "***Failure***". The run ends with pass and fail totals, and any failure throws an AssertionError
 * so an automated runner also detects it.</p>
 */
public class LessonsLearnedCRUDTests {
	private static int passed;
	private static int failed;
	private static int testCaseNumber;
	private static final String URL = "jdbc:h2:~/HW2TestDatabase";

	private static Database db;
	private static Database reopened;
	private static LessonLearned first;
	private static LessonLearned second;
	private static LessonLearned duplicate;
	private static LessonLearned persistent;
	private static ExperienceRecord recordedExperience;
	private static ExperienceRecord bobAccountExperience;
	private static ExperienceRecord renamedExperience;

	/**
	 * <p>Creates the test database and users, runs every requirement group in order, and prints the
	 * final totals.</p>
	 *
	 * <p>Output: each group prints a banner followed by numbered assertions tagged with the
	 * requirement they exercise. "***Success***" means the observed database result matched the
	 * expectation described in that group's Javadoc; "***Failure***" means the database accepted
	 * invalid data, rejected valid data, leaked another Contributor's data, or lost data that should
	 * have persisted. The final failure total must be zero, otherwise the process throws an
	 * AssertionError.</p>
	 *
	 * <p>Run with the VM argument {@code -Duser.home=/tmp/cse360-test-home} so the test database
	 * never replaces the real {@code ~/HW2TestDatabase} file.</p>
	 *
	 * @param args not used
	 * @throws Exception if the H2 driver or schema setup fails
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("______________________________________\n\nLessons Learned CRUD Test Automation");
		deleteTestDatabase();
		createLegacyLessonTable();
		db = new Database(URL);
		db.connectToDatabase();
		db.register(new User("yogi", "password", "Yogi", "", "Tester", "Yogi", "yogi@example.com", false, true, false));
		db.register(new User("bob", "password", "Bob", "", "Tester", "Bob", "bob@example.com", false, true, false));

		testEmptyAndCreate();
		testReadAndDefaults();
		testLockedUpdate();
		testLockedDelete();
		testExperienceValidationAndCreation();
		testExperienceReadAndListing();
		testExperienceUpdateAndDelete();
		testExperienceCascades();
		testSubsetsAndLargeList();
		testLessonUpdateAndOwnership();
		testPersistenceAcrossReconnect();
		testUsernameRenameCascade();
		testAccountDeletionCascade();

		System.out.println("______________________________________\n");
		System.out.println("Number of tests passed: " + passed);
		System.out.println("Number of tests failed: " + failed);
		if (failed > 0) throw new AssertionError("Lesson CRUD tests failed: " + failed);
	}

	/**
	 * <p>Requirements tested: R1, R2, R11, and R14. An empty database reads as an empty list, invalid
	 * lessons are rejected before storage, a valid lesson receives an ID, and titles are unique
	 * regardless of capitalization.</p>
	 *
	 * <p>How the code satisfies it: {@code readAllLessonsLearned} returns an empty
	 * {@link LessonLearnedList}; {@code createLessonLearned} runs {@code checkLessonLearned},
	 * inserts only valid rows, assigns the generated key to the lesson, and compares titles
	 * case-insensitively before inserting.</p>
	 *
	 * <p>Reading the output: the empty-read and valid-create successes show R1/R2 work; the two
	 * rejection successes show R14 and the duplicate-title rule work. A failure on a create means the
	 * ID was not assigned or an invalid lesson reached the table.</p>
	 */
	private static void testEmptyAndCreate() {
		check("R2", "Reads an empty lesson database", db.readAllLessonsLearned().isEmpty());
		check("R14", "Rejects invalid creation with a title error",
				db.createLessonLearned(new LessonLearned("yogi", "", "Technical",
						"A valid situation with enough detail.",
						"A valid lesson with enough detail.", "A valid recommendation.", "test"))
						.contains("Title"));

		first = lesson("yogi", "H2 database");
		check("R1", "Creates a lesson",
				db.createLessonLearned(first).isEmpty() && first.getLessonID() > 0);
		check("R1", "Rejects duplicate title",
				!db.createLessonLearned(lesson("yogi", "h2 DATABASE")).isEmpty());

		second = lesson("bob", "Git workflow");
		second.setFieldLocked("title", true);
		db.createLessonLearned(second);
		duplicate = lesson("yogi", "Duplicate target");
		duplicate.setKeywords("duplicate");
		duplicate.setLessonLocked(true);
		duplicate.setCuratorEdited(true);
		duplicate.setEditedBy("curator_test");
		db.createLessonLearned(duplicate);
	}

	/**
	 * <p>Requirements tested: R2, R4, R9, and R12. A created lesson reads back with default lock and
	 * curator metadata; a curator-edited flag and editor name persist; and another Contributor
	 * cannot see the curator-edited lesson.</p>
	 *
	 * <p>How the code satisfies it: {@code readLessonLearned} reconstructs every column, including
	 * the lock columns and {@code curatorEdited}/{@code editedBy}; {@code
	 * readLessonsLearnedForContributor} filters by {@code contributorUserName}, so Bob's list never
	 * contains Yogi's lesson.</p>
	 *
	 * <p>Reading the output: successes on the default and curator checks mean the metadata survived
	 * a write-read cycle; the isolation success means a different username received an empty result
	 * for that ID. A failure would indicate leaked or lost metadata.</p>
	 */
	private static void testReadAndDefaults() {
		LessonLearned firstRead = db.readLessonLearned(first.getLessonID());
		check("R2", "Reads a lesson by ID", firstRead != null);
		check("R2", "Returns null for a missing lesson ID", db.readLessonLearned(999999) == null);
		check("R4/R9", "Defaults lock and curator-edit metadata for a new lesson",
				firstRead != null && !firstRead.isLessonLocked() && firstRead.getLockedFields().isEmpty()
						&& !firstRead.isCuratorEdited() && firstRead.getEditedBy() == null
						&& firstRead.getSummary().isEmpty() && firstRead.getRootCause().isEmpty());
		LessonLearned curatorEditedLesson = db.readLessonLearned(duplicate.getLessonID());
		check("R9", "Reads curator-edited marker and editor identity after saving a lesson",
				curatorEditedLesson != null && curatorEditedLesson.isCuratorEdited()
						&& curatorEditedLesson.getEditedBy().equals("curator_test"));
		check("R12", "Keeps curator-edited lesson invisible to another Contributor",
				db.readLessonsLearnedForContributor("bob").findByID(duplicate.getLessonID()) == null);
	}

	/**
	 * <p>Requirements tested: R4 and R5, negative cases. Updating a locked field is rejected with an
	 * error naming that field; updating an unlocked field is allowed; and a whole-lesson lock
	 * rejects every change.</p>
	 *
	 * <p>How the code satisfies it: {@code updateLessonLearned} calls
	 * {@code checkLessonLockAllowsUpdate}, which compares the stored row with the submitted row only
	 * for fields that changed and returns a field-specific message for any locked change; the
	 * whole-lesson flag short-circuits every comparison.</p>
	 *
	 * <p>Reading the output: the first success means the title-change error mentions "title"; the
	 * next two successes mean an unlocked situation update saved while the locked title stayed
	 * unchanged; the last two means the whole-locked lesson was rejected and its stored situation
	 * did not change.</p>
	 */
	private static void testLockedUpdate() {
		LessonLearned lockedTitleEdit = lesson("bob", "Changed Git workflow");
		lockedTitleEdit.setLessonID(second.getLessonID());
		String titleLockError = db.updateLessonLearned(lockedTitleEdit, "bob");
		check("R5", "Rejects editing a locked field with an error naming that field",
				titleLockError.toLowerCase().contains("title"));

		LessonLearned unlockedFieldEdit = lesson("bob", "Git workflow");
		unlockedFieldEdit.setLessonID(second.getLessonID());
		unlockedFieldEdit.setSituation("The team recorded a new situation while preserving the locked title.");
		check("R4", "Allows updating an unlocked field when another field is locked",
				db.updateLessonLearned(unlockedFieldEdit, "bob").isEmpty());
		LessonLearned partiallyUpdated = db.readLessonLearned(second.getLessonID());
		check("R4", "Preserves the locked title while saving an unlocked field change",
				partiallyUpdated != null && partiallyUpdated.getTitle().equals("Git workflow")
						&& partiallyUpdated.getSituation().equals(unlockedFieldEdit.getSituation()));

		LessonLearned wholeLessonEdit = lesson("yogi", "Duplicate target");
		wholeLessonEdit.setLessonID(duplicate.getLessonID());
		wholeLessonEdit.setKeywords(duplicate.getKeywords());
		wholeLessonEdit.setSituation("The contributor attempts to change a whole-lesson locked record.");
		String wholeLessonLockError = db.updateLessonLearned(wholeLessonEdit, "yogi");
		check("R5", "Rejects an edit to any field when the whole lesson is locked",
				wholeLessonLockError.toLowerCase().contains("locked"));
		LessonLearned stillLocked = db.readLessonLearned(duplicate.getLessonID());
		check("R5", "Leaves whole-lesson-locked content unchanged after a rejected update",
				stillLocked != null && !stillLocked.getSituation().equals(wholeLessonEdit.getSituation()));
	}

	/**
	 * <p>Requirements tested: R4, R5, and R6, negative cases. A lesson with any locked field and a
	 * whole-lesson-locked lesson must both refuse deletion.</p>
	 *
	 * <p>How the code satisfies it: {@code deleteLessonLearned} calls
	 * {@code checkLessonLockAllowsDelete}, which returns an error when the whole-lesson lock is set
	 * or the individual lock set is non-empty, before any DELETE statement is prepared.</p>
	 *
	 * <p>Reading the output: both rejection successes mean delete was blocked with a message
	 * containing "locked"; the two retention successes mean the records still read back. A failure
	 * would mean a locked lesson was destroyed.</p>
	 */
	private static void testLockedDelete() {
		String fieldLockedDeleteError = db.deleteLessonLearned(second.getLessonID(), "bob");
		check("R5", "Rejects deleting a lesson with a locked field",
				fieldLockedDeleteError.toLowerCase().contains("locked"));
		if (fieldLockedDeleteError.isEmpty()) {
			LessonLearned restoredSecond = lesson("bob", "Git workflow");
			restoredSecond.setFieldLocked("title", true);
			db.createLessonLearned(restoredSecond);
			second = restoredSecond;
		}
		check("R6", "Retains a lesson after deletion is blocked by a field lock",
				db.readLessonLearned(second.getLessonID()) != null);
		String wholeLockedDeleteError = db.deleteLessonLearned(duplicate.getLessonID(), "yogi");
		check("R5", "Rejects deleting a whole-lesson-locked record",
				wholeLockedDeleteError.toLowerCase().contains("locked"));
		if (wholeLockedDeleteError.isEmpty()) {
			LessonLearned restoredDuplicate = lesson("yogi", "Duplicate target");
			restoredDuplicate.setKeywords("duplicate");
			restoredDuplicate.setLessonLocked(true);
			db.createLessonLearned(restoredDuplicate);
			duplicate = restoredDuplicate;
		}
		check("R6", "Retains a lesson after deletion is blocked by a whole-lesson lock",
				db.readLessonLearned(duplicate.getLessonID()) != null);
	}

	/**
	 * <p>Requirements tested: R8 and R12. Experience creation must reject a missing parent lesson
	 * and a forged owner, then store a valid record with an assigned ID for its real owner.</p>
	 *
	 * <p>How the code satisfies it: {@code createExperienceRecord(experience,
	 * requestingUserName)} verifies that the lesson exists and that the requester owns it, ignores
	 * the owner field supplied by the caller, writes the requester as owner, and assigns the
	 * generated key.</p>
	 *
	 * <p>Reading the output: the missing-lesson error must mention "lesson"; the forged-owner error
	 * must mention "owner"; the create success means the ID is positive and the authenticated owner
	 * was recorded.</p>
	 */
	private static void testExperienceValidationAndCreation() {
		ExperienceRecord missingParent = experience(999999, "yogi");
		check("R8", "Rejects creating experience for a missing lesson",
				db.createExperienceRecord(missingParent, "yogi").toLowerCase().contains("lesson"));
		ExperienceRecord forgedOwner = experience(second.getLessonID(), "bob");
		check("R12", "Rejects adding experience to another Contributor's lesson",
				db.createExperienceRecord(forgedOwner, "yogi").toLowerCase().contains("owner"));

		recordedExperience = experience(second.getLessonID(), "bob");
		check("R8", "Creates an experience record with a database ID",
				db.createExperienceRecord(recordedExperience, "bob").isEmpty()
						&& recordedExperience.getExperienceID() > 0);
	}

	/**
	 * <p>Requirements tested: R8 and R12. A stored experience re-reads with every field and member
	 * effort; a missing ID returns null; and listing is restricted to the owning Contributor.</p>
	 *
	 * <p>How the code satisfies it: {@code readExperienceRecord} rebuilds the effort map from the
	 * effort table; {@code readExperienceRecordsForLesson} filters by both lesson ID and contributor
	 * username, so another Contributor receives an empty list.</p>
	 *
	 * <p>Reading the output: the field-equality success proves the descriptions, duration, and
	 * effort map survived; the null success proves a missing ID is handled; the one-item and empty
	 * list successes prove ownership filtering.</p>
	 */
	private static void testExperienceReadAndListing() {
		ExperienceRecord readExperience = db.readExperienceRecord(recordedExperience.getExperienceID());
		check("R8", "Reads the experience fields and member efforts that were stored",
				readExperience != null && readExperience.getLessonID() == second.getLessonID()
						&& readExperience.getContributorUserName().equals("bob")
						&& readExperience.getWhatWasDone().equals(recordedExperience.getWhatWasDone())
						&& readExperience.getHow().equals(recordedExperience.getHow())
						&& readExperience.getDurationHours() == recordedExperience.getDurationHours()
						&& readExperience.getEffortHoursByMember().equals(recordedExperience.getEffortHoursByMember()));
		check("R8", "Returns null when an experience ID is missing",
				db.readExperienceRecord(999999) == null);
		List<ExperienceRecord> bobExperiences = db.readExperienceRecordsForLesson(second.getLessonID(), "bob");
		check("R8", "Lists experiences for the owning Contributor's lesson",
				bobExperiences.size() == 1
						&& bobExperiences.get(0).getExperienceID() == recordedExperience.getExperienceID());
		check("R12", "Does not list another Contributor's experience records",
				db.readExperienceRecordsForLesson(second.getLessonID(), "yogi").isEmpty());
	}

	/**
	 * <p>Requirements tested: R8 and R12. The owner can update and delete an experience; another
	 * Contributor can do neither; rejected actions leave the record intact.</p>
	 *
	 * <p>How the code satisfies it: {@code updateExperienceRecord} and
	 * {@code deleteExperienceRecord} verify the stored contributor against the requester before
	 * writing, and the update replaces both the record columns and its effort rows.</p>
	 *
	 * <p>Reading the output: the update success plus the re-read equality proves changes persisted;
	 * the two "owner" error successes plus the retention success prove isolation; the final delete
	 * and null-read successes prove the owner removed it.</p>
	 */
	private static void testExperienceUpdateAndDelete() {
		ExperienceRecord updatedExperience = experience(second.getLessonID(), "bob");
		updatedExperience.setExperienceID(recordedExperience.getExperienceID());
		updatedExperience.setWhatWasDone("The team completed a second integration attempt and recorded the outcome.");
		updatedExperience.setEffortHoursByMember(Map.of("Alice", 2.0));
		check("R8", "Allows the experience owner to update the record",
				db.updateExperienceRecord(updatedExperience, "bob").isEmpty());
		ExperienceRecord afterUpdate = db.readExperienceRecord(recordedExperience.getExperienceID());
		check("R8", "Persists updated experience details and effort entries",
				afterUpdate != null && afterUpdate.getWhatWasDone().equals(updatedExperience.getWhatWasDone())
						&& afterUpdate.getEffortHoursByMember().equals(updatedExperience.getEffortHoursByMember()));
		ExperienceRecord unauthorizedUpdate = experience(second.getLessonID(), "bob");
		unauthorizedUpdate.setExperienceID(recordedExperience.getExperienceID());
		unauthorizedUpdate.setWhatWasDone("Another Contributor attempts to update this experience record.");
		check("R12", "Rejects another Contributor's experience update",
				db.updateExperienceRecord(unauthorizedUpdate, "yogi").toLowerCase().contains("owner"));
		check("R12", "Rejects another Contributor's experience deletion",
				db.deleteExperienceRecord(recordedExperience.getExperienceID(), "yogi")
						.toLowerCase().contains("owner"));
		check("R12", "Retains an experience after another Contributor's rejected delete",
				db.readExperienceRecord(recordedExperience.getExperienceID()) != null);
		check("R8", "Allows the experience owner to delete the record",
				db.deleteExperienceRecord(recordedExperience.getExperienceID(), "bob").isEmpty());
		check("R8", "Removes an experience after its owner deletes it",
				db.readExperienceRecord(recordedExperience.getExperienceID()) == null);
	}

	/**
	 * <p>Requirements tested: R8 and R11. Deleting a lesson must delete its experience records, and
	 * deleting an account must delete that Contributor's experience records.</p>
	 *
	 * <p>How the code satisfies it: the experience table declares a foreign key to the lesson with
	 * {@code ON DELETE CASCADE}, and {@code deleteUser} removes the Contributor's experience rows
	 * before the account row.</p>
	 *
	 * <p>Reading the output: the create successes establish the fixtures; the lesson-delete success
	 * followed by a null experience read proves the cascade. Account-deletion cascade is checked in
	 * {@code testAccountDeletionCascade}.</p>
	 */
	private static void testExperienceCascades() {
		bobAccountExperience = experience(second.getLessonID(), "bob");
		check("R8", "Creates an experience for the account-deletion cascade check",
				db.createExperienceRecord(bobAccountExperience, "bob").isEmpty());
		LessonLearned cascadeParent = lesson("bob", "Experience cascade parent");
		check("R1", "Creates a lesson for the experience cascade check",
				db.createLessonLearned(cascadeParent).isEmpty());
		ExperienceRecord cascadeExperience = experience(cascadeParent.getLessonID(), "bob");
		check("R8", "Creates an experience for the lesson-deletion cascade check",
				db.createExperienceRecord(cascadeExperience, "bob").isEmpty());
		check("R6", "Deletes the parent lesson for the experience cascade check",
				db.deleteLessonLearned(cascadeParent.getLessonID(), "bob").isEmpty());
		check("R11", "Deletes experience records when their lesson is deleted",
				db.readExperienceRecord(cascadeExperience.getExperienceID()) == null);
	}

	/**
	 * <p>Requirements tested: R2, R7, and R11. Contributor reads are scoped, the full list is
	 * available, keyword search returns matching and empty subsets, and the list has no fixed size
	 * limit.</p>
	 *
	 * <p>How the code satisfies it: {@code readLessonsLearnedForContributor} filters by owner;
	 * {@code readAllLessonsLearned} returns every row; {@code getSubsetByKeyword} calls
	 * {@code LessonLearned.matchesKeyword}; the backing collection is an unbounded
	 * {@code ArrayList}.</p>
	 *
	 * <p>Reading the output: the size-two and size-three successes show correct scoping and full
	 * reads; the matching and empty subset successes show R7; the 502 success after inserting 500
	 * rows proves there is no application limit.</p>
	 */
	private static void testSubsetsAndLargeList() {
		check("R2", "Reads only the contributor's lessons",
				db.readLessonsLearnedForContributor("yogi").size() == 2);
		LessonLearnedList all = db.readAllLessonsLearned();
		check("R2", "Reads all lessons", all.size() == 3);
		check("R7", "Creates a matching subset", all.getSubsetByKeyword("database").size() == 1);
		check("R7", "Supports an empty subset", all.getSubsetByKeyword("missing term").isEmpty());
		for (int i = 1; i <= 500; i++) db.createLessonLearned(lesson("yogi", "Practice lesson " + i));
		check("R7", "Supports a large contributor list without a fixed limit",
				db.readLessonsLearnedForContributor("yogi").size() == 502);
	}

	/**
	 * <p>Requirements tested: R2, R3, R6, R12, and R14. Update rejects a missing ID and a duplicate
	 * title, accepts an owned change, and rejects another Contributor; delete accepts the owner and
	 * rejects a missing ID.</p>
	 *
	 * <p>How the code satisfies it: {@code updateLessonLearned} checks existence, ownership, and
	 * duplicate titles before writing; {@code deleteLessonLearned} checks existence, ownership, and
	 * lock state.</p>
	 *
	 * <p>Reading the output: the "not found", "another lesson", and "belongs" successes prove each
	 * rejection path; the update and delete successes mean the owner's change and removal were
	 * accepted; the final "not found" success proves deleting twice is rejected.</p>
	 */
	private static void testLessonUpdateAndOwnership() {
		LessonLearned missing = lesson("yogi", "Missing update");
		missing.setLessonID(999999);
		check("R3", "Rejects updating a missing lesson with a not-found error",
				db.updateLessonLearned(missing, "yogi").contains("not found"));
		first.setTitle("Duplicate target");
		check("R14", "Rejects an update to a duplicate title",
				db.updateLessonLearned(first, "yogi").contains("another lesson"));
		first.setTitle("Updated H2 database");
		check("R3", "Updates an owned lesson", db.updateLessonLearned(first, "yogi").isEmpty());
		check("R12", "Rejects an unauthorized update with an ownership error",
				db.updateLessonLearned(first, "bob").contains("belongs"));
		check("R6", "Deletes an owned lesson",
				db.deleteLessonLearned(first.getLessonID(), "yogi").isEmpty());
		check("R6", "Rejects deleting a missing lesson with a not-found error",
				db.deleteLessonLearned(first.getLessonID(), "yogi").contains("not found"));
	}

	/**
	 * <p>Requirements tested: R4, R5, R9, R10, and R11. Lesson locks, individual field locks,
	 * summary/root-cause coaching attributes, curator markers, and experience effort must all
	 * survive closing and reopening the database.</p>
	 *
	 * <p>How the code satisfies it: the schema stores one column per lock plus the summary,
	 * root-cause, curator, and effort attributes; {@code connectToDatabase} reads them back, and
	 * {@code isFieldLocked} recomputes effective locks from the stored columns.</p>
	 *
	 * <p>Reading the output: the create successes establish the fixtures; after reconnect, the
	 * lock, summary, individual-lock, effective-lock, effort, and curator successes all mean the
	 * values survived. A failure identifies a column that was not persisted or not reconstructed.</p>
	 */
	private static void testPersistenceAcrossReconnect() throws Exception {
		persistent = lesson("yogi", "Persistence test");
		persistent.setLessonLocked(true);
		persistent.setFieldLocked("title", true);
		persistent.setFieldLocked("keywords", true);
		persistent.setSummary("The team verified the setup before starting integration.");
		persistent.setRootCause("The failure occurred because the initial environment configuration was incomplete.");
		check("R4", "Creates a lesson with whole-lesson and field lock metadata",
				db.createLessonLearned(persistent).isEmpty() && persistent.getLessonID() > 0);
		renamedExperience = experience(persistent.getLessonID(), "yogi");
		check("R8", "Creates an experience for the username-rename cascade check",
				db.createExperienceRecord(renamedExperience, "yogi").isEmpty());

		db.closeConnection();
		reopened = new Database(URL);
		reopened.connectToDatabase();
		LessonLearned persistedLocks = reopened.readLessonLearned(persistent.getLessonID());
		check("R11", "Persists after reconnecting", persistedLocks != null);
		check("R11", "Returns null for a missing lesson after reconnecting",
				reopened.readLessonLearned(999999) == null);
		check("R4", "Persists the whole-lesson lock across reconnecting",
				persistedLocks != null && persistedLocks.isLessonLocked()
						&& persistedLocks.isFieldLocked("title") && persistedLocks.isFieldLocked("category"));
		check("R10", "Persists summary and root-cause attributes after reconnecting",
				persistedLocks != null && persistedLocks.getSummary().equals(persistent.getSummary())
						&& persistedLocks.getRootCause().equals(persistent.getRootCause()));
		check("R4", "Persists individual field locks separately from the whole-lesson lock",
				persistedLocks != null && persistedLocks.getLockedFields().contains("title")
						&& persistedLocks.getLockedFields().contains("keywords")
						&& !persistedLocks.getLockedFields().contains("category"));
		if (persistedLocks != null) persistedLocks.setLessonLocked(false);
		check("R4", "Restores effective field locks independently after reading from the database",
				persistedLocks != null && persistedLocks.isFieldLocked("title")
						&& persistedLocks.isFieldLocked("keywords") && !persistedLocks.isFieldLocked("category"));
		ExperienceRecord persistedExperience = reopened.readExperienceRecord(bobAccountExperience.getExperienceID());
		check("R8", "Persists the experience record and team-member effort data after reconnecting",
				persistedExperience != null
						&& persistedExperience.getEffortHoursByMember().equals(bobAccountExperience.getEffortHoursByMember()));
		LessonLearned curatorEditedAfterReconnect = reopened.readLessonLearned(duplicate.getLessonID());
		check("R9", "Persists curator-edited marker and editor identity after reconnecting",
				curatorEditedAfterReconnect != null && curatorEditedAfterReconnect.isCuratorEdited()
						&& curatorEditedAfterReconnect.getEditedBy().equals("curator_test"));
	}

	/**
	 * <p>Requirements tested: R6, R11, and R12. Renaming an account must migrate lesson and
	 * experience ownership, the renamed owner must still be blocked by a whole-lesson lock, and the
	 * locked lesson must remain after the rejected delete.</p>
	 *
	 * <p>How the code satisfies it: {@code updateUsername} updates the username column in the user,
	 * lesson, and experience tables in one transaction, so the foreign ownership text follows the
	 * account; the lock check reads the unchanged lock columns.</p>
	 *
	 * <p>Reading the output: the size-502 success means every lesson followed the rename; the
	 * experience success means its owner text followed too; the "locked" and retention successes
	 * mean the renamed owner still cannot delete the locked lesson.</p>
	 */
	private static void testUsernameRenameCascade() {
		reopened.updateUsername("yogi", "yogi_renamed");
		check("R12", "Migrates lesson ownership when the contributor renames their account",
				reopened.readLessonsLearnedForContributor("yogi_renamed").size() == 502);
		ExperienceRecord experienceAfterRename = reopened.readExperienceRecord(renamedExperience.getExperienceID());
		check("R12", "Migrates experience ownership when the Contributor renames their account",
				experienceAfterRename != null
						&& experienceAfterRename.getContributorUserName().equals("yogi_renamed"));
		check("R5", "Rejects deleting a renamed contributor's whole-lesson-locked record",
				reopened.deleteLessonLearned(persistent.getLessonID(), "yogi_renamed").toLowerCase().contains("locked"));
		check("R6", "Retains a persisted locked lesson after a rejected delete",
				reopened.readLessonLearned(persistent.getLessonID()) != null);
	}

	/**
	 * <p>Requirements tested: R2, R8, R11, and R12. Deleting an account removes its experience
	 * records, and a recreated username must not inherit the deleted user's lessons.</p>
	 *
	 * <p>How the code satisfies it: {@code deleteUser} deletes the Contributor's experience and
	 * lesson rows before the account row; registration starts a new account with no lesson rows
	 * because ownership is stored per row, not by username alone.</p>
	 *
	 * <p>Reading the output: the account-delete and null-experience successes prove the cascade; the
	 * final empty-list success proves a recreated "bob" inherits nothing. A failure would mean
	 * deleted data remained reachable.</p>
	 */
	private static void testAccountDeletionCascade() throws Exception {
		check("R6", "Deletes a user account", reopened.deleteUser("bob"));
		check("R11", "Deletes a Contributor's experience records when their account is deleted",
				reopened.readExperienceRecord(bobAccountExperience.getExperienceID()) == null);
		reopened.register(new User("bob", "password", "Bob", "", "Recreated", "Bob", "bob2@example.com", false, true, false));
		check("R12", "Prevents a recreated username from inheriting deleted user's lessons",
				reopened.readLessonsLearnedForContributor("bob").isEmpty());
		reopened.closeConnection();
	}

	/**
	 * <p>Builds a valid lesson for database tests.</p>
	 *
	 * <p>Reading the output: this helper returns data only; failures appear when the calling test
	 * asserts on the database result.</p>
	 *
	 * @param user the Contributor username
	 * @param title the unique title used for the test
	 * @return a populated lesson that passes validation
	 */
	private static LessonLearned lesson(String user, String title) {
		String keywords = title.equals("Git workflow") ? "git, workflow" : "database, testing";
		return new LessonLearned(user, title, "Technical",
				"The team encountered a specific problem while using the application.",
				"The team learned to verify the setup before starting work.",
				"Check the setup before repeating the work.", keywords);
	}

	/**
	 * <p>Builds a valid experience record for database tests.</p>
	 *
	 * <p>Reading the output: this helper returns data only; failures appear when the calling test
	 * asserts on the database result.</p>
	 *
	 * @param lessonID the parent lesson
	 * @param contributorUserName the claimed owner; creation uses the authenticated requester instead
	 * @return a populated experience record
	 */
	private static ExperienceRecord experience(int lessonID, String contributorUserName) {
		return new ExperienceRecord(lessonID, contributorUserName,
				"The team completed an integration attempt and recorded the result.",
				"The team followed the setup steps and recorded the actions taken.", 1.5,
				Map.of("Alice", 1.0, "Bob", 0.5));
	}

	/**
	 * <p>Deletes the test database files so each run starts from a known empty state.</p>
	 *
	 * <p>Reading the output: this method prints nothing; a failure to delete simply means the
	 * subsequent empty-database assertion reports the problem.</p>
	 */
	private static void deleteTestDatabase() {
		String home = System.getProperty("user.home");
		new File(home, "HW2TestDatabase.mv.db").delete();
		new File(home, "HW2TestDatabase.trace.db").delete();
	}

	/**
	 * <p>Creates the pre-lock schema so {@code connectToDatabase} must run the lock-column
	 * migration.</p>
	 *
	 * <p>Reading the output: a thrown SQLException identifies a broken migration fixture rather than
	 * a product failure.</p>
	 *
	 * @throws Exception if the H2 driver or table creation fails
	 */
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

	/**
	 * <p>Records one numbered result and prints the requirement tag and description.</p>
	 *
	 * <p>Reading the output: "***Success***" means the assertion matched the expectation described
	 * in the calling group's Javadoc; "***Failure***" means it did not and the final failure total
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
