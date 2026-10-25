package validatorTests;

import java.util.Map;

import entityClasses.LessonLearned;
import validators.LessonLearnedValidator;

/*******
 * <p> Title: LessonLearnedValidatorTests Class. </p>
 *
 * <p> Description: Semi-automated tests for every lesson and experience validation rule.  The
 * tests follow the TP1 validator pattern: each case supplies an input, states whether that input
 * should be accepted, prints the message returned by the validator, and updates pass and fail
 * counts.  The negative cases also require the message to name the field or rule so the
 * Contributor can fix the problem.</p>
 *
 * <p> Requirement coverage: R1, R3, R7, R8, R10, and R14.  Each rule has at least one positive
 * case that must be accepted and one negative case that must be rejected with a helpful
 * message.</p>
 *
 * <p> Copyright: Yug Thapar, Ishaan Arekar, Avishek Lodh, Shreetej Hadge © 2026 </p>
 *
 * @author Yug Thapar, Ishaan Arekar, Avishek Lodh, Shreetej Hadge
 *
 * @version 1.00		2026-10-24 Initial version for TP2
 * @version 1.01		2026-10-25 Foundations-style header
 *
 */
public class LessonLearnedValidatorTests {
	/** Creates the test runner. */
	public LessonLearnedValidatorTests() { }

	/** Number of cases that passed. */
	private static int numPassed = 0;
	/** Number of cases that failed. */
	private static int numFailed = 0;
	/** Number printed with each case. */
	private static int testCaseNumber = 0;

	/** A valid What Was Done description for experience cases. */
	private static final String VALID_WHAT_WAS_DONE =
			"The team completed an integration attempt and recorded the result.";
	/** A valid How description for experience cases. */
	private static final String VALID_HOW =
			"The team followed the setup steps and recorded the actions taken.";
	/** A valid effort entry for experience cases. */
	private static final Map<String, Double> VALID_EFFORT = Map.of("Yogi", 1.5);

	/**
	 * <p>Runs every validation group and prints the final totals.</p>
	 *
	 * <p>Output: each case prints its input, expected outcome, and the actual validator result. A
	 * case marked "***Success***" behaved exactly as its group Javadoc describes; "***Failure***"
	 * means the validator accepted invalid input, rejected valid input, or returned a message that
	 * did not identify the broken rule. The final failure total must be zero, otherwise the process
	 * throws an AssertionError so an automated runner also detects the failure.</p>
	 *
	 * @param args not used
	 */
	public static void main(String[] args) {
		System.out.println("______________________________________\n\nLesson Learned Validator Test Automation");

		testTitleRules();
		testCategoryRules();
		testRequiredDetailRules();
		testOptionalFieldRules();
		testWholeLessonValidation();
		testExperienceDescriptionRules();
		testExperienceDurationRules();
		testExperienceEffortRules();
		testExperienceAcceptance();
		testSummaryRules();
		testRootCauseRules();

		System.out.println("______________________________________\n");
		System.out.println("Number of tests passed: " + numPassed);
		System.out.println("Number of tests failed: " + numFailed);
		if (numFailed > 0) throw new AssertionError("Lesson Learned validator tests failed: " + numFailed);
	}

	/**
	 * <p>Requirement tested: R1 and R14. A title is required, must be 5 to 80 characters, must be a
	 * single line, and must contain at least one letter.</p>
	 *
	 * <p>How the code satisfies it: {@code LessonLearnedValidator.checkTitle} rejects blank input,
	 * trims the value, rejects line breaks, checks the length boundaries, and finally scans for a
	 * letter. Each failure returns a message naming "Title".</p>
	 *
	 * <p>Reading the output: cases 1, 4, 5, and 6 must report "Correctly rejected" with the title
	 * rule in the message; cases 2 and 3 must report "Correctly accepted". Any other result is a
	 * failure line.</p>
	 */
	private static void testTitleRules() {
		performTestCase("R14", "empty title", LessonLearnedValidator.checkTitle(""), false, "Title");
		performTestCase("R14", "five-character title", LessonLearnedValidator.checkTitle("abcde"), true, "");
		performTestCase("R14", "80-character title", LessonLearnedValidator.checkTitle("a".repeat(80)), true, "");
		performTestCase("R14", "81-character title", LessonLearnedValidator.checkTitle("a".repeat(81)), false, "80");
		performTestCase("R14", "title containing a line break",
				LessonLearnedValidator.checkTitle("valid\ntitle"), false, "line");
		performTestCase("R14", "title containing no letters",
				LessonLearnedValidator.checkTitle("12345"), false, "letter");
	}

