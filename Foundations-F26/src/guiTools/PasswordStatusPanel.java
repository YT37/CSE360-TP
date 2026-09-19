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

	private final Label lblLength = new Label("at least 8 characters");
	private final Label lblUpper = new Label("an uppercase letter");
	private final Label lblLower = new Label("a lowercase letter");
	private final Label lblDigit = new Label("a number");
	private final Label lblSpecial = new Label("a special character");
	private final Label lblMaxLength = new Label("no more than 64 characters");
	private final Label lblChars = new Label("no spaces or unsupported characters");
	private final Label lblMatch = new Label("both passwords match");

	private boolean hasUserTyped = false;
	
	private final boolean includeMatchRow;
	private final boolean alwaysVisible;

	public PasswordStatusPanel() { this(true, false); }   

	public PasswordStatusPanel(boolean includeMatchRow, boolean alwaysVisible) {
	    super(3);
	    this.includeMatchRow = includeMatchRow;
	    this.alwaysVisible = alwaysVisible;
	    setAlignment(Pos.TOP_LEFT);
	    getChildren().addAll(lblLength, lblUpper, lblLower, lblDigit, lblSpecial, lblMaxLength, lblChars);
	    if (includeMatchRow) getChildren().add(lblMatch);
	    reset();
	}

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

	public void updateMatch(boolean passwordsMatch) {
		if (hasUserTyped) {
			setStatus(lblMatch, passwordsMatch);
		}
	}

	public boolean isFullyValid() {
		return PasswordValidator.foundLongEnough && PasswordValidator.foundUpperCase
				&& PasswordValidator.foundLowerCase && PasswordValidator.foundNumericDigit
				&& PasswordValidator.foundSpecialChar && !PasswordValidator.foundInvalidChar;
	}

	public boolean isFullyValidWithMatch() {
		return isFullyValid() && (!includeMatchRow || lblMatch.getText().startsWith("\u2713"));
	}

	public boolean isFullyValidWithMaxLength() {
		return isFullyValid() && lblMaxLength.getText().startsWith("\u2713");
	}

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