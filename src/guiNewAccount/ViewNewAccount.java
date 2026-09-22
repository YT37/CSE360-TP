package guiNewAccount;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import database.Database;
import entityClasses.User;
import guiTools.PasswordStatusPanel;

/*******
 * <p> Title: ViewNewAccount Class. </p>
 * 
 * <p> Description: The ViewNewAccount Page is used to enable a potential user with an invitation
 * code to establish an account after they have specified an invitation code on the standard login
 * page. </p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * 
 * @author Lynn Robert Carter
 * 
 * @version 1.00		2025-08-19 Initial version
 *  
 */

public class ViewNewAccount {
	
	/*-********************************************************************************************

	Attributes
	
	*/
	
	// These are the application values required by the user interface
	
	private static double width = applicationMain.FoundationsMain.WINDOW_WIDTH;
	private static double height = applicationMain.FoundationsMain.WINDOW_HEIGHT;
	
	// This is a simple GUI login Page, very similar to the FirstAdmin login page.  The only real
	// difference is in this case we also know an email address, since it was used to send the
	// invitation to the potential user.
	private static Label label_ApplicationTitle = 
			new Label("Lessons Learned System");
    protected static Label label_NewUserCreation = new Label("Create your account");
    protected static Label label_NewUserLine = new Label("Please enter a username and a password.");
    protected static TextField text_Username = new TextField();
    protected static PasswordField text_Password1 = new PasswordField();
    protected static PasswordField text_Password2 = new PasswordField();
    protected static Button button_UserSetup = new Button("Create Account");

	// The panel that groups the account fields and the password requirements
	private static Region panel_Account = new Region();
    protected static TextField text_Invitation = new TextField();

	// This alert is used should the invitation code be invalid
    protected static Alert alertInvitationCodeIsInvalid = new Alert(AlertType.INFORMATION);

	// This alert is used should the user enter two passwords that do not match
	protected static Alert alertUsernamePasswordError = new Alert(AlertType.INFORMATION);

	// Password status panel for live requirement feedback, shown from the start
	private static PasswordStatusPanel passwordStatus = new PasswordStatusPanel(true, true);

    protected static Button button_Quit = new Button("Quit");

	// These attributes are used to configure the page and populate it with this user's information
	private static ViewNewAccount theView;		// Is instantiation of the class needed?

	// Reference for the in-memory database so this package has access
	private static Database theDatabase = applicationMain.FoundationsMain.database;		

	protected static Stage theStage;			// The Stage that JavaFX has established for us
	private static Pane theRootPane;			// The Pane that holds all the GUI widgets 
	protected static User theUser;				// The current logged in User
   
    protected static String theInvitationCode;	// The invitation code links to an email address
    											// and a role for this user
    protected static String emailAddress;		// Established here for use by the controller
    protected static String theRole;			// Established here for use by the controller
	public static Scene theNewAccountScene = null;	// Access to the User Update page's GUI Widgets
	

	/*-********************************************************************************************

	Constructors
	
	*/

