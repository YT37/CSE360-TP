package database;

import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import entityClasses.User;
import entityClasses.ExperienceRecord;
import entityClasses.LessonLearned;
import entityClasses.LessonLearnedList;
import validators.LessonLearnedValidator;

/*******
 * <p> Title: Database Class. </p>
 * 
 * <p> Description: This is an in-memory database built on H2.  Detailed documentation of H2 can
 * be found at https://www.h2database.com/html/main.html (Click on "PDF (2MB)" on the l3ft side
 * of the page under the heading "Reference" for a PDF of 438 pages.)  This class leverages H2
 * and provides numerous special supporting methods.
 * </p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * 
 * @author Lynn Robert Carter
 * 
 * @version 2.00		2025-04-29 Updated and expanded from the version produce by Pravalika 
 * 							Mukkiri and Ishwarya Hidkimath Basavaraj
 * @version 2.01		2025-12-17 Minor updates for Spring 2026
 */

/*
 * The Database class is responsible for establishing and managing the connection to the database,
 * and performing operations such as user registration, login validation, handling invitation 
 * codes, and numerous other database related functions.
 */
public class Database {

	// JDBC driver name and database URL 
	static final String JDBC_DRIVER = "org.h2.Driver";   
	static final String DB_URL = "jdbc:h2:~/FoundationDatabase";  

	//  Database credentials 
	static final String USER = "sa"; 
	static final String PASS = ""; 
	private String databaseURL = DB_URL;

	//  Shared variables used within this class
	private Connection connection = null;		// Singleton to access the database 
	private Statement statement = null;			// The H2 Statement is used to construct queries
	private String lastLessonError = "";
	private String lastExperienceError = "";
	
	// These are the easily accessible attributes of the currently logged-in user
	// This is only useful for single user applications
	private String currentUsername;
	private String currentPassword;
	private String currentFirstName;
	private String currentMiddleName;
	private String currentLastName;
	private String currentPreferredFirstName;
	private String currentEmailAddress;
	private boolean currentAdminRole;
	private boolean currentContributorRole;
	private boolean currentViewerRole;
	private boolean currentIsOneTimePassword;

	/*******
	 * <p> Method: Database </p>
	 * 
	 * <p> Description: The default constructor used to establish this singleton object.</p>
	 * 
	 */
	
	public Database () {
		
	}

	/** Creates a database facade that uses the supplied JDBC URL.
	 *
	 * @param databaseURL JDBC URL for the H2 database
	 */
	public Database(String databaseURL) {
		this.databaseURL = databaseURL;
	}
	
	
/*******
 * <p> Method: connectToDatabase </p>
 * 
 * <p> Description: Used to establish the in-memory instance of the H2 database from secondary
 *		storage.</p>
 *
 * @throws SQLException when the DriverManager is unable to establish a connection
 * 
 */
	public void connectToDatabase() throws SQLException {
		try {
			Class.forName(JDBC_DRIVER); // Load the JDBC driver
			connection = DriverManager.getConnection(databaseURL, USER, PASS);
			statement = connection.createStatement(); 
			// You can use this command to clear the database and restart from fresh.
			//statement.execute("DROP ALL OBJECTS");

			createTables();  // Create the necessary tables if they don't exist
		} catch (ClassNotFoundException e) {
			System.err.println("JDBC Driver not found: " + e.getMessage());
		}
	}

	
/*******
 * <p> Method: createTables </p>
 * 
 * <p> Description: Used to create new instances of the two database tables used by this class.</p>
 *
 * @throws SQLException when a table cannot be created
 */
	private void createTables() throws SQLException {
		// Create the user database
		String userTable = "CREATE TABLE IF NOT EXISTS userDB ("
				+ "id INT AUTO_INCREMENT PRIMARY KEY, "
				+ "userName VARCHAR(255) UNIQUE, "
				+ "password VARCHAR(255), "
				+ "firstName VARCHAR(255), "
				+ "middleName VARCHAR(255), "
				+ "lastName VARCHAR (255), "
				+ "preferredFirstName VARCHAR(255), "
				+ "emailAddress VARCHAR(255), "
				+ "adminRole BOOL DEFAULT FALSE, "
				+ "contributorRole BOOL DEFAULT FALSE, "
				+ "viewerRole BOOL DEFAULT FALSE)";
		statement.execute(userTable);
		
		// Create the invitation codes table
	    String invitationCodesTable = "CREATE TABLE IF NOT EXISTS InvitationCodes ("
	            + "code VARCHAR(10) PRIMARY KEY, "
	    		+ "emailAddress VARCHAR(255), "
	            + "role VARCHAR(20))";
	    statement.execute(invitationCodesTable);
	    statement.execute("ALTER TABLE userDB ADD COLUMN IF NOT EXISTS adminRole BOOL DEFAULT FALSE");
	    statement.execute("ALTER TABLE userDB ADD COLUMN IF NOT EXISTS contributorRole BOOL DEFAULT FALSE");
	    statement.execute("ALTER TABLE userDB ADD COLUMN IF NOT EXISTS viewerRole BOOL DEFAULT FALSE");
	    statement.execute("ALTER TABLE userDB ADD COLUMN IF NOT EXISTS isOneTimePassword BOOL DEFAULT FALSE");
	    statement.execute("ALTER TABLE InvitationCodes ADD COLUMN IF NOT EXISTS deadline TIMESTAMP");
	    String lessonTable = "CREATE TABLE IF NOT EXISTS lessonsLearnedDB ("
				+ "lessonID INT AUTO_INCREMENT PRIMARY KEY, "
				+ "contributorUserName VARCHAR(255) NOT NULL, "
				+ "title VARCHAR(80) NOT NULL, "
				+ "category VARCHAR(40) NOT NULL, "
				+ "situation VARCHAR(2000) NOT NULL, "
				+ "lessonText VARCHAR(2000) NOT NULL, "
				+ "recommendation VARCHAR(1000), "
				+ "keywords VARCHAR(200), "
				+ "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
				+ "updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
		statement.execute(lessonTable);
		// R4: Add the lock metadata to new and existing lesson tables without destroying lesson data.
		statement.execute("ALTER TABLE lessonsLearnedDB ADD COLUMN IF NOT EXISTS lessonLocked BOOL DEFAULT FALSE");
		statement.execute("ALTER TABLE lessonsLearnedDB ADD COLUMN IF NOT EXISTS titleLocked BOOL DEFAULT FALSE");
		statement.execute("ALTER TABLE lessonsLearnedDB ADD COLUMN IF NOT EXISTS categoryLocked BOOL DEFAULT FALSE");
		statement.execute("ALTER TABLE lessonsLearnedDB ADD COLUMN IF NOT EXISTS situationLocked BOOL DEFAULT FALSE");
		statement.execute("ALTER TABLE lessonsLearnedDB ADD COLUMN IF NOT EXISTS lessonTextLocked BOOL DEFAULT FALSE");
		statement.execute("ALTER TABLE lessonsLearnedDB ADD COLUMN IF NOT EXISTS recommendationLocked BOOL DEFAULT FALSE");
		statement.execute("ALTER TABLE lessonsLearnedDB ADD COLUMN IF NOT EXISTS keywordsLocked BOOL DEFAULT FALSE");
		// R9: Preserve curator edit provenance when lessons are reopened from storage.
		statement.execute("ALTER TABLE lessonsLearnedDB ADD COLUMN IF NOT EXISTS curatorEdited BOOL DEFAULT FALSE");
		statement.execute("ALTER TABLE lessonsLearnedDB ADD COLUMN IF NOT EXISTS editedBy VARCHAR(255)");
		statement.execute("ALTER TABLE lessonsLearnedDB ADD COLUMN IF NOT EXISTS summary VARCHAR(200)");
		statement.execute("ALTER TABLE lessonsLearnedDB ADD COLUMN IF NOT EXISTS rootCause VARCHAR(1000)");
		String experienceTable = "CREATE TABLE IF NOT EXISTS experienceRecordsDB ("
				+ "experienceID INT AUTO_INCREMENT PRIMARY KEY, "
				+ "lessonID INT NOT NULL, "
				+ "contributorUserName VARCHAR(255) NOT NULL, "
				+ "whatWasDone VARCHAR(2000) NOT NULL, "
				+ "how VARCHAR(2000) NOT NULL, "
				+ "durationHours DOUBLE NOT NULL, "
				+ "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
				+ "updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
				+ "FOREIGN KEY (lessonID) REFERENCES lessonsLearnedDB(lessonID) ON DELETE CASCADE)";
		statement.execute(experienceTable);
		String effortTable = "CREATE TABLE IF NOT EXISTS experienceEffortDB ("
				+ "experienceID INT NOT NULL, "
				+ "teamMemberName VARCHAR(255) NOT NULL, "
				+ "effortHours DOUBLE NOT NULL, "
				+ "PRIMARY KEY (experienceID, teamMemberName), "
				+ "FOREIGN KEY (experienceID) REFERENCES experienceRecordsDB(experienceID) ON DELETE CASCADE)";
		statement.execute(effortTable);
	}


/*******
 * <p> Method: isDatabaseEmpty </p>
 * 
 * <p> Description: If the user database has no rows, true is returned, else false.</p>
 * 
 * @return true if the database is empty, else it returns false
 * 
 */
	public boolean isDatabaseEmpty() {
		String query = "SELECT COUNT(*) AS count FROM userDB";
		try {
			ResultSet resultSet = statement.executeQuery(query);
			if (resultSet.next()) {
				return resultSet.getInt("count") == 0;
			}
		}  catch (SQLException e) {
	        return false;
	    }
		return true;
	}
	
	
/*******
 * <p> Method: getNumberOfUsers </p>
 * 
 * <p> Description: Returns an integer .of the number of users currently in the user database. </p>
 * 
 * @return the number of user records in the database.
 * 
 */
	public int getNumberOfUsers() {
		String query = "SELECT COUNT(*) AS count FROM userDB";
		try {
			ResultSet resultSet = statement.executeQuery(query);
			if (resultSet.next()) {
				return resultSet.getInt("count");
			}
		} catch (SQLException e) {
	        return 0;
	    }
		return 0;
	}

/*******
 * <p> Method: register(User user) </p>
 * 
 * <p> Description: Creates a new row in the database using the user parameter. </p>
 * 
 * @throws SQLException when there is an issue creating the SQL command or executing it.
 * 
 * @param user specifies a user object to be added to the database.
 * 
 */
	public void register(User user) throws SQLException {
		String insertUser = "INSERT INTO userDB (userName, password, firstName, middleName, "
				+ "lastName, preferredFirstName, emailAddress, adminRole, contributorRole, viewerRole) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(insertUser)) {
			currentUsername = user.getUserName();
			pstmt.setString(1, currentUsername);
			
			currentPassword = user.getPassword();
			pstmt.setString(2, currentPassword);
			
			currentFirstName = user.getFirstName();
			pstmt.setString(3, currentFirstName);
			
			currentMiddleName = user.getMiddleName();			
			pstmt.setString(4, currentMiddleName);
			
			currentLastName = user.getLastName();
			pstmt.setString(5, currentLastName);
			
			currentPreferredFirstName = user.getPreferredFirstName();
			pstmt.setString(6, currentPreferredFirstName);
			
			currentEmailAddress = user.getEmailAddress();
			pstmt.setString(7, currentEmailAddress);
			
			currentAdminRole = user.getAdminRole();
			pstmt.setBoolean(8, currentAdminRole);
			
			currentContributorRole = user.getContributorRole();
			pstmt.setBoolean(9, currentContributorRole);
			
			currentViewerRole = user.getViewerRole();
			pstmt.setBoolean(10, currentViewerRole);
			
			pstmt.executeUpdate();
		}
		
	}
	
