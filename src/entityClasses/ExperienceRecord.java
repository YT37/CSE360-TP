package entityClasses;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents one Contributor's attempt to apply a lesson and the time and team effort it used (R8).
 * Database assigns the experience ID and timestamps when the record is persisted.
 */
public class ExperienceRecord {
	/** Database identifier, or -1 until the record has been persisted. */
	private int experienceID = -1;
	/** ID of the lesson this experience belongs to. */
	private int lessonID = -1;
	/** Username of the Contributor who recorded the experience. */
	private String contributorUserName;
	/** Description of the work that was performed. */
	private String whatWasDone;
	/** Description of how the work was performed. */
	private String how;
	/** Elapsed time for the attempt, in hours. */
	private double durationHours;
	/** Team-member names mapped to their effort in hours. */
	private final Map<String, Double> effortHoursByMember = new LinkedHashMap<>();
	/** Timestamp assigned when the record was created. */
	private Timestamp createdAt;
	/** Timestamp assigned when the record was last updated. */
	private Timestamp updatedAt;

	/** Creates an empty object for Database to populate from stored rows. */
	public ExperienceRecord() { }

	/** Creates a new experience record for an existing lesson.
	 *
	 * @param lessonID ID of the lesson this experience belongs to
	 * @param contributorUserName username of the Contributor recording the experience
	 * @param whatWasDone description of the work that was performed
	 * @param how description of how the work was performed
	 * @param durationHours elapsed time in hours
	 * @param effortHoursByMember team-member names mapped to their effort hours
	 */
	public ExperienceRecord(int lessonID, String contributorUserName, String whatWasDone, String how,
			double durationHours, Map<String, Double> effortHoursByMember) {
		this.lessonID = lessonID;
		this.contributorUserName = contributorUserName;
		this.whatWasDone = whatWasDone;
		this.how = how;
		this.durationHours = durationHours;
		setEffortHoursByMember(effortHoursByMember);
	}

	/** Returns the database ID, or -1 before persistence.
	 *
	 * @return the persistent experience ID, or -1 when not persisted
	 */
	public int getExperienceID() { return experienceID; }

	/** Sets the database ID after persistence.
	 *
	 * @param value the persistent experience ID
	 */
	public void setExperienceID(int value) { experienceID = value; }

	/** Returns the ID of the related lesson.
	 *
	 * @return the lesson ID
	 */
	public int getLessonID() { return lessonID; }

	/** Sets the ID of the related lesson.
	 *
	 * @param value the lesson ID
	 */
	public void setLessonID(int value) { lessonID = value; }

	/** Returns the username of the Contributor who recorded the experience.
	 *
	 * @return the Contributor's username
	 */
	public String getContributorUserName() { return contributorUserName; }

	/** Sets the username of the Contributor who recorded the experience.
	 *
	 * @param value the Contributor's username
	 */
	public void setContributorUserName(String value) { contributorUserName = value; }

	/** Returns the description of the work performed.
	 *
	 * @return the work description
	 */
	public String getWhatWasDone() { return whatWasDone; }

	/** Sets the description of the work performed.
	 *
	 * @param value the work description
	 */
	public void setWhatWasDone(String value) { whatWasDone = value; }

	/** Returns the description of how the work was performed.
	 *
	 * @return the method description
	 */
	public String getHow() { return how; }

	/** Sets the description of how the work was performed.
	 *
	 * @param value the method description
	 */
	public void setHow(String value) { how = value; }

	/** Returns the elapsed time for the attempt in hours.
	 *
	 * @return the duration in hours
	 */
	public double getDurationHours() { return durationHours; }

	/** Sets the elapsed time for the attempt in hours.
	 *
	 * @param value the duration in hours
	 */
	public void setDurationHours(double value) { durationHours = value; }

	/** Returns when the record was created.
	 *
	 * @return the creation timestamp, or null before persistence
	 */
	public Timestamp getCreatedAt() { return createdAt; }

	/** Sets the creation timestamp.
	 *
	 * @param value the creation timestamp
	 */
	public void setCreatedAt(Timestamp value) { createdAt = value; }

	/** Returns when the record was last updated.
	 *
	 * @return the update timestamp, or null before an update
	 */
	public Timestamp getUpdatedAt() { return updatedAt; }

	/** Sets the last-update timestamp.
	 *
	 * @param value the update timestamp
	 */
	public void setUpdatedAt(Timestamp value) { updatedAt = value; }

	/** Returns team-member names mapped to their effort in hours as a read-only map.
	 *
	 * @return an unmodifiable map of member names to effort hours
	 */
	public Map<String, Double> getEffortHoursByMember() {
		return Collections.unmodifiableMap(effortHoursByMember);
	}

	/** Replaces the per-member effort entries.
	 *
	 * @param values member names mapped to effort hours; null clears the entries
	 */
	public void setEffortHoursByMember(Map<String, Double> values) {
		effortHoursByMember.clear();
		if (values != null) effortHoursByMember.putAll(values);
	}

	@Override
	public String toString() {
		return "#" + experienceID + " - " + durationHours + " hours";
	}
}
