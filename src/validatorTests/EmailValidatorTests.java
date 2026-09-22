package validatorTests;

import validators.EmailAddressRecognizer;

/*******
 * <p> Title: EmailValidatorTests Class. </p>
 * 
 * <p> Description: A semi-automated test of the EmailAddressRecognizer.  It uses the same approach
 * as the PasswordEvaluationTestingAutomation class provided with the course: each test case passes
 * one input string to EmailAddressRecognizer.checkEmailAddress() along with whether that input is
 * expected to be accepted.  The result of every case is printed to the console as a Success or a
 * Failure, followed by the number of tests that passed and failed.  A tester runs main() and
 * checks that no test has failed.</p>
 * 
 * <p> Rules tested: the address must not be empty and must be no more than 255 characters; only
 * the allowed characters may be used; there must be exactly one "@"; the local part and each
 * domain label must start and end with a letter or digit, with no empty labels; and a hyphen may
 * appear inside a domain label.</p>
 */
public class EmailValidatorTests {
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
		System.out.println("______________________________________\n\nEmail Test Automation");

		// Empty input is rejected
		performTestCase(1, "", false);
		// More than 255 characters is rejected (length check)
		performTestCase(2, "a".repeat(251) + "@gmail.com", false);
		// An invalid character ("#") is rejected
		performTestCase(3, "user#name@gmail.com", false);
		// Two "@" characters are rejected
		performTestCase(4, "user@@gmail.com", false);
		// A missing "@" is rejected
		performTestCase(5, "usergmail.com", false);
		// A hyphen is not allowed in the local part
		performTestCase(6, "us-er@gmail.com", false);
		// An empty domain label ("..") is rejected
		performTestCase(7, "user@gmail..com", false);
		// A domain label may not end with a hyphen
		performTestCase(8, "user@gmail-.com", false);
		// The local part may not start with a period
		performTestCase(9, ".user@gmail.com", false);
		// The domain may not start with a period
		performTestCase(10, "user@.gmail.com", false);
		// A hyphen inside a domain label is accepted
		performTestCase(11, "user@gm-ail.com", true);
		// Periods in the local part and a subdomain are accepted
		performTestCase(12, "first.last@sub.gmail.com", true);

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
		System.out.println("Input: \"" + inputText + "\"");

		String resultText = EmailAddressRecognizer.checkEmailAddress(inputText);

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