/*******
 *  <p> Method: List getUserList() </p>
 *  
 *  <P> Description: Generate an List of Strings, one for each user in the database,
 *  starting with "&lt;Select User&gt;" at the start of the list. </p>
 *  
 *  @return a list of userNames found in the database.
 */
	public List<String> getUserList () {
		List<String> userList = new ArrayList<String>();
		userList.add("<Select a User>");
		String query = "SELECT userName FROM userDB";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				userList.add(rs.getString("userName"));
			}
		} catch (SQLException e) {
	        return null;
	    }
//		System.out.println(userList);
		return userList;
	}

/*******
 * <p> Method: boolean loginAdmin(User user) </p>
 * 
 * <p> Description: Check to see that a user with the specified username, password, and role
 * 		is the same as a row in the table for the username, password, and role. </p>
 * 
 * @param user specifies the specific user that should be logged in playing the Admin role.
 * 
 * @return true if the specified user has been logged in as an Admin else false.
 * 
 */
	public boolean loginAdmin(User user){
		// Validates an admin user's login credentials so the user can login in as an Admin.
		String query = "SELECT * FROM userDB WHERE userName = ? AND password = ? AND "
				+ "adminRole = TRUE";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			ResultSet rs = pstmt.executeQuery();
			return rs.next();	// If a row is returned, rs.next() will return true		
		} catch  (SQLException e) {
	        e.printStackTrace();
	    }
		return false;
	}
	
	
