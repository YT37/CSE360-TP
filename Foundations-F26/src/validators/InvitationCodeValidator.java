package validators;

/*******
 * <p> Title: InvitationCodeValidator Class. </p>
 *
 * <p> Description: Validates the invitation code a potential new user enters on the login page
 * per TP1's Input Validation rules.  An invitation code is the first six characters of a UUID
 * (see Database.generateInvitationCode), so it must be exactly six characters long and may
 * contain only hexadecimal digits: 0-9 and the lowercase letters a-f.  The length is checked
 * before any other processing is performed, and the code is checked before it is used to query
 * the database. </p>
 */
public class InvitationCodeValidator {

	public static final int INVITATION_CODE_LENGTH = 6;	// TP1: invitation codes are always this
														// long, so this is also the maximum

	/**********
	 * <p> Method: String checkInvitationCode(String input) </p>
	 *
	 * <p> Description: This method checks that an invitation code is not empty, is exactly
	 * INVITATION_CODE_LENGTH characters long, and contains only the digits 0-9 and the lowercase
	 * letters a-f. </p>
	 *
	 * @param input		The invitation code to be checked
	 *
	 * @return an empty String if the code is acceptable, else an error message
	 */
	public static String checkInvitationCode(String input) {
		// An empty code is rejected first
		if (input.length() == 0) {
			return "The invitation code is empty. Enter the code from your invitation.";
		}

		// Check the length before looking at any of the characters
		if (input.length() > INVITATION_CODE_LENGTH) {
			return "An invitation code must be no more than " + INVITATION_CODE_LENGTH +
					" characters long.";
		}
		if (input.length() < INVITATION_CODE_LENGTH) {
			return "An invitation code must be exactly " + INVITATION_CODE_LENGTH +
					" characters long.";
		}

		// Every character must be a hexadecimal digit (0-9 or a-f)
		for (int ndx = 0; ndx < input.length(); ndx++) {
			char currentChar = input.charAt(ndx);
			if (!((currentChar >= '0' && currentChar <= '9') ||
					(currentChar >= 'a' && currentChar <= 'f'))) {
				return "An invitation code may contain only the digits 0-9 and the lowercase " +
						"letters a-f. Character " + (ndx + 1) + " (\"" + currentChar +
						"\") is not allowed.";
			}
		}
		return "";
	}
}
