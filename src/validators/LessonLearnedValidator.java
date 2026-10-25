package validators;

import java.util.Map;

import entityClasses.LessonLearned;

/**
 * <p>Title: LessonLearnedValidator Class.</p>
 * <p>Description: Validates core lesson information for both the GUI and database layer. Keeping
 * the checks here prevents a future caller from bypassing GUI validation.</p>
 */
public class LessonLearnedValidator {
	/** Categories presented by the lesson form and accepted by the validator. */
	public static final String[] CATEGORIES = { "Technical", "Tools and Environment", "Teamwork",
			"Process", "Testing", "Documentation", "Other" };
	/** Minimum accepted title length. */
	public static final int MIN_TITLE_LENGTH = 5;
	/** Maximum accepted title length. */
	public static final int MAX_TITLE_LENGTH = 80;
	/** Minimum length for situation, lesson, and experience descriptions. */
	public static final int MIN_DETAIL_LENGTH = 20;
	/** Maximum length for situation, lesson, and experience descriptions. */
	public static final int MAX_DETAIL_LENGTH = 2000;
	/** Maximum accepted recommendation length. */
	public static final int MAX_RECOMMENDATION_LENGTH = 1000;
	/** Maximum accepted total keywords length. */
	public static final int MAX_KEYWORDS_LENGTH = 200;
	/** Maximum number of comma-separated keywords. */
	public static final int MAX_NUMBER_OF_KEYWORDS = 10;
	/** Minimum length of one keyword. */
	public static final int MIN_KEYWORD_LENGTH = 2;
	/** Maximum length of one keyword. */
	public static final int MAX_KEYWORD_LENGTH = 30;
	/** Maximum experience duration, equal to one week. */
	public static final double MAX_EXPERIENCE_DURATION_HOURS = 168.0;
	/** Maximum accepted coaching summary length. */
	public static final int MAX_SUMMARY_LENGTH = 200;
	/** Maximum accepted root-cause description length. */
	public static final int MAX_ROOT_CAUSE_LENGTH = 1000;

	/** Creates a validator instance; the validation methods are static. */
	public LessonLearnedValidator() { }

	/** R1 and R14: checks title presence, length, line format, and meaningful content.
	 *
	 * @param input title text to validate
	 * @return an empty string when valid, or an error message
	 */
	public static String checkTitle(String input) {
		if (isBlank(input)) return "Title is required. Please enter a short title (5 to 80 characters).";
		String title = input.trim();
		if (title.contains("\n") || title.contains("\r")) return "Title must be on a single line.";
		if (title.length() < MIN_TITLE_LENGTH) return "Title must be at least 5 characters long.";
		if (title.length() > MAX_TITLE_LENGTH) return "Title must be 80 characters or fewer.";
		for (int i = 0; i < title.length(); i++)
			if (Character.isLetter(title.charAt(i))) return "";
		return "Title must contain at least one letter.";
	}

	/** R1 and R14: checks that category is one of the categories presented by the GUI.
	 *
	 * @param input category text to validate
	 * @return an empty string when valid, or an error message
	 */
	public static String checkCategory(String input) {
		if (isBlank(input)) return "Category is required. Please choose a category.";
		for (String category : CATEGORIES)
			if (category.equals(input)) return "";
		return "Invalid category. Choose one of: " + String.join(", ", CATEGORIES) + ".";
	}

	/** R1 and R14: requires 20 to 2000 characters of situation detail.
	 *
	 * @param input situation text to validate
	 * @return an empty string when valid, or an error message
	 */
	public static String checkSituation(String input) {
		return checkRequiredDetail("What Happened", input);
	}

	/** R1 and R14: requires 20 to 2000 characters of lesson detail.
	 *
	 * @param input lesson text to validate
	 * @return an empty string when valid, or an error message
	 */
	public static String checkLessonText(String input) {
		return checkRequiredDetail("What I Learned", input);
	}

	/** R1 and R14: allows an empty recommendation but caps it at 1000 characters.
	 *
	 * @param input recommendation text to validate
	 * @return an empty string when valid, or an error message
	 */
	public static String checkRecommendation(String input) {
		if (input == null || input.trim().isEmpty()) return "";
		if (input.trim().length() > MAX_RECOMMENDATION_LENGTH)
			return "Next Time must be 1000 characters or fewer.";
		return "";
	}

	/** R1, R7, and R14: allows empty keywords but enforces count, length, and character rules so
	 * search terms stay meaningful.
	 *
	 * @param input comma-separated keywords to validate
	 * @return an empty string when valid, or an error message
	 */
	public static String checkKeywords(String input) {
		if (input == null || input.trim().isEmpty()) return "";
		String keywords = input.trim();
		if (keywords.length() > MAX_KEYWORDS_LENGTH) return "Keywords must be 200 characters or fewer.";
		String[] parts = keywords.split(",", -1);
		if (parts.length > MAX_NUMBER_OF_KEYWORDS) return "At most 10 keywords are allowed.";
		for (int i = 0; i < parts.length; i++) {
			String keyword = parts[i].trim();
			if (keyword.isEmpty()) return "Keyword " + (i + 1) + " is empty.";
			if (keyword.length() < MIN_KEYWORD_LENGTH || keyword.length() > MAX_KEYWORD_LENGTH)
				return "Each keyword must be 2 to 30 characters long.";
			for (int j = 0; j < keyword.length(); j++) {
				char c = keyword.charAt(j);
				if (!Character.isLetterOrDigit(c) && " -+#.".indexOf(c) < 0)
					return "Keywords may only use letters, digits, spaces, and - + # .";
			}
		}
		return "";
	}

