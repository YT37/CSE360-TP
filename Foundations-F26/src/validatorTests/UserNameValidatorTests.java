package validatorTests;

import validators.UserNameRecognizer;

public class UserNameValidatorTests {
	static int numPassed = 0;
	static int numFailed = 0;

	public static void main(String[] args) {
		System.out.println("______________________________________\n\nUserName Test Automation");

		performTestCase(1, "", false);
		performTestCase(2, "abc", false);
		performTestCase(3, "abcd", true);
		performTestCase(4, "abcdefghijklmnop", true);
		performTestCase(5, "abcdefghijklmnopq", false);
		performTestCase(6, "user%name", false);
		performTestCase(7, "1username", false);
		performTestCase(8, "user..name", false);
		performTestCase(9, "user-", false);
		performTestCase(10, "user_1", true);
		performTestCase(11, "Valid.User1", true);

		System.out.println("______________________________________\n");
		System.out.println("Number of tests passed: " + numPassed);
		System.out.println("Number of tests failed: " + numFailed);
	}

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