	/**********
	 * <p> Method: displayNewAccount(Stage ps, String ic) </p>
	 * 
	 * <p> Description: This method is the single entry point from outside this package to cause
	 * the NewAccount page to be displayed.
	 * 
	 * It first sets up very shared attributes so we don't have to pass parameters.
	 * 
	 * It then checks to see if the page has been setup.  If not, it instantiates the class, 
	 * initializes all the static aspects of the GUI widgets (e.g., location on the page, font,
	 * size, and any methods to be performed).
	 * 
	 * After the instantiation, the code then populates the elements that change based on the user
	 * and the system's current state.  It then sets the Scene onto the stage, and makes it visible
	 * to the user.
	 * 
	 * @param ps specifies the JavaFX Stage to be used for this GUI and it's methods
	 * 
	 * @param ic specifies the user's invitation code for this GUI and it's methods
	 * 
	 */
	public static void displayNewAccount(Stage ps, String ic) {
		// This is the only way some component of the system can cause a New User Account page to
		// appear.  The first time, the class is created and initialized.  Every subsequent call it
		// is reused with only the elements that differ being initialized.
		
		// Establish the references to the GUI and the current user
		theStage = ps;				// Save the reference to the Stage for the rest of this package
		theInvitationCode = ic;		// Establish the invitation code so it can be easily accessed
		
		if (theView == null) theView = new ViewNewAccount();
		
		text_Username.setText("");	// Clear the input fields so previously entered values do not
		text_Password1.setText("");	// appear for a new user
		text_Password2.setText("");
		passwordStatus.reset();	// Reset password status panel for new user
		
		// Fetch the role for this user
		theRole = theDatabase.getRoleGivenAnInvitationCode(theInvitationCode);
		
		if (theRole.length() == 0) {// If there is an issue with the invitation code, display a
			alertInvitationCodeIsInvalid.showAndWait();	// dialog box saying that are when it it
			return;					// acknowledged, return so the proper code can be entered
		}
		
		// Get the email address associated with the invitation code
		emailAddress = theDatabase.getEmailAddressUsingCode(theInvitationCode);
		
		// Tell the user which invitation this account is being created for
		label_NewUserLine.setText("Choose a username and a password for " + emailAddress + 
				" (" + theRole + ").");
		
    	// Place all of the established GUI elements into the pane
    	theRootPane.getChildren().clear();
    	theRootPane.getChildren().addAll(panel_Account, label_ApplicationTitle, 
    			label_NewUserCreation, label_NewUserLine, text_Username,
    			text_Password1, text_Password2, button_UserSetup, button_Quit, passwordStatus);    	

		// Set the title for the window, display the page, and wait for the Admin to do something
		theStage.setTitle("Lessons Learned System: Create Your Account");	
        theStage.setScene(theNewAccountScene);
		theStage.show();
	}
	
	/**********
	 * <p> Constructor: ViewNewAccount() </p>
	 * 
	 * <p> Description: This constructor is called just once, the first time a new account needs to
	 * be created.  It establishes all of the common GUI widgets for the page so they are only
	 * created once and reused when needed.
	 * 
	 * The do
	 * 		
	 */
	private ViewNewAccount() {
		
		// Create the Pane for the list of widgets and the Scene for the window
		theRootPane = new Pane();
		theNewAccountScene = new Scene(theRootPane, width, height);

		// Label the Panle with the name of the startup screen, centered at the top of the pane
		setupLabelUI(label_ApplicationTitle, "Arial", 18, width, Pos.CENTER, 0, 24);
		label_ApplicationTitle.getStyleClass().add("subtitle");
		
    	// Label to display the welcome message for the new user
    	setupLabelUI(label_NewUserCreation, "Arial", 28, width, Pos.CENTER, 0, 52);
    	label_NewUserCreation.getStyleClass().add("page-title");
	
    	// Label to display the  message for the first user
    	setupLabelUI(label_NewUserLine, "Arial", 14, width, Pos.CENTER, 0, 100);
    	label_NewUserLine.getStyleClass().add("helper-text");

		// Set up the account panel
		panel_Account.getStyleClass().add("surface");
		panel_Account.setLayoutX(85);
		panel_Account.setLayoutY(135);
		panel_Account.setPrefSize(640, 290);
		
		// Establish the text input operand asking for a username
		setupTextUI(text_Username, "Arial", 18, 300, Pos.BASELINE_LEFT, 120, 165, true);
		text_Username.setPromptText("Enter the Username");
		text_Username.textProperty().addListener((obs, oldVal, newVal)
				-> {updateUserSetupButtonState(); });
		
		// Establish the text input operand field for the password
		setupTextUI(text_Password1, "Arial", 18, 300, Pos.BASELINE_LEFT, 120, 215, true);
		text_Password1.setPromptText("Enter the Password");
		text_Password1.textProperty().addListener((obs, oldVal, newVal)
				-> {passwordStatus.update(newVal);
					updateUserSetupButtonState(); });
		
		// Establish the text input operand field to confirm the password
		setupTextUI(text_Password2, "Arial", 18, 300, Pos.BASELINE_LEFT, 120, 265, true);
		text_Password2.setPromptText("Enter the Password Again");
		text_Password2.textProperty().addListener((obs, oldVal, newVal)
				-> {passwordStatus.updateMatch(text_Password1.getText().equals(newVal) && !newVal.isEmpty());
					updateUserSetupButtonState(); });

		// If the invitation code is wrong, this alert dialog will tell the user
		alertInvitationCodeIsInvalid.setTitle("Invalid Invitation Code");
		alertInvitationCodeIsInvalid.setHeaderText("The invitation code is not valid.");
		alertInvitationCodeIsInvalid.setContentText("Correct the code and try again.");

		// If the passwords do not match, this alert dialog will tell the user
		alertUsernamePasswordError.setTitle("Passwords Do Not Match");
		alertUsernamePasswordError.setHeaderText("The two passwords must be identical.");
		alertUsernamePasswordError.setContentText("Correct the passwords and try again.");

        // Set up the Create Account button
        setupButtonUI(button_UserSetup, "Dialog", 18, 300, Pos.CENTER, 120, 330);
        button_UserSetup.getStyleClass().add("primary");
        button_UserSetup.setDisable(true);
        button_UserSetup.setOnAction((_) -> {ControllerNewAccount.doCreateUser(); });
		
        // Enable the user to quit the application
        setupButtonUI(button_Quit, "Dialog", 18, 150, Pos.CENTER, 325, 520);
        button_Quit.setOnAction((_) -> {ControllerNewAccount.performQuit(); });

		// Position the password status panel to the right of the password fields
		passwordStatus.setLayoutX(450);
		passwordStatus.setLayoutY(167);
	}
	
	
	/*-********************************************************************************************

	Helper methods to reduce code length

	 */
	
