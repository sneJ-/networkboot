package de.rowekamp.networkboot.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BootScriptDatabase {

	private Connection connection;
	private Statement statement;
	private PreparedStatement pstmt;
	private ResultSet resultSet;

	/**
	 * Creates the database, connects to the given file and enables foreign key
	 * constrain checks.
	 * 
	 * @param sqliteDB
	 */
	public BootScriptDatabase(File sqliteDB) {
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:"
					+ sqliteDB.getPath());
			statement = connection.createStatement();
			statement.executeUpdate("PRAGMA foreign_keys=ON");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Finds a hostId regarding to the given mac.
	 * 
	 * @param mac
	 * @return -2 if an error occurs, -1 if the host coudln't be found, 0 if the
	 *         host isn't validated and hostId if the host could be found.
	 */
	public int getHostId(String mac) {
		int hostId = -1;
		try {
			pstmt = connection
					.prepareStatement("SELECT id, validated FROM hosts WHERE mac = ?");
			pstmt.setString(1, mac);
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				hostId = resultSet.getInt("id");
				if (resultSet.getInt("validated") == 0) {
					hostId = 0;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			hostId = -2;
		}
		return hostId;
	}

	/**
	 * Adds a host to the database.
	 * 
	 * @param mac
	 * @param hostname
	 * @throws SQLException
	 *             if i.e. the hostname exists
	 */
	public void addHost(String mac, String hostname) throws SQLException {
		pstmt = connection
				.prepareStatement("INSERT INTO hosts (mac, name) VALUES (?, ?);");
		pstmt.setString(1, mac);
		pstmt.setString(2, hostname);
		pstmt.execute();
	}
	
	/**
	 * Retrieves the mapped server rootURL regarding to the given image.
	 * @param imageId
	 * @return
	 */
	public String getRootURL(int imageId) throws SQLException{
		String rootURL = "";
		pstmt = connection
				.prepareStatement("SELECT rootURL FROM servers, images WHERE images.id = ? AND images.serverId = servers.id;");
		pstmt.setLong(1, imageId);
		resultSet = pstmt.executeQuery();
		while (resultSet.next()) {
			rootURL = resultSet.getString("rootURL");
		}
		return rootURL;
	}
	
	/**
	 * Retrieves the image's script.
	 * 
	 * @param imageId
	 * @return script or null if no script could be found.
	 */
	public String getImageScript(int imageId) throws SQLException {
		String ipxeScript = null;
		pstmt = connection
				.prepareStatement("SELECT script FROM images WHERE id = ?;");
		pstmt.setLong(1, imageId);
		resultSet = pstmt.executeQuery();
		while (resultSet.next()) {
			ipxeScript = resultSet.getString("script");
		}
		return ipxeScript;
	}

	/**
	 * Retrieves the image's variables and returns them as HashMap.
	 * 
	 * @param imageId
	 * @return
	 */
	public Map<String, String> getImageVariables(int imageId)
			throws SQLException {
		Map<String, String> imageVariables = new HashMap<String, String>();
		pstmt = connection
				.prepareStatement("SELECT variableName, variableValue FROM imageVariables WHERE imageId = ?;");
		pstmt.setLong(1, imageId);
		resultSet = pstmt.executeQuery();
		while (resultSet.next()) {
			imageVariables.put(resultSet.getString("variableName"),
					resultSet.getString("variableValue"));
		}
		return imageVariables;
	}

	/**
	 * Retrieves the global variables and returns them as HashMap.
	 * 
	 * @return
	 */
	public Map<String, String> getGlobalVariables() throws SQLException {
		Map<String, String> globalVariables = new HashMap<String, String>();
		pstmt = connection
				.prepareStatement("SELECT variableName, variableValue FROM globalVariables;");
		resultSet = pstmt.executeQuery();
		while (resultSet.next()) {
			globalVariables.put(resultSet.getString("variableName"),
					resultSet.getString("variableValue"));
		}
		return globalVariables;
	}

	/**
	 * Retrieves the HostImageMapping's variables and returns them as HashMap.
	 * 
	 * @param hostImageMappingId
	 * @return
	 */
	public Map<String, String> getHostImageMappingVariables(
			int hostImageMappingId) throws SQLException {
		Map<String, String> hostImageMappingVariables = new HashMap<String, String>();
		pstmt = connection
				.prepareStatement("SELECT variableName, hostImageMappingVariables.variableValue FROM imageVariables, hostImageMappingVariables WHERE hostImageMappingId = ? AND imageVariableId = imageVariables.id;");
		pstmt.setLong(1, hostImageMappingId);
		resultSet = pstmt.executeQuery();
		while (resultSet.next()) {
			hostImageMappingVariables.put(resultSet.getString("variableName"),
					resultSet.getString("variableValue"));
		}
		return hostImageMappingVariables;
	}

	/**
	 * Retrieves the GroupImageMapping's variables and returns them as HashMap.
	 * 
	 * @param hostImageMappingId
	 * @return
	 */
	public Map<String, String> getGroupImageMappingVariables(
			int groupImageMappingId) throws SQLException {
		Map<String, String> groupImageMappingVariables = new HashMap<String, String>();
		pstmt = connection
				.prepareStatement("SELECT variableName, groupImageMappingVariables.variableValue FROM imageVariables, groupImageMappingVariables WHERE groupImageMappingId = ? AND imageVariableId = imageVariables.id;");
		pstmt.setLong(1, groupImageMappingId);
		resultSet = pstmt.executeQuery();
		while (resultSet.next()) {
			groupImageMappingVariables.put(resultSet.getString("variableName"),
					resultSet.getString("variableValue"));
		}
		return groupImageMappingVariables;
	}

	/**
	 * Finds the corresponding image with the highest priority regarding to the
	 * system time without authentication.
	 * 
	 * @param hostId
	 * @return Object[boolean hostImage, int imageId, int imageMappingId]
	 */
	public Object[] findImage(int hostId) throws SQLException {
		boolean hostImage = false;
		int imageId = 0;
		int imageMappingId = 0;
		int priority = 0;

		// determine highest groupImageMapping untimed
		pstmt = connection
				.prepareStatement("SELECT groupImageMappings.id, imageId, priority FROM groupImageMappings, hostGroupMappings WHERE hostId = ? AND hostGroupMappings.groupId=groupImageMappings.groupId AND timed=0 ORDER BY priority DESC LIMIT 1");
		pstmt.setLong(1, hostId);
		resultSet = pstmt.executeQuery();
		while (resultSet.next()) {
			imageId = resultSet.getInt("imageId");
			priority = resultSet.getInt("priority");
			imageMappingId = resultSet.getInt("id");
		}

		// determine highest hostImageMapping untimed
		pstmt = connection
				.prepareStatement("SELECT id, imageId, priority FROM hostImageMappings WHERE hostId=? AND priority>=? AND timed=0 ORDER BY priority DESC LIMIT 1");
		pstmt.setLong(1, hostId);
		pstmt.setLong(2, priority);
		resultSet = pstmt.executeQuery();
		while (resultSet.next()) {
			imageId = resultSet.getInt("imageId");
			priority = resultSet.getInt("priority");
			imageMappingId = resultSet.getInt("id");
			hostImage = true;
		}

		// Get the actual time.
		Calendar cal = Calendar.getInstance(Locale.getDefault());
		int dow = cal.get(Calendar.DAY_OF_WEEK) - 1;
		int dom = cal.get(Calendar.DAY_OF_MONTH);
		int month = cal.get(Calendar.MONTH) + 1;
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);

		// determine highest groupImageMapping timed
		pstmt = connection
				.prepareStatement("SELECT groupImageMappings.id, groupImageMappings.imageId, priority"
						+ " FROM groupImageMappings, hostGroupMappings, groupImageMappingTimes"
						+ " WHERE hostId = ?"
						+ " AND hostGroupMappings.groupId=groupImageMappings.groupId"
						+ " AND timed=1"
						+ " AND priority>=?"
						+ " AND groupImageMappingId=groupImageMappings.id"
						+ " AND (dom IS NULL OR dom = ?)"
						+ " AND ('month' IS NULL OR 'month' = ?)"
						+ " AND (dow IS NULL OR dow = ?)"
						+ " AND (?*60 + ? - minute - hour*60 < validMinutes)"
						+ " AND (?*60 + ? - minute - hour*60 >= 0)"
						+ " ORDER BY priority DESC LIMIT 1");
		pstmt.setLong(1, hostId);
		pstmt.setLong(2, priority);
		pstmt.setLong(3, dom);
		pstmt.setLong(4, month);
		pstmt.setLong(5, dow);
		pstmt.setLong(6, hour);
		pstmt.setLong(7, minute);
		pstmt.setLong(8, hour);
		pstmt.setLong(9, minute);
		resultSet = pstmt.executeQuery();
		while (resultSet.next()) {
			imageId = resultSet.getInt("imageId");
			priority = resultSet.getInt("priority");
			imageMappingId = resultSet.getInt("id");
			hostImage = false;
		}

		// determine highest hostImageMapping timed
		pstmt = connection
				.prepareStatement("SELECT hostImageMappings.id, hostImageMappings.imageId, priority"
						+ " FROM hostImageMappings, hostImageMappingTimes"
						+ " WHERE hostId=?"
						+ " AND priority>=?"
						+ " AND timed=1"
						+ " AND (dom IS NULL OR dom = ?)"
						+ " AND ('month' IS NULL OR 'month' = ?)"
						+ " AND (dow IS NULL OR dow = ?)"
						+ " AND (?*60 + ? - minute - hour*60 < validMinutes)"
						+ " AND (?*60 + ? - minute - hour*60 >= 0)"
						+ " ORDER BY priority DESC LIMIT 1");
		pstmt.setLong(1, hostId);
		pstmt.setLong(2, priority);
		pstmt.setLong(3, dom);
		pstmt.setLong(4, month);
		pstmt.setLong(5, dow);
		pstmt.setLong(6, hour);
		pstmt.setLong(7, minute);
		pstmt.setLong(8, hour);
		pstmt.setLong(9, minute);
		resultSet = pstmt.executeQuery();
		while (resultSet.next()) {
			imageId = resultSet.getInt("imageId");
			priority = resultSet.getInt("priority");
			imageMappingId = resultSet.getInt("id");
			hostImage = true;
		}

		Object[] image = { hostImage, imageId, imageMappingId };
		return image;
	}

	/**
	 * Returns the userId.
	 * 
	 * @param username
	 * @return id of the user, else 0.
	 */
	public int getUserId(String username) throws SQLException {
		int userId = 0;
		pstmt = connection
				.prepareStatement("SELECT id FROM users WHERE name = ?;");
		pstmt.setString(1, username);
		resultSet = pstmt.executeQuery();
		while (resultSet.next()) {
			userId = resultSet.getInt("id");
		}
		return userId;
	}

	/**
	 * Retrieves the UserImageMapping's variables and returns them as HashMap.
	 * 
	 * @param hostImageMappingId
	 * @return
	 */
	public Map<String, String> getUserImageMappingVariables(
			int userImageMappingId) throws SQLException {
		Map<String, String> userImageMappingVariables = new HashMap<String, String>();
		pstmt = connection
				.prepareStatement("SELECT variableName, userImageMappingVariables.variableValue FROM imageVariables, userImageMappingVariables WHERE userImageMappingId = ? AND imageVariableId = imageVariables.id;");
		pstmt.setLong(1, userImageMappingId);
		resultSet = pstmt.executeQuery();
		while (resultSet.next()) {
			userImageMappingVariables.put(resultSet.getString("variableName"),
					resultSet.getString("variableValue"));
		}
		return userImageMappingVariables;
	}

	/**
	 * Retrieves the UserGroupImageMapping's variables and returns them as
	 * HashMap.
	 * 
	 * @param hostImageMappingId
	 * @return
	 */
	public Map<String, String> getUserGroupImageMappingVariables(
			int userGroupImageMappingId) throws SQLException {
		Map<String, String> userGroupImageMappingVariables = new HashMap<String, String>();
		pstmt = connection
				.prepareStatement("SELECT variableName, userGroupImageMappingVariables.variableValue FROM imageVariables, userGroupImageMappingVariables WHERE userGroupImageMappingId = ? AND imageVariableId = imageVariables.id;");
		pstmt.setLong(1, userGroupImageMappingId);
		resultSet = pstmt.executeQuery();
		while (resultSet.next()) {
			userGroupImageMappingVariables.put(
					resultSet.getString("variableName"),
					resultSet.getString("variableValue"));
		}
		return userGroupImageMappingVariables;
	}

	/**
	 * Finds the corresponding image with the highest priority regarding to the
	 * system time without authentication.
	 * 
	 * @param username
	 * @return Object[boolean userImage, int imageId, int imageMappingId]
	 */
	public Object[] findUserImage(String username) throws SQLException {
		int userId = getUserId(username);
		boolean userImage = false;
		int imageId = 0;
		int imageMappingId = 0;
		int priority = 0;

		// determine highest userGroupImageMapping untimed
		pstmt = connection
				.prepareStatement("SELECT userGroupImageMappings.id, imageId, priority FROM userGroupImageMappings, userGroupMappings WHERE userId = ? AND userGroupMappings.groupId=userGroupImageMappings.groupId AND timed=0 ORDER BY priority DESC LIMIT 1");
		pstmt.setLong(1, userId);
		resultSet = pstmt.executeQuery();
		while (resultSet.next()) {
			imageId = resultSet.getInt("imageId");
			priority = resultSet.getInt("priority");
			imageMappingId = resultSet.getInt("id");
		}

		// determine highest hostImageMapping untimed
		pstmt = connection
				.prepareStatement("SELECT id, imageId, priority FROM userImageMappings WHERE userId=? AND priority>=? AND timed=0 ORDER BY priority DESC LIMIT 1");
		pstmt.setLong(1, userId);
		pstmt.setLong(2, priority);
		resultSet = pstmt.executeQuery();
		while (resultSet.next()) {
			imageId = resultSet.getInt("imageId");
			priority = resultSet.getInt("priority");
			imageMappingId = resultSet.getInt("id");
			userImage = true;
		}

		// Get the actual time.
		Calendar cal = Calendar.getInstance(Locale.getDefault());
		int dow = cal.get(Calendar.DAY_OF_WEEK) - 1;
		int dom = cal.get(Calendar.DAY_OF_MONTH);
		int month = cal.get(Calendar.MONTH) + 1;
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);

		// determine highest groupImageMapping timed
		pstmt = connection
				.prepareStatement("SELECT userGroupImageMappings.id, userGroupImageMappings.imageId, priority"
						+ " FROM userGroupImageMappings, userGroupMappings, userGroupImageMappingTimes"
						+ " WHERE userId = ?"
						+ " AND userGroupMappings.groupId=userGroupImageMappings.groupId"
						+ " AND timed=1"
						+ " AND priority>=?"
						+ " AND userGroupImageMappingId=userGroupImageMappings.id"
						+ " AND (dom IS NULL OR dom = ?)"
						+ " AND ('month' IS NULL OR 'month' = ?)"
						+ " AND (dow IS NULL OR dow = ?)"
						+ " AND (?*60 + ? - minute - hour*60 < validMinutes)"
						+ " AND (?*60 + ? - minute - hour*60 >= 0)"
						+ " ORDER BY priority DESC LIMIT 1");
		pstmt.setLong(1, userId);
		pstmt.setLong(2, priority);
		pstmt.setLong(3, dom);
		pstmt.setLong(4, month);
		pstmt.setLong(5, dow);
		pstmt.setLong(6, hour);
		pstmt.setLong(7, minute);
		pstmt.setLong(8, hour);
		pstmt.setLong(9, minute);
		resultSet = pstmt.executeQuery();
		while (resultSet.next()) {
			imageId = resultSet.getInt("imageId");
			priority = resultSet.getInt("priority");
			imageMappingId = resultSet.getInt("id");
			userImage = false;
		}

		// determine highest hostImageMapping timed
		pstmt = connection
				.prepareStatement("SELECT userImageMappings.id, userImageMappings.imageId, priority"
						+ " FROM userImageMappings, userImageMappingTimes"
						+ " WHERE userId=?"
						+ " AND priority>=?"
						+ " AND timed=1"
						+ " AND (dom IS NULL OR dom = ?)"
						+ " AND ('month' IS NULL OR 'month' = ?)"
						+ " AND (dow IS NULL OR dow = ?)"
						+ " AND (?*60 + ? - minute - hour*60 < validMinutes)"
						+ " AND (?*60 + ? - minute - hour*60 >= 0)"
						+ " ORDER BY priority DESC LIMIT 1");
		pstmt.setLong(1, userId);
		pstmt.setLong(2, priority);
		pstmt.setLong(3, dom);
		pstmt.setLong(4, month);
		pstmt.setLong(5, dow);
		pstmt.setLong(6, hour);
		pstmt.setLong(7, minute);
		pstmt.setLong(8, hour);
		pstmt.setLong(9, minute);
		resultSet = pstmt.executeQuery();
		while (resultSet.next()) {
			imageId = resultSet.getInt("imageId");
			priority = resultSet.getInt("priority");
			imageMappingId = resultSet.getInt("id");
			userImage = true;
		}

		Object[] image = { userImage, imageId, imageMappingId };
		return image;
	}

	/**
	 * Closes the sql statements and database.
	 */
	public void close() {
		try {
			statement.close();
			if (pstmt != null)
				pstmt.close();
			connection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}