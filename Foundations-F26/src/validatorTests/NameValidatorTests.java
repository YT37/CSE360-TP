package validatorTests;

import validators.NameValidator;

/*******
 * <p> Title: NameValidatorTests Class. </p>
 * 
 * <p> Description: A semi-automated test of the NameValidator.  It uses the same approach as the
 * PasswordEvaluationTestingAutomation class provided with the course: each test case passes one
 * input string to NameValidator.checkName() along with whether that input is expected to be
 * accepted.  The result of every case is printed to the console as a Success or a Failure,
 * followed by the number of tests that passed and failed.  A tester runs main() and checks that no
 * test has failed.</p>
 * 
 * <p> Rules tested: a name may be no more than 32 characters long.</p>
 */
public class NameValidatorTests {
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
		System.out.println("______________________________________\n\nName Test Automation");

		// Exactly the maximum of 32 characters is accepted (boundary)
		performTestCase(1, "a".repeat(32), true);
		// 33 characters is rejected (one over the maximum)
		performTestCase(2, "a".repeat(33), false);
		// A normal short name is accepted
		performTestCase(3, "yogi", true);

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
		System.out.println("Input length: " + inputText.length());

		String resultText = NameValidator.checkName(inputText);

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