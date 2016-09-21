package de.rowekamp.networkboot.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class InitDatabase {

	private Connection connection;
	private Statement statement;
	private PreparedStatement pstmt;

	/**
	 * Creates the database, connects to the given file and enables foreign key
	 * constrain checks.
	 * 
	 * @param sqliteDB
	 */
	public InitDatabase(File sqliteDB) {
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
	
	public void initializeTables() throws SQLException{
		InputStream sqlFile = getClass().getResourceAsStream("/de/rowekamp/networkboot/database/initialize_tables.sql");
		BufferedReader reader = new BufferedReader(new InputStreamReader(sqlFile));
		StringBuilder sqlCommand = new StringBuilder();
		String currentLine;
		String[] currentLineTrimmed;
		try {
			while ((currentLine = reader.readLine()) != null) {
				currentLineTrimmed = currentLine.split("--"); //avoid nasty comments
				if (currentLineTrimmed.length > 0){
					sqlCommand.append(currentLineTrimmed[0]);
					if (currentLineTrimmed[0].endsWith(";")){
						statement.executeUpdate(sqlCommand.toString());
						sqlCommand.setLength(0);
					}
				}
			}
		}
		catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				sqlFile.close();
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void insertSystemVariables(boolean backendHttps, String ip, Integer backendPort) throws SQLException {
		pstmt = connection.prepareStatement("INSERT INTO globalVariables (variableName, variableValue, description, systemVariable) VALUES ('BOOTSCRIPT_SERVER_PROTOCOL', ?, 'Protocol of the bootscript server', 1), ('BOOTSCRIPT_SERVER_IP', ?, 'IPv4 Address of the Bootscript Server', 1), ('BOOTSCRIPT_SERVER_PORT', ?, 'Port of the Bootscript Server', 1)");
		if (backendHttps) pstmt.setString(1, "https");
		else pstmt.setString(1, "http");
		pstmt.setString(2, ip);
		pstmt.setString(3, backendPort.toString());
		pstmt.executeUpdate();
	}

	public void updateSystemVariables(boolean backendHttps, String ip, Integer backendPort) throws SQLException {
		String[] deletes = {"BOOTSCRIPT_SERVER_PROTOCOL","BOOTSCRIPT_SERVER_IP","BOOTSCRIPT_SERVER_PORT"};
		for (String del : deletes){
			statement.executeUpdate("DELETE FROM globalVariables WHERE variableName = '"+del+"'");
		}
		pstmt = connection.prepareStatement("INSERT INTO globalVariables (variableName, variableValue, description, systemVariable) VALUES ('BOOTSCRIPT_SERVER_PROTOCOL', ?, 'Protocol of the bootscript server', 1), ('BOOTSCRIPT_SERVER_IP', ?, 'IPv4 Address of the Bootscript Server', 1), ('BOOTSCRIPT_SERVER_PORT', ?, 'Port of the Bootscript Server', 1)");
		if (backendHttps) pstmt.setString(1, "https");
		else pstmt.setString(1, "http");
		pstmt.setString(2, ip);
		pstmt.setString(3, backendPort.toString());
		pstmt.executeUpdate();
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
