package entityClasses;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>Title: LessonLearned Class.</p>
 * <p>Description: Represents the core information in one persistent lesson learned. The database
 * assigns the ID and timestamps, so a default constructor is retained for database reconstruction.</p>
 * <p>Validation is intentionally kept in LessonLearnedValidator so the GUI, database, and tests
 * use one set of rules.</p>
 * <p>Requirement map: R1/R2/R3/R6 core data, R4/R5 lock state, R7 keyword match, R9 curator
 * provenance, and R10 future coaching attributes.</p>
 */
public class LessonLearned {
	private int lessonID = -1;
	private String contributorUserName;
	private String title;
	private String category;
	private String situation;
	private String lessonText;
	private String recommendation;
	private String keywords;
	private Timestamp createdAt;
	private Timestamp updatedAt;
	private boolean lessonLocked;
	private final Set<String> lockedFields = new HashSet<>();
	private boolean curatorEdited;
	private String editedBy;
	private String summary;
	private String rootCause;
	private static final Set<String> LOCKABLE_FIELDS = Set.of("title", "category", "situation",
			"lessonText", "recommendation", "keywords");

	/** Creates an empty object for Database to populate from a row. */
	public LessonLearned() { }

	/** Creates a new, not-yet-persisted lesson. */
	public LessonLearned(String contributorUserName, String title, String category, String situation,
			String lessonText, String recommendation, String keywords) {
		this.contributorUserName = contributorUserName;
		this.title = title;
		this.category = category;
		this.situation = situation;
		this.lessonText = lessonText;
		this.recommendation = recommendation;
		this.keywords = keywords;
	}

	/** Returns the database ID, or -1 before persistence. */
	public int getLessonID() { return lessonID; }
	public void setLessonID(int lessonID) { this.lessonID = lessonID; }
	public String getContributorUserName() { return contributorUserName; }
	public void setContributorUserName(String value) { contributorUserName = value; }
	public String getTitle() { return title; }
	public void setTitle(String value) { title = value; }
	public String getCategory() { return category; }
	public void setCategory(String value) { category = value; }
	public String getSituation() { return situation; }
	public void setSituation(String value) { situation = value; }
	public String getLessonText() { return lessonText; }
	public void setLessonText(String value) { lessonText = value; }
	public String getRecommendation() { return recommendation; }
	public void setRecommendation(String value) { recommendation = value; }
	public String getKeywords() { return keywords; }
	public void setKeywords(String value) { keywords = value; }
	public Timestamp getCreatedAt() { return createdAt; }
	public void setCreatedAt(Timestamp value) { createdAt = value; }
	public Timestamp getUpdatedAt() { return updatedAt; }
	public void setUpdatedAt(Timestamp value) { updatedAt = value; }

	/** Returns whether the complete lesson is locked against Contributor changes (R4). */
	public boolean isLessonLocked() { return lessonLocked; }

	/** Sets the complete-lesson lock independently of individual field locks (R4). */
	public void setLessonLocked(boolean locked) { lessonLocked = locked; }

	/** Returns whether a supported field is locked individually or by the whole-lesson lock (R4).
	 *
	 * @param field one of title, category, situation, lessonText, recommendation, or keywords
	 * @return true when the field cannot be changed by the Contributor
	 * @throws IllegalArgumentException if field is not a lockable lesson field
	 */
	public boolean isFieldLocked(String field) {
		validateLockableField(field);
		return lessonLocked || lockedFields.contains(field);
	}

	/** Sets or clears the independent lock for one supported field (R4).
	 *
	 * @param field one of title, category, situation, lessonText, recommendation, or keywords
	 * @param locked true to lock the field; false to unlock it
	 * @throws IllegalArgumentException if field is not a lockable lesson field
	 */
	public void setFieldLocked(String field, boolean locked) {
		validateLockableField(field);
		if (locked) lockedFields.add(field);
		else lockedFields.remove(field);
	}

	/** Returns the individual field locks, excluding a whole-lesson lock, as a read-only set. */
	public Set<String> getLockedFields() {
		return Collections.unmodifiableSet(lockedFields);
	}

	/** Returns whether a Curator has edited this lesson (R9). */
	public boolean isCuratorEdited() { return curatorEdited; }

	/** Sets whether a Curator has edited this lesson (R9). */
	public void setCuratorEdited(boolean value) { curatorEdited = value; }

	/** Returns the username recorded as the Curator editor, or null when none is recorded. */
	public String getEditedBy() { return editedBy; }

	/** Records the username of the Curator who edited this lesson. */
	public void setEditedBy(String value) { editedBy = value; }

	/** Returns the optional short summary used by future coaching features (R10). */
	public String getSummary() { return summary; }

	/** Sets the optional short summary used by future coaching features (R10). */
	public void setSummary(String value) { summary = value; }

	/** Returns the optional root-cause description used by future coaching features (R10). */
	public String getRootCause() { return rootCause; }

	/** Sets the optional root-cause description used by future coaching features (R10). */
	public void setRootCause(String value) { rootCause = value; }

	private static void validateLockableField(String field) {
		if (field == null || !LOCKABLE_FIELDS.contains(field))
			throw new IllegalArgumentException("Unknown lockable lesson field: " + field);
	}

	/** R7: returns true when a nonblank search term occurs in any searchable lesson field. */
	public boolean matchesKeyword(String searchText) {
		if (searchText == null || searchText.trim().isEmpty()) return false;
		String search = searchText.trim().toLowerCase();
		return contains(title, search) || contains(category, search) || contains(situation, search)
				|| contains(lessonText, search) || contains(recommendation, search) || contains(keywords, search);
	}

	private static boolean contains(String value, String search) {
		return value != null && value.toLowerCase().contains(search);
	}

	@Override
	public String toString() {
		return "#" + lessonID + "  " + title;
	}
}