	/**********
	 * Private local method to initialize the standard fields for a label
	 */
	
	private void setupLabelUI(Label l, String ff, double f, double w, Pos p, double x, double y){
		l.setFont(Font.font(applicationMain.Theme.FONT_FAMILY, f));
		l.setMinWidth(w);
		l.setAlignment(p);
		l.setLayoutX(x);
		l.setLayoutY(y);		
	}
	
	
	/**********
	 * Private local method to initialize the standard fields for a button
	 * 
	 * @param b		The Button object to be initialized
	 * @param ff	The font requested by the caller (Theme.FONT_FAMILY is used instead)
	 * @param f		The size of the font to be used
	 * @param w		The width of the Button
	 * @param p		The alignment (e.g. left, centered, or right)
	 * @param x		The location from the left edge (x axis)
	 * @param y		The location from the top (y axis)
	 */
	private void setupButtonUI(Button b, String ff, double f, double w, Pos p, double x, double y){
		b.setFont(Font.font(applicationMain.Theme.FONT_FAMILY, f));
		b.setMinWidth(w);
		b.setAlignment(p);
		b.setLayoutX(x);
		b.setLayoutY(y);		
	}

	/**********
	 * Private local method to initialize the standard fields for a text field
	 */
	private void setupTextUI(TextField t, String ff, double f, double w, Pos p, double x, double y, boolean e){
		t.setFont(Font.font(applicationMain.Theme.FONT_FAMILY, f));
		t.setMinWidth(w);
		t.setMaxWidth(w);
		t.setAlignment(p);
		t.setLayoutX(x);
		t.setLayoutY(y);		
		t.setEditable(e);
	}	

	/**********
	 * <p> Method: updateUserSetupButtonState() </p>
	 * 
	 * <p> Description: This method is called whenever any of the three fields changes.  It
	 * refreshes the "both passwords match" requirement (which can change when either password
	 * changes) and enables the Create Account button only when a username has been entered and
	 * the password meets every requirement, including the maximum length.  The Controller still
	 * validates everything when the button is pressed. </p>
	 */
	private static void updateUserSetupButtonState() {
		boolean passwordsMatch = text_Password1.getText().equals(text_Password2.getText())
				&& !text_Password1.getText().isEmpty();
		passwordStatus.updateMatch(passwordsMatch);
		button_UserSetup.setDisable(text_Username.getText().isEmpty()
				|| !passwordStatus.isFullyValidWithMatch()
				|| !passwordStatus.isFullyValidWithMaxLength());
	}
}
