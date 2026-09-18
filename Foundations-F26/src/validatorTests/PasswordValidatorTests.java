package validatorTests;

import validators.PasswordValidator;

public class PasswordValidatorTests {
	static int numPassed = 0;
	static int numFailed = 0;

	public static void main(String[] args) {
		System.out.println("______________________________________\n\nPassword Test Automation");

		performTestCase(1, "", false);
		performTestCase(2, "Aa1!567", false);
		performTestCase(3, "Aa1!5678", true);
		performTestCase(4, "Aa!1111111111111111111111111111111111111111111111111111111111111", true);
		performTestCase(5, "Aa!11111111111111111111111111111111111111111111111111111111111111", false);
		performTestCase(6, "Aa1!567\u20AC", false);
		performTestCase(7, "aa1!5678", false);
		performTestCase(8, "AA1!5678", false);
		performTestCase(9, "Aa1234567", false);
		performTestCase(10, "Aaaaaaa!", false);
		performTestCase(11, "Aa1!5678", true);

		System.out.println("______________________________________\n");
		System.out.println("Number of tests passed: " + numPassed);
		System.out.println("Number of tests failed: " + numFailed);
	}

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