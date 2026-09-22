package guiUserUpdate;

import database.Database;
import entityClasses.User;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class ControllerUserUpdate {
	/*-********************************************************************************************

	The Controller for ViewUserUpdate 
	
	**********************************************************************************************/

	/**********
	 * <p> Title: ControllerUserUpdate Class</p>
	 * 
	 * <p> Description: This static class supports the actions initiated by the ViewUserUpdate
	 * class. In this case, there is just one method, no constructors, and one attribute, the
	 * reference to the database.</p>
	 *
	 */

	/*-********************************************************************************************

	The User Interface Actions for this page
	
	**********************************************************************************************/

	
	// Reference for the in-memory database so this package has access
	private static Database theDatabase = applicationMain.FoundationsMain.database;

	
	/**********
	 * <p> Method: public goToUserHomePage(Stage theStage, User theUser) </p>
	 * 
	 * <p> Description: This method is called when the user has clicked on the button to
	 * proceed to the user's home page.  If the user is still using a one-time password, an alert
	 * tells the user to set a new password first and the user stays on this page.  Otherwise, a
	 * user with more than one role is sent to the page to select a role, and a user with one role
	 * is sent directly to that role's home page.</p>
	 * 
	 * @param theStage specifies the JavaFX Stage for next next GUI page and it's methods
	 * 
	 * @param theUser specifies the user so we go to the right page and so the right information
	 */
	protected static void goToUserHomePage(Stage theStage, User theUser) {
		
		// A user with a one-time password must set a new password before leaving this page
		theDatabase.getUserAccountDetails(theUser.getUserName());
		if (theDatabase.getCurrentIsOneTimePassword()) {
			Alert alert = new Alert(Alert.AlertType.WARNING,
					"You must set a new password before continuing.");
			alert.showAndWait();
			return;
		}
		
		// Use the number of roles to decide which home page dispatch to use
		int numberOfRoles = theDatabase.getNumberOfRoles(theUser);
		if (numberOfRoles > 1) {
			guiMultipleRoleDispatch.ViewMultipleRoleDispatch.displayMultipleRoleDispatch(theStage, theUser);
		} else {
			guiTools.GUISingleRoleDispatch.doSingleRoleDispatch(theStage, theUser);
		}
	}
}
