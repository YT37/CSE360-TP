package entityClasses;

import java.sql.Timestamp;

/**
 * <p>Title: LessonLearned Class.</p>
 * <p>Description: Represents the core information in one persistent lesson learned. The database
 * assigns the ID and timestamps, so a default constructor is retained for database reconstruction.</p>
 * <p>Validation is intentionally kept in LessonLearnedValidator so the GUI, database, and tests
 * use one set of rules.</p>
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

	/** Returns true when a nonblank search term occurs in any searchable lesson field. */
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
