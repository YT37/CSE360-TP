package guiExperience;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import entityClasses.ExperienceRecord;
import javafx.collections.FXCollections;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import validators.LessonLearnedValidator;

/*******
 * <p> Title: ControllerExperience Class. </p>
 *
 * <p> Description: The Java/FX-based Experience Records Page.  This class provides the
 * controller actions based on the user's use of the JavaFX GUI widgets defined by the View
 * class.  The database repeats the validation and ownership checks, so this controller cannot
 * bypass them.</p>
 *
 * <p> Operations: performAddEffort and performRemoveEffort build the per-member effort list.
 * performAdd checks the form with LessonLearnedValidator.checkExperienceRecord and then calls
 * Database.createExperienceRecord (R8, R14).  refreshList and performSelect read and show only the
 * signed-in Contributor's records (R8, R12).  performDelete asks for confirmation and then calls
 * Database.deleteExperienceRecord, which checks the owner (R8, R12).</p>
 *
 * <p> The methods are protected static and are called only by the View.  Do not change them to
 * public.</p>
 *
 * <p> Copyright: Yug Thapar, Ishaan Arekar, Avishek Lodh, Shreetej Hadge © 2026 </p>
 *
 * @author Yug Thapar, Ishaan Arekar, Avishek Lodh, Shreetej Hadge
 *
 * @version 1.00		2026-10-24 Initial version for TP2
 * @version 1.01		2026-10-25 Foundations-style header; source and rationale for the attribute
 *
 */
public class ControllerExperience {
	/** True while the list is being rebuilt.  Source: R8.  Rationale: rebuilding the ListView
	 * fires selection events, and this flag stops them from loading a record into the form by
	 * mistake. */
	private static boolean rebuildingList;

	/** Creates the controller; all actions are invoked statically by the view. */
	public ControllerExperience() { }

	/** R8/R12: loads only the signed-in Contributor's records for the selected lesson. */
	protected static void refreshList() {
		ModelExperience.experiences = ViewExperience.theDatabase.readExperienceRecordsForLesson(
				ModelExperience.lesson.getLessonID(), ViewExperience.theUser.getUserName());
		rebuildingList = true;
		ViewExperience.listView_Experiences.getItems().setAll(ModelExperience.experiences);
		rebuildingList = false;
		if (!ViewExperience.theDatabase.getLastExperienceError().isEmpty())
			showMessage(ViewExperience.theDatabase.getLastExperienceError(), true);
	}

	/** R8: loads a selected experience record into the form.
	 *
	 * @param experience the selected record, or null when the selection was cleared
	 */
	protected static void performSelect(ExperienceRecord experience) {
		if (experience == null || rebuildingList) return;
		ModelExperience.selectedExperience = experience;
		ViewExperience.text_WhatWasDone.setText(experience.getWhatWasDone());
		ViewExperience.text_How.setText(experience.getHow());
		ViewExperience.text_DurationHours.setText(Double.toString(experience.getDurationHours()));
		ModelExperience.effortHoursByMember.clear();
		ModelExperience.effortHoursByMember.putAll(experience.getEffortHoursByMember());
		refreshEffortList();
		ViewExperience.setEditing(false);
		showMessage("Experience #" + experience.getExperienceID() + " selected.", false);
	}

	/** Clears the form so the Contributor can enter a new experience record. */
	protected static void performNew() {
		ModelExperience.selectedExperience = null;
		ViewExperience.listView_Experiences.getSelectionModel().clearSelection();
		ViewExperience.text_WhatWasDone.clear();
		ViewExperience.text_How.clear();
		ViewExperience.text_DurationHours.clear();
		ViewExperience.text_TeamMember.clear();
		ViewExperience.text_EffortHours.clear();
		ModelExperience.effortHoursByMember.clear();
		refreshEffortList();
		ViewExperience.setEditing(true);
		showMessage("Enter the experience details and team-member effort.", false);
	}

	/** Adds or replaces one team member's effort entry in the form. */
	protected static void performAddEffort() {
		String member = ViewExperience.text_TeamMember.getText().trim();
		if (member.isEmpty()) {
			showMessage("Enter a team member name.", true);
			return;
		}
		double hours;
		try {
			hours = Double.parseDouble(ViewExperience.text_EffortHours.getText().trim());
		} catch (NumberFormatException e) {
			showMessage("Enter a valid number of effort hours.", true);
			return;
		}
		if (!Double.isFinite(hours) || hours <= 0) {
			showMessage("Effort hours must be greater than 0.", true);
			return;
		}
		ModelExperience.effortHoursByMember.put(member, hours);
		refreshEffortList();
		ViewExperience.text_TeamMember.clear();
		ViewExperience.text_EffortHours.clear();
		showMessage("Effort saved for " + member + ".", false);
	}

