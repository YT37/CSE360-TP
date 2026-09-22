package guiViewer;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import database.Database;
//import database.Database;
import entityClasses.User;


/*******
 * <p> Title: ViewViewerHome Class. </p>
 * 
 * <p> Description: The Java/FX-based Viewer Home Page.  The page welcomes the user by name and
 * describes what the Viewer role will be able to do.  The widgets the user needs to play this role
 * will be added to GUI Area 2 in later phases.</p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * 
 * @author Lynn Robert Carter
 * 
 * @version 1.00		2025-04-20 Initial version
 *  
 */

public class ViewViewerHome {
	
	/*-*******************************************************************************************

	Attributes
	
	 */
	
	// These are the application values required by the user interface
	
	private static double width = applicationMain.FoundationsMain.WINDOW_WIDTH;
	private static double height = applicationMain.FoundationsMain.WINDOW_HEIGHT;


	// These are the widget attributes for the GUI. There are 3 areas for this GUI.
	
	// GUI Area 1: It informs the user about the purpose of this page, whose account is being used,
	// and a button to allow this user to update the account settings
	protected static Label label_PageTitle = new Label();
	protected static Label label_UserDetails = new Label();
	protected static Button button_UpdateThisUser = new Button("Account Update");
		
	// This is a separator and it is used to partition the GUI for various tasks
	protected static Line line_Separator1 = new Line(20, 95, width-20, 95);

	// GUI Area 2: A panel that welcomes the user by name and describes what this role will be
	// able to do.  In later phases, this area will hold the widgets the user needs to play the
	// Viewer role.
	protected static Region panel_Welcome = new Region();
	protected static Label label_Welcome = new Label();
	protected static Label label_RoleDescription = new Label(
			"Viewers will be able to browse and read the content that Contributors share.");
	
	
	// This is a separator and it is used to partition the GUI for various tasks
	protected static Line line_Separator4 = new Line(20, 525, width-20,525);
	
	// GUI Area 3: This is last of the GUI areas.  It is used for quitting the application and for
	// logging out.
	protected static Button button_Logout = new Button("Logout");
	protected static Button button_Quit = new Button("Quit");

	// This is the end of the GUI objects for the page.
	
	// These attributes are used to configure the page and populate it with this user's information
	private static ViewViewerHome theView;		// Used to determine if instantiation of the class
												// is needed

	// Reference for the in-memory database so this package has access
	private static Database theDatabase = applicationMain.FoundationsMain.database;

	protected static Stage theStage;			// The Stage that JavaFX has established for us	
	protected static Pane theRootPane;			// The Pane that holds all the GUI widgets
	protected static User theUser;				// The current logged in User
	
	private static Scene theViewerHomeScene;		// The shared Scene each invocation populates
	protected static final int theRole = 3;		// Admin: 1; Contributor: 2; Viewer: 3

	/*-*******************************************************************************************

	Constructors
	
	 */

	/**********
	 * <p> Method: displayViewerHome(Stage ps, User user) </p>
	 * 
	 * <p> Description: This method is the single entry point from outside this package to cause
	 * the Viewer Home page to be displayed.
	 * 
	 * It first sets up every shared attributes so we don't have to pass parameters.
	 * 
	 * It then checks to see if the page has been setup.  If not, it instantiates the class, 
	 * initializes all the static aspects of the GIUI widgets (e.g., location on the page, font,
	 * size, and any methods to be performed).
	 * 
	 * After the instantiation, the code then populates the elements that change based on the user
	 * and the system's current state.  It then sets the Scene onto the stage, and makes it visible
	 * to the user.
	 * 
	 * @param ps specifies the JavaFX Stage to be used for this GUI and it's methods
	 * 
	 * @param user specifies the User for this GUI and it's methods
	 * 
	 */
	public static void displayViewerHome(Stage ps, User user) {
		
		// Establish the references to the GUI and the current user
		theStage = ps;
		theUser = user;
		
		// If not yet established, populate the static aspects of the GUI
		if (theView == null) theView = new ViewViewerHome();		// Instantiate singleton if needed
		
		// Populate the dynamic aspects of the GUI with the data from the user and the current
		// state of the system.
		theDatabase.getUserAccountDetails(user.getUserName());
		applicationMain.FoundationsMain.activeHomePage = theRole;
		
		label_UserDetails.setText("Signed in as " + theUser.getUserName());	// Set the username
		
		// Welcome the user by preferred first name, or by first name, or by username
		String name = theDatabase.getCurrentPreferredFirstName();
		if (name == null || name.length() == 0) name = theDatabase.getCurrentFirstName();
		if (name == null || name.length() == 0) name = theUser.getUserName();
		label_Welcome.setText("Welcome, " + name + "!");

		// Set the title for the window, display the page, and wait for the Admin to do something
		theStage.setTitle("Lessons Learned System: Viewer Home");
		theStage.setScene(theViewerHomeScene);						// Set this page onto the stage
		theStage.show();											// Display it to the user
	}
	
