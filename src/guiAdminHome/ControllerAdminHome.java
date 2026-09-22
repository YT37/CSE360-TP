package guiAdminHome;

import database.Database;
import validators.EmailAddressRecognizer;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import java.util.Optional;
import java.util.List;
import guiTools.UserListView;
import validators.PasswordValidator;
import validators.UserNameRecognizer;
import guiTools.InvitationListView;

/*******
 * <p> Title: GUIAdminHomePage Class. </p>
 * 
 * <p> Description: The Java/FX-based Admin Home Page.  This class provides the controller actions
 * basic on the user's use of the JavaFX GUI widgets defined by the View class.
 * 
 * This page contains the buttons an admin uses to invite users, manage invitations, set one-time
 * passwords, delete users, list users, and add or remove roles.  Be aware that what has been
 * implemented may not work the way the final product requires and there maybe defects in this
 * code.
 * 
 * The class has been written assuming that the View or the Model are the only class methods that
 * can invoke these methods.  This is why each has been declared at "protected".  Do not change any
 * of these methods to public.</p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * 
 * @author Lynn Robert Carter
 * 
 * @version 1.00		2025-08-17 Initial version
 * @version 1.01		2025-09-16 Update Javadoc documentation *  
 */

public class ControllerAdminHome {
	
	/*-*******************************************************************************************

	User Interface Actions for this page
	
	This controller is not a class that gets instantiated.  Rather, it is a collection of protected
	static methods that can be called by the View (which is a singleton instantiated object) and 
	the Model is often just a stub, or will be a singleton instantiated object.
	
	*/
	
	/**
	 * Default constructor is not used.
	 */
	public ControllerAdminHome() {
	}
	
	// Reference for the in-memory database so this package has access
	private static Database theDatabase = applicationMain.FoundationsMain.database;

