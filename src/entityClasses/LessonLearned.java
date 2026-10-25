package entityClasses;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/*******
 * <p> Title: LessonLearned Class. </p>
 *
 * <p> Description: This LessonLearned class represents one lesson learned that a Contributor
 * shares with the rest of the system.  It holds what happened, what was learned, a category,
 * optional advice and keywords, and the lock, curator, and coaching data the later epics need.
 * The database assigns the ID and timestamps, so a default constructor is kept for rebuilding a
 * lesson from a table row.</p>
 *
 * <p> Like the User class, this class only holds data.  The rules for each field are in
 * validators.LessonLearnedValidator so the GUI, the database, and the tests use one set of
 * rules.</p>
 *
 * <p> Operations: the getters and setters carry the lesson between the form, the validator, and
 * the database for the create, read, update, and delete stories (R1, R2, R3, R6).
 * setLessonLocked, setFieldLocked, and isFieldLocked hold the lock state the database checks
 * before an update or delete (R4, R5).  matchesKeyword decides whether the lesson belongs in a
 * search subset (R7).</p>
 *
 * <p> Planned for TP3: some attributes are stored and tested now but no TP2 page uses them yet.
 * lessonLocked, lockedFields, curatorEdited, and editedBy are for the Curator epic, where a
 * Curator reviews a lesson, locks what has been checked, and is credited for the edit.  summary and
 * rootCause are for the AI coaching epic: they are fields 8 and 7 in the team's AICoaching plan,
 * which proposes both for TP3.  The other six AICoaching fields are the TP2 attributes title,
 * situation, lessonText, recommendation, keywords, and category.  createdAt and updatedAt let a
 * Curator see which lessons are new or stale.  Adding them now means the TP3 pages will not need a
 * schema change that could lose TP2 data.</p>
 *
 * <p> Requirement map: R1/R2/R3/R6 core data, R4/R5 lock state, R7 keyword match, R9 curator
 * provenance, and R10 future coaching attributes.</p>
 *
 * <p> Story references: Contributor stories 1 to 4 are the four Contributor (Phase Two) stories
 * in Lessons Learned System V1 (2026-08-19), in order: 1 add lessons with core information, 2
 * see my list and examine a lesson and its locks, 3 add experience information, and 4 see only
 * my own content.  US numbers are the team's own user stories, as used in the AICoaching
 * plan.</p>
 *
 * <p> Copyright: Yug Thapar, Ishaan Arekar, Avishek Lodh, Shreetej Hadge © 2026 </p>
 *
 * @author Yug Thapar, Ishaan Arekar, Avishek Lodh, Shreetej Hadge
 *
 * @version 1.00		2026-10-24 Initial version for TP2
 * @version 1.01		2026-10-25 Foundations-style header; source and rationale for each attribute
 *
 */
public class LessonLearned {
	/** Database identifier, or -1 until the lesson has been persisted.  Source: R2 and R11;
	 * Contributor story 2 (pick a lesson from my list to examine).  Rationale: update, delete, and
	 * experience records need a key that does not change when the title is edited. */
	private int lessonID = -1;
	/** Username of the Contributor who owns this lesson.  Source: R1 and R12; Contributor story 4
	 * (I can only see the content I have added).  Rationale: the database compares it with the
	 * signed-in user so a Contributor sees and changes only their own lessons. */
	private String contributorUserName;
	/** Short title that identifies the lesson.  Source: R1; Contributor story 1 (core
	 * information); US8 and US13; AICoaching field 4, Title.  Rationale: the short, searchable
	 * name other students scan in the list, and it decides whether a lesson gets opened. */
	private String title;
	/** Category chosen from LessonLearnedValidator.CATEGORIES.  Source: R1; Contributor story 1
	 * (core information); US9; AICoaching field 6, Category.  Rationale: a fixed list groups
	 * similar lessons and stops "Testing" and "testing" from becoming two categories. */
	private String category;
	/** What happened that produced the lesson; shown on the form as What Happened.  Source: R1;
	 * Contributor story 1 (core information); US10; AICoaching field 2, What Happened.
	 * Rationale: without the situation, a reader cannot tell whether the lesson applies to
	 * them. */
	private String situation;
	/** What the Contributor learned; shown on the form as What I Learned.  Source: R1;
	 * Contributor story 1 (core information); US10; AICoaching field 1, What I Learned.
	 * Rationale: this is the lesson itself and the main thing other students come to read. */
	private String lessonText;
	/** Optional advice for what to do next time; shown on the form as Next Time.  Source: R1;
	 * Contributor story 1 (core information); US11; AICoaching field 3, Next Time.  Rationale:
	 * turns a story into an action someone can take.  Optional because not every lesson has a
	 * clear fix. */
	private String recommendation;
	/** Comma-separated search terms.  Source: R7; Contributor story 1 (core information); US4
	 * and US11; AICoaching field 5, Keywords; Viewer keyword search story (Phase 3).  Rationale:
	 * decides whether a search can find the lesson at all, even by a word that is not in its
	 * title or text. */
	private String keywords;
	/** When the lesson row was created; set by the database.  Source: R11; Contributor story 1
	 * (see and manage it).  Rationale: shows how old a lesson is, and a TP3 Curator can use it to
	 * find new lessons to review. */
	private Timestamp createdAt;
	/** When the lesson row was last updated; set by the database.  Source: R3 and R11;
	 * Contributor story 1 (see and manage it).  Rationale: shows that an update was saved and lets
	 * a Curator spot lessons that changed after review. */
	private Timestamp updatedAt;
	/** True when the whole lesson is locked against Contributor changes.  Source: R4 and R5;
	 * Contributor stories 1 and 2 (a locked lesson cannot be deleted); Curator epic (lock entire
	 * lessons).  Rationale: a Curator can freeze a reviewed lesson so it is not edited or deleted
	 * afterward. */
	private boolean lessonLocked;
	/** Names of the individual fields that are locked.  Source: R4 and R5; Contributor stories 1
	 * and 2 (see which fields are locked and unavailable to edit); Curator epic (lock specific
	 * fields).  Rationale: a Curator can protect the parts that were checked while the
	 * Contributor still edits the rest. */
	private final Set<String> lockedFields = new HashSet<>();
	/** True when a Curator has edited the lesson.  Source: R9; Contributor story 4 (content I
	 * added after a curator has edited it); Curator epic (edit and refine lesson details).
	 * Rationale: readers and the Contributor can tell the text is no longer exactly what the
	 * Contributor wrote. */
	private boolean curatorEdited;
	/** Username of the Curator who last edited the lesson, or null.  Source: R9; Contributor
	 * story 4; Curator epic (edit and refine lesson details).  Rationale: records who is
	 * responsible for a curated change. */
	private String editedBy;
	/** Optional one-sentence summary, at most 200 characters.  Source: R10; Contributor Phase
	 * Three epic (AI-based writing tools); AICoaching field 8, One-line Summary (proposed for
	 * TP3).  Rationale: lets viewers scan a long list of lessons.  It is written once and stored,
	 * not made again every time the lesson is viewed. */
	private String summary;
	/** Optional root-cause description, at most 1000 characters.  Source: R10; Contributor Phase
	 * Three epic (AI-based writing tools); AICoaching field 7, Root Cause (proposed for TP3).
	 * Rationale: separates why something happened from what happened, so the lesson deals with
	 * the cause and not only the symptom. */
	private String rootCause;
	/** Field names that may be locked one at a time.  Source: R4; Curator epic (lock specific
	 * fields).  Rationale: one list that both the setters and the database check against, so an
	 * unknown name is rejected. */
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