	/**
	 * <p>Requirement tested: R1 and R14. A lesson category must be one of the values offered by the
	 * GUI.</p>
	 *
	 * <p>How the code satisfies it: {@code LessonLearnedValidator.checkCategory} compares the input
	 * with the {@code CATEGORIES} array and lists the allowed values in the error message.</p>
	 *
	 * <p>Reading the output: "Technical" must be accepted and "Unknown" must be rejected with a
	 * message containing "category".</p>
	 */
	private static void testCategoryRules() {
		performTestCase("R14", "valid category", LessonLearnedValidator.checkCategory("Technical"), true, "");
		performTestCase("R14", "invalid category", LessonLearnedValidator.checkCategory("Unknown"), false, "category");
	}

	/**
	 * <p>Requirement tested: R1 and R14. The "What Happened" and "What I Learned" fields are
	 * required and must contain 20 to 2000 characters.</p>
	 *
	 * <p>How the code satisfies it: {@code checkSituation} and {@code checkLessonText} delegate to
	 * {@code checkRequiredDetail}, which rejects blanks, values shorter than 20, and values longer
	 * than 2000 with a message naming the affected field.</p>
	 *
	 * <p>Reading the output: the 20- and 2000-character cases must be accepted; the short and empty
	 * cases must be rejected with "What Happened" or "What I Learned" in the message.</p>
	 */
	private static void testRequiredDetailRules() {
		performTestCase("R14", "20-character situation",
				LessonLearnedValidator.checkSituation("a".repeat(20)), true, "");
		performTestCase("R14", "2000-character situation",
				LessonLearnedValidator.checkSituation("a".repeat(2000)), true, "");
		performTestCase("R14", "short situation",
				LessonLearnedValidator.checkSituation("Too short"), false, "What Happened");
		performTestCase("R14", "empty lesson text",
				LessonLearnedValidator.checkLessonText(""), false, "What I Learned");
	}

	/**
	 * <p>Requirement tested: R1 and R14. Optional fields still have format rules: the
	 * recommendation is capped at 1000 characters, keywords must be non-empty, limited to 10, and
	 * use only the documented characters.</p>
	 *
	 * <p>How the code satisfies it: {@code checkRecommendation} checks the length after allowing an
	 * empty value; {@code checkKeywords} splits on commas, rejects empty parts, enforces the count
	 * and length limits, and validates each character.</p>
	 *
	 * <p>Reading the output: each of these four cases must be rejected, and the message must mention
	 * "Next Time", "Keyword", "10", or "Keywords" respectively so the Contributor knows what to
	 * correct.</p>
	 */
	private static void testOptionalFieldRules() {
		performTestCase("R14", "1001-character recommendation",
				LessonLearnedValidator.checkRecommendation("a".repeat(1001)), false, "Next Time");
		performTestCase("R14", "empty keyword entry", LessonLearnedValidator.checkKeywords("java,,h2"), false, "Keyword");
		performTestCase("R14", "11 keywords",
				LessonLearnedValidator.checkKeywords("a1,b2,c3,d4,e5,f6,g7,h8,i9,j0,k1"), false, "10");
		performTestCase("R14", "invalid keyword character", LessonLearnedValidator.checkKeywords("c$harp"), false, "Keywords");
	}

	/**
	 * <p>Requirement tested: R1 and R14. Whole-object validation must reject a missing lesson and
	 * accept a complete, realistic lesson.</p>
	 *
	 * <p>How the code satisfies it: {@code checkLessonLearned} returns a message for null and
	 * otherwise concatenates the contributor, title, category, detail, recommendation, keyword,
	 * summary, and root-cause checks.</p>
	 *
	 * <p>Reading the output: null must be rejected with "lesson"; the complete lesson must be
	 * accepted with no message.</p>
	 */
	private static void testWholeLessonValidation() {
		performTestCase("R1", "null lesson", LessonLearnedValidator.checkLessonLearned(null), false, "lesson");
		LessonLearned completeLesson = new LessonLearned("yogi", "H2 database", "Technical",
				"The team opened two copies of the application at the same time.",
				"Only one process should use the H2 database file at a time.",
				"Stop the first copy before starting another.", "h2, database");
		performTestCase("R1", "complete realistic CSE 360 lesson",
				LessonLearnedValidator.checkLessonLearned(completeLesson), true, "");
	}

