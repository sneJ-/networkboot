package de.rowekamp.networkboot.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import de.rowekamp.networkboot.wakeOnLan.GroupJob;
import de.rowekamp.networkboot.wakeOnLan.HostJob;

public class WakeOnLanDatabase {

	private Connection connection;
	private Statement statement;
	private PreparedStatement pstmt;
	private ResultSet resultSet, resultSet2;

	/**
	 * Creates the database, connects to the given file and enables foreign key constrain checks.
	 * @param sqliteDB
	 */
	public WakeOnLanDatabase(File sqliteDB) {
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:"	+ sqliteDB.getPath());
			statement = connection.createStatement();
			statement.executeUpdate("PRAGMA foreign_keys=ON");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * Gets all hosts wake on LAN jobs from the database.
	 * @return
	 */
	public ArrayList<HostJob> getHostJobs() throws SQLException{
		ArrayList<HostJob> hostJobs = new ArrayList<HostJob>();
		resultSet = statement.executeQuery("SELECT mac, hostWakeOnLanScheduler.id, cronSchedule FROM hosts, hostWakeOnLanScheduler WHERE hostWakeOnLanScheduler.id = hosts.id");
		while(resultSet.next()){
			hostJobs.add(new HostJob(resultSet.getInt("id"),resultSet.getString("cronSchedule"),resultSet.getString("mac")));
		}
		return hostJobs;
	}


	/**
	 * Gets all group wake on LAN jobs from the database.
	 * @return
	 */
	public ArrayList<GroupJob> getGroupJobs() throws SQLException{
		ArrayList<GroupJob> groupJobs = new ArrayList<GroupJob>();
		resultSet = statement.executeQuery("SELECT * FROM groupWakeOnLanScheduler");
		int id, groupId;
		while (resultSet.next()) {
			ArrayList<String> macs = new ArrayList<String>();
			id = resultSet.getInt("id");
			groupId = resultSet.getInt("groupId");
			String cronSchedule = resultSet.getString("cronSchedule");
			pstmt = connection.prepareStatement("SELECT mac FROM hosts, hostGroupMappings WHERE hosts.id = hostGroupMappings.hostId AND hostGroupMappings.groupId = ?");
			pstmt.setInt(1, groupId);
			resultSet2 = pstmt.executeQuery();
			while(resultSet2.next()){
				macs.add(resultSet2.getString("mac"));
			}
			groupJobs.add(new GroupJob(id,cronSchedule,macs));
		}
		return groupJobs;
	}

	
	/**
	 * Closes the sql statements and database.
	 */
	public void close() {
		try {
			statement.close();
			if (pstmt != null) pstmt.close();
			connection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}