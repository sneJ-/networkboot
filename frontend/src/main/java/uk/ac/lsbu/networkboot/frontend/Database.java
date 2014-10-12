package uk.ac.lsbu.networkboot.frontend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Database {
	private String databaseFile = "C:\\Users\\Kiteflyer\\workspace\\network boot\\config\\mapping.db3";
	// private final String databaseFile =
	// "/opt/NetworkBoot/config/mapping.db3";
	private Connection connection = null;
	private ResultSet resultSet = null;
	private ResultSet resultSet2 = null;
	private Statement statement = null;
	private PreparedStatement pstmt = null;
	private PreparedStatement pstmt2 = null;

	public Database(String databaseFile) {
		this.databaseFile = databaseFile;
	}

	/**
	 * Fetches the full hostlist from the database.
	 * 
	 * @return hostlist
	 */
	public List<Host> fetchHostlist() {
		List<Host> hostlist = new ArrayList<Host>();
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:"
					+ databaseFile);
			statement = connection.createStatement();
			statement.executeUpdate("PRAGMA foreign_keys=ON");
			resultSet = statement
					.executeQuery("SELECT id, name, mac, validated FROM host ORDER BY name;");
			while (resultSet.next()) {
				hostlist.add(new Host(resultSet.getInt("id"), resultSet
						.getString("name"), resultSet.getString("mac"),
						resultSet.getBoolean("validated")));
			}
		} catch (Exception e) {
			System.err.println("getHostlist");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				statement.close();
				connection.close();
			} catch (Exception e) {
				System.err.println("getHostlist");
				System.err.println(e.getClass().getName() + ": "
						+ e.getMessage());
			}
		}
		return hostlist;
	}

	/**
	 * Fetches the full grouplist from the database.
	 * 
	 * @return
	 */
	public List<Group> fetchGrouplist() {
		List<Group> grouplist = new ArrayList<Group>();
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:"
					+ databaseFile);
			statement = connection.createStatement();
			statement.executeUpdate("PRAGMA foreign_keys=ON");
			resultSet = statement
					.executeQuery("SELECT id, name, description FROM 'group' ORDER BY name;");
			while (resultSet.next()) {
				grouplist
						.add(new Group(resultSet.getInt("id"), resultSet
								.getString("name"), resultSet
								.getString("description")));
			}
		} catch (Exception e) {
			System.err.println("fetchGrouplist");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				statement.close();
				connection.close();
			} catch (Exception e) {
				System.err.println("fetchGrouplist");
				System.err.println(e.getClass().getName() + ": "
						+ e.getMessage());
			}
		}
		return grouplist;
	}

	/**
	 * Fetches the full storagelist from the database.
	 * 
	 * @return
	 */
	public List<Storage> fetchStoragelist() {
		List<Storage> storagelist = new ArrayList<Storage>();
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:"
					+ databaseFile);
			statement = connection.createStatement();
			statement.executeUpdate("PRAGMA foreign_keys=ON");
			resultSet = statement
					.executeQuery("SELECT * FROM storage ORDER BY name;");
			while (resultSet.next()) {
				storagelist.add(new Storage(resultSet.getInt("id"), resultSet
						.getString("name"), resultSet.getString("type"),
						resultSet.getString("baseURL"), resultSet
								.getString("directory")));
			}
		} catch (Exception e) {
			System.err.println("fetchStoragelist");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				statement.close();
				connection.close();
			} catch (Exception e) {
				System.err.println("fetchStoragelist");
				System.err.println(e.getClass().getName() + ": "
						+ e.getMessage());
			}
		}
		return storagelist;
	}

	/**
	 * Inserts the host into the database.
	 * 
	 * @param Host
	 * @return The id under the host is stored into the database
	 */
	public int insertHost(Host h) throws Exception {
		int id = 0;
		Class.forName("org.sqlite.JDBC");
		connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile);
		statement = connection.createStatement();
		statement.executeUpdate("PRAGMA foreign_keys=ON");
		pstmt = connection
				.prepareStatement("INSERT INTO host (name, mac, validated) VALUES (?,?,?)");
		pstmt.setString(1, h.getName());
		pstmt.setString(2, h.getMac());
		pstmt.setBoolean(3, h.isValidated());
		pstmt.executeUpdate();
		pstmt = connection
				.prepareStatement("SELECT id FROM host WHERE name = ?");
		pstmt.setString(1, h.getName());
		resultSet = pstmt.executeQuery();
		while (resultSet.next()) {
			id = resultSet.getInt("id");
		}
		statement.close();
		connection.close();
		return id;
	}

	/**
	 * Inserts the group into the database.
	 * 
	 * @param Group
	 * @return The id under the group is stored into the database
	 */
	public int insertGroup(Group g) throws Exception {
		int id = 0;
		Class.forName("org.sqlite.JDBC");
		connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile);
		statement = connection.createStatement();
		statement.executeUpdate("PRAGMA foreign_keys=ON");
		pstmt = connection
				.prepareStatement("INSERT INTO 'group' (name, description) VALUES (?,?)");
		pstmt.setString(1, g.getName());
		pstmt.setString(2, g.getDescription());
		pstmt.executeUpdate();
		pstmt = connection
				.prepareStatement("SELECT id FROM 'group' WHERE name = ?");
		pstmt.setString(1, g.getName());
		resultSet = pstmt.executeQuery();
		while (resultSet.next()) {
			id = resultSet.getInt("id");
		}
		statement.close();
		connection.close();
		return id;
	}

	/**
	 * Inserts the storage into the database. Returns its id.
	 * 
	 * @param storage
	 *            s
	 * @return The id under the storage is stored in the database.
	 */
	public int insertStorage(Storage s) throws Exception {
		int id = 0;
		Class.forName("org.sqlite.JDBC");
		connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile);
		statement = connection.createStatement();
		statement.executeUpdate("PRAGMA foreign_keys=ON");
		pstmt = connection
				.prepareStatement("INSERT INTO storage (name, type, baseURL, directory) VALUES (?,?,?,?)");
		pstmt.setString(1, s.getName());
		pstmt.setString(2, s.getType());
		pstmt.setString(3, s.getBaseURL());
		pstmt.setString(4, s.getDirectory());
		pstmt.executeUpdate();
		pstmt = connection
				.prepareStatement("SELECT id FROM storage WHERE name = ?");
		pstmt.setString(1, s.getName());
		resultSet = pstmt.executeQuery();
		while (resultSet.next()) {
			id = resultSet.getInt("id");
		}
		statement.close();
		connection.close();
		return id;
	}

	/**
	 * Inserts the image into the database. Returns its id.
	 * 
	 * @param Image
	 *            i
	 * @return The id under the image is stored in the database.
	 */
	public int insertImage(Image i) throws Exception {
		int id = 0;
		Class.forName("org.sqlite.JDBC");
		connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile);
		statement = connection.createStatement();
		statement.executeUpdate("PRAGMA foreign_keys=ON");
		pstmt = connection
				.prepareStatement("INSERT INTO image (name, type, description, storageID, script, directory) VALUES (?,?,?,?,?,?)");
		pstmt.setString(1, i.getName());
		pstmt.setString(2, i.getType());
		pstmt.setString(3, i.getDescription());
		pstmt.setInt(4, i.getStorageId());
		pstmt.setString(5, i.getScript());
		pstmt.setString(6, i.getDirectory());
		pstmt.executeUpdate();
		pstmt = connection
				.prepareStatement("SELECT id FROM image WHERE name = ?");
		pstmt.setString(1, i.getName());
		resultSet = pstmt.executeQuery();
		while (resultSet.next()) {
			id = resultSet.getInt("id");
		}
		statement.close();
		connection.close();
		return id;
	}

	/**
	 * Updates the host in the database.
	 * 
	 * @param Host
	 */
	public void updateHost(Host h) throws Exception {
		Class.forName("org.sqlite.JDBC");
		connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile);
		statement = connection.createStatement();
		statement.executeUpdate("PRAGMA foreign_keys=ON");
		pstmt = connection
				.prepareStatement("UPDATE host SET name = ?, mac = ?, validated = ? WHERE id = ?");
		pstmt.setString(1, h.getName());
		pstmt.setString(2, h.getMac());
		pstmt.setBoolean(3, h.isValidated());
		pstmt.setInt(4, h.getId());
		pstmt.executeUpdate();
		statement.close();
		connection.close();
	}

	/**
	 * Updates the group in the database.
	 * 
	 * @param Group
	 */
	public void updateGroup(Group g) throws Exception {
		Class.forName("org.sqlite.JDBC");
		connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile);
		statement = connection.createStatement();
		statement.executeUpdate("PRAGMA foreign_keys=ON");
		pstmt = connection
				.prepareStatement("UPDATE 'group' SET name = ?, description = ? WHERE id = ?");
		pstmt.setString(1, g.getName());
		pstmt.setString(2, g.getDescription());
		pstmt.setInt(3, g.getId());
		pstmt.executeUpdate();
		statement.close();
		connection.close();
	}

	/**
	 * Deletes the host from the database.
	 * 
	 * @param Host
	 */
	public void deleteHost(int hostId) {
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:"
					+ databaseFile);
			statement = connection.createStatement();
			statement.executeUpdate("PRAGMA foreign_keys=ON");
			pstmt = connection
					.prepareStatement("DELETE FROM host WHERE id = ?");
			pstmt.setInt(1, hostId);
			pstmt.executeUpdate();
		} catch (Exception e) {
			System.err.println("deleteHost");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				statement.close();
				connection.close();
			} catch (Exception e) {
				System.err.println("deleteHost");
				System.err.println(e.getClass().getName() + ": "
						+ e.getMessage());
			}
		}
	}

	/**
	 * Deletes the group from the database.
	 * 
	 * @param Group
	 */
	public void deleteGroup(int groupId) {
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:"
					+ databaseFile);
			statement = connection.createStatement();
			statement.executeUpdate("PRAGMA foreign_keys=ON");
			pstmt = connection
					.prepareStatement("DELETE FROM 'group' WHERE id = ?");
			pstmt.setInt(1, groupId);
			pstmt.executeUpdate();
		} catch (Exception e) {
			System.err.println("deleteGroup");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				statement.close();
				connection.close();
			} catch (Exception e) {
				System.err.println("deleteGroup");
				System.err.println(e.getClass().getName() + ": "
						+ e.getMessage());
			}
		}
	}

	/**
	 * Deletes the storage from the database.
	 * 
	 * @param storageId
	 * @return true if storage could be deleted false if not.
	 */
	public boolean deleteStorage(int storageId) {
		boolean value = false;
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:"
					+ databaseFile);
			statement = connection.createStatement();
			statement.executeUpdate("PRAGMA foreign_keys=ON");
			pstmt = connection
					.prepareStatement("DELETE FROM storage WHERE id = ?");
			pstmt.setInt(1, storageId);
			pstmt.executeUpdate();
			value = true;
		} catch (Exception e) {
			System.err.println("deleteStorage");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				statement.close();
				connection.close();
			} catch (Exception e) {
				System.err.println("deleteStorage");
				System.err.println(e.getClass().getName() + ": "
						+ e.getMessage());
			}
		}
		return value;
	}

	/**
	 * Deletes the image from the database.
	 * 
	 * @param image
	 *            ID to delete.
	 * @return true if the image could be deleted otherwise false.
	 */
	public boolean deleteImage(int imageId) throws SQLException {
		boolean value = false;
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			System.err.println("deleteImage");
			e.printStackTrace();
		}
		connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile);
		statement = connection.createStatement();
		statement.executeUpdate("PRAGMA foreign_keys=ON");
		pstmt = connection.prepareStatement("DELETE FROM image WHERE id = ?");
		pstmt.setInt(1, imageId);
		pstmt.executeUpdate();
		value = true;
		statement.close();
		connection.close();
		return value;
	}

	/**
	 * Gets the actual host object based on its id from the database.
	 * 
	 * @param host
	 *            id
	 * @return host object
	 */
	public Host fetchHost(int id) {
		String name = null;
		String mac = null;
		boolean validated = false;
		Host h = null;
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:"
					+ databaseFile);
			statement = connection.createStatement();
			statement.executeUpdate("PRAGMA foreign_keys=ON");
			pstmt = connection
					.prepareStatement("SELECT name, mac, validated FROM host WHERE id = ?");
			pstmt.setInt(1, id);
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				name = resultSet.getString("name");
				mac = resultSet.getString("mac");
				validated = resultSet.getBoolean("validated");
			}
		} catch (Exception e) {
			System.err.println("fetchHost");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				statement.close();
				connection.close();
			} catch (Exception e) {
				System.err.println("fetchHost");
				System.err.println(e.getClass().getName() + ": "
						+ e.getMessage());
			}
		}
		if (name != null) {
			h = new Host(id, name, mac, validated);
		} else {
			h = new Host();
		}
		return h;
	}

	/**
	 * Gets the actual group object based on its id from the database.
	 * 
	 * @param group
	 *            id
	 * @return group object
	 */
	public Group fetchGroup(int id) {
		Group group = new Group(0, "New group...", "");
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:"
					+ databaseFile);
			statement = connection.createStatement();
			statement.executeUpdate("PRAGMA foreign_keys=ON");
			pstmt = connection
					.prepareStatement("SELECT name, description FROM 'group' WHERE id = ?");
			pstmt.setInt(1, id);
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				group = new Group(id, resultSet.getString("name"),
						resultSet.getString("description"));
			}
		} catch (Exception e) {
			System.err.println("fetchGroup");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				statement.close();
				connection.close();
			} catch (Exception e) {
				System.err.println("fetchGroup");
				System.err.println(e.getClass().getName() + ": "
						+ e.getMessage());
			}
		}
		return group;
	}

	/**
	 * Gets the actual storage object based on its id from the database.
	 * 
	 * @param storageID
	 * @return storage object
	 */
	public Storage fetchStorage(int storageId) {
		Storage storage = new Storage();
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:"
					+ databaseFile);
			statement = connection.createStatement();
			statement.executeUpdate("PRAGMA foreign_keys=ON");
			pstmt = connection
					.prepareStatement("SELECT * FROM storage WHERE id = ?");
			pstmt.setInt(1, storageId);
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				storage = new Storage(storageId, resultSet.getString("name"),
						resultSet.getString("type"),
						resultSet.getString("baseURL"),
						resultSet.getString("directory"));
			}
		} catch (Exception e) {
			System.err.println("fetchStorage");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				statement.close();
				connection.close();
			} catch (Exception e) {
				System.err.println("fetchStorage");
				System.err.println(e.getClass().getName() + ": "
						+ e.getMessage());
			}
		}
		return storage;
	}

	/**
	 * Fetches the groups assigned to the host.
	 * 
	 * @param host
	 *            id
	 * @return list of groups
	 */
	public List<Group> fetchHostGroups(int id) {
		List<Group> hostGroups = new ArrayList<Group>();
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:"
					+ databaseFile);
			statement = connection.createStatement();
			statement.executeUpdate("PRAGMA foreign_keys=ON");
			pstmt = connection
					.prepareStatement("SELECT 'group'.id, 'group'.name FROM 'group', HostGroup WHERE HostGroup.hostID = ? AND HostGroup.groupID = 'group'.id ORDER BY 'group'.name");
			pstmt.setInt(1, id);
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				hostGroups.add(new Group(resultSet.getInt("id"), resultSet
						.getString("name"), ""));
			}
		} catch (Exception e) {
			System.err.println("fetchHostGroups");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				statement.close();
				connection.close();
			} catch (Exception e) {
				System.err.println("fetchHostGroups");
				System.err.println(e.getClass().getName() + ": "
						+ e.getMessage());
			}
		}
		return hostGroups;
	}

	/**
	 * Fetches the groups assigned to the image from the db.
	 * 
	 * @param imageId
	 * @return list of groups assigned to the image
	 */
	public List<Group> fetchMappedGroups(int imageId) {
		List<Group> mappedGroups = new ArrayList<Group>();
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:"
					+ databaseFile);
			statement = connection.createStatement();
			statement.executeUpdate("PRAGMA foreign_keys=ON");
			pstmt = connection
					.prepareStatement("SELECT DISTINCT 'group'.id, 'group'.name FROM 'group', GroupImage WHERE 'group'.id = GroupImage.groupID AND GroupImage.imageID = ? ORDER BY 'group'.name");
			pstmt.setInt(1, imageId);
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				mappedGroups.add(new Group(resultSet.getInt("id"), resultSet
						.getString("name"), ""));
			}
		} catch (Exception e) {
			System.err.println("fetchMappedGroups");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				statement.close();
				connection.close();
			} catch (Exception e) {
				System.err.println("fetchMappedGroups");
				System.err.println(e.getClass().getName() + ": "
						+ e.getMessage());
			}
		}
		return mappedGroups;
	}

	/**
	 * Fetches the hosts assigned to the group from the db.
	 * 
	 * @param groupId
	 * @return
	 */
	public List<Host> fetchHosts(int groupId) {
		List<Host> hostlist = new ArrayList<Host>();
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:"
					+ databaseFile);
			statement = connection.createStatement();
			statement.executeUpdate("PRAGMA foreign_keys=ON");
			pstmt = connection
					.prepareStatement("SELECT host.id, host.name, host.mac, host.validated FROM host, HostGroup WHERE HostGroup.hostID = host.id AND HostGroup.groupID = ? ORDER BY host.name");
			pstmt.setInt(1, groupId);
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				hostlist.add(new Host(resultSet.getInt("id"), resultSet
						.getString("name"), resultSet.getString("mac"),
						resultSet.getBoolean("validated")));
			}
		} catch (Exception e) {
			System.err.println("fetchHosts");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				statement.close();
				connection.close();
			} catch (Exception e) {
				System.err.println("fetchHosts");
				System.err.println(e.getClass().getName() + ": "
						+ e.getMessage());
			}
		}
		return hostlist;
	}

	/**
	 * Fetches the suitable storages to the assigned type
	 * 
	 * @param storage
	 *            type
	 * @return List of storages
	 */
	public List<Storage> fetchSuitableStorages(String type) {
		List<Storage> storagelist = new ArrayList<Storage>();
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:"
					+ databaseFile);
			statement = connection.createStatement();
			statement.executeUpdate("PRAGMA foreign_keys=ON");
			pstmt = connection
					.prepareStatement("SELECT * FROM storage WHERE type = ? ORDER by name");
			pstmt.setString(1, type);
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				storagelist
						.add(new Storage(resultSet.getInt("id"), resultSet
								.getString("name"), type, resultSet
								.getString("baseURL"), resultSet
								.getString("directory")));
			}
		} catch (Exception e) {
			System.err.println("fetchSuitableStorages");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				statement.close();
				connection.close();
			} catch (Exception e) {
				System.err.println("fetchSuitableStorages");
				System.err.println(e.getClass().getName() + ": "
						+ e.getMessage());
			}
		}
		return storagelist;
	}

	/**
	 * Fetches the hosts assigned to the image from the db.
	 * 
	 * @param image
	 *            Id
	 * @return hostlist
	 */
	public List<Host> fetchMappedHosts(int imageId) {
		List<Host> hostlist = new ArrayList<Host>();
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:"
					+ databaseFile);
			statement = connection.createStatement();
			statement.executeUpdate("PRAGMA foreign_keys=ON");
			pstmt = connection
					.prepareStatement("SELECT DISTINCT host.id, host.name, host.mac, host.validated FROM host, HostImage WHERE HostImage.hostID = host.id AND HostImage.imageID = ? ORDER BY host.name");
			pstmt.setInt(1, imageId);
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				hostlist.add(new Host(resultSet.getInt("id"), resultSet
						.getString("name"), resultSet.getString("mac"),
						resultSet.getBoolean("validated")));
			}
		} catch (Exception e) {
			System.err.println("fetchMappedHosts");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				statement.close();
				connection.close();
			} catch (Exception e) {
				System.err.println("fetchMappedHosts");
				System.err.println(e.getClass().getName() + ": "
						+ e.getMessage());
			}
		}
		return hostlist;
	}

	/**
	 * Fetches the images stored on the storage from the db.
	 * 
	 * @param storageID
	 * @return
	 */
	public List<Image> fetchImagesOnStorage(int storageID) {
		List<Image> imagelist = new ArrayList<Image>();
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:"
					+ databaseFile);
			statement = connection.createStatement();
			statement.executeUpdate("PRAGMA foreign_keys=ON");
			pstmt = connection
					.prepareStatement("SELECT * FROM image WHERE storageID = ? ORDER BY name");
			pstmt.setInt(1, storageID);
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				imagelist.add(new Image(resultSet.getInt("id"), resultSet
						.getString("name"), resultSet.getString("description"),
						resultSet.getInt("storageID"), resultSet
								.getString("type"), resultSet
								.getString("script"), resultSet
								.getString("directory")));
			}
		} catch (Exception e) {
			System.err.println("fetchImagesOnStorage");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				statement.close();
				connection.close();
			} catch (Exception e) {
				System.err.println("fetchImagesOnStorage");
				System.err.println(e.getClass().getName() + ": "
						+ e.getMessage());
			}
		}
		return imagelist;
	}

	/**
	 * Deletes the mapping Host Group from the database.
	 * 
	 * @param host
	 *            Id
	 * @param group
	 *            Id
	 */
	public void deleteHostGroup(int hostId, int groupId) {
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:"
					+ databaseFile);
			statement = connection.createStatement();
			statement.executeUpdate("PRAGMA foreign_keys=ON");
			pstmt = connection
					.prepareStatement("DELETE FROM HostGroup WHERE hostID = ? AND groupID = ?");
			pstmt.setInt(1, hostId);
			pstmt.setInt(2, groupId);
			pstmt.executeUpdate();
		} catch (Exception e) {
			System.err.println("deleteHostGroup");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				statement.close();
				connection.close();
			} catch (Exception e) {
				System.err.println("deleteHostGroup");
				System.err.println(e.getClass().getName() + ": "
						+ e.getMessage());
			}
		}
	}

	/**
	 * Fetches all images from the db.
	 * 
	 * @param storageID
	 * @return
	 */
	public List<Image> fetchImagelist() {
		List<Image> imagelist = new ArrayList<Image>();
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:"
					+ databaseFile);
			statement = connection.createStatement();
			statement.executeUpdate("PRAGMA foreign_keys=ON");
			pstmt = connection
					.prepareStatement("SELECT * FROM image ORDER BY name");
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				imagelist.add(new Image(resultSet.getInt("id"), resultSet
						.getString("name"), resultSet.getString("description"),
						resultSet.getInt("storageID"), resultSet
								.getString("type"), resultSet
								.getString("script"), resultSet
								.getString("directory")));
			}
		} catch (Exception e) {
			System.err.println("fetchImageslist");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				statement.close();
				connection.close();
			} catch (Exception e) {
				System.err.println("fetchImageslist");
				System.err.println(e.getClass().getName() + ": "
						+ e.getMessage());
			}
		}
		return imagelist;
	}

	/**
	 * Fetches the image from the db on the basis of its id.
	 * 
	 * @param imageId
	 * @return
	 */
	public Image fetchImage(int imageId) {
		Image image = new Image();
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:"
					+ databaseFile);
			statement = connection.createStatement();
			statement.executeUpdate("PRAGMA foreign_keys=ON");
			pstmt = connection
					.prepareStatement("SELECT * FROM image WHERE id = ?");
			pstmt.setInt(1, imageId);
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				image = new Image(resultSet.getInt("id"),
						resultSet.getString("name"),
						resultSet.getString("description"),
						resultSet.getInt("storageID"),
						resultSet.getString("type"),
						resultSet.getString("script"),
						resultSet.getString("directory"));
			}
		} catch (Exception e) {
			System.err.println("fetchImage");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				statement.close();
				connection.close();
			} catch (Exception e) {
				System.err.println("fetchImage");
				System.err.println(e.getClass().getName() + ": "
						+ e.getMessage());
			}
		}
		return image;
	}

	/**
	 * Fetches the list of assigned images (host and group) to the host.
	 * 
	 * @param hostId
	 * @return The list of images assigned to the host.
	 */
	public List<ImageMappingListObject> fetchHostImages(int hostId) {
		List<ImageMappingListObject> images = new ArrayList<ImageMappingListObject>();
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:"
					+ databaseFile);
			statement = connection.createStatement();
			statement.executeUpdate("PRAGMA foreign_keys=ON");
			// Non timed host images
			pstmt = connection
					.prepareStatement("SELECT HostImage.id, image.name, HostImage.priority "
							+ "FROM HostImage, image "
							+ "WHERE HostImage.imageID = image.id "
							+ "AND HostImage.hostID = ? "
							+ "AND HostImage.timePeriod = 0 "
							+ "ORDER BY HostImage.priority DESC");
			pstmt.setInt(1, hostId);
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				images.add(new ImageMappingListObject(resultSet.getInt("id"),
						resultSet.getString("name"), resultSet
								.getInt("priority"), false, "None", false, null));
			}
			// timed host images
			pstmt = connection
					.prepareStatement("SELECT HostImage.id, image.name, HostImage.priority, HostTime.'minute', "
							+ "HostTime.'hour', HostTime.dom, HostTime.'month', HostTime.dow, HostTime.validMinutes, "
							+ "HostTime.id "
							+ "FROM HostImage, image, HostTime "
							+ "WHERE HostImage.imageID = image.id "
							+ "AND HostImage.hostID = ? "
							+ "AND HostImage.timePeriod = 1 "
							+ "AND HostImage.id = HostTime.hostImageID "
							+ "ORDER BY HostImage.priority DESC");
			pstmt.setInt(1, hostId);
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				Integer dow = resultSet.getInt("dow");
				if (resultSet.wasNull()) {
					dow = null;
				}
				Integer dom = resultSet.getInt("dom");
				if (resultSet.wasNull()) {
					dom = null;
				}
				Integer month = resultSet.getInt(7);
				if (resultSet.wasNull()) {
					month = null;
				}
				int beginMinute = resultSet.getInt(4);
				int beginHour = resultSet.getInt(5);
				int validMinutes = resultSet.getInt("validMinutes");

				TimeConstraint time = new TimeConstraint(beginMinute,
						beginHour, TimeConstraint.calculateEndMinute(
								beginMinute, validMinutes),
						TimeConstraint.calculateEndHour(beginHour, beginMinute,
								validMinutes), dom, month, dow,
						resultSet.getInt(10));
				images.add(new ImageMappingListObject(resultSet.getInt(1),
						resultSet.getString("name"), resultSet
								.getInt("priority"), false, "None", true, time));
			}
			// Non timed group images
			pstmt2 = connection
					.prepareStatement("SELECT groupID FROM HostGroup WHERE hostID = ?");
			pstmt2.setInt(1, hostId);
			resultSet2 = pstmt2.executeQuery();
			while (resultSet2.next()) {
				pstmt = connection
						.prepareStatement("SELECT GroupImage.id, 'group'.name, image.name, GroupImage.priority "
								+ "FROM GroupImage, image, 'group' "
								+ "WHERE GroupImage.imageID = image.id "
								+ "AND GroupImage.groupID = ? "
								+ "AND 'group'.id = GroupImage.groupID "
								+ "AND GroupImage.timePeriod = 0 "
								+ "ORDER BY GroupImage.priority DESC");
				pstmt.setInt(1, resultSet2.getInt("groupID"));
				resultSet = pstmt.executeQuery();
				while (resultSet.next()) {
					images.add(new ImageMappingListObject(resultSet
							.getInt("id"), resultSet.getString(3), resultSet
							.getInt("priority"), true, resultSet.getString(2),
							false, null));
				}
			}
			// timed group images
			pstmt2 = connection
					.prepareStatement("SELECT groupID FROM HostGroup WHERE hostID = ?");
			pstmt2.setInt(1, hostId);
			resultSet2 = pstmt2.executeQuery();
			while (resultSet2.next()) {
				pstmt = connection
						.prepareStatement("SELECT GroupImage.id, image.name, 'group'.name, GroupImage.priority, GroupTime.'minute', "
								+ "GroupTime.'hour', GroupTime.dom, GroupTime.'month', GroupTime.dow, GroupTime.validMinutes, "
								+ "GroupTime.id "
								+ "FROM GroupImage, image, GroupTime, 'group' "
								+ "WHERE GroupImage.imageID = image.id "
								+ "AND GroupImage.groupID = ? "
								+ "AND 'group'.id = GroupImage.groupID "
								+ "AND GroupImage.timePeriod = 1 "
								+ "AND GroupImage.id = GroupTime.groupImageID "
								+ "ORDER BY GroupImage.priority DESC");
				pstmt.setInt(1, resultSet2.getInt("groupID"));
				resultSet = pstmt.executeQuery();
				while (resultSet.next()) {
					Integer dow = resultSet.getInt("dow");
					if (resultSet.wasNull()) {
						dow = null;
					}
					Integer dom = resultSet.getInt("dom");
					if (resultSet.wasNull()) {
						dom = null;
					}
					Integer month = resultSet.getInt(8);
					if (resultSet.wasNull()) {
						month = null;
					}
					int beginMinute = resultSet.getInt(5);
					int beginHour = resultSet.getInt(6);
					int validMinutes = resultSet.getInt("validMinutes");

					TimeConstraint time = new TimeConstraint(beginMinute,
							beginHour, TimeConstraint.calculateEndMinute(
									beginMinute, validMinutes),
							TimeConstraint.calculateEndHour(beginHour,
									beginMinute, validMinutes), dom, month,
							dow, resultSet.getInt(11));
					images.add(new ImageMappingListObject(resultSet.getInt(1),
							resultSet.getString(2), resultSet
									.getInt("priority"), true, resultSet
									.getString(3), true, time));
				}
			}
		} catch (SQLException e) {
			System.err.println("fetchHostImages");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			System.err.println("fetchHostImages");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				statement.close();
				connection.close();
			} catch (SQLException e) {
				System.err.println("fetchHostImages");
				System.err.println(e.getClass().getName() + ": "
						+ e.getMessage());
			}
		}
		return images;
	}

	/**
	 * Fetches the list of assigned images (only group) to the group.
	 * 
	 * @param groupId
	 * @return The list of images assigned to the group.
	 */
	public List<ImageMappingListObject> fetchGroupImages(int groupId) {
		List<ImageMappingListObject> images = new ArrayList<ImageMappingListObject>();
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:"
					+ databaseFile);
			statement = connection.createStatement();
			statement.executeUpdate("PRAGMA foreign_keys=ON");
			// Non timed group images
			pstmt = connection
					.prepareStatement("SELECT GroupImage.id, 'group'.name, image.name, GroupImage.priority "
							+ "FROM GroupImage, image, 'group' "
							+ "WHERE GroupImage.imageID = image.id "
							+ "AND GroupImage.groupID = ? "
							+ "AND 'group'.id = GroupImage.groupID "
							+ "AND GroupImage.timePeriod = 0 "
							+ "ORDER BY GroupImage.priority DESC");
			pstmt.setInt(1, groupId);
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				images.add(new ImageMappingListObject(resultSet.getInt("id"),
						resultSet.getString(3), resultSet.getInt("priority"),
						true, resultSet.getString(2), false, null));
			}
			// timed group images
			pstmt = connection
					.prepareStatement("SELECT GroupImage.id, image.name, 'group'.name, GroupImage.priority, GroupTime.'minute', "
							+ "GroupTime.'hour', GroupTime.dom, GroupTime.'month', GroupTime.dow, GroupTime.validMinutes, "
							+ "GroupTime.id "
							+ "FROM GroupImage, image, GroupTime, 'group' "
							+ "WHERE GroupImage.imageID = image.id "
							+ "AND GroupImage.groupID = ? "
							+ "AND 'group'.id = GroupImage.groupID "
							+ "AND GroupImage.timePeriod = 1 "
							+ "AND GroupImage.id = GroupTime.groupImageID "
							+ "ORDER BY GroupImage.priority DESC");
			pstmt.setInt(1, groupId);
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				Integer dow = resultSet.getInt("dow");
				if (resultSet.wasNull()) {
					dow = null;
				}
				Integer dom = resultSet.getInt("dom");
				if (resultSet.wasNull()) {
					dom = null;
				}
				Integer month = resultSet.getInt(8);
				if (resultSet.wasNull()) {
					month = null;
				}
				int beginMinute = resultSet.getInt(5);
				int beginHour = resultSet.getInt(6);
				int validMinutes = resultSet.getInt("validMinutes");

				TimeConstraint time = new TimeConstraint(beginMinute,
						beginHour, TimeConstraint.calculateEndMinute(
								beginMinute, validMinutes),
						TimeConstraint.calculateEndHour(beginHour, beginMinute,
								validMinutes), dom, month, dow,
						resultSet.getInt(11));
				images.add(new ImageMappingListObject(resultSet.getInt(1),
						resultSet.getString(2), resultSet.getInt("priority"),
						true, resultSet.getString(3), true, time));
			}
		} catch (SQLException e) {
			System.err.println("fetchGroupImages");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			System.err.println("fetchGroupImages");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				statement.close();
				connection.close();
			} catch (SQLException e) {
				System.err.println("fetchGroupImages");
				System.err.println(e.getClass().getName() + ": "
						+ e.getMessage());
			}
		}
		return images;
	}

	/**
	 * Fetches the specific image mapping from the db.
	 * 
	 * @param hostImageId
	 *            or groupImageId
	 * @param grouped
	 *            (Group Image or Host Image)
	 * @return ImageMaping Object
	 */
	public ImageMapping fetchImageMapping(int hostImageId, boolean grouped) {
		ImageMapping out = new ImageMapping(grouped);
		List<TimeConstraint> times = new ArrayList<TimeConstraint>();
		Image image = new Image();
		int imageId = 0;
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:"
					+ databaseFile);
			statement = connection.createStatement();
			statement.executeUpdate("PRAGMA foreign_keys=ON");
			if (grouped) {
				pstmt = connection
						.prepareStatement("SELECT * FROM GroupImage WHERE id = ?");
				pstmt.setInt(1, hostImageId);
				resultSet = pstmt.executeQuery();
				while (resultSet.next()) {
					out = new ImageMapping(resultSet.getInt("id"),
							resultSet.getInt(2), grouped, image,
							resultSet.getBoolean("timePeriod"),
							resultSet.getString("bootParameter"),
							resultSet.getInt("priority"), times);
					imageId = resultSet.getInt("imageID");
				}
				pstmt = connection
						.prepareStatement("SELECT * FROM GroupTime WHERE groupImageID = ?");
				pstmt.setInt(1, hostImageId);
				resultSet = pstmt.executeQuery();
				while (resultSet.next()) {
					Integer dow = resultSet.getInt("dow");
					if (resultSet.wasNull()) {
						dow = null;
					}
					Integer dom = resultSet.getInt("dom");
					if (resultSet.wasNull()) {
						dom = null;
					}
					Integer month = resultSet.getInt("month");
					if (resultSet.wasNull()) {
						month = null;
					}
					int beginMinute = resultSet.getInt("minute");
					int beginHour = resultSet.getInt("hour");
					int validMinutes = resultSet.getInt("validMinutes");

					TimeConstraint time = new TimeConstraint(beginMinute,
							beginHour, TimeConstraint.calculateEndMinute(
									beginMinute, validMinutes),
							TimeConstraint.calculateEndHour(beginHour,
									beginMinute, validMinutes), dom, month,
							dow, resultSet.getInt("id"));
					times.add(time);
				}
			} else {
				pstmt = connection
						.prepareStatement("SELECT * FROM HostImage WHERE id = ?");
				pstmt.setInt(1, hostImageId);
				resultSet = pstmt.executeQuery();
				while (resultSet.next()) {
					out = new ImageMapping(resultSet.getInt("id"),
							resultSet.getInt(2), grouped, image,
							resultSet.getBoolean("timePeriod"),
							resultSet.getString("bootParameter"),
							resultSet.getInt("priority"), times);
					imageId = resultSet.getInt("imageID");
				}
				pstmt = connection
						.prepareStatement("SELECT * FROM HostTime WHERE hostImageID = ?");
				pstmt.setInt(1, hostImageId);
				resultSet = pstmt.executeQuery();
				while (resultSet.next()) {
					Integer dow = resultSet.getInt("dow");
					if (resultSet.wasNull()) {
						dow = null;
					}
					Integer dom = resultSet.getInt("dom");
					if (resultSet.wasNull()) {
						dom = null;
					}
					Integer month = resultSet.getInt("month");
					if (resultSet.wasNull()) {
						month = null;
					}
					int beginMinute = resultSet.getInt("minute");
					int beginHour = resultSet.getInt("hour");
					int validMinutes = resultSet.getInt("validMinutes");

					TimeConstraint time = new TimeConstraint(beginMinute,
							beginHour, TimeConstraint.calculateEndMinute(
									beginMinute, validMinutes),
							TimeConstraint.calculateEndHour(beginHour,
									beginMinute, validMinutes), dom, month,
							dow, resultSet.getInt("id"));
					times.add(time);
				}
			}
		} catch (SQLException e) {
			System.err.println("fetchImageMapping");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			System.err.println("fetchImageMapping");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				statement.close();
				connection.close();
			} catch (SQLException e) {
				System.err.println("fetchImageMapping");
				System.err.println(e.getClass().getName() + ": "
						+ e.getMessage());
			}
		}
		out.setImage(fetchImage(imageId));
		return out;
	}

	/**
	 * Deletes the mapping of hostImage and hostTime. If there is only one
	 * mapping, it deletes the image entry in the database too.
	 * 
	 * @param hostImageId
	 * @param hostTimeId
	 */
	public void deleteHostImage(int hostImageId, int hostTimeId) {
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:"
					+ databaseFile);
			statement = connection.createStatement();
			statement.executeUpdate("PRAGMA foreign_keys=ON");
			// If no time constraint is found delete the image.
			if (hostTimeId == 0) {
				pstmt = connection
						.prepareStatement("DELETE FROM HostImage WHERE id = ?");
				pstmt.setInt(1, hostImageId);
				pstmt.executeUpdate();
			}
			// Otherwise delete the time constraint in HostTime
			else {
				pstmt = connection
						.prepareStatement("SELECT COUNT(*) FROM HostTime WHERE hostImageID = ?");
				pstmt.setInt(1, hostImageId);
				resultSet = pstmt.executeQuery();
				int numberOfMappings = 1;
				while (resultSet.next()) {
					numberOfMappings = resultSet.getInt(1);
				}
				pstmt = connection
						.prepareStatement("DELETE FROM HostTime WHERE id = ?");
				pstmt.setInt(1, hostTimeId);
				pstmt.executeUpdate();
				// If it was the only time constraint, delete the host image in
				// HostImage.
				if (numberOfMappings == 1) {
					pstmt = connection
							.prepareStatement("DELETE FROM HostImage WHERE id = ?");
					pstmt.setInt(1, hostImageId);
					pstmt.executeUpdate();
				}
			}
		} catch (Exception e) {
			System.err.println("deleteHostImage");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				statement.close();
				connection.close();
			} catch (Exception e) {
				System.err.println("deleteHostImage");
				System.err.println(e.getClass().getName() + ": "
						+ e.getMessage());
			}
		}
	}

	/**
	 * Deletes the mapping of groupImage and groupTime. If there is only one
	 * mapping, it deletes the image entry in the database too.
	 * 
	 * @param groupImageId
	 * @param groupTimeId
	 */
	public void deleteGroupImage(int groupImageId, int groupTimeId) {
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:"
					+ databaseFile);
			statement = connection.createStatement();
			statement.executeUpdate("PRAGMA foreign_keys=ON");
			// If no time constraint is found delete the image.
			if (groupTimeId == 0) {
				pstmt = connection
						.prepareStatement("DELETE FROM GroupImage WHERE id = ?");
				pstmt.setInt(1, groupImageId);
				pstmt.executeUpdate();
			}
			// Otherwise delete the time constraint in HostTime
			else {
				pstmt = connection
						.prepareStatement("SELECT COUNT(*) FROM GroupTime WHERE groupImageID = ?");
				pstmt.setInt(1, groupImageId);
				resultSet = pstmt.executeQuery();
				int numberOfMappings = 1;
				while (resultSet.next()) {
					numberOfMappings = resultSet.getInt(1);
				}
				pstmt = connection
						.prepareStatement("DELETE FROM GroupTime WHERE id = ?");
				pstmt.setInt(1, groupTimeId);
				pstmt.executeUpdate();
				// If it was the only time constraint, delete the host image in
				// HostImage.
				if (numberOfMappings == 1) {
					pstmt = connection
							.prepareStatement("DELETE FROM GroupImage WHERE id = ?");
					pstmt.setInt(1, groupImageId);
					pstmt.executeUpdate();
				}
			}
		} catch (Exception e) {
			System.err.println("deleteGroupImage");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				statement.close();
				connection.close();
			} catch (Exception e) {
				System.err.println("deleteGroupImage");
				System.err.println(e.getClass().getName() + ": "
						+ e.getMessage());
			}
		}
	}

	/**
	 * Deletes all mappings of host/groupImage and hostTime.
	 * 
	 * @param hostImageId
	 * @param grouped
	 *            - true if group image false if host image
	 */
	public void deleteHostImage(int hostImageId, boolean grouped) {
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:"
					+ databaseFile);
			statement = connection.createStatement();
			statement.executeUpdate("PRAGMA foreign_keys=ON");
			if (grouped) {
				pstmt = connection
						.prepareStatement("DELETE FROM GroupImage WHERE id = ?");
				pstmt.setInt(1, hostImageId);
				pstmt.executeUpdate();
			} else {
				pstmt = connection
						.prepareStatement("DELETE FROM HostImage WHERE id = ?");
				pstmt.setInt(1, hostImageId);
				pstmt.executeUpdate();
			}
		} catch (Exception e) {
			System.err.println("deleteHostImage");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				statement.close();
				connection.close();
			} catch (Exception e) {
				System.err.println("deleteHostImage");
				System.err.println(e.getClass().getName() + ": "
						+ e.getMessage());
			}
		}
	}

	/**
	 * Fetches the list of HostGroups which could be added to the host.
	 * 
	 * @param hostID
	 * @return
	 */
	public List<Group> fetchHostGroupsToAdd(int id) {
		List<Group> hostGroups = new ArrayList<Group>();
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:"
					+ databaseFile);
			statement = connection.createStatement();
			statement.executeUpdate("PRAGMA foreign_keys=ON");
			pstmt = connection
					.prepareStatement("SELECT * FROM 'group' WHERE 'group'.id NOT IN (SELECT GroupID FROM HostGroup WHERE HostID = ?)");
			pstmt.setInt(1, id);
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				hostGroups
						.add(new Group(resultSet.getInt("id"), resultSet
								.getString("name"), resultSet
								.getString("description")));
			}
		} catch (Exception e) {
			System.err.println("fetchHostGroupsToAdd");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				statement.close();
				connection.close();
			} catch (Exception e) {
				System.err.println("fetchHostGroupsToAdd");
				System.err.println(e.getClass().getName() + ": "
						+ e.getMessage());
			}
		}
		return hostGroups;
	}

	/**
	 * Fetches the list of hosts which could be added to the group.
	 * 
	 * @param groupId
	 * @return
	 */
	public List<Host> fetchHostsToAdd(int groupId) {
		List<Host> hosts = new ArrayList<Host>();
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:"
					+ databaseFile);
			statement = connection.createStatement();
			statement.executeUpdate("PRAGMA foreign_keys=ON");
			pstmt = connection
					.prepareStatement("SELECT * FROM host WHERE host.id NOT IN (SELECT HostID FROM HostGroup WHERE GroupID = ?)");
			pstmt.setInt(1, groupId);
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				hosts.add(new Host(resultSet.getInt("id"), resultSet
						.getString("name"), resultSet.getString("mac"),
						resultSet.getBoolean("validated")));
			}
		} catch (Exception e) {
			System.err.println("fetchHostsToAdd");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				statement.close();
				connection.close();
			} catch (Exception e) {
				System.err.println("fetchHostsToAdd");
				System.err.println(e.getClass().getName() + ": "
						+ e.getMessage());
			}
		}
		return hosts;
	}

	/**
	 * Maps the host to the group in the db.
	 * 
	 * @param hostId
	 * @param groupId
	 */
	public void addHostGroup(int hostId, int groupId) {
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:"
					+ databaseFile);
			statement = connection.createStatement();
			statement.executeUpdate("PRAGMA foreign_keys=ON");
			pstmt = connection
					.prepareStatement("INSERT INTO HostGroup VALUES (?,?)");
			pstmt.setInt(1, hostId);
			pstmt.setInt(2, groupId);
			pstmt.executeUpdate();
		} catch (Exception e) {
			System.err.println("addHostGroup");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				statement.close();
				connection.close();
			} catch (Exception e) {
				System.err.println("addHostGroup");
				System.err.println(e.getClass().getName() + ": "
						+ e.getMessage());
			}
		}
	}

	/**
	 * Updates the imageMapping in the db.
	 * 
	 * @param imageMapping
	 */
	public void updateImageMapping(ImageMapping imageMapping) {
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:"
					+ databaseFile);
			statement = connection.createStatement();
			statement.executeUpdate("PRAGMA foreign_keys=ON");
			// UPDATE the IMAGE mapping (priority, parameter and timed)
			if (imageMapping.isGrouped()) {
				pstmt = connection
						.prepareStatement("UPDATE GroupImage SET timePeriod = ?, bootParameter = ?, priority = ?, imageID = ? WHERE id = ?");
				pstmt.setBoolean(1, imageMapping.isTimed());
				String bootparam = "";
				if (imageMapping.getBootParameter() != null) {
					bootparam = imageMapping.getBootParameter();
				}
				pstmt.setString(2, bootparam);
				pstmt.setInt(3, imageMapping.getPriority());
				pstmt.setInt(4, imageMapping.getImage().getId());
				pstmt.setInt(5, imageMapping.getId());
				pstmt.executeUpdate();
			} else {
				pstmt = connection
						.prepareStatement("UPDATE HostImage SET timePeriod = ?, bootParameter = ?, priority = ?, imageID = ? WHERE id = ?");
				pstmt.setBoolean(1, imageMapping.isTimed());
				String bootparam = "";
				if (imageMapping.getBootParameter() != null) {
					bootparam = imageMapping.getBootParameter();
				}
				pstmt.setString(2, bootparam);
				pstmt.setInt(3, imageMapping.getPriority());
				pstmt.setInt(4, imageMapping.getImage().getId());
				pstmt.setInt(5, imageMapping.getId());
				pstmt.executeUpdate();
			}
			// DELETE the time constraints
			if (imageMapping.isGrouped()) {
				pstmt = connection
						.prepareStatement("DELETE FROM GroupTime WHERE groupImageID = ?");
				pstmt.setInt(1, imageMapping.getId());
				pstmt.executeUpdate();
			} else {
				pstmt = connection
						.prepareStatement("DELETE FROM HostTime WHERE hostImageID = ?");
				pstmt.setInt(1, imageMapping.getId());
				pstmt.executeUpdate();
			}

			// ADD the time constraints
			if (imageMapping.isTimed()) {
				if (imageMapping.isGrouped()) {
					Iterator<TimeConstraint> iterator = imageMapping.getTimes()
							.iterator();
					while (iterator.hasNext()) {
						TimeConstraint tc = iterator.next();
						pstmt = connection
								.prepareStatement("INSERT INTO GroupTime (groupImageID, 'minute', 'hour', dom, 'month', dow, validMinutes) VALUES (?, ?, ?, ?, ?, ?, ?)");
						pstmt.setInt(1, imageMapping.getId());
						pstmt.setInt(2, tc.getBeginMinute());
						pstmt.setInt(3, tc.getBeginHour());
						if (tc.getDom() == null) {
							pstmt.setString(4, null);
						} else {
							pstmt.setInt(4, tc.getDom());
						}
						if (tc.getMonth() == null) {
							pstmt.setString(5, null);
						} else {
							pstmt.setInt(5, tc.getMonth());
						}
						if (tc.getDow() == null) {
							pstmt.setString(6, null);
						} else {
							pstmt.setInt(6, tc.getDow());
						}
						pstmt.setInt(7, tc.getValidMinutes());
						pstmt.executeUpdate();
					}
				} else {
					Iterator<TimeConstraint> iterator = imageMapping.getTimes()
							.iterator();
					while (iterator.hasNext()) {
						TimeConstraint tc = iterator.next();
						pstmt = connection
								.prepareStatement("INSERT INTO HostTime (hostImageID, 'minute', 'hour', dom, 'month', dow, validMinutes) VALUES (?, ?, ?, ?, ?, ?, ?)");
						pstmt.setInt(1, imageMapping.getId());
						pstmt.setInt(2, tc.getBeginMinute());
						pstmt.setInt(3, tc.getBeginHour());
						if (tc.getDom() == null) {
							pstmt.setString(4, null);
						} else {
							pstmt.setInt(4, tc.getDom());
						}
						if (tc.getMonth() == null) {
							pstmt.setString(5, null);
						} else {
							pstmt.setInt(5, tc.getMonth());
						}
						if (tc.getDow() == null) {
							pstmt.setString(6, null);
						} else {
							pstmt.setInt(6, tc.getDow());
						}
						pstmt.setInt(7, tc.getValidMinutes());
						pstmt.executeUpdate();
					}
				}
			}

		} catch (SQLException e) {
			System.err.println("updateImageMapping");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			System.err.println("updateImageMapping");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				statement.close();
				connection.close();
			} catch (SQLException e) {
				System.err.println("updateImageMapping");
				System.err.println(e.getClass().getName() + ": "
						+ e.getMessage());
			}
		}
	}

	/**
	 * Updates the storage in the db.
	 * 
	 * @param storage
	 */
	public void updateStorage(Storage storage) throws Exception {
		Class.forName("org.sqlite.JDBC");
		connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile);
		statement = connection.createStatement();
		statement.executeUpdate("PRAGMA foreign_keys=ON");
		pstmt = connection
				.prepareStatement("UPDATE storage SET name = ?, baseURL = ? WHERE id = ?");
		pstmt.setString(1, storage.getName());
		pstmt.setString(2, storage.getBaseURL());
		pstmt.setInt(3, storage.getId());
		pstmt.executeUpdate();
		statement.close();
		connection.close();
	}

	/**
	 * Updates the image in the db.
	 * 
	 * @param image
	 */
	public void updateImage(Image image) throws Exception {
		Class.forName("org.sqlite.JDBC");
		connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile);
		statement = connection.createStatement();
		statement.executeUpdate("PRAGMA foreign_keys=ON");
		pstmt = connection
				.prepareStatement("UPDATE image SET name = ?, description = ?, script = ? WHERE id = ?");
		pstmt.setString(1, image.getName());
		pstmt.setString(2, image.getDescription());
		pstmt.setString(3, image.getScript());
		pstmt.setInt(4, image.getId());
		pstmt.executeUpdate();
		statement.close();
		connection.close();
	}

	/**
	 * Fetches a list of assignable images
	 * 
	 * @return
	 */
	public List<Image> fetchImageList() {
		List<Image> imageList = new ArrayList<Image>();
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:"
					+ databaseFile);
			statement = connection.createStatement();
			statement.executeUpdate("PRAGMA foreign_keys=ON");
			resultSet = statement
					.executeQuery("SELECT * FROM image ORDER BY name ASC");
			while (resultSet.next()) {
				imageList.add(new Image(resultSet.getInt("id"), resultSet
						.getString("name"), resultSet.getString("description"),
						resultSet.getInt("storageID"), resultSet
								.getString("type"), resultSet
								.getString("script"), resultSet
								.getString("directory")));
			}
		} catch (Exception e) {
			System.err.println("fetchImageList");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				statement.close();
				connection.close();
			} catch (Exception e) {
				System.err.println("fetchImageList");
				System.err.println(e.getClass().getName() + ": "
						+ e.getMessage());
			}
		}
		return imageList;
	}

	/**
	 * Adds a new image mapping to the database.
	 * 
	 * @param mapping
	 */
	public void createImageMapping(ImageMapping imageMapping, int hostId) {
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:"
					+ databaseFile);
			statement = connection.createStatement();
			statement.executeUpdate("PRAGMA foreign_keys=ON");
			// CREATE the IMAGE mapping (hostID, imageID, timePeriod,
			// bootParameter, priority)
			if (imageMapping.isGrouped()) {
				pstmt = connection
						.prepareStatement("INSERT INTO GroupImage (groupID, imageID, timePeriod, bootParameter, priority) VALUES (?, ?, ?, ?, ?)");
				pstmt.setInt(1, hostId);
				pstmt.setInt(2, imageMapping.getImage().getId());
				pstmt.setBoolean(3, imageMapping.isTimed());
				String bootparam = "";
				if (imageMapping.getBootParameter() != null) {
					bootparam = imageMapping.getBootParameter();
				}
				pstmt.setString(4, bootparam);
				pstmt.setInt(5, imageMapping.getPriority());
				pstmt.executeUpdate();
			} else {
				pstmt = connection
						.prepareStatement("INSERT INTO HostImage (hostID, imageID, timePeriod, bootParameter, priority) VALUES (?, ?, ?, ?, ?)");
				pstmt.setInt(1, hostId);
				pstmt.setInt(2, imageMapping.getImage().getId());
				pstmt.setBoolean(3, imageMapping.isTimed());
				String bootparam = "";
				if (imageMapping.getBootParameter() != null) {
					bootparam = imageMapping.getBootParameter();
				}
				pstmt.setString(4, bootparam);
				pstmt.setInt(5, imageMapping.getPriority());
				pstmt.executeUpdate();
			}

			// Add the timeConstraints if isTimed()
			if (imageMapping.isTimed()) {
				// Get the mapping ID from the database
				int mappingID = 0;
				if (imageMapping.isGrouped()) {
					pstmt = connection
							.prepareStatement("SELECT id FROM GroupImage WHERE groupID = ? AND imageID = ? AND timePeriod = ? AND bootParameter = ? AND priority = ? ");
					pstmt.setInt(1, hostId);
					pstmt.setInt(2, imageMapping.getImage().getId());
					pstmt.setBoolean(3, imageMapping.isTimed());
					String bootparam = "";
					if (imageMapping.getBootParameter() != null) {
						bootparam = imageMapping.getBootParameter();
					}
					pstmt.setString(4, bootparam);
					pstmt.setInt(5, imageMapping.getPriority());
					resultSet = pstmt.executeQuery();
					while (resultSet.next()) {
						mappingID = resultSet.getInt("id");
					}
				} else {
					pstmt = connection
							.prepareStatement("SELECT id FROM HostImage WHERE hostID = ? AND imageID = ? AND timePeriod = ? AND bootParameter = ? AND priority = ? ");
					pstmt.setInt(1, hostId);
					pstmt.setInt(2, imageMapping.getImage().getId());
					pstmt.setBoolean(3, imageMapping.isTimed());
					String bootparam = "";
					if (imageMapping.getBootParameter() != null) {
						bootparam = imageMapping.getBootParameter();
					}
					pstmt.setString(4, bootparam);
					pstmt.setInt(5, imageMapping.getPriority());
					resultSet = pstmt.executeQuery();
					while (resultSet.next()) {
						mappingID = resultSet.getInt("id");
					}
				}

				// ADD the time constraints if a mapping has been found.
				if (mappingID > 0) {
					if (imageMapping.isGrouped()) {
						Iterator<TimeConstraint> iterator = imageMapping
								.getTimes().iterator();
						while (iterator.hasNext()) {
							TimeConstraint tc = iterator.next();
							pstmt = connection
									.prepareStatement("INSERT INTO GroupTime (groupImageID, 'minute', 'hour', dom, 'month', dow, validMinutes) VALUES (?, ?, ?, ?, ?, ?, ?)");
							pstmt.setInt(1, mappingID);
							pstmt.setInt(2, tc.getBeginMinute());
							pstmt.setInt(3, tc.getBeginHour());
							if (tc.getDom() == null) {
								pstmt.setString(4, null);
							} else {
								pstmt.setInt(4, tc.getDom());
							}
							if (tc.getMonth() == null) {
								pstmt.setString(5, null);
							} else {
								pstmt.setInt(5, tc.getMonth());
							}
							if (tc.getDow() == null) {
								pstmt.setString(6, null);
							} else {
								pstmt.setInt(6, tc.getDow());
							}
							pstmt.setInt(7, tc.getValidMinutes());
							pstmt.executeUpdate();
						}
					} else {
						Iterator<TimeConstraint> iterator = imageMapping
								.getTimes().iterator();
						while (iterator.hasNext()) {
							TimeConstraint tc = iterator.next();
							pstmt = connection
									.prepareStatement("INSERT INTO HostTime (hostImageID, 'minute', 'hour', dom, 'month', dow, validMinutes) VALUES (?, ?, ?, ?, ?, ?, ?)");
							pstmt.setInt(1, mappingID);
							pstmt.setInt(2, tc.getBeginMinute());
							pstmt.setInt(3, tc.getBeginHour());
							if (tc.getDom() == null) {
								pstmt.setString(4, null);
							} else {
								pstmt.setInt(4, tc.getDom());
							}
							if (tc.getMonth() == null) {
								pstmt.setString(5, null);
							} else {
								pstmt.setInt(5, tc.getMonth());
							}
							if (tc.getDow() == null) {
								pstmt.setString(6, null);
							} else {
								pstmt.setInt(6, tc.getDow());
							}
							pstmt.setInt(7, tc.getValidMinutes());
							pstmt.executeUpdate();
						}
					}
				}
			}

		} catch (SQLException e) {
			System.err.println("createImageMapping");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			System.err.println("createImageMapping");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				statement.close();
				connection.close();
			} catch (SQLException e) {
				System.err.println("createImageMapping");
				System.err.println(e.getClass().getName() + ": "
						+ e.getMessage());
			}
		}
	}

	/**
	 * Deletes all mappings between host/group and image.
	 * 
	 * @param hostId
	 *            / groupId
	 * @param imageId
	 * @param grouped
	 *            (True for group False for host)
	 */
	public void deleteAllHostMappings(int hostId, int imageId, boolean grouped) {
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:"
					+ databaseFile);
			statement = connection.createStatement();
			statement.executeUpdate("PRAGMA foreign_keys=ON");
			if (grouped) {
				pstmt = connection
						.prepareStatement("DELETE FROM GroupImage WHERE groupID = ? AND imageID = ?");
			} else {
				pstmt = connection
						.prepareStatement("DELETE FROM HostImage WHERE hostID = ? AND imageID = ?");
			}
			pstmt.setInt(1, hostId);
			pstmt.setInt(2, imageId);
			pstmt.executeUpdate();
		} catch (Exception e) {
			System.err.println("deleteAllHostMappings");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				statement.close();
				connection.close();
			} catch (Exception e) {
				System.err.println("deleteAllHostMappings");
				System.err.println(e.getClass().getName() + ": "
						+ e.getMessage());
			}
		}
	}

	/**
	 * Fetches a list of available storages from the database.
	 * 
	 * @return String list of available storages.
	 */
	public List<String> fetchAvailableStorages() {
		List<String> storages = new ArrayList<String>();
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:"
					+ databaseFile);
			statement = connection.createStatement();
			statement.executeUpdate("PRAGMA foreign_keys=ON");
			resultSet = statement
					.executeQuery("SELECT DISTINCT type FROM storage ORDER BY type ASC");
			while (resultSet.next()) {
				storages.add(resultSet.getString(1));
			}
		} catch (Exception e) {
			System.err.println("fetchAvailableStorages");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				statement.close();
				connection.close();
			} catch (Exception e) {
				System.err.println("fetchAvailableStorages");
				System.err.println(e.getClass().getName() + ": "
						+ e.getMessage());
			}
		}
		return storages;
	}
}