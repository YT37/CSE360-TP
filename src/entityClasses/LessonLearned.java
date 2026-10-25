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
	/** Database identifier, or -1 until the lesson has been persisted. */
	private int lessonID = -1;
	/** Username of the Contributor who owns this lesson. */
	private String contributorUserName;
	/** Short title that identifies the lesson. */
	private String title;
	/** Category selected from the validator's supported category list. */
	private String category;
	/** Description of the situation that produced the lesson. */
	private String situation;
	/** Description of what the Contributor learned. */
	private String lessonText;
	/** Optional advice for what to do next time. */
	private String recommendation;
	/** Comma-separated search terms associated with the lesson. */
	private String keywords;
	/** Timestamp assigned when the lesson row was created. */
	private Timestamp createdAt;
	/** Timestamp assigned when the lesson row was last updated. */
	private Timestamp updatedAt;
	/** True when the whole lesson is locked against Contributor changes. */
	private boolean lessonLocked;
	/** Names of individual fields that are locked. */
	private final Set<String> lockedFields = new HashSet<>();
	/** True when a Curator has edited the lesson. */
	private boolean curatorEdited;
	/** Username of the Curator who last edited the lesson, or null. */
	private String editedBy;
	/** Optional short coaching summary. */
	private String summary;
	/** Optional coaching root-cause description. */
	private String rootCause;
	/** Field names that may be locked individually. */
	private static final Set<String> LOCKABLE_FIELDS = Set.of("title", "category", "situation",
			"lessonText", "recommendation", "keywords");

	/** Creates an empty object for Database to populate from a row. */
	public LessonLearned() { }

	/** Creates a new, not-yet-persisted lesson.
	 *
	 * @param contributorUserName username of the owning Contributor
	 * @param title short lesson title
	 * @param category one of the validator's supported categories
	 * @param situation description of the situation that produced the lesson
	 * @param lessonText description of what was learned
	 * @param recommendation optional advice for next time
	 * @param keywords optional comma-separated search terms
	 */
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

	/** Returns the database ID, or -1 before persistence.
	 *
	 * @return the persistent lesson ID, or -1 when not persisted
	 */
	public int getLessonID() { return lessonID; }

	/** Sets the database ID after persistence.
	 *
	 * @param lessonID the persistent lesson ID
	 */
	public void setLessonID(int lessonID) { this.lessonID = lessonID; }

	/** Returns the username of the Contributor who owns the lesson.
	 *
	 * @return the owner's username
	 */
	public String getContributorUserName() { return contributorUserName; }

	/** Sets the username of the Contributor who owns the lesson.
	 *
	 * @param value the owner's username
	 */
	public void setContributorUserName(String value) { contributorUserName = value; }

	/** Returns the lesson title.
	 *
	 * @return the title
	 */
	public String getTitle() { return title; }

	/** Sets the lesson title.
	 *
	 * @param value the title
	 */
	public void setTitle(String value) { title = value; }

	/** Returns the lesson category.
	 *
	 * @return the category
	 */
	public String getCategory() { return category; }

	/** Sets the lesson category.
	 *
	 * @param value the category
	 */
	public void setCategory(String value) { category = value; }

	/** Returns the situation description.
	 *
	 * @return the situation text
	 */
	public String getSituation() { return situation; }

	/** Sets the situation description.
	 *
	 * @param value the situation text
	 */
	public void setSituation(String value) { situation = value; }

	/** Returns the lesson text.
	 *
	 * @return what was learned
	 */
	public String getLessonText() { return lessonText; }

	/** Sets the lesson text.
	 *
	 * @param value what was learned
	 */
	public void setLessonText(String value) { lessonText = value; }

	/** Returns the recommendation.
	 *
	 * @return the recommendation text, possibly null
	 */
	public String getRecommendation() { return recommendation; }

	/** Sets the recommendation.
	 *
	 * @param value the recommendation text
	 */
	public void setRecommendation(String value) { recommendation = value; }

	/** Returns the comma-separated keywords.
	 *
	 * @return the keywords text, possibly null
	 */
	public String getKeywords() { return keywords; }

	/** Sets the comma-separated keywords.
	 *
	 * @param value the keywords text
	 */
	public void setKeywords(String value) { keywords = value; }

	/** Returns when the lesson was created.
	 *
	 * @return the creation timestamp, or null before persistence
	 */
	public Timestamp getCreatedAt() { return createdAt; }

	/** Sets the creation timestamp.
	 *
	 * @param value the creation timestamp
	 */
	public void setCreatedAt(Timestamp value) { createdAt = value; }

	/** Returns when the lesson was last updated.
	 *
	 * @return the update timestamp, or null before an update
	 */
	public Timestamp getUpdatedAt() { return updatedAt; }

	/** Sets the last-update timestamp.
	 *
	 * @param value the update timestamp
	 */
	public void setUpdatedAt(Timestamp value) { updatedAt = value; }

	/** Returns whether the complete lesson is locked against Contributor changes (R4).
	 *
	 * @return true when the whole lesson is locked
	 */
	public boolean isLessonLocked() { return lessonLocked; }

	/** Sets the complete-lesson lock independently of individual field locks (R4).
	 *
	 * @param locked true to lock the whole lesson; false to unlock it
	 */
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

	/** Returns the individual field locks, excluding a whole-lesson lock, as a read-only set.
	 *
	 * @return an unmodifiable set of locked field names
	 */
	public Set<String> getLockedFields() {
		return Collections.unmodifiableSet(lockedFields);
	}

	/** Returns whether a Curator has edited this lesson (R9).
	 *
	 * @return true when a Curator edited the lesson
	 */
	public boolean isCuratorEdited() { return curatorEdited; }

	/** Sets whether a Curator has edited this lesson (R9).
	 *
	 * @param value true when a Curator edited the lesson
	 */
	public void setCuratorEdited(boolean value) { curatorEdited = value; }

	/** Returns the username recorded as the Curator editor, or null when none is recorded.
	 *
	 * @return the Curator editor's username, or null
	 */
	public String getEditedBy() { return editedBy; }

	/** Records the username of the Curator who edited this lesson.
	 *
	 * @param value the Curator editor's username
	 */
	public void setEditedBy(String value) { editedBy = value; }

	/** Returns the optional short summary used by future coaching features (R10).
	 *
	 * @return the summary, or null when not provided
	 */
	public String getSummary() { return summary; }

	/** Sets the optional short summary used by future coaching features (R10).
	 *
	 * @param value the summary
	 */
	public void setSummary(String value) { summary = value; }

	/** Returns the optional root-cause description used by future coaching features (R10).
	 *
	 * @return the root cause, or null when not provided
	 */
	public String getRootCause() { return rootCause; }

	/** Sets the optional root-cause description used by future coaching features (R10).
	 *
	 * @param value the root cause
	 */
	public void setRootCause(String value) { rootCause = value; }

	/** Validates that a field name is lockable.
	 *
	 * @param field the field name to validate
	 * @throws IllegalArgumentException if the field is not lockable
	 */
	private static void validateLockableField(String field) {
		if (field == null || !LOCKABLE_FIELDS.contains(field))
			throw new IllegalArgumentException("Unknown lockable lesson field: " + field);
	}

	/** R7: returns true when a nonblank search term occurs in any searchable lesson field.
	 *
	 * @param searchText text to search for; null or blank returns false
	 * @return true when any searchable field contains the text
	 */
	public boolean matchesKeyword(String searchText) {
		if (searchText == null || searchText.trim().isEmpty()) return false;
		String search = searchText.trim().toLowerCase();
		return contains(title, search) || contains(category, search) || contains(situation, search)
				|| contains(lessonText, search) || contains(recommendation, search) || contains(keywords, search);
	}

	/** Case-insensitive containment check that tolerates null field values.
	 *
	 * @param value the field value, possibly null
	 * @param search the lower-case search text
	 * @return true when value contains search
	 */
	private static boolean contains(String value, String search) {
		return value != null && value.toLowerCase().contains(search);
	}

	@Override
	public String toString() {
		return "#" + lessonID + "  " + title;
	}
}
