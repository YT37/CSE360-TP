module CSE360TP {
	requires javafx.controls;
	requires java.sql;

	opens applicationMain to javafx.graphics, javafx.fxml;
}
