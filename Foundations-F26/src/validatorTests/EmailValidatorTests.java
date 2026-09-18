package validatorTests;

import validators.EmailAddressRecognizer;

public class EmailValidatorTests {
	static int numPassed = 0;
	static int numFailed = 0;

	public static void main(String[] args) {
		System.out.println("______________________________________\n\nEmail Test Automation");

		performTestCase(1, "", false);
		performTestCase(2, "a".repeat(251) + "@gmail.com", false);
		performTestCase(3, "user#name@gmail.com", false);
		performTestCase(4, "user@@gmail.com", false);
		performTestCase(5, "usergmail.com", false);
		performTestCase(6, "us-er@gmail.com", false);
		performTestCase(7, "user@gmail..com", false);
		performTestCase(8, "user@gmail-.com", false);
		performTestCase(9, ".user@gmail.com", false);
		performTestCase(10, "user@.gmail.com", false);
		performTestCase(11, "user@gm-ail.com", true);
		performTestCase(12, "first.last@sub.gmail.com", true);

		System.out.println("______________________________________\n");
		System.out.println("Number of tests passed: " + numPassed);
		System.out.println("Number of tests failed: " + numFailed);
	}

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