/*******
 * <p> Method: boolean loginContributor(User user) </p>
 * 
 * <p> Description: Check to see that a user with the specified username, password, and role
 * 		is the same as a row in the table for the username, password, and role. </p>
 * 
 * @param user specifies the specific user that should be logged in playing the Contributor role.
 * 
 * @return true if the specified user has been logged in as a Contributor else false.
 * 
 */
	public boolean loginContributor(User user) {
		// Validates a contributor user's login credentials.
		String query = "SELECT * FROM userDB WHERE userName = ? AND password = ? AND "
				+ "contributorRole = TRUE";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			ResultSet rs = pstmt.executeQuery();
			return rs.next();
		} catch  (SQLException e) {
		       e.printStackTrace();
		}
		return false;
	}

	/*******
	 * <p> Method: boolean loginViewer(User user) </p>
	 * 
	 * <p> Description: Check to see that a user with the specified username, password, and role
	 * 		is the same as a row in the table for the username, password, and role. </p>
	 * 
	 * @param user specifies the specific user that should be logged in playing the Viewer role.
	 * 
	 * @return true if the specified user has been logged in as a Viewer else false.
	 * 
	 */
	// Validates a viewer user's login credentials.
	public boolean loginViewer(User user) {
		String query = "SELECT * FROM userDB WHERE userName = ? AND password = ? AND "
				+ "viewerRole = TRUE";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			ResultSet rs = pstmt.executeQuery();
			return rs.next();
		} catch  (SQLException e) {
		       e.printStackTrace();
		}
		return false;
	}
	
	
	/*******
	 * <p> Method: boolean doesUserExist(User user) </p>
	 * 
	 * <p> Description: Check to see that a user with the specified username is  in the table. </p>
	 * 
	 * @param userName specifies the specific user that we want to determine if it is in the table.
	 * 
	 * @return true if the specified user is in the table else false.
	 * 
	 */
	// Checks if a user already exists in the database based on their userName.
	public boolean doesUserExist(String userName) {
	    String query = "SELECT COUNT(*) FROM userDB WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            // If the count is greater than 0, the user exists
	            return rs.getInt(1) > 0;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false; // If an error occurs, assume user doesn't exist
	}

	
	/*******
	 * <p> Method: int getNumberOfRoles(User user) </p>
	 * 
	 * <p> Description: Determine the number of roles a specified user plays. </p>
	 * 
	 * @param user specifies the specific user that we want to determine if it is in the table.
	 * 
	 * @return the number of roles this user plays (0 - 3).
	 * 
	 */	
	// Get the number of roles that this user plays
	public int getNumberOfRoles (User user) {
		int numberOfRoles = 0;
		if (user.getAdminRole()) numberOfRoles++;
		if (user.getContributorRole()) numberOfRoles++;
		if (user.getViewerRole()) numberOfRoles++;
		return numberOfRoles;
	}	

	
	/*******
	 * <p> Method: String generateInvitationCode(String emailAddress, String role) </p>
	 * 
	 * <p> Description: Given an email address and a roles, this method establishes and invitation
	 * code and adds a record to the InvitationCodes table.  When the invitation code is used, the
	 * stored email address is used to establish the new user and the record is removed from the
	 * table.  Each code is given a deadline seven days from now, after which it is removed as
	 * expired.</p>
	 * 
	 * @param emailAddress specifies the email address for this new user.
	 * 
	 * @param role specified the role that this new user will play.
	 * 
	 * @return the code of six characters so the new user can use it to securely setup an account.
	 * 
	 */
	// Generates a new invitation code and inserts it into the database.
	public String generateInvitationCode(String emailAddress, String role) {
	    String code = UUID.randomUUID().toString().substring(0, 6); // Generate a random 6-character code
	    String query = "INSERT INTO InvitationCodes (code, emailaddress, role, deadline) VALUES (?, ?, ?, ?)";
	    Timestamp deadline = new Timestamp(System.currentTimeMillis() + (7L * 24 * 60 * 60 * 1000));

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        pstmt.setString(2, emailAddress);
	        pstmt.setString(3, role);
	        pstmt.setTimestamp(4, deadline);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return code;
	}


	/*******
	 * <p> Method: void removeExpiredInvitations() </p>
	 *
	 * <p> Description: Remove every record from the InvitationCodes table whose deadline has
	 * passed, so an expired invitation code can no longer be used or listed.</p>
	 *
	 */
	// Delete the invitation codes whose deadline has passed.
	public void removeExpiredInvitations() {
	    String query = "DELETE FROM InvitationCodes WHERE deadline IS NOT NULL AND deadline < CURRENT_TIMESTAMP";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}


	/*******
	 * <p> Method: List&lt;String[]&gt; getAllInvitations() </p>
	 *
	 * <p> Description: Remove any expired invitations and then return one String[4] per
	 * outstanding invitation: {code, email address, role, deadline}.  This list is used by the
	 * admin's "Manage Invitations" window.</p>
	 *
	 * @return a list of the outstanding invitations, which is empty if there are none.
	 *
	 */
	// Get the details of every outstanding invitation.
	public List<String[]> getAllInvitations() {
	    removeExpiredInvitations();
	    List<String[]> invitations = new ArrayList<String[]>();
	    String query = "SELECT code, emailAddress, role, deadline FROM InvitationCodes";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        ResultSet rs = pstmt.executeQuery();
	        while (rs.next()) {
	            invitations.add(new String[] {
	                rs.getString("code"),
	                rs.getString("emailAddress"),
	                rs.getString("role"),
	                rs.getTimestamp("deadline") == null ? "<none>" : rs.getTimestamp("deadline").toString()
	            });
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return invitations;
	}


	/*******
	 * <p> Method: void cancelInvitation(String code) </p>
	 *
	 * <p> Description: Remove the record for the specified invitation code from the
	 * InvitationCodes table so the code can no longer be used to establish an account.</p>
	 *
	 * @param code is the 6 character String invitation code to be cancelled
	 *
	 */
	// Cancel an outstanding invitation.
	public void cancelInvitation(String code) {
	    String query = "DELETE FROM InvitationCodes WHERE code = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	
	/*******
	 * <p> Method: int getNumberOfInvitations() </p>
	 * 
	 * <p> Description: Determine the number of outstanding invitations in the table.</p>
	 *  
	 * @return the number of invitations in the table.
	 * 
	 */
	// Number of invitations in the database
	public int getNumberOfInvitations() {
		String query = "SELECT COUNT(*) AS count FROM InvitationCodes";
		try {
			ResultSet resultSet = statement.executeQuery(query);
			if (resultSet.next()) {
				return resultSet.getInt("count");
			}
		} catch  (SQLException e) {
	        e.printStackTrace();
	    }
		return 0;
	}
	
	
	/*******
	 * <p> Method: boolean emailaddressHasBeenUsed(String emailAddress) </p>
	 * 
	 * <p> Description: Determine if an email address has been user to establish a user.</p>
	 * 
	 * @param emailAddress is a string that identifies a user in the table
	 *  
	 * @return true if the email address is in the table, else return false.
	 * 
	 */
	// Check to see if an email address is already in the database
	public boolean emailaddressHasBeenUsed(String emailAddress) {
	    String query = "SELECT COUNT(*) AS count FROM InvitationCodes WHERE emailAddress = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, emailAddress);
	        ResultSet rs = pstmt.executeQuery();
	 //     System.out.println(rs);
	        if (rs.next()) {
	            // Mark the code as used
	        	return rs.getInt("count")>0;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return false;
	}
	
	
	/*******
	 * <p> Method: String getRoleGivenAnInvitationCode(String code) </p>
	 * 
	 * <p> Description: Get the role associated with an invitation code.  Expired invitations are
	 * removed first, so an expired code returns an empty string.</p>
	 * 
	 * @param code is the 6 character String invitation code
	 *  
	 * @return the role for the code or an empty string.
	 * 
	 */
	// Obtain the roles associated with an invitation code.
	public String getRoleGivenAnInvitationCode(String code) {
		removeExpiredInvitations();
	    String query = "SELECT * FROM InvitationCodes WHERE code = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            return rs.getString("role");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return "";
	}

	
	/*******
	 * <p> Method: String getEmailAddressUsingCode (String code ) </p>
	 * 
	 * <p> Description: Get the email addressed associated with an invitation code.</p>
	 * 
	 * @param code is the 6 character String invitation code
	 *  
	 * @return the email address for the code or an empty string.
	 * 
	 */
	// For a given invitation code, return the associated email address of an empty string
	public String getEmailAddressUsingCode (String code ) {
	    String query = "SELECT emailAddress FROM InvitationCodes WHERE code = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            return rs.getString("emailAddress");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return "";
	}
	
	
	/*******
	 * <p> Method: void removeInvitationAfterUse(String code) </p>
	 * 
	 * <p> Description: Remove an invitation record once it is used.</p>
	 * 
	 * @param code is the 6 character String invitation code
	 *  
	 */
	// Remove an invitation using an email address once the user account has been setup
	public void removeInvitationAfterUse(String code) {
	    String query = "SELECT COUNT(*) AS count FROM InvitationCodes WHERE code = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	        	int counter = rs.getInt(1);
	            // Only do the remove if the code is still in the invitation table
	        	if (counter > 0) {
        			query = "DELETE FROM InvitationCodes WHERE code = ?";
	        		try (PreparedStatement pstmt2 = connection.prepareStatement(query)) {
	        			pstmt2.setString(1, code);
	        			pstmt2.executeUpdate();
	        		}catch (SQLException e) {
	        	        e.printStackTrace();
	        	    }
	        	}
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return;
	}
	
	
	/*******
	 * <p> Method: String getFirstName(String username) </p>
	 * 
	 * <p> Description: Get the first name of a user given that user's username.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @return the first name of a user given that user's username 
	 *  
	 */
	// Get the First Name
	public String getFirstName(String username) {
		String query = "SELECT firstName FROM userDB WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            return rs.getString("firstName"); // Return the first name if user exists
	        }
			
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return null;
	}
	

	/*******
	 * <p> Method: void updateFirstName(String username, String firstName) </p>
	 * 
	 * <p> Description: Update the first name of a user given that user's username and the new
	 *		first name.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @param firstName is the new first name for the user
	 *  
	 */
	// update the first name
	public void updateFirstName(String username, String firstName) {
	    String query = "UPDATE userDB SET firstName = ? WHERE username = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, firstName);
	        pstmt.setString(2, username);
	        pstmt.executeUpdate();
	        currentFirstName = firstName;
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	
	/*******
	 * <p> Method: String getMiddleName(String username) </p>
	 * 
	 * <p> Description: Get the middle name of a user given that user's username.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @return the middle name of a user given that user's username 
	 *  
	 */
	// get the middle name
	public String getMiddleName(String username) {
		String query = "SELECT MiddleName FROM userDB WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            return rs.getString("middleName"); // Return the middle name if user exists
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return null;
	}

	
	/*******
	 * <p> Method: void updateMiddleName(String username, String middleName) </p>
	 * 
	 * <p> Description: Update the middle name of a user given that user's username and the new
	 * 		middle name.</p>
	 * 
	 * @param username is the username of the user
	 *  
	 * @param middleName is the new middle name for the user
	 *  
	 */
	// update the middle name
	public void updateMiddleName(String username, String middleName) {
	    String query = "UPDATE userDB SET middleName = ? WHERE username = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, middleName);
	        pstmt.setString(2, username);
	        pstmt.executeUpdate();
	        currentMiddleName = middleName;
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	
	/*******
	 * <p> Method: String getLastName(String username) </p>
	 * 
	 * <p> Description: Get the last name of a user given that user's username.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @return the last name of a user given that user's username 
	 *  
	 */
	// get he last name
	public String getLastName(String username) {
		String query = "SELECT LastName FROM userDB WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            return rs.getString("lastName"); // Return last name role if user exists
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return null;
	}
	
	
	/*******
	 * <p> Method: void updateLastName(String username, String lastName) </p>
	 * 
	 * <p> Description: Update the middle name of a user given that user's username and the new
	 * 		middle name.</p>
	 * 
	 * @param username is the username of the user
	 *  
	 * @param lastName is the new last name for the user
	 *  
	 */
	// update the last name
	public void updateLastName(String username, String lastName) {
	    String query = "UPDATE userDB SET lastName = ? WHERE username = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, lastName);
	        pstmt.setString(2, username);
	        pstmt.executeUpdate();
	        currentLastName = lastName;
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	
	/*******
	 * <p> Method: String getPreferredFirstName(String username) </p>
	 * 
	 * <p> Description: Get the preferred first name of a user given that user's username.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @return the preferred first name of a user given that user's username 
	 *  
	 */
	// get the preferred first name
	public String getPreferredFirstName(String username) {
		String query = "SELECT preferredFirstName FROM userDB WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            return rs.getString("firstName"); // Return the preferred first name if user exists
	        }
			
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return null;
	}
	
	
	/*******
	 * <p> Method: void updatePreferredFirstName(String username, String preferredFirstName) </p>
	 * 
	 * <p> Description: Update the preferred first name of a user given that user's username and
	 * 		the new preferred first name.</p>
	 * 
	 * @param username is the username of the user
	 *  
	 * @param preferredFirstName is the new preferred first name for the user
	 *  
	 */
	// update the preferred first name of the user
	public void updatePreferredFirstName(String username, String preferredFirstName) {
	    String query = "UPDATE userDB SET preferredFirstName = ? WHERE username = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, preferredFirstName);
	        pstmt.setString(2, username);
	        pstmt.executeUpdate();
	        currentPreferredFirstName = preferredFirstName;
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	
	/*******
	 * <p> Method: String getEmailAddress(String username) </p>
	 * 
	 * <p> Description: Get the email address of a user given that user's username.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @return the email address of a user given that user's username 
	 *  
	 */
	// get the email address
	public String getEmailAddress(String username) {
		String query = "SELECT emailAddress FROM userDB WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            return rs.getString("emailAddress"); // Return the email address if user exists
	        }
			
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return null;
	}
	
	
	/*******
	 * <p> Method: void updateEmailAddress(String username, String emailAddress) </p>
	 * 
	 * <p> Description: Update the email address name of a user given that user's username and
	 * 		the new email address.</p>
	 * 
	 * @param username is the username of the user
	 *  
	 * @param emailAddress is the new preferred first name for the user
	 *  
	 */
	// update the email address
	public void updateEmailAddress(String username, String emailAddress) {
	    String query = "UPDATE userDB SET emailAddress = ? WHERE username = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, emailAddress);
	        pstmt.setString(2, username);
	        pstmt.executeUpdate();
	        currentEmailAddress = emailAddress;
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	
	/*******
	 * <p> Method: boolean getUserAccountDetails(String username) </p>
	 * 
	 * <p> Description: Get all the attributes of a user given that user's username.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @return true of the get is successful, else false
	 *  
	 */
	// get the attributes for a specified user
	public boolean getUserAccountDetails(String username) {
		String query = "SELECT * FROM userDB WHERE username = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
	        ResultSet rs = pstmt.executeQuery();			
			rs.next();
	    	currentUsername = rs.getString(2);
	    	currentPassword = rs.getString(3);
	    	currentFirstName = rs.getString(4);
	    	currentMiddleName = rs.getString(5);
	    	currentLastName = rs.getString(6);
	    	currentPreferredFirstName = rs.getString(7);
	    	currentEmailAddress = rs.getString(8);
	    	currentAdminRole = rs.getBoolean(9);
	    	currentContributorRole = rs.getBoolean(10);
	    	currentViewerRole = rs.getBoolean(11);
	    	currentIsOneTimePassword = rs.getBoolean(12);
			return true;
	    } catch (SQLException e) {
			return false;
	    }
	}
	
	
	/*******
	 * <p> Method: boolean updateUserRole(String username, String role, String value) </p>
	 * 
	 * <p> Description: Update a specified role for a specified user's and set and update all the
	 * 		current user attributes.</p>
	 * 
	 * @param username is the username of the user
	 *  
	 * @param role is string that specifies the role to update
	 * 
	 * @param value is the string that specified TRUE or FALSE for the role
	 * 
	 * @return true if the update was successful, else false
	 *  
	 */
	// Update a users role
	public boolean updateUserRole(String username, String role, String value) {
		if (role.compareTo("Admin") == 0) {
			String query = "UPDATE userDB SET adminRole = ? WHERE username = ?";
			try (PreparedStatement pstmt = connection.prepareStatement(query)) {
				pstmt.setString(1, value);
				pstmt.setString(2, username);
				pstmt.executeUpdate();
				if (value.compareTo("true") == 0)
					currentAdminRole = true;
				else
					currentAdminRole = false;
				return true;
			} catch (SQLException e) {
				return false;
			}
		}
		if (role.compareTo("Contributor") == 0) {
			String query = "UPDATE userDB SET contributorRole = ? WHERE username = ?";
			try (PreparedStatement pstmt = connection.prepareStatement(query)) {
				pstmt.setString(1, value);
				pstmt.setString(2, username);
				pstmt.executeUpdate();
				if (value.compareTo("true") == 0)
					currentContributorRole = true;
				else
					currentContributorRole = false;
				return true;
			} catch (SQLException e) {
				return false;
			}
		}
		if (role.compareTo("Viewer") == 0) {
			String query = "UPDATE userDB SET viewerRole = ? WHERE username = ?";
			try (PreparedStatement pstmt = connection.prepareStatement(query)) {
				pstmt.setString(1, value);
				pstmt.setString(2, username);
				pstmt.executeUpdate();
				if (value.compareTo("true") == 0)
					currentViewerRole = true;
				else
					currentViewerRole = false;
				return true;
			} catch (SQLException e) {
				return false;
			}
		}
		return false;
	}
	
	/*******
	 * <p> Method: void updatePassword(String username, String password) </p>
	 * 
	 * <p> Description: Update the password of a user given that user's username and the new
	 *		password.</p>
	 *
	 * @param username is the username of the user
	 *
	 * @param password is the new password for the user
	 *
	 */
	// Update a user's password
	public void updatePassword(String username, String password) {
	    String query = "UPDATE userDB SET password = ? WHERE username = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, password);
	        pstmt.setString(2, username);
	        pstmt.executeUpdate();
	        currentPassword = password;
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}


	/*******
	 * <p> Method: void setOneTimePassword(String username, String tempPassword) </p>
	 *
	 * <p> Description: Replace a user's password with a temporary one set by an admin and mark
	 *		it as a one-time password, so the user is required to set a new password the next
	 *		time they log in.</p>
	 *
	 * @param username is the username of the user
	 *
	 * @param tempPassword is the temporary password the user will use to log in once
	 *
	 */
	// Set a one-time password for a user
	public void setOneTimePassword(String username, String tempPassword) {
	    String query = "UPDATE userDB SET password = ?, isOneTimePassword = TRUE WHERE username = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, tempPassword);
	        pstmt.setString(2, username);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}


	/*******
	 * <p> Method: void clearOneTimePasswordFlag(String username) </p>
	 *
	 * <p> Description: Mark a user's password as a normal password once the user has replaced
	 *		their one-time password with a new one.</p>
	 *
	 * @param username is the username of the user
	 *
	 */
	// Clear the one-time password flag for a user
	public void clearOneTimePasswordFlag(String username) {
	    String query = "UPDATE userDB SET isOneTimePassword = FALSE WHERE username = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, username);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	

	/*******
	 * <p> Method: void updateUsername(String oldUsername, String newUsername) </p>
	 * 
	 * <p> Description: Update a user's username. The caller is responsible for checking
	 *		doesUserExist(newUsername) first to avoid a duplicate-key error.</p>
	 *
	 * @param oldUsername is the user's current username
	 *
	 * @param newUsername is the username that will replace it
	 *
	 */
	// Update a user's username
	public void updateUsername(String oldUsername, String newUsername) {
	    String userQuery = "UPDATE userDB SET username = ? WHERE username = ?";
	    String lessonQuery = "UPDATE lessonsLearnedDB SET contributorUserName = ? WHERE contributorUserName = ?";
	    String experienceQuery = "UPDATE experienceRecordsDB SET contributorUserName = ? WHERE contributorUserName = ?";
	    boolean autoCommit = true;
	    try {
	        autoCommit = connection.getAutoCommit();
	        connection.setAutoCommit(false);
	        try (PreparedStatement userStatement = connection.prepareStatement(userQuery);
	             PreparedStatement lessonStatement = connection.prepareStatement(lessonQuery);
	             PreparedStatement experienceStatement = connection.prepareStatement(experienceQuery)) {
	            userStatement.setString(1, newUsername);
	            userStatement.setString(2, oldUsername);
	            userStatement.executeUpdate();

	            lessonStatement.setString(1, newUsername);
	            lessonStatement.setString(2, oldUsername);
	            lessonStatement.executeUpdate();

	            experienceStatement.setString(1, newUsername);
	            experienceStatement.setString(2, oldUsername);
	            experienceStatement.executeUpdate();
        }
        connection.commit();
        currentUsername = newUsername;
	    } catch (SQLException e) {
	        try { connection.rollback(); } catch (SQLException rollbackError) { /* preserve original error */ }
	        e.printStackTrace();
	    } finally {
	        try { connection.setAutoCommit(autoCommit); } catch (SQLException e) { e.printStackTrace(); }
	    }
	}

	/*******
	 * <p> Method: boolean deleteUser(String username) </p>
	 * 
	 * <p> Description: Delete a user account given that user's username.</p>
	 *
	 * @param username is the username of the user to be deleted
	 *
	 * @return true if a row was actually deleted, false otherwise
	 *
	 */
	// Delete a user account
	public boolean deleteUser(String username) {
	    String userQuery = "DELETE FROM userDB WHERE username = ?";
	    String lessonQuery = "DELETE FROM lessonsLearnedDB WHERE contributorUserName = ?";
	    boolean autoCommit = true;
	    try {
	        autoCommit = connection.getAutoCommit();
	        connection.setAutoCommit(false);
	        int rowsAffected;
	        try (PreparedStatement userStatement = connection.prepareStatement(userQuery);
	             PreparedStatement lessonStatement = connection.prepareStatement(lessonQuery)) {
	            userStatement.setString(1, username);
	            rowsAffected = userStatement.executeUpdate();
	            if (rowsAffected > 0) {
	                lessonStatement.setString(1, username);
	                lessonStatement.executeUpdate();
	            }
	        }
	        connection.commit();
	        return rowsAffected > 0;
	    } catch (SQLException e) {
	        try { connection.rollback(); } catch (SQLException rollbackError) { /* preserve original error */ }
	        e.printStackTrace();
	        return false;
	    } finally {
	        try { connection.setAutoCommit(autoCommit); } catch (SQLException e) { e.printStackTrace(); }
	    }
	}
	
	/*******
	 * <p> Method: List&lt;String[]&gt; getAllUsersDetails() </p>
	 * 
	 * <p> Description: Returns one String[4] per user: {username, full name, email, roles},
	 * for the admin's "List All Users" and "Delete a User" screens.</p>
	 *
	 * @return a list with the details of every user account
	 *
	 */
	// Get the details of every user account
	public List<String[]> getAllUsersDetails() {
	    List<String[]> details = new ArrayList<String[]>();
	    String query = "SELECT userName, firstName, lastName, emailAddress, adminRole, "
	            + "contributorRole, viewerRole FROM userDB";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        ResultSet rs = pstmt.executeQuery();
	        while (rs.next()) {
	            String roles = "";
	            if (rs.getBoolean("adminRole")) roles += "Admin ";
	            if (rs.getBoolean("contributorRole")) roles += "Contributor ";
	            if (rs.getBoolean("viewerRole")) roles += "Viewer ";
	            if (roles.isEmpty()) roles = "<none>";

	            String first = rs.getString("firstName");
	            String last = rs.getString("lastName");
	            String fullName = ((first == null ? "" : first) + " " + (last == null ? "" : last)).trim();
	            if (fullName.isEmpty()) fullName = "<none>";

	            String email = rs.getString("emailAddress");
	            if (email == null || email.isEmpty()) email = "<none>";

	            details.add(new String[] { rs.getString("userName"), fullName, email, roles.trim() });
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return details;
	}
	
	
	// Attribute getters for the current user
	/*******
	 * <p> Method: String getCurrentUsername() </p>
	 * 
	 * <p> Description: Get the current user's username.</p>
	 * 
	 * @return the username value is returned
	 *  
	 */
	public String getCurrentUsername() { return currentUsername;};

	
	/*******
	 * <p> Method: String getCurrentPassword() </p>
	 * 
	 * <p> Description: Get the current user's password.</p>
	 * 
	 * @return the password value is returned
	 *  
	 */
	public String getCurrentPassword() { return currentPassword;};

	
	/*******
	 * <p> Method: String getCurrentFirstName() </p>
	 * 
	 * <p> Description: Get the current user's first name.</p>
	 * 
	 * @return the first name value is returned
	 *  
	 */
	public String getCurrentFirstName() { return currentFirstName;};

	
	/*******
	 * <p> Method: String getCurrentMiddleName() </p>
	 * 
	 * <p> Description: Get the current user's middle name.</p>
	 * 
	 * @return the middle name value is returned
	 *  
	 */
	public String getCurrentMiddleName() { return currentMiddleName;};

	
	/*******
	 * <p> Method: String getCurrentLastName() </p>
	 * 
	 * <p> Description: Get the current user's last name.</p>
	 * 
	 * @return the last name value is returned
	 *  
	 */
	public String getCurrentLastName() { return currentLastName;};

	
	/*******
	 * <p> Method: String getCurrentPreferredFirstName( </p>
	 * 
	 * <p> Description: Get the current user's preferred first name.</p>
	 * 
	 * @return the preferred first name value is returned
	 *  
	 */
	public String getCurrentPreferredFirstName() { return currentPreferredFirstName;};

	
	/*******
	 * <p> Method: String getCurrentEmailAddress() </p>
	 * 
	 * <p> Description: Get the current user's email address name.</p>
	 * 
	 * @return the email address value is returned
	 *  
	 */
	public String getCurrentEmailAddress() { return currentEmailAddress;};

	
	/*******
	 * <p> Method: boolean getCurrentAdminRole() </p>
	 * 
	 * <p> Description: Get the current user's Admin role attribute.</p>
	 * 
	 * @return true if this user plays an Admin role, else false
	 *  
	 */
	public boolean getCurrentAdminRole() { return currentAdminRole;};

	
	/*******
	 * <p> Method: boolean getCurrentContributorRole() </p>
	 * 
	 * <p> Description: Get the current user's Contributor role attribute.</p>
	 * 
	 * @return true if this user plays a Contributor role, else false
	 *  
	 */
	public boolean getCurrentContributorRole() { return currentContributorRole;};

	
	/*******
	 * <p> Method: boolean getCurrentViewerRole() </p>
	 * 
	 * <p> Description: Get the current user's Viewer role attribute.</p>
	 * 
	 * @return true if this user plays a Viewer role, else false
	 *  
	 */
	public boolean getCurrentViewerRole() { return currentViewerRole;};


	/*******
	 * <p> Method: boolean getCurrentIsOneTimePassword() </p>
	 *
	 * <p> Description: Get whether the current user's password is a one-time password.</p>
	 *
	 * @return true if this user must set a new password after logging in, else false
	 *
	 */
	public boolean getCurrentIsOneTimePassword() { return currentIsOneTimePassword; }

	
	/*******
	 * <p> Debugging method</p>
	 * 
	 * <p> Description: Debugging method that dumps the database of the console.</p>
	 * 
	 * @throws SQLException if there is an issues accessing the database.
	 * 
	 */
	// Dumps the database.
	public void dump() throws SQLException {
		String query = "SELECT * FROM userDB";
		ResultSet resultSet = statement.executeQuery(query);
		ResultSetMetaData meta = resultSet.getMetaData();
		while (resultSet.next()) {
		for (int i = 0; i < meta.getColumnCount(); i++) {
		System.out.println(
		meta.getColumnLabel(i + 1) + ": " +
				resultSet.getString(i + 1));
		}
		System.out.println();
		}
		resultSet.close();
	}

	/** R1 create and R14 validation: validates every field, rejects a duplicate title, assigns the
	 * generated ID, and stores lock and coaching metadata so later reads can satisfy R4 and R10.
	 *
	 * @param lesson the lesson to validate and persist
	 * @return an empty string on success, or a user-facing error message
	 */
	public String createLessonLearned(LessonLearned lesson) {
		String errors = LessonLearnedValidator.checkLessonLearned(lesson);
		if (!errors.isEmpty()) return errors;
		try {
			if (contributorHasTitle(lesson.getContributorUserName().trim(), lesson.getTitle().trim(), -1))
				return "You already have a lesson with this title. Please use a different title.";
		} catch (SQLException e) {
			return "The lesson could not be checked for duplicate titles: " + e.getMessage();
		}
		String insert = "INSERT INTO lessonsLearnedDB (contributorUserName, title, category, situation, lessonText, recommendation, keywords, lessonLocked, titleLocked, categoryLocked, situationLocked, lessonTextLocked, recommendationLocked, keywordsLocked, curatorEdited, editedBy, summary, rootCause) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
			pstmt.setString(1, lesson.getContributorUserName().trim());
			pstmt.setString(2, lesson.getTitle().trim());
			pstmt.setString(3, lesson.getCategory());
			pstmt.setString(4, lesson.getSituation().trim());
			pstmt.setString(5, lesson.getLessonText().trim());
			pstmt.setString(6, trimOrEmpty(lesson.getRecommendation()));
			pstmt.setString(7, trimOrEmpty(lesson.getKeywords()));
			pstmt.setBoolean(8, lesson.isLessonLocked());
			pstmt.setBoolean(9, lesson.getLockedFields().contains("title"));
			pstmt.setBoolean(10, lesson.getLockedFields().contains("category"));
			pstmt.setBoolean(11, lesson.getLockedFields().contains("situation"));
			pstmt.setBoolean(12, lesson.getLockedFields().contains("lessonText"));
			pstmt.setBoolean(13, lesson.getLockedFields().contains("recommendation"));
			pstmt.setBoolean(14, lesson.getLockedFields().contains("keywords"));
			pstmt.setBoolean(15, lesson.isCuratorEdited());
			pstmt.setString(16, lesson.getEditedBy());
			pstmt.setString(17, trimOrEmpty(lesson.getSummary()));
			pstmt.setString(18, trimOrEmpty(lesson.getRootCause()));
			pstmt.executeUpdate();
			try (ResultSet keys = pstmt.getGeneratedKeys()) {
				if (keys.next()) lesson.setLessonID(keys.getInt(1));
			}
		} catch (SQLException e) {
			return "The lesson could not be saved: " + e.getMessage();
		}
		return "";
	}

	/** R2 read: reconstructs one lesson by ID, including every lock, curator, and coaching attribute.
	 *
	 * @param lessonID positive ID of the lesson to read
	 * @return the lesson, or null when it is missing or cannot be read
	 */
	public LessonLearned readLessonLearned(int lessonID) {
		lastLessonError = "";
		if (lessonID <= 0) {
			lastLessonError = "Lesson ID must be positive.";
			return null;
		}
		String query = "SELECT * FROM lessonsLearnedDB WHERE lessonID = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, lessonID);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) return lessonFromRow(rs);
			}
		} catch (SQLException e) {
			lastLessonError = "The lesson could not be read: " + e.getMessage();
		}
		if (lastLessonError.isEmpty()) lastLessonError = "Lesson #" + lessonID + " was not found.";
		return null;
	}

	/** R2 read and R7 subset source: returns every stored lesson in an unbounded list.
	 *
	 * @return all stored lessons, ordered by ID
	 */
	public LessonLearnedList readAllLessonsLearned() {
		return readLessonList("SELECT * FROM lessonsLearnedDB ORDER BY lessonID", null);
	}

	/** R2 read and R12 isolation: filters by owner so a Contributor never sees another Contributor's
	 * lessons.
	 *
	 * @param userName owner whose lessons should be returned
	 * @return the owner's lessons, ordered by ID
	 */
	public LessonLearnedList readLessonsLearnedForContributor(String userName) {
		return readLessonList("SELECT * FROM lessonsLearnedDB WHERE contributorUserName = ? ORDER BY lessonID", userName);
	}

	/** R3 update, R5 lock enforcement, R12 ownership, and R14 validation: rejects a missing lesson, a
	 * non-owner, a locked change, or invalid fields before writing.
	 *
	 * @param lesson the lesson containing the replacement values
	 * @param requestingUserName authenticated user requesting the update
	 * @return an empty string on success, or a user-facing error message
	 */
	public String updateLessonLearned(LessonLearned lesson, String requestingUserName) {
		if (lesson == null) return "No lesson was provided to update.";
		LessonLearned stored = readLessonLearned(lesson.getLessonID());
		String accessError = checkChangeAllowed(stored, lesson.getLessonID(), requestingUserName, "updated");
		if (!accessError.isEmpty()) return accessError;
		String lockError = checkLessonLockAllowsUpdate(stored, lesson);
		if (!lockError.isEmpty()) return lockError;
		lesson.setContributorUserName(stored.getContributorUserName());
		// Older Contributor forms do not edit these optional TP3 fields, so keep their stored values.
		if (lesson.getSummary() == null) lesson.setSummary(stored.getSummary());
		if (lesson.getRootCause() == null) lesson.setRootCause(stored.getRootCause());
		String errors = LessonLearnedValidator.checkLessonLearned(lesson);
		if (!errors.isEmpty()) return errors;
		try {
			if (contributorHasTitle(stored.getContributorUserName(), lesson.getTitle().trim(), lesson.getLessonID()))
				return "You already have another lesson with this title.";
		} catch (SQLException e) {
			return "The lesson could not be checked for duplicate titles: " + e.getMessage();
		}
		String update = "UPDATE lessonsLearnedDB SET title = ?, category = ?, situation = ?, lessonText = ?, recommendation = ?, keywords = ?, summary = ?, rootCause = ?, updatedAt = CURRENT_TIMESTAMP WHERE lessonID = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(update)) {
			pstmt.setString(1, lesson.getTitle().trim());
			pstmt.setString(2, lesson.getCategory());
			pstmt.setString(3, lesson.getSituation().trim());
			pstmt.setString(4, lesson.getLessonText().trim());
			pstmt.setString(5, trimOrEmpty(lesson.getRecommendation()));
			pstmt.setString(6, trimOrEmpty(lesson.getKeywords()));
			pstmt.setString(7, trimOrEmpty(lesson.getSummary()));
			pstmt.setString(8, trimOrEmpty(lesson.getRootCause()));
			pstmt.setInt(9, lesson.getLessonID());
			if (pstmt.executeUpdate() == 0) return "Lesson #" + lesson.getLessonID() + " was not found.";
		} catch (SQLException e) { return "The lesson could not be updated: " + e.getMessage(); }
		return "";
	}

	/** R6 delete with R5 lock enforcement and R12 ownership: the GUI confirms first; the database then
	 * rejects a missing, locked, or non-owned lesson before deleting.
	 *
	 * @param lessonID ID of the lesson to delete
	 * @param requestingUserName authenticated user requesting the delete
	 * @return an empty string on success, or a user-facing error message
	 */
	public String deleteLessonLearned(int lessonID, String requestingUserName) {
		LessonLearned stored = readLessonLearned(lessonID);
		String accessError = checkChangeAllowed(stored, lessonID, requestingUserName, "deleted");
		if (!accessError.isEmpty()) return accessError;
		String lockError = checkLessonLockAllowsDelete(stored);
		if (!lockError.isEmpty()) return lockError;
		try (PreparedStatement pstmt = connection.prepareStatement("DELETE FROM lessonsLearnedDB WHERE lessonID = ?")) {
			pstmt.setInt(1, lessonID);
			if (pstmt.executeUpdate() == 0) return "Lesson #" + lessonID + " was not found.";
		} catch (SQLException e) { return "The lesson could not be deleted: " + e.getMessage(); }
		return "";
	}

	/** Validates and persists one experience record and its per-member effort entries (R8).
	 *
	 * @param experience the experience record to persist
	 * @param requestingUserName authenticated user requesting the create
	 * @return an empty string on success, or a user-facing error message
	 */
	public String createExperienceRecord(ExperienceRecord experience, String requestingUserName) {
		if (experience == null) return "No experience record was provided.";
		String errors = LessonLearnedValidator.checkExperienceRecord(experience.getWhatWasDone(),
				experience.getHow(), experience.getDurationHours(), experience.getEffortHoursByMember());
		if (!errors.isEmpty()) return errors;
		LessonLearned parent = readLessonLearned(experience.getLessonID());
		if (parent == null) return "The lesson for this experience record was not found.";
		if (requestingUserName == null || !parent.getContributorUserName().equals(requestingUserName))
			return "Only the owner of the lesson can add experience records.";
		// The DTO's owner field is data, not proof of identity; trust the authenticated caller instead.
		experience.setContributorUserName(requestingUserName);

		boolean originalAutoCommit = true;
		try {
			originalAutoCommit = connection.getAutoCommit();
			connection.setAutoCommit(false);
			String insertExperience = "INSERT INTO experienceRecordsDB (lessonID, contributorUserName, whatWasDone, how, durationHours) VALUES (?, ?, ?, ?, ?)";
			try (PreparedStatement pstmt = connection.prepareStatement(insertExperience, Statement.RETURN_GENERATED_KEYS)) {
				pstmt.setInt(1, experience.getLessonID());
				pstmt.setString(2, experience.getContributorUserName());
				pstmt.setString(3, experience.getWhatWasDone().trim());
				pstmt.setString(4, experience.getHow().trim());
				pstmt.setDouble(5, experience.getDurationHours());
				pstmt.executeUpdate();
				try (ResultSet keys = pstmt.getGeneratedKeys()) {
					if (!keys.next()) throw new SQLException("No experience record ID was generated.");
					experience.setExperienceID(keys.getInt(1));
				}
			}
			replaceExperienceEfforts(experience.getExperienceID(), experience.getEffortHoursByMember());
			connection.commit();
		} catch (SQLException e) {
			try { connection.rollback(); } catch (SQLException rollbackError) { /* preserve original error */ }
			experience.setExperienceID(-1);
			return "The experience record could not be saved: " + e.getMessage();
		} finally {
			try { connection.setAutoCommit(originalAutoCommit); } catch (SQLException e) { e.printStackTrace(); }
		}
		return "";
	}

	/** R8 read: reconstructs an experience and its per-member effort entries by ID.
	 *
	 * @param experienceID positive ID of the experience to read
	 * @return the experience, or null when it is missing or cannot be read
	 */
	public ExperienceRecord readExperienceRecord(int experienceID) {
		lastExperienceError = "";
		if (experienceID <= 0) {
			lastExperienceError = "Experience ID must be positive.";
			return null;
		}
		String query = "SELECT * FROM experienceRecordsDB WHERE experienceID = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, experienceID);
			ExperienceRecord experience = null;
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					experience = new ExperienceRecord();
					experience.setExperienceID(rs.getInt("experienceID"));
					experience.setLessonID(rs.getInt("lessonID"));
					experience.setContributorUserName(rs.getString("contributorUserName"));
					experience.setWhatWasDone(rs.getString("whatWasDone"));
					experience.setHow(rs.getString("how"));
					experience.setDurationHours(rs.getDouble("durationHours"));
					experience.setCreatedAt(rs.getTimestamp("createdAt"));
					experience.setUpdatedAt(rs.getTimestamp("updatedAt"));
				}
			}
			if (experience == null) {
				lastExperienceError = "Experience #" + experienceID + " was not found.";
				return null;
			}
			Map<String, Double> effortHoursByMember = new LinkedHashMap<>();
			String effortQuery = "SELECT teamMemberName, effortHours FROM experienceEffortDB WHERE experienceID = ? ORDER BY teamMemberName";
			try (PreparedStatement effortStatement = connection.prepareStatement(effortQuery)) {
				effortStatement.setInt(1, experienceID);
				try (ResultSet efforts = effortStatement.executeQuery()) {
					while (efforts.next())
						effortHoursByMember.put(efforts.getString("teamMemberName"), efforts.getDouble("effortHours"));
				}
			}
			experience.setEffortHoursByMember(effortHoursByMember);
			return experience;
		} catch (SQLException e) {
			lastExperienceError = "The experience record could not be read: " + e.getMessage();
			return null;
		}
	}

	/** R8 read and R12 isolation: lists only the requesting Contributor's records for a lesson.
	 *
	 * @param lessonID ID of the lesson whose experiences should be read
	 * @param contributorUserName owner whose records should be returned
	 * @return the matching experience records, possibly empty
	 */
	public List<ExperienceRecord> readExperienceRecordsForLesson(int lessonID, String contributorUserName) {
		lastExperienceError = "";
		List<ExperienceRecord> experiences = new ArrayList<>();
		if (lessonID <= 0 || contributorUserName == null) return experiences;
		String query = "SELECT experienceID FROM experienceRecordsDB WHERE lessonID = ? AND contributorUserName = ? ORDER BY experienceID";
		List<Integer> experienceIDs = new ArrayList<>();
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, lessonID);
			pstmt.setString(2, contributorUserName);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) experienceIDs.add(rs.getInt("experienceID"));
			}
		} catch (SQLException e) {
			lastExperienceError = "The lesson experiences could not be read: " + e.getMessage();
			return experiences;
		}
		for (int experienceID : experienceIDs) {
			ExperienceRecord experience = readExperienceRecord(experienceID);
			if (experience != null) experiences.add(experience);
		}
		return experiences;
	}

	/** Returns the most recent experience read error, or an empty string after a successful read.
	 *
	 * @return the last experience error message, or an empty string
	 */
	public String getLastExperienceError() { return lastExperienceError; }

	/** R8 update and R12 ownership: the stored owner must match the requester before the record and
	 * its effort rows are replaced in one transaction.
	 *
	 * @param experience the experience containing the replacement values
	 * @param requestingUserName authenticated user requesting the update
	 * @return an empty string on success, or a user-facing error message
	 */
	public String updateExperienceRecord(ExperienceRecord experience, String requestingUserName) {
		if (experience == null) return "No experience record was provided to update.";
		ExperienceRecord stored = readExperienceRecord(experience.getExperienceID());
		String accessError = checkExperienceChangeAllowed(stored, experience.getExperienceID(),
				requestingUserName, "update");
		if (!accessError.isEmpty()) return accessError;
		experience.setLessonID(stored.getLessonID());
		experience.setContributorUserName(stored.getContributorUserName());
		String errors = LessonLearnedValidator.checkExperienceRecord(experience.getWhatWasDone(),
				experience.getHow(), experience.getDurationHours(), experience.getEffortHoursByMember());
		if (!errors.isEmpty()) return errors;

		boolean originalAutoCommit = true;
		try {
			originalAutoCommit = connection.getAutoCommit();
			connection.setAutoCommit(false);
			String update = "UPDATE experienceRecordsDB SET whatWasDone = ?, how = ?, durationHours = ?, updatedAt = CURRENT_TIMESTAMP WHERE experienceID = ? AND contributorUserName = ?";
			try (PreparedStatement pstmt = connection.prepareStatement(update)) {
				pstmt.setString(1, experience.getWhatWasDone().trim());
				pstmt.setString(2, experience.getHow().trim());
				pstmt.setDouble(3, experience.getDurationHours());
				pstmt.setInt(4, experience.getExperienceID());
				pstmt.setString(5, requestingUserName);
				if (pstmt.executeUpdate() == 0) throw new SQLException("Experience record was not found.");
			}
			replaceExperienceEfforts(experience.getExperienceID(), experience.getEffortHoursByMember());
			connection.commit();
		} catch (SQLException e) {
			try { connection.rollback(); } catch (SQLException rollbackError) { /* preserve original error */ }
			return "The experience record could not be updated: " + e.getMessage();
		} finally {
			try { connection.setAutoCommit(originalAutoCommit); } catch (SQLException e) { e.printStackTrace(); }
		}
		return "";
	}

	/** R8 delete and R12 ownership: only the stored owner may delete the record and its effort rows.
	 *
	 * @param experienceID ID of the experience to delete
	 * @param requestingUserName authenticated user requesting the delete
	 * @return an empty string on success, or a user-facing error message
	 */
	public String deleteExperienceRecord(int experienceID, String requestingUserName) {
		ExperienceRecord stored = readExperienceRecord(experienceID);
		String accessError = checkExperienceChangeAllowed(stored, experienceID, requestingUserName, "delete");
		if (!accessError.isEmpty()) return accessError;
		String delete = "DELETE FROM experienceRecordsDB WHERE experienceID = ? AND contributorUserName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(delete)) {
			pstmt.setInt(1, experienceID);
			pstmt.setString(2, requestingUserName);
			if (pstmt.executeUpdate() == 0) return "Experience record was not found.";
		} catch (SQLException e) {
			return "The experience record could not be deleted: " + e.getMessage();
		}
		return "";
	}

	/** R12: verifies that the requester owns the stored experience record.
	 *
	 * @param stored the stored record, or null when it was not found
	 * @param experienceID ID used in error messages
	 * @param requestingUserName authenticated user requesting the change
	 * @param action action name used in error messages
	 * @return an empty string when allowed, or an error message
	 */
	private String checkExperienceChangeAllowed(ExperienceRecord stored, int experienceID,
			String requestingUserName, String action) {
		if (stored == null)
			return lastExperienceError.isEmpty() ? "Experience #" + experienceID + " was not found." : lastExperienceError;
		if (requestingUserName == null || !stored.getContributorUserName().equals(requestingUserName))
			return "Only the owner can " + action + " this experience record.";
		return "";
	}

	/** R8 effort storage: replaces the per-member rows so an update cannot leave stale effort data.
	 *
	 * @param experienceID ID of the experience whose effort rows are replaced
	 * @param efforts member names mapped to effort hours
	 * @throws SQLException when the effort rows cannot be replaced
	 */
	private void replaceExperienceEfforts(int experienceID, Map<String, Double> efforts) throws SQLException {
		try (PreparedStatement delete = connection.prepareStatement(
				"DELETE FROM experienceEffortDB WHERE experienceID = ?")) {
			delete.setInt(1, experienceID);
			delete.executeUpdate();
		}
		String insert = "INSERT INTO experienceEffortDB (experienceID, teamMemberName, effortHours) VALUES (?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(insert)) {
			for (Map.Entry<String, Double> effort : efforts.entrySet()) {
				pstmt.setInt(1, experienceID);
				pstmt.setString(2, effort.getKey());
				pstmt.setDouble(3, effort.getValue());
				pstmt.executeUpdate();
			}
		}
	}

	/** R1 create and R3 update duplicate-title rule: comparison is case-insensitive and ignores the
	 * row being updated.
	 *
	 * @param userName owner whose titles are checked
	 * @param title title to compare
	 * @param excludeID lesson ID to exclude, or -1 for a create
	 * @return true when the owner already has the title
	 * @throws SQLException when the check cannot be completed
	 */
	private boolean contributorHasTitle(String userName, String title, int excludeID) throws SQLException {
		String query = "SELECT COUNT(*) FROM lessonsLearnedDB WHERE contributorUserName = ? AND LOWER(title) = LOWER(?) AND lessonID <> ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, userName);
			pstmt.setString(2, title);
			pstmt.setInt(3, excludeID);
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next() && rs.getInt(1) > 0;
			}
		}
	}

	/** R12 isolation: a lesson change is allowed only when the stored owner matches the requester.
	 *
	 * @param stored the stored lesson, or null when it was not found
	 * @param lessonID ID used in error messages
	 * @param requestingUserName authenticated user requesting the change
	 * @param action action name used in error messages
	 * @return an empty string when allowed, or an error message
	 */
	private String checkChangeAllowed(LessonLearned stored, int lessonID, String requestingUserName, String action) {
		if (stored == null) return lastLessonError.isEmpty() ? "Lesson #" + lessonID + " was not found." : lastLessonError;
		if (requestingUserName == null || !stored.getContributorUserName().equals(requestingUserName))
			return "Lesson #" + lessonID + " can't be " + action + " because it belongs to another Contributor.";
		return "";
	}

	/** R5: Rejects whole-lesson locks and edits that change explicitly locked fields.
	 *
	 * @param stored the currently stored lesson
	 * @param replacement the requested replacement values
	 * @return an empty string when the update is allowed, or an error message
	 */
	private String checkLessonLockAllowsUpdate(LessonLearned stored, LessonLearned replacement) {
		String wholeLessonError = checkWholeLessonLock(stored, "updated");
		if (!wholeLessonError.isEmpty()) return wholeLessonError;
		for (String field : stored.getLockedFields()) {
			String oldValue = lessonFieldValue(stored, field);
			String newValue = lessonFieldValue(replacement, field);
			if (!Objects.equals(oldValue == null ? "" : oldValue, newValue == null ? "" : newValue))
				return "Lesson #" + stored.getLessonID() + " field '" + field + "' is locked and cannot be changed.";
		}
		return "";
	}

	/** R5: A whole-lesson lock or any field lock prevents deleting the lesson.
	 *
	 * @param stored the lesson to check
	 * @return an empty string when deletion is allowed, or an error message
	 */
	private String checkLessonLockAllowsDelete(LessonLearned stored) {
		String wholeLessonError = checkWholeLessonLock(stored, "deleted");
		if (!wholeLessonError.isEmpty()) return wholeLessonError;
		if (!stored.getLockedFields().isEmpty())
			return "Lesson #" + stored.getLessonID() + " cannot be deleted because one or more fields are locked.";
		return "";
	}

	/** Shared whole-lesson lock message for edit and delete checks.
	 *
	 * @param stored the lesson to check
	 * @param action action name used in the error message
	 * @return an empty string when unlocked, or an error message
	 */
	private String checkWholeLessonLock(LessonLearned stored, String action) {
		return stored.isLessonLocked()
				? "Lesson #" + stored.getLessonID() + " is locked and cannot be " + action + "."
				: "";
	}

	/** Returns the value of one lockable lesson field.
	 *
	 * @param lesson the lesson to inspect
	 * @param field the lockable field name
	 * @return the field value, possibly null
	 * @throws IllegalArgumentException if the field is not lockable
	 */
	private String lessonFieldValue(LessonLearned lesson, String field) {
		return switch (field) {
			case "title" -> lesson.getTitle();
			case "category" -> lesson.getCategory();
			case "situation" -> lesson.getSituation();
			case "lessonText" -> lesson.getLessonText();
			case "recommendation" -> lesson.getRecommendation();
			case "keywords" -> lesson.getKeywords();
			default -> throw new IllegalArgumentException("Unknown lockable lesson field: " + field);
		};
	}

	/** Executes a lesson query and builds a list from the result rows.
	 *
	 * @param query SQL query to execute
	 * @param parameter optional first parameter, or null when the query has none
	 * @return the resulting lesson list, possibly empty
	 */
	private LessonLearnedList readLessonList(String query, String parameter) {
		lastLessonError = "";
		LessonLearnedList list = new LessonLearnedList();
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			if (parameter != null) pstmt.setString(1, parameter);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) list.addLesson(lessonFromRow(rs));
			}
		} catch (SQLException e) {
			lastLessonError = "The lessons could not be read: " + e.getMessage();
		}
		return list;
	}

	/** Returns the most recent database read error, or an empty string after a successful read.
	 *
	 * @return the last lesson error message, or an empty string
	 */
	public String getLastLessonError() { return lastLessonError; }

	/** Reconstructs a lesson from its current result-set row.
	 *
	 * @param rs result set positioned at the lesson row
	 * @return the populated lesson
	 * @throws SQLException when a column cannot be read
	 */
	private LessonLearned lessonFromRow(ResultSet rs) throws SQLException {
		LessonLearned lesson = new LessonLearned();
		lesson.setLessonID(rs.getInt("lessonID"));
		lesson.setContributorUserName(rs.getString("contributorUserName"));
		lesson.setTitle(rs.getString("title"));
		lesson.setCategory(rs.getString("category"));
		lesson.setSituation(rs.getString("situation"));
		lesson.setLessonText(rs.getString("lessonText"));
		lesson.setRecommendation(rs.getString("recommendation"));
		lesson.setKeywords(rs.getString("keywords"));
		lesson.setCreatedAt(rs.getTimestamp("createdAt"));
		lesson.setUpdatedAt(rs.getTimestamp("updatedAt"));
		lesson.setLessonLocked(rs.getBoolean("lessonLocked"));
		lesson.setFieldLocked("title", rs.getBoolean("titleLocked"));
		lesson.setFieldLocked("category", rs.getBoolean("categoryLocked"));
		lesson.setFieldLocked("situation", rs.getBoolean("situationLocked"));
		lesson.setFieldLocked("lessonText", rs.getBoolean("lessonTextLocked"));
		lesson.setFieldLocked("recommendation", rs.getBoolean("recommendationLocked"));
		lesson.setFieldLocked("keywords", rs.getBoolean("keywordsLocked"));
		lesson.setCuratorEdited(rs.getBoolean("curatorEdited"));
		lesson.setEditedBy(rs.getString("editedBy"));
		lesson.setSummary(rs.getString("summary"));
		lesson.setRootCause(rs.getString("rootCause"));
		return lesson;
	}

	/** Trims a nullable string, returning an empty string for null.
	 *
	 * @param value the string to trim
	 * @return the trimmed string, or an empty string
	 */
	private static String trimOrEmpty(String value) {
		return value == null ? "" : value.trim();
	}


	/*******
	 * <p> Method: void closeConnection()</p>
	 * 
	 * <p> Description: Closes the database statement and connection.</p>
	 * 
	 */
	// Closes the database statement and connection.
	public void closeConnection() {
		try{ 
			if(statement!=null) statement.close(); 
		} catch(SQLException se2) { 
			se2.printStackTrace();
		} 
		try { 
			if(connection!=null) connection.close(); 
		} catch(SQLException se){ 
			se.printStackTrace(); 
		} 
	}
}
