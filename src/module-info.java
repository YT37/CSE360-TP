/** JavaFX and JDBC module requirements for the Lessons Learned application. */
module CSE360TP {
	requires javafx.controls;
	requires java.sql;

	opens applicationMain to javafx.graphics, javafx.fxml;
}
