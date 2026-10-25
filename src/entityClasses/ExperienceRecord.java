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
	private int experienceID = -1;
	private int lessonID = -1;
	private String contributorUserName;
	private String whatWasDone;
	private String how;
	private double durationHours;
	private final Map<String, Double> effortHoursByMember = new LinkedHashMap<>();
	private Timestamp createdAt;
	private Timestamp updatedAt;

	/** Creates an empty object for Database to populate from stored rows. */
	public ExperienceRecord() { }

	/** Creates a new experience record for an existing lesson. */
	public ExperienceRecord(int lessonID, String contributorUserName, String whatWasDone, String how,
			double durationHours, Map<String, Double> effortHoursByMember) {
		this.lessonID = lessonID;
		this.contributorUserName = contributorUserName;
		this.whatWasDone = whatWasDone;
		this.how = how;
		this.durationHours = durationHours;
		setEffortHoursByMember(effortHoursByMember);
	}

	public int getExperienceID() { return experienceID; }
	public void setExperienceID(int value) { experienceID = value; }
	public int getLessonID() { return lessonID; }
	public void setLessonID(int value) { lessonID = value; }
	public String getContributorUserName() { return contributorUserName; }
	public void setContributorUserName(String value) { contributorUserName = value; }
	public String getWhatWasDone() { return whatWasDone; }
	public void setWhatWasDone(String value) { whatWasDone = value; }
	public String getHow() { return how; }
	public void setHow(String value) { how = value; }
	public double getDurationHours() { return durationHours; }
	public void setDurationHours(double value) { durationHours = value; }
	public Timestamp getCreatedAt() { return createdAt; }
	public void setCreatedAt(Timestamp value) { createdAt = value; }
	public Timestamp getUpdatedAt() { return updatedAt; }
	public void setUpdatedAt(Timestamp value) { updatedAt = value; }

	/** Returns team-member names mapped to their effort in hours as a read-only map. */
	public Map<String, Double> getEffortHoursByMember() {
		return Collections.unmodifiableMap(effortHoursByMember);
	}

	/** Replaces the per-member effort entries. */
	public void setEffortHoursByMember(Map<String, Double> values) {
		effortHoursByMember.clear();
		if (values != null) effortHoursByMember.putAll(values);
	}

	@Override
	public String toString() {
		return "#" + experienceID + " - " + durationHours + " hours";
	}
}
