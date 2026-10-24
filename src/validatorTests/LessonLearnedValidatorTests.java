package validatorTests;

import entityClasses.LessonLearned;
import validators.LessonLearnedValidator;

/*******
 * <p> Title: LessonLearnedValidatorTests Class. </p>
 *
 * <p> Description: A semi-automated test of LessonLearnedValidator.  It follows the same pattern
 * as the TP1 validator tests: each numbered case supplies an input, states whether the input is
 * expected to pass, prints the returned error message, and updates passed and failed counts. </p>
 *
 * <p> The negative cases also check that the returned message identifies the relevant field or
 * validation rule.  This matters because the message is what the Contributor sees in the GUI. </p>
 */
public class LessonLearnedValidatorTests {
	static int numPassed = 0;
	static int numFailed = 0;

	/**********
	 * <p> Method: main(String[] args) </p>
	 *
	 * <p> Description: Runs all lesson learned validation cases and prints the final totals. </p>
	 *
	 * @param args The command line arguments are not used
	 */
	public static void main(String[] args) {
		System.out.println("______________________________________\n\nLesson Learned Validator Test Automation");

		// Title: required, 5 to 80 characters, one line, and at least one letter
		performTestCase(1, "empty title", LessonLearnedValidator.checkTitle(""), false, "Title");
		performTestCase(2, "five-character title", LessonLearnedValidator.checkTitle("abcde"), true, "");
		performTestCase(3, "80-character title", LessonLearnedValidator.checkTitle("a".repeat(80)), true, "");
		performTestCase(4, "81-character title", LessonLearnedValidator.checkTitle("a".repeat(81)), false, "80");
		performTestCase(5, "title containing a line break", LessonLearnedValidator.checkTitle("valid\ntitle"), false, "line");
		performTestCase(6, "title containing no letters", LessonLearnedValidator.checkTitle("12345"), false, "letter");

		// Category: must be one of the values supplied to the GUI
		performTestCase(7, "valid category", LessonLearnedValidator.checkCategory("Technical"), true, "");
		performTestCase(8, "invalid category", LessonLearnedValidator.checkCategory("Unknown"), false, "category");

		// Detail fields: 20 to 2000 characters
		performTestCase(9, "20-character situation", LessonLearnedValidator.checkSituation("a".repeat(20)), true, "");
		performTestCase(10, "2000-character situation", LessonLearnedValidator.checkSituation("a".repeat(2000)), true, "");
		performTestCase(11, "short situation", LessonLearnedValidator.checkSituation("Too short"), false, "What Happened");
		performTestCase(12, "empty lesson text", LessonLearnedValidator.checkLessonText(""), false, "What I Learned");

		// Optional fields still have format and length rules
		performTestCase(13, "1001-character recommendation",
				LessonLearnedValidator.checkRecommendation("a".repeat(1001)), false, "Next Time");
		performTestCase(14, "empty keyword entry", LessonLearnedValidator.checkKeywords("java,,h2"), false, "Keyword");
		performTestCase(15, "11 keywords",
				LessonLearnedValidator.checkKeywords("a1,b2,c3,d4,e5,f6,g7,h8,i9,j0,k1"), false, "10");
		performTestCase(16, "invalid keyword character", LessonLearnedValidator.checkKeywords("c$harp"), false, "Keywords");

		// Whole-object validation
		performTestCase(17, "null lesson", LessonLearnedValidator.checkLessonLearned(null), false, "lesson");
		LessonLearned completeLesson = new LessonLearned("yogi", "H2 database", "Technical",
				"The team opened two copies of the application at the same time.",
				"Only one process should use the H2 database file at a time.",
				"Stop the first copy before starting another.", "h2, database");
		performTestCase(18, "complete realistic CSE 360 lesson",
				LessonLearnedValidator.checkLessonLearned(completeLesson), true, "");

		System.out.println("______________________________________\n");
		System.out.println("Number of tests passed: " + numPassed);
		System.out.println("Number of tests failed: " + numFailed);
		// F0: Make a failed case visible to automated runners as well as to a person reading output.
		if (numFailed > 0) throw new AssertionError("Lesson Learned validator tests failed: " + numFailed);
	}

	/**********
	 * <p> Method: performTestCase(int testCase, String inputDescription, String resultText,
	 * boolean expectedPass, String expectedMessage) </p>
	 *
	 * <p> Description: Checks whether the validator result agrees with the expected pass/fail value
	 * and, for a negative case, whether the error contains the expected helpful text. </p>
	 *
	 * @param testCase The number of the test case
	 * @param inputDescription A short description of the supplied input
	 * @param resultText The message returned by the validator
	 * @param expectedPass Whether the input is expected to be accepted
	 * @param expectedMessage Text that must appear in a negative error message
	 */
	private static void performTestCase(int testCase, String inputDescription, String resultText,
			boolean expectedPass, String expectedMessage) {
		boolean accepted = resultText.isEmpty();
		boolean messageIsHelpful = expectedPass || resultText.contains(expectedMessage);
		boolean testPassed = accepted == expectedPass && messageIsHelpful;

		System.out.println("______________________________________\n\nTest case: " + testCase);
		System.out.println("Input: " + inputDescription);
		System.out.println("Expected: " + (expectedPass ? "Pass" : "Fail"));
		if (testPassed) {
			System.out.println("***Success*** " + (expectedPass ? "Correctly accepted." :
					"Correctly rejected: " + resultText));
			numPassed++;
		} else {
			System.out.println("***Failure*** Returned: " + (accepted ? "accepted" : resultText));
			numFailed++;
		}
	}
}