	/** Removes the effort entry currently selected in the effort list. */
	protected static void performRemoveEffort() {
		int selectedIndex = ViewExperience.listView_Efforts.getSelectionModel().getSelectedIndex();
		if (selectedIndex < 0) {
			showMessage("Select an effort entry to remove.", true);
			return;
		}
		String member = new ArrayList<>(ModelExperience.effortHoursByMember.keySet()).get(selectedIndex);
		ModelExperience.effortHoursByMember.remove(member);
		refreshEffortList();
	}

	/** R8/R14: validates descriptions, duration, and effort before asking the database to store. */
	protected static void performAdd() {
		double durationHours;
		try {
			durationHours = Double.parseDouble(ViewExperience.text_DurationHours.getText().trim());
		} catch (NumberFormatException e) {
			showMessage("Duration must be a number of hours.", true);
			return;
		}
		ExperienceRecord experience = new ExperienceRecord(ModelExperience.lesson.getLessonID(),
				ViewExperience.theUser.getUserName(), ViewExperience.text_WhatWasDone.getText(),
				ViewExperience.text_How.getText(), durationHours, ModelExperience.effortHoursByMember);
		String errors = LessonLearnedValidator.checkExperienceRecord(experience.getWhatWasDone(),
				experience.getHow(), experience.getDurationHours(), experience.getEffortHoursByMember());
		if (!errors.isEmpty()) {
			showMessage(errors, true);
			return;
		}
		String result = ViewExperience.theDatabase.createExperienceRecord(experience,
				ViewExperience.theUser.getUserName());
		if (!result.isEmpty()) {
			showMessage(result, true);
			return;
		}
		refreshList();
		select(experience.getExperienceID());
		showMessage("Experience #" + experience.getExperienceID() + " was added.", false);
	}

	/** R8/R12: confirms deletion and lets the database verify the owner before removing. */
	protected static void performDelete() {
		ExperienceRecord selected = ModelExperience.selectedExperience;
		if (selected == null) {
			showMessage("Select an experience record before deleting.", true);
			return;
		}
		Alert confirmation = new Alert(AlertType.CONFIRMATION);
		confirmation.setTitle("Delete Experience");
		confirmation.setHeaderText("Delete experience #" + selected.getExperienceID() + "?");
		confirmation.setContentText("This action cannot be undone.");
		Optional<ButtonType> answer = confirmation.showAndWait();
		if (answer.isEmpty() || answer.get() != ButtonType.OK) return;
		String result = ViewExperience.theDatabase.deleteExperienceRecord(selected.getExperienceID(),
				ViewExperience.theUser.getUserName());
		if (!result.isEmpty()) {
			showMessage(result, true);
			return;
		}
		refreshList();
		performNew();
		showMessage("Experience record was deleted.", false);
	}

	/** Returns to the Lessons Learned page. */
	protected static void performReturn() {
		guiLessonsLearned.ViewLessonsLearned.displayLessonsLearned(ViewExperience.theStage,
				ViewExperience.theUser);
	}

	/** Logs out and returns to the login page. */
	protected static void performLogout() {
		guiUserLogin.ViewUserLogin.displayUserLogin(ViewExperience.theStage);
	}

	/** Exits the application. */
	protected static void performQuit() { System.exit(0); }

	/** Rebuilds the displayed effort entries from the model's map. */
	private static void refreshEffortList() {
		List<String> entries = new ArrayList<>();
		for (Map.Entry<String, Double> effort : ModelExperience.effortHoursByMember.entrySet())
			entries.add(effort.getKey() + " — " + effort.getValue() + " hours");
		ViewExperience.listView_Efforts.getItems().setAll(FXCollections.observableArrayList(entries));
	}

	/** Selects a persisted experience record in the list by ID.
	 *
	 * @param experienceID ID of the record to select
	 */
	private static void select(int experienceID) {
		for (ExperienceRecord experience : ModelExperience.experiences) {
			if (experience.getExperienceID() == experienceID) {
				ViewExperience.listView_Experiences.getSelectionModel().select(experience);
				return;
			}
		}
	}

	/** Displays a status or error message using the matching style class.
	 *
	 * @param message the message to display
	 * @param error true for an error style, false for success
	 */
	private static void showMessage(String message, boolean error) {
		ViewExperience.label_Message.setText(message);
		ViewExperience.label_Message.getStyleClass().removeAll("error-text", "success-text");
		ViewExperience.label_Message.getStyleClass().add(error ? "error-text" : "success-text");
	}
}
