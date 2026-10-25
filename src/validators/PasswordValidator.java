package validators;

import java.util.List;
import java.util.ArrayList;

/*******
 * <p> Title: PasswordValidator Class. </p>
 * 
 * <p> Description: Evaluates a password against the password rules.  A password must be 8 to
 * MAX_PASSWORD_LENGTH characters long, may contain only letters, digits, and the listed special
 * characters, and must include an uppercase letter, a lowercase letter, a digit, and a special
 * character.  The length is checked before any other processing is performed.  The found...
 * attributes record which requirements the most recently evaluated password met, so a page can
 * show live feedback as the user types. </p>
 * 
 * @version 1.00		2026-09-18 Derived from the password evaluation logic in
 * 							PasswordEvaluationTestbed-F26's Model.java, without its GUI code
 */
public class PasswordValidator {

	/** Error message produced by the most recent evaluation. */
	public static String passwordErrorMessage = "";
	/** Password processed by the most recent evaluation. */
	public static String passwordInput = "";
	/** Index where the most recent error was located. */
	public static int passwordIndexofError = -1;
	/** Maximum accepted password length. */
	public static final int MAX_PASSWORD_LENGTH = 64;
	/** True when the most recent password contained an uppercase letter. */
	public static boolean foundUpperCase = false;
	/** True when the most recent password contained a lowercase letter. */
	public static boolean foundLowerCase = false;
	/** True when the most recent password contained a digit. */
	public static boolean foundNumericDigit = false;
	/** True when the most recent password contained a special character. */
	public static boolean foundSpecialChar = false;
	/** True when the most recent password met the minimum length. */
	public static boolean foundLongEnough = false;
	/** True when the most recent password contained a disallowed character. */
	public static boolean foundInvalidChar = false;

	/** Creates the validator; its API is static. */
	public PasswordValidator() { }
	private static String inputLine = "";
	private static char currentChar;
	private static int currentCharNdx;
	private static boolean running;

	/** Evaluates a password and records each requirement result.
	 *
	 * @param input the password to evaluate
	 * @return an empty string when the password is valid, or an error message
	 */
	public static String evaluatePassword(String input) {
		passwordErrorMessage = "";
		passwordIndexofError = 0;
		inputLine = input;
		currentCharNdx = 0;

		if (input.length() <= 0) {
			return "*** Error *** The password is empty!";
		}

		// Check the size before doing anything else with the input
		if (input.length() > MAX_PASSWORD_LENGTH) {
			passwordIndexofError = MAX_PASSWORD_LENGTH;
			return "*** Error *** The password is too long! Passwords must be no more than " +
					MAX_PASSWORD_LENGTH + " characters.";
		}

		currentChar = input.charAt(0);
		passwordInput = input;
		foundUpperCase = false;
		foundLowerCase = false;
		foundNumericDigit = false;
		foundSpecialChar = false;
		foundLongEnough = false;
		foundInvalidChar = false;
		running = true;

		while (running) {
			if (currentChar >= 'A' && currentChar <= 'Z') {
				foundUpperCase = true;
			} else if (currentChar >= 'a' && currentChar <= 'z') {
				foundLowerCase = true;
			} else if (currentChar >= '0' && currentChar <= '9') {
				foundNumericDigit = true;
			} else if ("~`!@#$%^&*()_-+={}[]|\\:;\"'<>,.?/".indexOf(currentChar) >= 0) {
				foundSpecialChar = true;
			} else {
				foundInvalidChar = true;
				passwordIndexofError = currentCharNdx;
				return "*** Error *** An invalid character has been found!";
			}
			if (currentCharNdx >= 7) {
				foundLongEnough = true;
			}

			currentCharNdx++;
			if (currentCharNdx >= inputLine.length())
				running = false;
			else
				currentChar = input.charAt(currentCharNdx);
		}

		List<String> missing = new ArrayList<String>();
		if (!foundUpperCase) missing.add("an uppercase letter");
		if (!foundLowerCase) missing.add("a lowercase letter");
		if (!foundNumericDigit) missing.add("a number");
		if (!foundSpecialChar) missing.add("a special character");
		if (!foundLongEnough) missing.add("at least 8 characters");

		if (missing.isEmpty())
		    return "";

		passwordIndexofError = currentCharNdx;
		return "Password must contain " + String.join(", ", missing) + ".";
	}
}