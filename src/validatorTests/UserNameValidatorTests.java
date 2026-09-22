package validatorTests;

import validators.UserNameRecognizer;

/*******
 * <p> Title: UserNameValidatorTests Class. </p>
 * 
 * <p> Description: A semi-automated test of the UserNameRecognizer.  It uses the same approach as
 * the PasswordEvaluationTestingAutomation class provided with the course: each test case passes
 * one input string to UserNameRecognizer.checkForValidUserName() along with whether that input is
 * expected to be accepted.  The result of every case is printed to the console as a Success or a
 * Failure, followed by the number of tests that passed and failed.  A tester runs main() and
 * checks that no test has failed.</p>
 * 
 * <p> Rules tested: a username must be 4 to 16 characters long, must start with a letter, may
 * contain only letters, digits, ".", "-", and "_", and each ".", "-", or "_" must be followed by a
 * letter or digit.</p>
 */
public class UserNameValidatorTests {
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
		System.out.println("______________________________________\n\nUserName Test Automation");

		// Empty input is rejected
		performTestCase(1, "", false);
		// 3 characters is rejected (one under the minimum of 4)
		performTestCase(2, "abc", false);
		// Exactly the minimum of 4 characters is accepted (boundary)
		performTestCase(3, "abcd", true);
		// Exactly the maximum of 16 characters is accepted (boundary)
		performTestCase(4, "abcdefghijklmnop", true);
		// 17 characters is rejected (one over the maximum)
		performTestCase(5, "abcdefghijklmnopq", false);
		// An invalid character ("%") is rejected
		performTestCase(6, "user%name", false);
		// A username may not start with a digit
		performTestCase(7, "1username", false);
		// Two separators in a row ("..") are rejected
		performTestCase(8, "user..name", false);
		// A username may not end with a separator
		performTestCase(9, "user-", false);
		// An underscore followed by a digit is accepted
		performTestCase(10, "user_1", true);
		// Mixed case with a period and a digit is accepted
		performTestCase(11, "Valid.User1", true);

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
	public static void performTestCase(int testCase, String inputText, boolean expectedPass) {
		System.out.println("______________________________________\n\nTest case: " + testCase);
		System.out.println("Input: \"" + inputText + "\"");

		String resultText = UserNameRecognizer.checkForValidUserName(inputText);

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