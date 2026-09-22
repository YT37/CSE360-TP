package validatorTests;

import validators.InvitationCodeValidator;

/*******
 * <p> Title: InvitationCodeValidatorTests Class. </p>
 *
 * <p> Description: A semi-automated test of the InvitationCodeValidator.  It uses the same approach
 * as the PasswordEvaluationTestingAutomation class provided with the course: each test case passes
 * one input string to InvitationCodeValidator.checkInvitationCode() along with whether that input
 * is expected to be accepted.  The result of every case is printed to the console as a Success or
 * a Failure, followed by the number of tests that passed and failed.  A tester runs main() and
 * checks that no test has failed.</p>
 *
 * <p> Rules tested: an invitation code must not be empty, must be exactly 6 characters long, and
 * may contain only the digits 0-9 and the lowercase letters a-f.  The length is checked before the
 * characters.</p>
 */
public class InvitationCodeValidatorTests {
	static int numPassed = 0;
	static int numFailed = 0;

	/**********
	 * <p> Method: main(String[] args) </p>
	 *
	 * <p> Description: This method runs every test case and then prints the number of tests that
	 * passed and failed. </p>
	 *
	 * @param args	The command line arguments are not used
	 */
	public static void main(String[] args) {
		System.out.println("______________________________________\n\nInvitation Code Test Automation");

		// Empty input is rejected
		performTestCase(1, "", false);
		// 5 characters is rejected (one under the required length of 6)
		performTestCase(2, "5ed00", false);
		// 7 characters is rejected (one over the maximum of 6)
		performTestCase(3, "5ed0045", false);
		// A very long input is rejected by the length check before its characters are examined
		performTestCase(4, "z".repeat(1000), false);
		// Exactly 6 hexadecimal characters is accepted (boundary)
		performTestCase(5, "5ed004", true);
		// All digits is accepted
		performTestCase(6, "123456", true);
		// All of the allowed letters a-f is accepted
		performTestCase(7, "abcdef", true);
		// A letter after f is rejected
		performTestCase(8, "5ed00g", false);
		// Uppercase hexadecimal letters are rejected (generated codes are lowercase)
		performTestCase(9, "5ED004", false);
		// A space is rejected
		performTestCase(10, "5ed 04", false);
		// A hyphen from the rest of the UUID is rejected
		performTestCase(11, "5ed00-", false);

		System.out.println("______________________________________\n");
		System.out.println("Number of tests passed: " + numPassed);
		System.out.println("Number of tests failed: " + numFailed);
	}

	/**********
	 * <p> Method: performTestCase(int testCase, String inputText, boolean expectedPass) </p>
	 *
	 * <p> Description: This method runs one test case, prints the input and whether the result
	 * matched what was expected, and updates the passed and failed counts. </p>
	 *
	 * @param testCase		The number of this test case
	 * @param inputText		The input to be validated
	 * @param expectedPass	Whether the input is expected to be accepted
	 */
	private static void performTestCase(int testCase, String inputText, boolean expectedPass) {
		System.out.println("______________________________________\n\nTest case: " + testCase);
		System.out.println("Input: \"" + (inputText.length() > 20 ?
				inputText.substring(0, 20) + "...\" (length " + inputText.length() + ")" :
				inputText + "\""));

		String resultText = InvitationCodeValidator.checkInvitationCode(inputText);

		if (resultText != "") {
			if (expectedPass) {
				System.out.println("***Failure*** Expected valid, got: " + resultText);
				numFailed++;
			} else {
				System.out.println("***Success*** Correctly rejected: " + resultText);
				numPassed++;
			}
		} else {
			if (expectedPass) {
				System.out.println("***Success*** Correctly accepted.");
				numPassed++;
			} else {
				System.out.println("***Failure*** Expected invalid, but was accepted.");
				numFailed++;
			}
		}
	}
}
