package guiFirstAdmin;

import java.sql.SQLException;
import database.Database;
import entityClasses.User;
import javafx.stage.Stage;
import validators.UserNameRecognizer;
import validators.PasswordValidator;

/*******
 * <p> Title: ControllerFirstAdmin Class. </p>
 * 
 * <p> Description: ControllerFirstAdmin class provides the controller actions based on the user's
 *  use of the JavaFX GUI widgets defined by the View class.
 * 
 * This page contains a number of buttons that have not yet been implemented.  WHhen those buttons
 * are pressed, an alert pops up to tell the user that the function associated with the button has
 * not been implemented. Also, be aware that What has been implemented may not work the way the
 * final product requires and there maybe defects in this code.
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
 *  
 */

public class ControllerFirstAdmin {
	/*-********************************************************************************************

	The controller attributes for this page
	
	This controller is not a class that gets instantiated.  Rather, it is a collection of protected
	static methods that can be called by the View (which is a singleton instantiated object) and 
	the Model is often just a stub, or will be a singleton instantiated object.
	
	*/
	
	private static String adminUsername = "";
	private static String adminPassword1 = "";
	private static String adminPassword2 = "";		
	protected static Database theDatabase = applicationMain.FoundationsMain.database;		

	/*-********************************************************************************************

	The User Interface Actions for this page
	
	*/
	
	/**
	 * Default constructor is not used.
	 */
	public ControllerFirstAdmin() {
	}

	/**********
	 * <p> Method: setAdminUsername() </p>
	 * 
	 * <p> Description: This method is called when the user adds text to the username field in the
	 * View.  A private local copy of what was last entered is kept here.</p>
	 * 
	 */
	protected static void setAdminUsername() {
		adminUsername = ViewFirstAdmin.text_AdminUsername.getText();
	}
	
	
	/**********
	 * <p> Method: setAdminPassword1() </p>
	 * 
	 * <p> Description: This method is called when the user adds text to the password 1 field in
	 * the View.  A private local copy of what was last entered is kept here.</p>
	 * 
	 */
	protected static void setAdminPassword1() {
		adminPassword1 = ViewFirstAdmin.text_AdminPassword1.getText();
		ViewFirstAdmin.label_PasswordsDoNotMatch.setText("");
	}
	
	
	/**********
	 * <p> Method: setAdminPassword2() </p>
	 * 
	 * <p> Description: This method is called when the user adds text to the password 2 field in
	 * the View.  A private local copy of what was last entered is kept here.</p>
	 * 
	 */
	protected static void setAdminPassword2() {
		adminPassword2 = ViewFirstAdmin.text_AdminPassword2.getText();		
		ViewFirstAdmin.label_PasswordsDoNotMatch.setText("");
	}
	
	
	/**********
	 * <p> Method: doSetupAdmin() </p>
	 * 
	 * <p> Description: This method is called when the user presses the button to set up the Admin
	 * account.  It start by trying to establish a new user and placing that user into the
	 * database.  If that is successful, we proceed to the UserUpdate page.</p>
	 * 
	 */
	protected static void doSetupAdmin(Stage ps, int r) {
		
		// Check the username format first
		String usernameError = UserNameRecognizer.checkForValidUserName(adminUsername);
		if (usernameError != "") {
			ViewFirstAdmin.alertUsernamePasswordError.setHeaderText("Invalid Username");
			ViewFirstAdmin.alertUsernamePasswordError.setContentText(usernameError);
			ViewFirstAdmin.alertUsernamePasswordError.showAndWait();
			return;
		}
		
		// Check that the username isn't already taken
		if (theDatabase.doesUserExist(adminUsername)) {
			ViewFirstAdmin.alertUsernamePasswordError.setHeaderText("Username Taken");
			ViewFirstAdmin.alertUsernamePasswordError.setContentText(
					"That username is already taken. Please choose another.");
			ViewFirstAdmin.alertUsernamePasswordError.showAndWait();
			return;
		}
		
		// Make sure the two passwords are the same
		if (adminPassword1.compareTo(adminPassword2) != 0) {
			ViewFirstAdmin.text_AdminPassword1.setText("");
			ViewFirstAdmin.text_AdminPassword2.setText("");
			ViewFirstAdmin.label_PasswordsDoNotMatch.setText(
					"The two passwords must match. Please try again!");
			return;
		}
		
		// Check the password format
		String passwordError = PasswordValidator.evaluatePassword(adminPassword1);
		if (passwordError != "") {
			ViewFirstAdmin.text_AdminPassword1.setText("");
			ViewFirstAdmin.text_AdminPassword2.setText("");
			ViewFirstAdmin.alertUsernamePasswordError.setHeaderText("Invalid Password");
			ViewFirstAdmin.alertUsernamePasswordError.setContentText(passwordError);
			ViewFirstAdmin.alertUsernamePasswordError.showAndWait();
			return;
		}
		
		// Everything checks out -- create the account
		User user = new User(adminUsername, adminPassword1, "", "", "", "", "", true, false, false);
		try {
			theDatabase.register(user);
		} catch (SQLException e) {
			System.err.println("*** ERROR *** Database error trying to register a user: " + e.getMessage());
			ViewFirstAdmin.alertUsernamePasswordError.setHeaderText("Error");
			ViewFirstAdmin.alertUsernamePasswordError.setContentText(
					"Something went wrong creating the account. Please try again.");
			ViewFirstAdmin.alertUsernamePasswordError.showAndWait();
			return;
		}

		// User was established in the database, so navigate to the User Update Page
		guiUserUpdate.ViewUserUpdate.displayUserUpdate(ViewFirstAdmin.theStage, user);
	}
	
	
	/**********
	 * <p> Method: performQuit() </p>
	 * 
	 * <p> Description: This method terminates the execution of the program.  It leaves the
	 * database in a state where the normal login page will be displayed when the application is
	 * restarted.</p>
	 * 
	 */
	protected static void performQuit() {
		System.out.println("Perform Quit");
		System.exit(0);
	}	
}