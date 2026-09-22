package guiUserLogin;

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


/*******
 * <p> Title: ViewUserLogin Class. </p>
 * 
 * <p> Description: The Java/FX-based System Startup Page.</p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * 
 * @author Lynn Robert Carter
 * 
 * @version 1.00		2025-04-20 Initial version
 *  
 */

public class ViewUserLogin {

	/*-********************************************************************************************

	Attributes

	 *********************************************************************************************/

	// These are the application values required by the user interface

	private static double width = applicationMain.FoundationsMain.WINDOW_WIDTH;
	private static double height = applicationMain.FoundationsMain.WINDOW_HEIGHT;

	private static Label label_ApplicationTitle = new Label("Lessons Learned System");

	// This set is for all subsequent starts of the system
	private static Label label_OperationalStartTitle = new Label("Sign in");
	private static Label label_LogInInsrtuctions = new Label("Enter your username and password.");
	protected static Alert alertUsernamePasswordError = new Alert(AlertType.INFORMATION);
	protected static Alert alertInvitationCodeError = new Alert(AlertType.INFORMATION);


	//	private User user;
	protected static TextField text_Username = new TextField();
	protected static PasswordField text_Password = new PasswordField();
	private static Button button_Login = new Button("Log In");	

	private static Label label_AccountSetupInsrtuctions = new Label("New here? Enter your " +
			"invitation code.");
	private static TextField text_Invitation = new TextField();
	private static Button button_SetupAccount = new Button("Set Up Account");

	// The panel that groups the sign in and account setup widgets
	private static Region panel_SignIn = new Region();

	private static Button button_Quit = new Button("Quit");

	private static Stage theStage;	
	private static Pane theRootPane;
	public static Scene theUserLoginScene = null;	


	private static ViewUserLogin theView = null;	//	private static guiUserLogin.ControllerUserLogin theController;


	/*-********************************************************************************************

	Constructor

	 *********************************************************************************************/

	public static void displayUserLogin(Stage ps) {
		
		// Establish the references to the GUI. There is no current user yet.
		theStage = ps;
		
		// If not yet established, populate the static aspects of the GUI
		if (theView == null) theView = new ViewUserLogin();
		
		// Populate the dynamic aspects of the GUI with the data from the user and the current
		// state of the system.		
		text_Username.setText("");		// Reset the username and password from the last use
		text_Password.setText("");
		text_Invitation.setText("");	// Same for the invitation code

		// Set the title for the window, display the page, and wait for the Admin to do something
		theStage.setTitle("Lessons Learned System: Sign In");		
		theStage.setScene(theUserLoginScene);
		theStage.show();
	}

