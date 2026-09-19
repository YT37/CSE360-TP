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
	 * class. In this case, there is just one method, no constructors, and no attributes.</p>
	 *
	 */

	/*-********************************************************************************************

	The User Interface Actions for this page
	
	**********************************************************************************************/

	
	/**********
	 * <p> Method: public goToUserHomePage(Stage theStage, User theUser) </p>
	 * 
	 * <p> Description: This method is called when the user has clicked on the button to
	 * proceed to the user's home page.
	 * 
	 * @param theStage specifies the JavaFX Stage for next next GUI page and it's methods
	 * 
	 * @param theUser specifies the user so we go to the right page and so the right information
	 */
	
	private static Database theDatabase = applicationMain.FoundationsMain.database;

	protected static void goToUserHomePage(Stage theStage, User theUser) {
		theDatabase.getUserAccountDetails(theUser.getUserName());
		if (theDatabase.getCurrentIsOneTimePassword()) {
			Alert alert = new Alert(Alert.AlertType.WARNING,
					"You must set a new password before continuing.");
			alert.showAndWait();
			return;
		}
		int numberOfRoles = theDatabase.getNumberOfRoles(theUser);
		if (numberOfRoles > 1) {
			guiMultipleRoleDispatch.ViewMultipleRoleDispatch.displayMultipleRoleDispatch(theStage, theUser);
		} else {
			guiTools.GUISingleRoleDispatch.doSingleRoleDispatch(theStage, theUser);
		}
	}
}
