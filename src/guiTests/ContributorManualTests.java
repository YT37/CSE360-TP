package guiTests;

/*******
 * <p> Title: ContributorManualTests Class. </p>
 *
 * <p> Description: Manual tests for the Contributor pages.  The semi-automated testbeds check the
 * entity, validator, and database code, but they cannot click a button, read a confirmation
 * dialog, or see that a field is shown as locked.  These tests cover that part by hand.  Running
 * this class prints each test as a numbered checklist that a tester follows in the running
 * application and marks Pass or Fail.</p>
 *
 * <p> How to run: run this class as a Java Application in Eclipse (it needs no JavaFX or H2) and
 * keep its console output open.  Then start the application with FoundationsMain and follow each
 * test in order.  The tests use two Contributor accounts, called Contributor A and Contributor B
 * below.  If they do not exist yet, sign in as an Admin, send two invitations with the
 * Contributor role, and use Set Up Account on the login page to create both accounts.</p>
 *
 * <table class="striped">
 * <caption>Manual tests</caption>
 * <tr><th>ID</th><th>Requirement</th><th>Pass criteria</th></tr>
 * <tr><td>M1</td><td>R1, R14</td><td>A valid lesson is created and appears in the list</td></tr>
 * <tr><td>M2</td><td>R14</td><td>An empty title and a short What Happened are refused, each
 * error names its field, and nothing is saved</td></tr>
 * <tr><td>M3</td><td>R3</td><td>An edited lesson shows the change after it is selected
 * again</td></tr>
 * <tr><td>M4</td><td>R6</td><td>Delete asks for confirmation; Cancel keeps the lesson and OK
 * removes it</td></tr>
 * <tr><td>M5</td><td>R7</td><td>A search shows the matching subset, a search with no matches
 * shows an empty list, and All Lessons shows everything again</td></tr>
 * <tr><td>M6</td><td>R4, R5</td><td>A locked field is marked and read-only, a locked lesson
 * cannot be changed, and Delete is unavailable</td></tr>
 * <tr><td>M7</td><td>R8</td><td>An experience record is added, viewed, and deleted after
 * confirmation; a record with a missing field is refused</td></tr>
 * <tr><td>M8</td><td>R12</td><td>Contributor B does not see Contributor A's lessons</td></tr>
 * </table>
 *
 * <p> Copyright: Yug Thapar, Ishaan Arekar, Avishek Lodh, Shreetej Hadge © 2026 </p>
 *
 * @author Yug Thapar, Ishaan Arekar, Avishek Lodh, Shreetej Hadge
 *
 * @version 1.00		2026-10-25 Initial version for TP2
 *
 */
public class ContributorManualTests {

	/** Creates the test runner. */
	public ContributorManualTests() { }

	/** Number of manual tests printed. */
	private static int testCount = 0;

	/**********
	 * <p> Method: main(String[] args) </p>
	 *
	 * <p> Description: Prints every manual test as a checklist entry: its ID, the requirements it
	 * covers in brackets, the steps, and the expected result.  The tester writes Pass or Fail next
	 * to each one.  The run ends with the number of manual tests printed.</p>
	 *
	 * @param args not used
	 */
	public static void main(String[] args) {
		System.out.println("______________________________________\n\nContributor Manual Test Checklist");
		System.out.println("Start the application with FoundationsMain and sign in as Contributor A.");

		manualTestCreateLesson();
		manualTestInvalidLesson();
		manualTestUpdateLesson();
		manualTestDeleteLesson();
		manualTestKeywordSearch();
		manualTestLockedLesson();
		manualTestExperienceRecords();
		manualTestContributorIsolation();

		System.out.println("______________________________________\n");
		System.out.println("Number of manual tests: " + testCount);
		System.out.println("Every test must be marked Pass.");
	}