	/**
	 * <p>Requirement tested: R8. An experience record must describe what was done and how it was
	 * done, using the same 20 to 2000 character detail rule as a lesson.</p>
	 *
	 * <p>How the code satisfies it: {@code checkExperienceRecord} calls
	 * {@code checkRequiredDetail} for "What Was Done" and "How", which supplies field-specific
	 * messages.</p>
	 *
	 * <p>Reading the output: the two missing-field cases and the short/long cases must be rejected;
	 * the message must identify "What Was Done" or "How" so the Contributor can fix the right
	 * field.</p>
	 */
	private static void testExperienceDescriptionRules() {
		performTestCase("R8", "missing what was done",
				LessonLearnedValidator.checkExperienceRecord("", VALID_HOW, 1.0, VALID_EFFORT),
				false, "What Was Done");
		performTestCase("R8", "missing how it was done",
				LessonLearnedValidator.checkExperienceRecord(VALID_WHAT_WAS_DONE, "", 1.0, VALID_EFFORT),
				false, "How");
		performTestCase("R8", "19-character what-was-done description",
				LessonLearnedValidator.checkExperienceRecord("a".repeat(19), VALID_HOW, 1.0, VALID_EFFORT),
				false, "What Was Done");
		performTestCase("R8", "2001-character how description",
				LessonLearnedValidator.checkExperienceRecord(VALID_WHAT_WAS_DONE, "a".repeat(2001), 1.0, VALID_EFFORT),
				false, "How");
	}

	/**
	 * <p>Requirement tested: R8. Experience duration is measured in hours and must be greater than
	 * zero and no more than 168.</p>
	 *
	 * <p>How the code satisfies it: {@code checkExperienceRecord} rejects non-finite, zero,
	 * negative, and over-168 values with a "Duration" message.</p>
	 *
	 * <p>Reading the output: zero, negative, and 168.1 must be rejected with "Duration"; a failure
	 * means an impossible duration could be stored.</p>
	 */
	private static void testExperienceDurationRules() {
		performTestCase("R8", "zero-hour duration",
				LessonLearnedValidator.checkExperienceRecord(VALID_WHAT_WAS_DONE, VALID_HOW, 0.0, VALID_EFFORT),
				false, "Duration");
		performTestCase("R8", "negative duration",
				LessonLearnedValidator.checkExperienceRecord(VALID_WHAT_WAS_DONE, VALID_HOW, -1.0, VALID_EFFORT),
				false, "Duration");
		performTestCase("R8", "duration above 168 hours",
				LessonLearnedValidator.checkExperienceRecord(VALID_WHAT_WAS_DONE, VALID_HOW, 168.1, VALID_EFFORT),
				false, "168");
	}

	/**
	 * <p>Requirement tested: R8. At least one team-member effort entry is required, each entry must
	 * identify a member, and each member's hours must be greater than zero.</p>
	 *
	 * <p>How the code satisfies it: {@code checkExperienceRecord} rejects a null or empty effort
	 * map and then checks every key and value in the map.</p>
	 *
	 * <p>Reading the output: the three cases must be rejected with messages mentioning "effort",
	 * "member", and "Effort" respectively.</p>
	 */
	private static void testExperienceEffortRules() {
		performTestCase("R8", "empty effort list",
				LessonLearnedValidator.checkExperienceRecord(VALID_WHAT_WAS_DONE, VALID_HOW, 1.0, Map.of()),
				false, "effort");
		performTestCase("R8", "blank effort member",
				LessonLearnedValidator.checkExperienceRecord(VALID_WHAT_WAS_DONE, VALID_HOW, 1.0, Map.of(" ", 1.0)),
				false, "member");
		performTestCase("R8", "zero effort for a member",
				LessonLearnedValidator.checkExperienceRecord(VALID_WHAT_WAS_DONE, VALID_HOW, 1.0, Map.of("Yogi", 0.0)),
				false, "Effort");
	}

