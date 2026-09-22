package validators;

/*******
 * <p> Title: NameValidator Class. </p>
 * 
 * <p> Description: Validates the First, Middle, Last, and Preferred First Name fields.  A name
 * may be no more than MAX_NAME_LENGTH characters long.  Its content is not restricted, since
 * names vary too much to enforce a strict character set. </p>
 */
public class NameValidator {

	public static final int MAX_NAME_LENGTH = 32;	// The maximum length of a name

	/**********
	 * <p> Method: String checkName(String input) </p>
	 * 
	 * <p> Description: This method checks that a name is no longer than MAX_NAME_LENGTH. </p>
	 * 
	 * @param input		The name to be checked
	 * 
	 * @return an empty String if the name is acceptable, else an error message
	 */
	public static String checkName(String input) {
		if (input.length() > MAX_NAME_LENGTH) {
			return "Can't be more than " + MAX_NAME_LENGTH + " characters long";
		}
		return "";
	}
}