	/**********
	 * <p> Method: manualTestCreateLesson() </p>
	 *
	 * <p> Requirements tested: R1 and R14, positive case.</p>
	 *
	 * <p> Steps: on the Contributor Home page press Lessons Learned, press Clear, fill in every
	 * field with valid values, and press Create.</p>
	 *
	 * <p> How the code satisfies it: ControllerLessonsLearned.performCreate builds a LessonLearned
	 * from the form and calls Database.createLessonLearned, which runs
	 * LessonLearnedValidator.checkLessonLearned, inserts the row, and sets the new lesson ID.
	 * refreshList then reads the Contributor's lessons again and selects the new one.</p>
	 *
	 * <p> How to tell it passed: the message says the lesson was created, the lesson is in the
	 * list and selected, and the count under the list went up by one.</p>
	 */
	private static void manualTestCreateLesson() {
		printTest("M1", "R1, R14", "Create a valid lesson",
				new String[] {
					"On the Contributor Home page press Lessons Learned.",
					"Press Clear.",
					"Title: H2 database locked",
					"Category: Tools and Environment",
					"What Happened: Eclipse could not open the database while another copy was running.",
					"What I Learned: Only one copy of the application can use the H2 file at a time.",
					"Next Time: Close javaw.exe in Task Manager before starting again.",
					"Keywords: h2, eclipse",
					"Press Create."
				},
				"The message reads \"Lesson #n was created.\", the lesson is selected in the list, "
				+ "and the count under the list went up by one.");
	}

	/**********
	 * <p> Method: manualTestInvalidLesson() </p>
	 *
	 * <p> Requirements tested: R14, negative case.</p>
	 *
	 * <p> Steps: press Clear, leave the title empty, choose a category, type a What Happened
	 * that is under 20 characters, fill in What I Learned, and press Create.</p>
	 *
	 * <p> How the code satisfies it: Database.createLessonLearned calls
	 * LessonLearnedValidator.checkLessonLearned before any SQL runs.  The validator returns one
	 * message per broken field, joined by line breaks, and performCreate shows the whole text in
	 * the message area and returns without refreshing the list.</p>
	 *
	 * <p> How to tell it passed: the message names Title and What Happened, and the count under
	 * the list did not change.</p>
	 */
	private static void manualTestInvalidLesson() {
		printTest("M2", "R14", "Refuse an empty title and a short situation",
				new String[] {
					"Note the count under the list.",
					"Press Clear.",
					"Leave Title empty.",
					"Category: Testing",
					"What Happened: Too short",
					"What I Learned: Each field is checked before the lesson is saved.",
					"Press Create."
				},
				"The message shows \"Title is required...\" and \"What Happened must be at least "
				+ "20 characters long.\", no new lesson is in the list, and the count is unchanged.");
	}

	/**********
	 * <p> Method: manualTestUpdateLesson() </p>
	 *
	 * <p> Requirements tested: R3, positive case.</p>
	 *
	 * <p> Steps: select the lesson from M1, change Next Time, press Update, select another
	 * lesson or press Clear, then select the M1 lesson again.</p>
	 *
	 * <p> How the code satisfies it: ControllerLessonsLearned.performUpdate copies the selected
	 * lesson's ID into a lesson built from the form and calls Database.updateLessonLearned with
	 * the signed-in username.  The database checks ownership, locks, and the validator before the
	 * UPDATE, then refreshList reads the stored row again.</p>
	 *
	 * <p> How to tell it passed: the message says the lesson was updated and the new Next Time
	 * text is shown after the lesson is selected again.</p>
	 */
	private static void manualTestUpdateLesson() {
		printTest("M3", "R3", "Edit an owned lesson",
				new String[] {
					"Select the lesson \"H2 database locked\".",
					"Change Next Time to: Check Task Manager for javaw.exe first.",
					"Press Update.",
					"Press Clear, then select \"H2 database locked\" again."
				},
				"The message reads \"Lesson was updated.\" and Next Time shows the new text.");
	}

	/**********
	 * <p> Method: manualTestDeleteLesson() </p>
	 *
	 * <p> Requirements tested: R6, positive and negative cases.</p>
	 *
	 * <p> Steps: create a second valid lesson, select it, press Delete and choose Cancel, then
	 * press Delete again and choose OK.</p>
	 *
	 * <p> How the code satisfies it: ControllerLessonsLearned.performDelete shows a confirmation
	 * Alert and returns without calling the database unless OK is chosen.  On OK it calls
	 * Database.deleteLessonLearned, which checks ownership and locks before the DELETE.</p>
	 *
	 * <p> How to tell it passed: after Cancel the lesson is still listed; after OK the message
	 * says the lesson was deleted and it is gone from the list.</p>
	 */
	private static void manualTestDeleteLesson() {
		printTest("M4", "R6", "Delete a lesson after confirmation",
				new String[] {
					"Press Clear and create a lesson titled \"Lesson to delete\" with valid values.",
					"Select \"Lesson to delete\" and press Delete.",
					"In the confirmation dialog press Cancel.",
					"Press Delete again and press OK."
				},
				"The dialog asks \"Delete lesson #n?\". After Cancel the lesson is still listed. "
				+ "After OK the message reads \"Lesson was deleted.\" and the lesson is gone.");
	}