	/**********
	 * <p> 
	 * 
	 * Title: performInvitation () Method. </p>
	 * 
	 * <p> Description: Protected method to send an email inviting a potential user to establish
	 * an account and a specific role. </p>
	 */
	protected static void performInvitation () {
		// Verify that the email address is valid - If not alert the user and return
		String emailAddress = ViewAdminHome.text_InvitationEmailAddress.getText();
		if (invalidEmailAddress(emailAddress)) {
			return;
		}
		
		// Check to ensure that we are not sending a second message with a new invitation code to
		// the same email address.  
		if (theDatabase.emailaddressHasBeenUsed(emailAddress)) {
			ViewAdminHome.alertEmailError.setContentText(
					"An invitation has already been sent to this email address.");
			ViewAdminHome.alertEmailError.showAndWait();
			return;
		}
		
		// Inform the user that the invitation has been sent and display the invitation code
		String theSelectedRole = (String) ViewAdminHome.combobox_SelectRole.getValue();
		String invitationCode = theDatabase.generateInvitationCode(emailAddress,
				theSelectedRole);
		String msg = "Code: " + invitationCode + " for role " + theSelectedRole + 
				" was sent to: " + emailAddress;
		System.out.println(msg);
		ViewAdminHome.alertEmailSent.setContentText(msg);
		ViewAdminHome.alertEmailSent.showAndWait();
		
		// Update the Admin Home pages status
		ViewAdminHome.text_InvitationEmailAddress.setText("");
		ViewAdminHome.label_NumberOfInvitations.setText("Number of outstanding invitations: " + 
				theDatabase.getNumberOfInvitations());
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: manageInvitations () Method. </p>
	 * 
	 * <p> Description: Protected method that opens the Manage Invitations window.  The window
	 * lists every outstanding invitation (code, email address, role, and deadline), after expired
	 * invitations have been removed, and allows the admin to cancel a selected invitation. </p>
	 */
	protected static void manageInvitations() {
		InvitationListView.show(ViewAdminHome.theStage, theDatabase);
		
		// An invitation may have been cancelled or expired, so refresh the counts
		ViewAdminHome.updateCounts();
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: setOnetimePassword () Method. </p>
	 * 
	 * <p> Description: Protected method that allows an admin to set a one-time password for an
	 * existing account.  The admin is asked for the username, which must exist, and then for a
	 * temporary password, which must satisfy the password rules.  The password is stored and
	 * flagged as one-time, so that user must set a new password the next time they log in. </p>
	 */
	protected static void setOnetimePassword() {
		// Ask for the username of the account; do nothing if the admin cancels
		TextInputDialog userDialog = new TextInputDialog("");
		userDialog.setTitle("Set a One-Time Password");
		userDialog.setHeaderText("Enter the username of the account");
		Optional<String> userResult = userDialog.showAndWait();
		if (!userResult.isPresent() || userResult.get().isEmpty()) return;
		String username = userResult.get();
		
		// Check the length before the lookup, then make sure the account exists
		if (username.length() > UserNameRecognizer.MAX_USERNAME_LENGTH || 
				!theDatabase.doesUserExist(username)) {
			ViewAdminHome.alertOneTimePasswordError.setContentText(
					"No account exists with that username.");
			ViewAdminHome.alertOneTimePasswordError.showAndWait();
			return;
		}
		
		// Ask for the temporary password; do nothing if the admin cancels
		TextInputDialog passwordDialog = new TextInputDialog("");
		passwordDialog.setTitle("Set a One-Time Password");
		passwordDialog.setHeaderText("Enter a temporary password for \"" + username + "\"");
		Optional<String> passwordResult = passwordDialog.showAndWait();
		if (!passwordResult.isPresent() || passwordResult.get().isEmpty()) return;
		String tempPassword = passwordResult.get();
		
		// The temporary password must satisfy the same rules as any other password
		String passwordError = PasswordValidator.evaluatePassword(tempPassword);
		if (passwordError != "") {
			ViewAdminHome.alertOneTimePasswordError.setContentText(passwordError);
			ViewAdminHome.alertOneTimePasswordError.showAndWait();
			return;
		}
		
		// Store the password, flag it as one-time, and tell the admin it has been set
		theDatabase.setOneTimePassword(username, tempPassword);
		ViewAdminHome.alertOneTimePasswordSet.setContentText(
				"A one-time password has been set for \"" + username + "\". "
				+ "They must set a new password the next time they log in.");
		ViewAdminHome.alertOneTimePasswordSet.showAndWait();
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: deleteUser () Method. </p>
	 * 
	 * <p> Description: Protected method that opens the user table with a Delete button.  The
	 * admin selects a user and must answer "Are you sure?" before the account is deleted.  An
	 * admin cannot delete their own account. </p>
	 */
	protected static void deleteUser() {
		UserListView.show(ViewAdminHome.theStage, theDatabase, ViewAdminHome.theUser, true);
		
		// A user may have been deleted, so refresh the counts
		ViewAdminHome.updateCounts();
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: listUsers () Method. </p>
	 * 
	 * <p> Description: Protected method that opens a table listing every user account with
	 * the username, name, email address, and roles of each user. </p>
	 */
	protected static void listUsers() {
		UserListView.show(ViewAdminHome.theStage, theDatabase, ViewAdminHome.theUser, false);
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: addRemoveRoles () Method. </p>
	 * 
	 * <p> Description: Protected method that allows an admin to add and remove roles for any of
	 * the users currently in the system.  This is done by invoking the AddRemoveRoles Page. There
	 * is no need to specify the home page for the return as this can only be initiated by and
	 * Admin.</p>
	 */
	protected static void addRemoveRoles() {
		guiAddRemoveRoles.ViewAddRemoveRoles.displayAddRemoveRoles(ViewAdminHome.theStage, 
				ViewAdminHome.theUser);
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: invalidEmailAddress () Method. </p>
	 * 
	 * <p> Description: Protected method that is intended to check an email address before it is
	 * used to reduce errors.  The code checks to see that the email address is not empty and then
	 * uses the EmailAddressRecognizer to perform a syntactic check.  If there is a problem, an
	 * alert explains it to the admin.</p>
	 * 
	 * @param emailAddress	This String holds what is expected to be an email address
	 * 
	 * @return true if the email address is invalid, else false
	 */
	protected static boolean invalidEmailAddress(String emailAddress) {
		if (emailAddress.length() == 0) {
			ViewAdminHome.alertEmailError.setContentText(
					"Correct the email address and try again.");
			ViewAdminHome.alertEmailError.showAndWait();
			return true;
		}
		
		String emailError = EmailAddressRecognizer.checkEmailAddress(emailAddress);
		if (emailError != "") {
			ViewAdminHome.alertEmailError.setContentText(emailError);
			ViewAdminHome.alertEmailError.showAndWait();
			return true;
		}
		
		return false;
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: performLogout () Method. </p>
	 * 
	 * <p> Description: Protected method that logs this user out of the system and returns to the
	 * login page for future use.</p>
	 */
	protected static void performLogout() {
		guiUserLogin.ViewUserLogin.displayUserLogin(ViewAdminHome.theStage);
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: performQuit () Method. </p>
	 * 
	 * <p> Description: Protected method that gracefully terminates the execution of the program.
	 * </p>
	 */
	protected static void performQuit() {
		System.exit(0);
	}
}
