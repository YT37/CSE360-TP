package validators;

/*******
 * <p> Title: NameValidator Class. </p>
 * 
 * <p> Description: Validates the First/Middle/Last/Preferred First Name fields per TP1's
 * Input Validation rules -- max 32 characters, no content restriction since names vary too
 * much to enforce a strict character set. </p>
 */
public class NameValidator {

	public static final int MAX_NAME_LENGTH = 32;

	public static String checkName(String input) {
		if (input.length() > MAX_NAME_LENGTH) {
			return "Can't be more than " + MAX_NAME_LENGTH + " characters long";
		}
		return "";
	}
}