	/**********
	 * <p> Method: manualTestKeywordSearch() </p>
	 *
	 * <p> Requirements tested: R7, positive and negative cases.</p>
	 *
	 * <p> Steps: search for a keyword used by one lesson, then for a word no lesson uses, then
	 * press All Lessons.</p>
	 *
	 * <p> How the code satisfies it: ControllerLessonsLearned.performSearch stores the search in
	 * ModelLessonsLearned.currentSearch, and refreshList calls
	 * LessonLearnedList.getSubsetByKeyword, which keeps each lesson whose
	 * LessonLearned.matchesKeyword is true.  performShowAll clears the search and lists every
	 * lesson again.</p>
	 *
	 * <p> How to tell it passed: the count line shows the subset size, then zero, then the full
	 * total.</p>
	 */
	private static void manualTestKeywordSearch() {
		printTest("M5", "R7", "Search with matches, with no matches, then show all",
				new String[] {
					"Make sure at least two lessons are listed.",
					"Type eclipse in the search box and press Search.",
					"Type zzzz in the search box and press Search.",
					"Press All Lessons."
				},
				"First only lessons containing \"eclipse\" are listed (\"Showing 1 of n lesson(s)\" "
				+ "or similar). Then the list is empty (\"Showing 0 of n lesson(s)\"). After All "
				+ "Lessons every lesson is listed (\"Showing n of n lesson(s)\").");
	}

	/**********
	 * <p> Method: manualTestLockedLesson() </p>
	 *
	 * <p> Requirements tested: R4 and R5, positive and negative cases.</p>
	 *
	 * <p> Setup: TP2 has no Curator page, so locks are set with SQL.  Quit the application (and
	 * end any javaw.exe left running), then open the H2 shell with the H2 jar from the Eclipse H2
	 * library:</p>
	 *
	 * <pre>
	 * java -cp "path\to\h2.jar" org.h2.tools.Shell -url jdbc:h2:~/FoundationDatabase -user sa -password ""
	 * UPDATE lessonsLearnedDB SET titleLocked = TRUE WHERE title = 'H2 database locked';
	 * exit
	 * </pre>
	 *
	 * <p> Steps: start the application, select the lesson, try to edit the title, change Next
	 * Time and press Update, and look at Delete.  For the whole-lesson part, run the same shell
	 * with "SET lessonLocked = TRUE", select the lesson, try to edit any field, and press Update
	 * without changes.  To undo both, set the two columns back to FALSE.</p>
	 *
	 * <p> How the code satisfies it: Database.readLessonsLearnedForContributor rebuilds the lock
	 * columns into the lesson.  ControllerLessonsLearned.performSelect calls
	 * ViewLessonsLearned.applyLockState, which adds "(Locked)" to the label, makes the control
	 * read-only, and disables Delete when any lock is set.  Database.updateLessonLearned calls
	 * checkLessonLockAllowsUpdate and Database.deleteLessonLearned calls
	 * checkLessonLockAllowsDelete, so the database refuses the change even if the GUI is
	 * bypassed.</p>
	 *
	 * <p> How to tell it passed: the title shows "(Locked)" and cannot be typed in, Delete is
	 * greyed out, and the Next Time update succeeds while only the title is locked.  With the
	 * whole lesson locked no field can be edited and Update is refused.</p>
	 */
	private static void manualTestLockedLesson() {
		printTest("M6", "R4, R5", "Locked fields and locked lessons",
				new String[] {
					"Quit the application and end any javaw.exe still running.",
					"Lock the title with the H2 shell (SQL is in this method's Javadoc):",
					"  UPDATE lessonsLearnedDB SET titleLocked = TRUE WHERE title = 'H2 database locked';",
					"Start the application, sign in as Contributor A, open Lessons Learned.",
					"Select \"H2 database locked\" and try to type in Title.",
					"Change Next Time and press Update.",
					"Look at the Delete button.",
					"Quit, then lock the whole lesson:",
					"  UPDATE lessonsLearnedDB SET lessonLocked = TRUE WHERE title = 'H2 database locked';",
					"Start again, select the lesson, try to type in any field, and press Update.",
					"Undo: set titleLocked and lessonLocked back to FALSE."
				},
				"With the title lock: the label reads \"Title (Locked)\", Title cannot be edited, "
				+ "the message reads \"Locked fields are read-only; Delete is disabled.\", Delete is "
				+ "greyed out, and the Next Time update succeeds. With the whole-lesson lock: every "
				+ "field label shows (Locked), no field can be edited, Delete is greyed out, and "
				+ "Update is refused with \"Lesson #n is locked and cannot be updated.\"");
	}