	/** Checks the optional coaching summary length (R10).
	 *
	 * @param input summary text to validate
	 * @return an empty string when valid, or an error message
	 */
	public static String checkSummary(String input) {
		if (isBlank(input)) return "";
		if (input.trim().length() > MAX_SUMMARY_LENGTH)
			return "Summary must be 200 characters or fewer.";
		return "";
	}

	/** Checks the optional root-cause description length (R10).
	 *
	 * @param input root-cause text to validate
	 * @return an empty string when valid, or an error message
	 */
	public static String checkRootCause(String input) {
		if (isBlank(input)) return "";
		if (input.trim().length() > MAX_ROOT_CAUSE_LENGTH)
			return "Root Cause must be 1000 characters or fewer.";
		return "";
	}

	/** Validates an experience attempt and per-member effort entries (R8).
	 *
	 * @param whatWasDone required description of the work performed
	 * @param how required description of how the work was performed
	 * @param durationHours elapsed time in hours; must be greater than zero and no more than 168
	 * @param effortHoursByMember member names mapped to their positive effort hours
	 * @return an empty string when valid, or field-specific errors separated by newlines
	 */
	public static String checkExperienceRecord(String whatWasDone, String how, double durationHours,
			Map<String, Double> effortHoursByMember) {
		StringBuilder errors = new StringBuilder();
		add(errors, checkRequiredDetail("What Was Done", whatWasDone));
		add(errors, checkRequiredDetail("How", how));
		add(errors, !Double.isFinite(durationHours) || durationHours <= 0
				|| durationHours > MAX_EXPERIENCE_DURATION_HOURS
				? "Duration must be greater than 0 and 168 hours or fewer." : "");
		if (effortHoursByMember == null || effortHoursByMember.isEmpty()) {
			add(errors, "At least one team member effort entry is required.");
		} else {
			for (Map.Entry<String, Double> effort : effortHoursByMember.entrySet()) {
				if (isBlank(effort.getKey()))
					add(errors, "Each effort entry must identify a team member.");
				Double hours = effort.getValue();
				if (hours == null || !Double.isFinite(hours) || hours <= 0)
					add(errors, "Effort hours for each team member must be greater than 0.");
			}
		}
		return errors.toString();
	}

	/** R1, R3, and R10: runs every lesson-field check, including the optional coaching attributes, and
	 * returns all errors separated by newlines so the GUI can show every problem at once.
	 *
	 * @param lesson the lesson to validate
	 * @return an empty string when valid, or newline-separated error messages
	 */
	public static String checkLessonLearned(LessonLearned lesson) {
		if (lesson == null) return "No lesson was provided.";
		StringBuilder errors = new StringBuilder();
		add(errors, isBlank(lesson.getContributorUserName()) ? "A Contributor is required." : "");
		add(errors, checkTitle(lesson.getTitle()));
		add(errors, checkCategory(lesson.getCategory()));
		add(errors, checkSituation(lesson.getSituation()));
		add(errors, checkLessonText(lesson.getLessonText()));
		add(errors, checkRecommendation(lesson.getRecommendation()));
		add(errors, checkKeywords(lesson.getKeywords()));
		add(errors, checkSummary(lesson.getSummary()));
		add(errors, checkRootCause(lesson.getRootCause()));
		return errors.toString();
	}

	/** Validates a required 20-to-2000-character detail field.
	 *
	 * @param field user-facing field name used in error messages
	 * @param input text to validate
	 * @return an empty string when valid, or an error message
	 */
	private static String checkRequiredDetail(String field, String input) {
		if (isBlank(input)) return field + " is required and must be 20 to 2000 characters.";
		int length = input.trim().length();
		if (length < MIN_DETAIL_LENGTH) return field + " must be at least 20 characters long.";
		if (length > MAX_DETAIL_LENGTH) return field + " must be 2000 characters or fewer.";
		return "";
	}

	/** Appends a nonempty error to the accumulated error text.
	 *
	 * @param errors the accumulated errors
	 * @param error the error to append; an empty string is ignored
	 */
	private static void add(StringBuilder errors, String error) {
		if (error.isEmpty()) return;
		if (errors.length() > 0) errors.append("\n");
		errors.append(error);
	}

	/** Returns whether a string is null or contains only whitespace.
	 *
	 * @param value the string to check
	 * @return true when the value is null or blank
	 */
	private static boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}
}
