package guiTools;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import validators.PasswordValidator;

/*******
 * <p> Title: PasswordStatusPanel Class. </p>
 * 
 * <p> Description: A reusable set of labels showing live pass/fail feedback for password
 * requirements. Call update(String) on every keystroke of the password field it's attached to.
 * Used by ViewFirstAdmin, ViewNewAccount, and PasswordEntryDialog. Wording matches
 * PasswordValidator.evaluatePassword() error message format. </p>
 */
public class PasswordStatusPanel extends VBox {

	// One label for each password requirement; each shows a check mark or a cross
	private final Label lblLength = new Label("at least 8 characters");
	private final Label lblUpper = new Label("an uppercase letter");
	private final Label lblLower = new Label("a lowercase letter");
	private final Label lblDigit = new Label("a number");
	private final Label lblSpecial = new Label("a special character");
	private final Label lblMaxLength = new Label("no more than 64 characters");
	private final Label lblChars = new Label("no spaces or unsupported characters");
	private final Label lblMatch = new Label("both passwords match");

	private boolean hasUserTyped = false;		// Has anything been typed into the password field?
	
	private final boolean includeMatchRow;		// Show the "both passwords match" row?
	private final boolean alwaysVisible;		// Show the panel before the user starts typing?

	/**********
	 * <p> Method: PasswordStatusPanel() </p>
	 * 
	 * <p> Description: This constructor establishes a panel that includes the "both passwords
	 * match" row and stays hidden until the user starts typing. </p>
	 */
	public PasswordStatusPanel() { this(true, false); }   

	/**********
	 * <p> Method: PasswordStatusPanel(boolean includeMatchRow, boolean alwaysVisible) </p>
	 * 
	 * <p> Description: This constructor establishes the panel with one label for each password
	 * requirement, all shown as not yet satisfied. </p>
	 * 
	 * @param includeMatchRow specifies whether the "both passwords match" row is shown
	 * 
	 * @param alwaysVisible specifies whether the panel is shown before the user starts typing
	 */
	public PasswordStatusPanel(boolean includeMatchRow, boolean alwaysVisible) {
	    super(3);
	    this.includeMatchRow = includeMatchRow;
	    this.alwaysVisible = alwaysVisible;
	    setAlignment(Pos.TOP_LEFT);
	    getChildren().addAll(lblLength, lblUpper, lblLower, lblDigit, lblSpecial, lblMaxLength, lblChars);
	    if (includeMatchRow) getChildren().add(lblMatch);
	    reset();
	}

	/**********
	 * <p> Method: void update(String password) </p>
	 * 
	 * <p> Description: This method evaluates the password with the PasswordValidator and marks
	 * each requirement as satisfied or not.  It should be called on every keystroke. </p>
	 * 
	 * @param password specifies the current contents of the password field
	 */
	public void update(String password) {
		if (password == null) password = "";
		hasUserTyped = password != null && !password.isEmpty();
		setVisible(alwaysVisible || hasUserTyped); setManaged(alwaysVisible || hasUserTyped);
		
		PasswordValidator.evaluatePassword(password);
		setStatus(lblLength, PasswordValidator.foundLongEnough);
		setStatus(lblUpper, PasswordValidator.foundUpperCase);
		setStatus(lblLower, PasswordValidator.foundLowerCase);
		setStatus(lblDigit, PasswordValidator.foundNumericDigit);
		setStatus(lblSpecial, PasswordValidator.foundSpecialChar);
		setStatus(lblMaxLength, password.length() <= PasswordValidator.MAX_PASSWORD_LENGTH);
		setStatus(lblChars, !PasswordValidator.foundInvalidChar);
	}

	/**********
	 * <p> Method: void updateMatch(boolean passwordsMatch) </p>
	 * 
	 * <p> Description: This method marks the "both passwords match" row as satisfied or not,
	 * once the user has started typing. </p>
	 * 
	 * @param passwordsMatch specifies whether the two password fields are the same
	 */
	public void updateMatch(boolean passwordsMatch) {
		if (hasUserTyped) {
			setStatus(lblMatch, passwordsMatch);
		}
	}

	/**********
	 * <p> Method: boolean isFullyValid() </p>
	 * 
	 * <p> Description: This method reports whether the most recently evaluated password meets
	 * the length, uppercase, lowercase, digit, special character, and valid character
	 * requirements.  It does not check the maximum length or the match row. </p>
	 * 
	 * @return true if those requirements are all satisfied, else false
	 */
	public boolean isFullyValid() {
		return PasswordValidator.foundLongEnough && PasswordValidator.foundUpperCase
				&& PasswordValidator.foundLowerCase && PasswordValidator.foundNumericDigit
				&& PasswordValidator.foundSpecialChar && !PasswordValidator.foundInvalidChar;
	}

	/**********
	 * <p> Method: boolean isFullyValidWithMatch() </p>
	 * 
	 * <p> Description: This method reports whether isFullyValid() is true and, when the match
	 * row is shown, whether both passwords match. </p>
	 * 
	 * @return true if those requirements are all satisfied, else false
	 */
	public boolean isFullyValidWithMatch() {
		return isFullyValid() && (!includeMatchRow || lblMatch.getText().startsWith("\u2713"));
	}

	/**********
	 * <p> Method: boolean isFullyValidWithMaxLength() </p>
	 * 
	 * <p> Description: This method reports whether isFullyValid() is true and the password is
	 * no longer than the maximum length. </p>
	 * 
	 * @return true if those requirements are all satisfied, else false
	 */
	public boolean isFullyValidWithMaxLength() {
		return isFullyValid() && lblMaxLength.getText().startsWith("\u2713");
	}

	/**********
	 * <p> Method: void reset() </p>
	 * 
	 * <p> Description: This method returns the panel to its initial state, as if nothing had
	 * been typed. </p>
	 */
	public void reset() {
		hasUserTyped = false;
		setVisible(alwaysVisible); 
		setManaged(alwaysVisible);
		setStatus(lblLength, false);
		setStatus(lblUpper, false);
		setStatus(lblLower, false);
		setStatus(lblDigit, false);
		setStatus(lblSpecial, false);
		setStatus(lblMaxLength, true);
		setStatus(lblChars, true);
		setStatus(lblMatch, false);
	}

	/**********
	 * Private local method to show a requirement label as satisfied (a green check mark) or not
	 * satisfied (a red cross)
	 * 
	 * @param label		The Label object for the requirement
	 * @param satisfied	Whether the requirement is satisfied
	 */
	private void setStatus(Label label, boolean satisfied) {
		String base = label.getText().replaceFirst("^[\u2713\u2717] ", "");
		if (satisfied) {
			label.setText("\u2713 " + base);
			label.setStyle("-fx-text-fill: green; -fx-font-size: 12;");
		} else {
			label.setText("\u2717 " + base);
			label.setStyle("-fx-text-fill: red; -fx-font-size: 12;");
		}
	}
}