	/**********
	 * <p> Method: manualTestExperienceRecords() </p>
	 *
	 * <p> Requirements tested: R8, positive and negative cases.</p>
	 *
	 * <p> Steps: add a valid record, select it to view the stored fields, then cancel and confirm
	 * deletion; also attempt to save with a missing required field and verify the error is
	 * shown.  (This check was first written in the ViewExperience class comment.)</p>
	 *
	 * <p> How the code satisfies it: ControllerExperience.performAdd checks the form with
	 * LessonLearnedValidator.checkExperienceRecord and then calls
	 * Database.createExperienceRecord, which checks that the signed-in Contributor owns the
	 * lesson.  performSelect shows the stored record read-only.  performDelete shows a
	 * confirmation Alert and only calls Database.deleteExperienceRecord on OK.</p>
	 *
	 * <p> How to tell it passed: the missing-field save shows an error and adds nothing; the valid
	 * record appears and shows its values when selected; Cancel keeps it and OK removes it.</p>
	 */
	private static void manualTestExperienceRecords() {
		printTest("M7", "R8", "Add, view, and delete an experience record",
				new String[] {
					"Select any unlocked lesson and press Experiences.",
					"Leave What Was Done empty, fill in the rest, add one effort, and press Add.",
					"Press New. What Was Done: The team set up H2 again on a clean machine.",
					"How: Closed javaw.exe, deleted the lock file, and restarted Eclipse.",
					"Duration (hours): 1.5",
					"Member name: Yug, Hours: 1, press Add Effort. Press Add.",
					"Press New, then select the new record in Previous Attempts.",
					"Press Delete and choose Cancel; press Delete again and choose OK."
				},
				"The first Add shows \"What Was Done is required...\" and adds nothing. The second "
				+ "Add reads \"Experience #n was added.\" Selecting the record shows the saved values "
				+ "read-only. After Cancel the record is still listed; after OK the message reads "
				+ "\"Experience record was deleted.\" and it is gone.");
	}

	/**********
	 * <p> Method: manualTestContributorIsolation() </p>
	 *
	 * <p> Requirements tested: R12, negative case.</p>
	 *
	 * <p> Steps: log out, sign in as Contributor B, and open Lessons Learned.</p>
	 *
	 * <p> How the code satisfies it: ControllerLessonsLearned.refreshList calls
	 * Database.readLessonsLearnedForContributor with the signed-in username, and that query only
	 * returns rows whose contributorUserName matches.  Update and delete also check the owner in
	 * Database.checkChangeAllowed.</p>
	 *
	 * <p> How to tell it passed: none of Contributor A's lessons, such as "H2 database locked",
	 * are in Contributor B's list, and a search for eclipse finds nothing.</p>
	 */
	private static void manualTestContributorIsolation() {
		printTest("M8", "R12", "A second Contributor cannot see the first one's lessons",
				new String[] {
					"Press Logout.",
					"Sign in as Contributor B and press Lessons Learned.",
					"Type eclipse in the search box and press Search."
				},
				"\"H2 database locked\" and Contributor A's other lessons are not listed, and the "
				+ "search shows \"Showing 0 of n lesson(s)\".");
	}

	/**********
	 * <p> Method: printTest(String id, String requirements, String title, String[] steps,
	 * String expected) </p>
	 *
	 * <p> Description: Prints one checklist entry in the same layout as the other testbeds.</p>
	 *
	 * @param id the manual test ID, such as M1
	 * @param requirements the requirement IDs this test covers
	 * @param title a short name for the test
	 * @param steps the steps to follow, in order
	 * @param expected what the tester should see if the test passes
	 */
	private static void printTest(String id, String requirements, String title, String[] steps,
			String expected) {
		testCount++;
		System.out.println("______________________________________\n");
		System.out.println("Manual test " + id + " [" + requirements + "]: " + title);
		System.out.println("Steps:");
		for (int i = 0; i < steps.length; i++)
			System.out.println("  " + (i + 1) + ". " + steps[i]);
		System.out.println("Expected result: " + expected);
		System.out.println("Result:  ___ Pass   ___ Fail");
	}
}
