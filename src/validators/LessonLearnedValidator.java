package validators;

import entityClasses.LessonLearned;

/**
 * <p>Title: LessonLearnedValidator Class.</p>
 * <p>Description: Validates core lesson information for both the GUI and database layer. Keeping
 * the checks here prevents a future caller from bypassing GUI validation.</p>
 */
public class LessonLearnedValidator {
	public static final String[] CATEGORIES = { "Technical", "Tools and Environment", "Teamwork",
			"Process", "Testing", "Documentation", "Other" };
	public static final int MIN_TITLE_LENGTH = 5;
	public static final int MAX_TITLE_LENGTH = 80;
	public static final int MIN_DETAIL_LENGTH = 20;
	public static final int MAX_DETAIL_LENGTH = 2000;
	public static final int MAX_RECOMMENDATION_LENGTH = 1000;
	public static final int MAX_KEYWORDS_LENGTH = 200;
	public static final int MAX_NUMBER_OF_KEYWORDS = 10;
	public static final int MIN_KEYWORD_LENGTH = 2;
	public static final int MAX_KEYWORD_LENGTH = 30;

	/** Checks title presence, length, line format, and meaningful content. */
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

	/** Checks that category is one of the categories presented by the GUI. */
	public static String checkCategory(String input) {
		if (isBlank(input)) return "Category is required. Please choose a category.";
		for (String category : CATEGORIES)
			if (category.equals(input)) return "";
		return "Invalid category. Choose one of: " + String.join(", ", CATEGORIES) + ".";
	}

	public static String checkSituation(String input) {
		return checkRequiredDetail("What Happened", input);
	}

	public static String checkLessonText(String input) {
		return checkRequiredDetail("What I Learned", input);
	}

	public static String checkRecommendation(String input) {
		if (input == null || input.trim().isEmpty()) return "";
		if (input.trim().length() > MAX_RECOMMENDATION_LENGTH)
			return "Next Time must be 1000 characters or fewer.";
		return "";
	}

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

	/** Runs every lesson-field check and returns all errors separated by newlines. */
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
		return errors.toString();
	}

	private static String checkRequiredDetail(String field, String input) {
		if (isBlank(input)) return field + " is required and must be 20 to 2000 characters.";
		int length = input.trim().length();
		if (length < MIN_DETAIL_LENGTH) return field + " must be at least 20 characters long.";
		if (length > MAX_DETAIL_LENGTH) return field + " must be 2000 characters or fewer.";
		return "";
	}

	private static void add(StringBuilder errors, String error) {
		if (error.isEmpty()) return;
		if (errors.length() > 0) errors.append("\n");
		errors.append(error);
	}

	private static boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}
}
