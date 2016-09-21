package de.rowekamp.networkboot.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class AuthDatabase {

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
	public AuthDatabase(File sqliteDB) {
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
	 * Checks if the given host is allowed to login.
	 * 
	 * @param mac
	 * @return
	 */
	public boolean isHostAllowedToLogin(String mac) throws SQLException {
		boolean allowed = false;
		pstmt = connection
				.prepareStatement("SELECT login FROM hosts WHERE mac = ? AND login = 1;");
		pstmt.setString(1, mac);
		resultSet = pstmt.executeQuery();
		while (resultSet.next()) {
			allowed = true;
		}
		return allowed;
	}
	
	/**
	 * Gets the failed login attempts from the user.
	 * 
	 * @param username
	 * @return
	 */
	public int getFailedLoginAttempts(String username) throws SQLException {
		int failedLoginAttempts = 0;
		pstmt = connection
				.prepareStatement("SELECT failedLoginAttempts FROM users WHERE name = ?;");
		pstmt.setString(1, username);
		resultSet = pstmt.executeQuery();
		while (resultSet.next()) {
			failedLoginAttempts = resultSet.getInt("failedLoginAttempts");
		}
		return failedLoginAttempts;
	}
	
	/**
	 * Gets the blockingEndTime for a given user.
	 * 
	 * @param username
	 * @return
	 */
	public long getBlockingEndTime(String username) throws SQLException {
		long blockingEndTime = 0;
		pstmt = connection
				.prepareStatement("SELECT blockingEndTime FROM users WHERE name = ?");
		pstmt.setString(1, username);
		resultSet = pstmt.executeQuery();
		while (resultSet.next()) {
			blockingEndTime = resultSet.getLong("blockingEndTime");
		}
		return blockingEndTime;
	}

	/**
	 * Increases the failed login attempts counter if a login failed.
	 * 
	 * @param username
	 */
	public void increaseFailedLogins(String username) throws SQLException {
		if (this.getUserId(username) > 0) {// If the username exists, increase
											// the counter.
			pstmt = connection
					.prepareStatement("UPDATE users SET failedLoginAttempts = failedLoginAttempts+1 WHERE name = ?");
			pstmt.setString(1, username);
			pstmt.executeUpdate();
		}
	}

	/**
	 * Resets the failed login attempts to 0.
	 * 
	 * @param username
	 */
	public void resetFailedLoginAttempts(String username) throws SQLException {
		if (this.getUserId(username) > 0) {// If the username exists, reset the
											// counter
			pstmt = connection
					.prepareStatement("UPDATE users SET failedLoginAttempts = 0 WHERE name = ?");
			pstmt.setString(1, username);
			pstmt.executeUpdate();
		}
	}

	/**
	 * Sets the blockingEndTime for a given user.
	 * 
	 * @param username
	 * @param blockingEndTime
	 * @throws SQLException
	 */
	public void updateBlockingEndTime(String username, long blockingEndTime)
			throws SQLException {
		pstmt = connection
				.prepareStatement("UPDATE users SET blockingEndTime = ? WHERE name = ?");
		pstmt.setLong(1, blockingEndTime);
		pstmt.setString(2, username);
		pstmt.executeUpdate();
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
	 * Gets the hashedPassword from the user.
	 * 
	 * @param username
	 * @return
	 */
	public String getHashedPassword(String username) throws SQLException {
		String hashedPassword = null;
		pstmt = connection
				.prepareStatement("SELECT hashedPassword FROM users WHERE name = ?;");
		pstmt.setString(1, username);
		resultSet = pstmt.executeQuery();
		while (resultSet.next()) {
			hashedPassword = resultSet.getString("hashedPassword");
		}
		return hashedPassword;
	}
	
	/**
	 * Checks if a user is allowed to login from the frontend.
	 * @param username
	 * @return true if allowed, otherwise false.
	 */
	public boolean isUserAllowedToLogin(String username) throws SQLException{
		boolean allowed = false;
		pstmt = connection
				.prepareStatement("SELECT name, frontendLogin FROM users WHERE name = ? AND frontendLogin = 1;");
		pstmt.setString(1, username);
		resultSet = pstmt.executeQuery();
		while (resultSet.next()) {
			allowed = true;
		}
		return allowed;
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