	/**********
	 * <p> Method: ViewViewerHome() </p>
	 * 
	 * <p> Description: This method initializes all the elements of the graphical user interface.
	 * This method determines the location, size, font, color, and change and event handlers for
	 * each GUI object. </p>
	 * 
	 * This is a singleton and is only performed once.  Subsequent uses fill in the changeable
	 * fields using the displayViewerHome method.</p>
	 * 
	 */
	private ViewViewerHome() {
		
		// Create the Pane for the list of widgets and the Scene for the window
		theRootPane = new Pane();
		theViewerHomeScene = new Scene(theRootPane, width, height);	// Create the scene
		
		// Set the title for the window
		
		// Populate the window with the title and other common widgets and set their static state
		
		// GUI Area 1
		label_PageTitle.setText("Viewer Home");
		setupLabelUI(label_PageTitle, "Arial", 28, 500, Pos.BASELINE_LEFT, 20, 12);
		label_PageTitle.getStyleClass().add("page-title");

		label_UserDetails.setText("Signed in as " + theUser.getUserName());
		setupLabelUI(label_UserDetails, "Arial", 14, 500, Pos.BASELINE_LEFT, 20, 60);
		label_UserDetails.getStyleClass().add("helper-text");
		
		setupButtonUI(button_UpdateThisUser, "Dialog", 18, 170, Pos.CENTER, 610, 30);
		button_UpdateThisUser.setOnAction((_) -> {ControllerViewerHome.performUpdate(); });
		
		// GUI Area 2
		panel_Welcome.getStyleClass().add("surface");
		panel_Welcome.setLayoutX(20);
		panel_Welcome.setLayoutY(120);
		panel_Welcome.setPrefSize(width-40, 150);
		
		setupLabelUI(label_Welcome, "Arial", 20, 700, Pos.BASELINE_LEFT, 45, 145);
		label_Welcome.getStyleClass().add("section-label");
		
		setupLabelUI(label_RoleDescription, "Arial", 16, 710, Pos.TOP_LEFT, 45, 190);
		label_RoleDescription.setMaxWidth(710);
		label_RoleDescription.setWrapText(true);
		label_RoleDescription.getStyleClass().add("subtitle");
		
		
		// GUI Area 3
        setupButtonUI(button_Logout, "Dialog", 18, 250, Pos.CENTER, 20, 540);
        button_Logout.setOnAction((_) -> {ControllerViewerHome.performLogout(); });
        
        setupButtonUI(button_Quit, "Dialog", 18, 250, Pos.CENTER, 300, 540);
        button_Quit.setOnAction((_) -> {ControllerViewerHome.performQuit(); });

		// This is the end of the GUI initialization code
		
		// Place all of the widget items into the Root Pane's list of children
        theRootPane.getChildren().addAll(
			label_PageTitle, label_UserDetails, button_UpdateThisUser, line_Separator1,
			panel_Welcome, label_Welcome, label_RoleDescription,
	        line_Separator4, button_Logout, button_Quit);
	}
	
	
	/*-********************************************************************************************

	Helper methods to reduce code length

	 */
	
	/**********
	 * Private local method to initialize the standard fields for a label
	 * 
	 * @param l		The Label object to be initialized
	 * @param ff	The font requested by the caller (Theme.FONT_FAMILY is used instead)
	 * @param f		The size of the font to be used
	 * @param w		The width of the Button
	 * @param p		The alignment (e.g. left, centered, or right)
	 * @param x		The location from the left edge (x axis)
	 * @param y		The location from the top (y axis)
	 */
	private static void setupLabelUI(Label l, String ff, double f, double w, Pos p, double x, 
			double y){
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
	private static void setupButtonUI(Button b, String ff, double f, double w, Pos p, double x, 
			double y){
		b.setFont(Font.font(applicationMain.Theme.FONT_FAMILY, f));
		b.setMinWidth(w);
		b.setAlignment(p);
		b.setLayoutX(x);
		b.setLayoutY(y);		
	}
}
