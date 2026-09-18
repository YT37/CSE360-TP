package validators;

/*******
 * <p> Title: PasswordValidator Class. </p>
 * 
 * <p> Description: Standalone copy of the password evaluation logic from
 * PasswordEvaluationTestbed-F26's Model.java, trimmed of its GUI-testbed-specific code so it can
 * be reused in Foundations-F26. Includes the TP1 max-length check (see MAX_PASSWORD_LENGTH). </p>
 */
public class PasswordValidator {

	public static String passwordErrorMessage = "";
	public static String passwordInput = "";
	public static int passwordIndexofError = -1;
	public static final int MAX_PASSWORD_LENGTH = 64;	// TP1: reasonable upper size limit, checked
														// before any other processing is performed
	public static boolean foundUpperCase = false;
	public static boolean foundLowerCase = false;
	public static boolean foundNumericDigit = false;
	public static boolean foundSpecialChar = false;
	public static boolean foundLongEnough = false;
	private static String inputLine = "";
	private static char currentChar;
	private static int currentCharNdx;
	private static boolean running;

	public static String evaluatePassword(String input) {
		passwordErrorMessage = "";
		passwordIndexofError = 0;
		inputLine = input;
		currentCharNdx = 0;

		if (input.length() <= 0) {
			return "*** Error *** The password is empty!";
		}

		// TP1: check size before doing anything else with the input
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

		String errMessage = "";
		if (!foundUpperCase) errMessage += "Upper case; ";
		if (!foundLowerCase) errMessage += "Lower case; ";
		if (!foundNumericDigit) errMessage += "Numeric digits; ";
		if (!foundSpecialChar) errMessage += "Special character; ";
		if (!foundLongEnough) errMessage += "Long Enough; ";

		if (errMessage == "")
			return "";

		passwordIndexofError = currentCharNdx;
		return errMessage + "conditions were not satisfied";
	}
}