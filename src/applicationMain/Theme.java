package applicationMain;

import java.net.URL;

import javafx.collections.ListChangeListener;
import javafx.scene.Scene;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.Region;
import javafx.stage.Window;

/*******
 * <p> Title: Theme Class </p>
 *
 * <p> Description: This class applies the application's dark theme, defined in application.css,
 * to every window the application displays.  Rather than asking each page, modal window, Alert,
 * and TextInputDialog to load the stylesheet itself, install() watches the list of open windows
 * and adds the stylesheet to each window's Scene when the window opens and whenever that window
 * is given a new Scene (as the main Stage is each time the user moves to another page).</p>
 *
 * <p> The class also holds the one font family used throughout the application, so the View
 * classes' setup helpers all use the same family while keeping their own font sizes.</p>
 *
 */
public class Theme {

	/**
	 * The font family used by every page.  "System" is JavaFX's name for the operating system's
	 * standard sans-serif user interface font (Segoe UI on Windows, San Francisco on macOS).
	 */
	public static final String FONT_FAMILY = "System";

	// The location of the stylesheet, or null if it could not be found
	private static final URL STYLESHEET = Theme.class.getResource("application.css");

	/**
	 * Default constructor is not used.
	 */
	public Theme() {
	}

	/**********
	 * <p> Method: install() </p>
	 *
	 * <p> Description: This method is called once, when the application starts.  From then on,
	 * every window that opens has the stylesheet added to its Scene, including the windows
	 * created by Alert, TextInputDialog, and the modal list windows. </p>
	 *
	 */
	public static void install() {
		// Without the stylesheet the application still works, it just uses the default look
		if (STYLESHEET == null) {
			System.out.println("*** WARNING *** application.css was not found; using default look");
			return;
		}

		// Watch for windows opening and style each one, and every Scene it is later given
		Window.getWindows().addListener((ListChangeListener<Window>) change -> {
			while (change.next()) {
				for (Window window : change.getAddedSubList()) {
					apply(window.getScene());
					window.sceneProperty().addListener((observable, oldScene, newScene) -> apply(newScene));

					// A window sizes itself before it opens, using the default look.  Now that the
					// theme's padding and fonts apply, size it again so text such as a long alert
					// message is not cut off.  A dialog is also allowed to grow as tall as its
					// wrapped message needs.  (The pages' Scenes have a fixed size, so this does
					// not change the size of the main window.)
					if (window.getScene() != null) {
						if (window.getScene().getRoot() instanceof DialogPane pane)
							pane.setMinHeight(Region.USE_PREF_SIZE);
						window.getScene().getRoot().applyCss();
						window.sizeToScene();
					}
				}
			}
		});
	}

	/**********
	 * <p> Method: apply(Scene scene) </p>
	 *
	 * <p> Description: This method adds the stylesheet to the specified Scene, if it is not
	 * already there. </p>
	 *
	 * @param scene specifies the Scene to be styled (null is ignored)
	 */
	public static void apply(Scene scene) {
		if (scene == null || STYLESHEET == null) return;
		String sheet = STYLESHEET.toExternalForm();
		if (!scene.getStylesheets().contains(sheet)) scene.getStylesheets().add(sheet);
	}
}
