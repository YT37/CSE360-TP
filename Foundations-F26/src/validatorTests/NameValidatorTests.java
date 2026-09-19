package validatorTests;

import validators.NameValidator;

public class NameValidatorTests {
	static int numPassed = 0;
	static int numFailed = 0;

	public static void main(String[] args) {
		System.out.println("______________________________________\n\nName Test Automation");

		performTestCase(1, "a".repeat(32), true);
		performTestCase(2, "a".repeat(33), false);
		performTestCase(3, "yogi", true);

		System.out.println("______________________________________\n");
		System.out.println("Number of tests passed: " + numPassed);
		System.out.println("Number of tests failed: " + numFailed);
	}

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