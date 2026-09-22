package validatorTests;

import validators.PasswordValidator;

/*******
 * <p> Title: PasswordValidatorTests Class. </p>
 * 
 * <p> Description: A semi-automated test of the PasswordValidator.  It uses the same approach as
 * the PasswordEvaluationTestingAutomation class provided with the course: each test case passes
 * one input string to PasswordValidator.evaluatePassword() along with whether that input is
 * expected to be accepted.  The result of every case is printed to the console as a Success or a
 * Failure, followed by the number of tests that passed and failed.  A tester runs main() and
 * checks that no test has failed.</p>
 * 
 * <p> Rules tested: the password must not be empty, must be 8 to 64 characters long, may contain
 * only letters, digits, and the listed special characters, and must include an uppercase letter, a
 * lowercase letter, a digit, and a special character.</p>
 */
public class PasswordValidatorTests {
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
		System.out.println("______________________________________\n\nPassword Test Automation");

		// Empty input is rejected
		performTestCase(1, "", false);
		// 7 characters is rejected (one under the minimum of 8)
		performTestCase(2, "Aa1!567", false);
		// Exactly the minimum of 8 characters with every character type is accepted (boundary)
		performTestCase(3, "Aa1!5678", true);
		// Exactly the maximum of 64 characters is accepted (boundary)
		performTestCase(4, "Aa!1111111111111111111111111111111111111111111111111111111111111", true);
		// 65 characters is rejected (one over the maximum)
		performTestCase(5, "Aa!11111111111111111111111111111111111111111111111111111111111111", false);
		// An unsupported character (the euro sign) is rejected
		performTestCase(6, "Aa1!567\u20AC", false);
		// A missing uppercase letter is rejected
		performTestCase(7, "aa1!5678", false);
		// A missing lowercase letter is rejected
		performTestCase(8, "AA1!5678", false);
		// A missing special character is rejected
		performTestCase(9, "Aa1234567", false);
		// A missing digit is rejected
		performTestCase(10, "Aaaaaaa!", false);
		// A typical valid password is accepted
		performTestCase(11, "Aa1!5678", true);

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

		String resultText = PasswordValidator.evaluatePassword(inputText);

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