	/**********
	 * <p> Method: ViewUserLoginPage() </p>
	 * 
	 * <p> Description: This method is called when the application first starts. It must handle
	 * two cases: 1) when no has been established and 2) when one or more users have been 
	 * established.
	 * 
	 * If there are no users in the database, this means that the person starting the system jmust
	 * be an administrator, so a special GUI is provided to allow this Admin to set a username and
	 * password.
	 * 
	 * If there is at least one user, then a different display is shown for existing users to login
	 * and for potential new users to provide an invitation code and if it is valid, they are taken
	 * to a page where they can specify a username and password.</p>
	 * 
	 * @param ps specifies the JavaFX Stage to be used for this GUI and it's methods
	 * 
	 * @param theRoot specifies the JavaFX Pane to be used for this GUI and it's methods
	 * 
	 * @param db specifies the Database to be used by this GUI and it's methods
	 * 
	 */
	private ViewUserLogin() {

		// Create the Pane for the list of widgets and the Scene for the window
		theRootPane = new Pane();
		theUserLoginScene = new Scene(theRootPane, width, height);
		
		// Populate the window with the title and other common widgets and set their static state
		setupLabelUI(label_ApplicationTitle, "Arial", 18, width, Pos.CENTER, 0, 24);
		label_ApplicationTitle.getStyleClass().add("subtitle");

		setupLabelUI(label_OperationalStartTitle, "Arial", 28, width, Pos.CENTER, 0, 52);
		label_OperationalStartTitle.getStyleClass().add("page-title");

		// The panel behind the sign in and account setup widgets, centered on the page
		panel_SignIn.getStyleClass().add("surface");
		panel_SignIn.setLayoutX(200);
		panel_SignIn.setLayoutY(110);
		panel_SignIn.setPrefSize(400, 370);


		// Existing user log in portion of the page

		setupLabelUI(label_LogInInsrtuctions, "Arial", 14, 340, Pos.BASELINE_LEFT, 230, 132);
		label_LogInInsrtuctions.getStyleClass().add("helper-text");

		// Establish the text input operand field for the username
		setupTextUI(text_Username, "Arial", 18, 340, Pos.BASELINE_LEFT, 230, 160, true);
		text_Username.setPromptText("Enter Username");

		// Establish the text input operand field for the password
		setupTextUI(text_Password, "Arial", 18, 340, Pos.BASELINE_LEFT, 230, 210, true);
		text_Password.setPromptText("Enter Password");

		// Set up the Log In button, the primary action, which stays disabled until both the
		// username and the password have been entered.  Pressing Enter in either field also
		// presses the button (a disabled button ignores it).
		setupButtonUI(button_Login, "Dialog", 18, 340, Pos.CENTER, 230, 264);
		button_Login.getStyleClass().add("primary");
		button_Login.disableProperty().bind(text_Username.textProperty().isEmpty()
				.or(text_Password.textProperty().isEmpty()));
		button_Login.setOnAction((_) -> {ControllerUserLogin.doLogin(theStage); });
		text_Username.setOnAction((_) -> {button_Login.fire(); });
		text_Password.setOnAction((_) -> {button_Login.fire(); });

		alertUsernamePasswordError.setTitle("Invalid username/password!");
		alertUsernamePasswordError.setHeaderText(null);
		alertInvitationCodeError.setTitle("Invalid Invitation Code");
		alertInvitationCodeError.setHeaderText(null);


		// The invitation to setup an account portion of the page

		setupLabelUI(label_AccountSetupInsrtuctions, "Arial", 14, 340, Pos.BASELINE_LEFT, 230, 338);
		label_AccountSetupInsrtuctions.getStyleClass().add("helper-text");

		// Establish the text input operand field for the password
		setupTextUI(text_Invitation, "Arial", 18, 340, Pos.BASELINE_LEFT, 230, 366, true);
		text_Invitation.setPromptText("Enter Invitation Code");

		// Set up the setup button, which stays disabled until a code has been entered
		setupButtonUI(button_SetupAccount, "Dialog", 18, 340, Pos.CENTER, 230, 418);
		button_SetupAccount.disableProperty().bind(text_Invitation.textProperty().isEmpty());
		text_Invitation.setOnAction((_) -> {button_SetupAccount.fire(); });
		button_SetupAccount.setOnAction((_) -> {
			System.out.println("**** Calling doSetupAccount");
			ControllerUserLogin.doSetupAccount(theStage, text_Invitation.getText());
		});

		// Set up the Quit button  
		setupButtonUI(button_Quit, "Dialog", 18, 150, Pos.CENTER, 325, 520);
		button_Quit.setOnAction((_) -> {ControllerUserLogin.performQuit(); });

		//		theRootPane.getChildren().clear();

		theRootPane.getChildren().addAll(
				panel_SignIn,
				label_ApplicationTitle, 
				label_OperationalStartTitle,
				label_LogInInsrtuctions, label_AccountSetupInsrtuctions, text_Username,
				button_Login, text_Password, text_Invitation, button_SetupAccount,
				button_Quit);
	}


	/*-********************************************************************************************

	Helper methods to reduce code length

	 *********************************************************************************************/

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
}