	/**
	 * <p>Requirement tested: R8, positive boundary cases. Fractional hours and the maximum duration
	 * of 168 hours must be accepted when all other fields are valid.</p>
	 *
	 * <p>How the code satisfies it: the duration check uses {@code durationHours > 0} and
	 * {@code durationHours <= MAX_EXPERIENCE_DURATION_HOURS}, so 0.5 and 168.0 pass.</p>
	 *
	 * <p>Reading the output: both cases must report "Correctly accepted"; a failure means a
	 * legitimate attempt could not be recorded.</p>
	 */
	private static void testExperienceAcceptance() {
		performTestCase("R8", "valid fractional duration and team effort",
				LessonLearnedValidator.checkExperienceRecord(VALID_WHAT_WAS_DONE, VALID_HOW, 0.5, VALID_EFFORT),
				true, "");
		performTestCase("R8", "maximum duration of 168 hours",
				LessonLearnedValidator.checkExperienceRecord(VALID_WHAT_WAS_DONE, VALID_HOW, 168.0, VALID_EFFORT),
				true, "");
	}

	/**
	 * <p>Requirement tested: R10. The coaching summary is optional but must not exceed 200
	 * characters.</p>
	 *
	 * <p>How the code satisfies it: {@code checkSummary} returns an empty message for blank input,
	 * accepts exactly 200 characters, and rejects 201 or more with "Summary".</p>
	 *
	 * <p>Reading the output: missing and 200-character values must be accepted; 201 characters must
	 * be rejected with "Summary".</p>
	 */
	private static void testSummaryRules() {
		performTestCase("R10", "missing optional summary", LessonLearnedValidator.checkSummary(null), true, "");
		performTestCase("R10", "200-character summary", LessonLearnedValidator.checkSummary("a".repeat(200)), true, "");
		performTestCase("R10", "201-character summary", LessonLearnedValidator.checkSummary("a".repeat(201)), false, "Summary");
	}

	/**
	 * <p>Requirement tested: R10. The coaching root cause is optional but must not exceed 1000
	 * characters.</p>
	 *
	 * <p>How the code satisfies it: {@code checkRootCause} treats blank input as valid, accepts
	 * exactly 1000 characters, and rejects 1001 or more with "Root Cause".</p>
	 *
	 * <p>Reading the output: the empty and 1000-character values must be accepted; 1001 characters
	 * must be rejected with "Root Cause".</p>
	 */
	private static void testRootCauseRules() {
		performTestCase("R10", "missing optional root cause", LessonLearnedValidator.checkRootCause(""), true, "");
		performTestCase("R10", "1000-character root cause", LessonLearnedValidator.checkRootCause("a".repeat(1000)), true, "");
		performTestCase("R10", "1001-character root cause", LessonLearnedValidator.checkRootCause("a".repeat(1001)), false, "Root Cause");
	}

	/**
	 * <p>Checks one validator result against the expected outcome and the required error text.</p>
	 *
	 * <p>Reading the output: "***Success***" appears when the accepted/rejected outcome matches the
	 * expectation and, for a negative case, the message contains the expected field or rule text.
	 * "***Failure***" prints the actual result so the faulty rule can be identified.</p>
	 *
	 * @param requirement the requirement identifier exercised by this case
	 * @param inputDescription a short description of the supplied input
	 * @param resultText the message returned by the validator
	 * @param expectedPass whether the input should be accepted
	 * @param expectedMessage text that must appear in a negative error message
	 */
	private static void performTestCase(String requirement, String inputDescription, String resultText,
			boolean expectedPass, String expectedMessage) {
		testCaseNumber++;
		boolean accepted = resultText.isEmpty();
		boolean messageIsHelpful = expectedPass || resultText.contains(expectedMessage);
		boolean testPassed = accepted == expectedPass && messageIsHelpful;

		System.out.println("Test case " + testCaseNumber + " [" + requirement + "]: " + inputDescription);
		System.out.println("  Expected: " + (expectedPass ? "accepted" : "rejected with \"" + expectedMessage + "\""));
		System.out.println("  Actual:   " + (accepted ? "accepted" : "rejected with \"" + resultText + "\""));
		if (testPassed) {
			numPassed++;
			System.out.println("***Success***");
		} else {
			numFailed++;
			System.out.println("***Failure***");
		}
	}
}
