package uk.ac.lsbu.networkboot.backend;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

/**
 * HTTP-Handler to generate the individual ipxe scripts.
 * 
 * @author Jens Röwekamp Last modified: 22.04.2014
 */

public class BootHandler extends AbstractHandler {

	/**
	 * Private variables
	 */
	// SQL-Connection
	private String databaseFile = "./../config/mapping.db3";
	private boolean addingEnabled = true;
	private int frontendPort = 8443;
	private boolean ssl = false;

	// Constructor
	public BootHandler(String databaseFile, boolean addingEnabled,
			int frontendPort, boolean ssl) {
		this.databaseFile = databaseFile;
		this.addingEnabled = addingEnabled;
		this.frontendPort = frontendPort;
		this.ssl = ssl;
	}

	/**
	 * Gets the MAC address from the GET parameter and deletes the colons.
	 * Checks also for integrity.
	 * 
	 * @param request
	 * @return Returns the MAC address without colons if valid or NULL.
	 */
	private String getMac(HttpServletRequest request) {
		String macAddr = null;
		if (request.getParameterValues("mac") != null) {
			macAddr = request.getParameterValues("mac")[0].replace(":", "")
					.toLowerCase();
			// Check if the transfered data is really a MAC address
			if (macAddr.length() == 12
					&& macAddr.matches("[abcdef0123456789]*")) {
				return macAddr;
			}
		}
		return null;
	}

	/**
	 * Checks if the mac Address is in the database.
	 * 
	 * @param Requires
	 *            the mac as low-character String.
	 * @return Returns -2 in case of an error, -1 if the host isn't in the
	 *         database, 0 if the host isn't validated yet and the hostID if the
	 *         host is found.
	 */
	private int checkMacInDatabase(String mac) {
		int returns = -1;
		Connection connection = null;;
		Statement statement = null;
		PreparedStatement pstmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile);
			statement = connection.createStatement();
			statement.executeUpdate("PRAGMA foreign_keys=ON");
			pstmt = connection
					.prepareStatement("SELECT id, validated FROM host WHERE MAC = ?");
			pstmt.setString(1, mac);
			ResultSet resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				returns = resultSet.getInt("id");
				if (resultSet.getInt("validated") == 0) {
					returns = 0;
				}
				// System.out.println(returns);
			}
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
			returns = -2;
		} finally {
			try {
				statement.close();
				pstmt.close();
				connection.close();
			} catch (Exception ef) {
				System.err.println("checkMacInDatabase - Closing");
				System.err.println(ef.getClass().getName() + ": "
						+ ef.getMessage());
				ef.printStackTrace();
				returns = -2;
			}
		}
		return returns;
	}

	/**
	 * Adds the host to the database if the GET parameter ADD is true.
	 * 
	 * @param macAddr
	 * @param request
	 */
	private void addHostToDB(String macAddr, HttpServletRequest request) {
		if (request.getParameterValues("add") != null
				&& request.getParameterValues("product") != null
				&& request.getParameterValues("uuid") != null
				&& request.getParameterValues("serial") != null
				&& request.getParameterValues("asset") != null) {
			if (request.getParameterValues("add")[0].equals("true")) {
				Connection connection = null;
				Statement statement = null;
				PreparedStatement pstmt = null;
				try {
					Class.forName("org.sqlite.JDBC");
					connection = DriverManager.getConnection("jdbc:sqlite:"
							+ databaseFile);
					statement = connection.createStatement();
					statement.executeUpdate("PRAGMA foreign_keys=ON");
					statement.close();
					pstmt = connection
							.prepareStatement("INSERT INTO host (name, MAC, validated) VALUES (?,?,0)");
					pstmt.setString(
							1,
							"Product:"
									+ request.getParameterValues("product")[0]
									+ " UUID:"
									+ request.getParameterValues("uuid")[0]
									+ " Serial:"
									+ request.getParameterValues("serial")[0]
									+ " Asset:"
									+ request.getParameterValues("asset")[0]);
					pstmt.setString(2, macAddr);
					pstmt.executeUpdate();
				} catch (Exception e) {
					System.err.println("addHostToDB");
					System.err.println(e.getClass().getName() + ": "
							+ e.getMessage());
				} finally {
					try {
						statement.close();
						pstmt.close();
						connection.close();
					} catch (Exception ef) {
						System.err.println("addHostToDB");
						System.err.println(ef.getClass().getName() + ": "
								+ ef.getMessage());
					}
				}
			}
		}
	}

	/**
	 * Produces the ipxe script for validated hosts in the database.
	 * 
	 * @param response
	 * @param hostID
	 * @throws IOException
	 */
	private void output(HttpServletResponse response, int hostID)
			throws IOException {
		response.getWriter().println("#!ipxe");
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:"
					+ databaseFile);
			statement = connection.createStatement();
			// Activating foreign keys in sqlite
			statement.executeUpdate("PRAGMA foreign_keys=ON");
			int priority = -1;
			int imageID = -1;
			boolean HostImage = true;
			String HostName = "";

			// HostImage no time
			resultSet = statement
					.executeQuery("SELECT HostImage.id, HostImage.priority"
							+ " FROM HostImage, image, storage"
							+ " WHERE HostImage.timePeriod = 0"
							+ " AND HostImage.hostID=" + hostID
							+ " AND HostImage.ImageID = image.id"
							+ " AND image.storageID=storage.id"
							+ " ORDER BY HostImage.priority DESC" + " LIMIT 1");
			while (resultSet.next()) {
				priority = resultSet.getInt("priority");
				imageID = resultSet.getInt("id");
				// System.out.println("HostImage no time - hostImageID:  " +
				// imageID
				// + " priority: " + priority);
			}
			// GroupImage no time
			resultSet = statement
					.executeQuery("SELECT GroupImage.id, GroupImage.priority"
							+ " FROM GroupImage, image, storage"
							+ " WHERE GroupImage.timePeriod = 0"
							+ " AND GroupImage.priority > "
							+ priority
							+ " AND GroupImage.GroupID=(SELECT groupID FROM HostGroup WHERE hostID="
							+ hostID + ")"
							+ " AND GroupImage.imageID = image.id"
							+ " AND image.storageID=storage.id"
							+ " ORDER BY GroupImage.priority DESC"
							+ " LIMIT 1;");
			while (resultSet.next()) {
				HostImage = false;
				priority = resultSet.getInt("priority");
				imageID = resultSet.getInt("id");
				// System.out.println("GroupImage no time -"
				// + " groupImageID:  " + imageID + " priority: " + priority);
			}

			// Get the actual time.
			Calendar cal = Calendar.getInstance(Locale.getDefault());
			int dow = cal.get(Calendar.DAY_OF_WEEK) - 1;
			int dom = cal.get(Calendar.DAY_OF_MONTH);
			int month = cal.get(Calendar.MONTH) + 1;
			int hour = cal.get(Calendar.HOUR_OF_DAY);
			int minute = cal.get(Calendar.MINUTE);

			// Group time
			resultSet = statement
					.executeQuery("SELECT GroupImage.id, GroupImage.priority"
							+ " FROM image, storage, GroupImage, GroupTime"
							+ " WHERE GroupImage.priority >= "
							+ priority
							+ " AND GroupImage.GroupID=(SELECT groupID FROM HostGroup WHERE hostID="
							+ hostID
							+ ")"
							+ " AND (GroupTime.dom IS NULL OR GroupTime.dom = "
							+ dom
							+ ")" // dom
							+ " AND (GroupTime.month IS NULL OR GroupTime.month = "
							+ month
							+ ")" // month
							+ " AND (GroupTime.dow IS NULL OR GroupTime.dow = "
							+ dow
							+ ")" // dow
							+ " AND ("
							+ hour
							+ "*60 + "
							+ minute
							+ " - GroupTime.minute - GroupTime.hour*60 < GroupTime.validMinutes) " // valid
							+ " AND ("
							+ hour
							+ "*60 + "
							+ minute
							+ " - GroupTime.minute - GroupTime.hour*60 >= 0)" // time
							+ " AND GroupImage.imageID = image.id"
							+ " AND image.storageID=storage.id"
							+ " AND GroupImage.id = GroupTime.groupImageID"
							+ " ORDER BY GroupImage.priority DESC"
							+ " LIMIT 1;");
			while (resultSet.next()) {
				HostImage = false;
				priority = resultSet.getInt("priority");
				imageID = resultSet.getInt("id");
				// System.out.println("GroupImage time - "
				// + " GroupImageID:  " + imageID + " priority: "
				// + priority);
			}
			// HostImage time
			resultSet = statement
					.executeQuery("SELECT HostImage.id, priority"
							+ " FROM HostImage, HostTime, image, storage"
							+ " WHERE HostImage.priority >= "
							+ priority
							+ " AND (HostTime.dom IS NULL OR HostTime.dom = "
							+ dom
							+ ")" // dom
							+ " AND (HostTime.month IS NULL OR HostTime.month = "
							+ month
							+ ")" // month
							+ " AND (HostTime.dow IS NULL OR HostTime.dow = "
							+ dow
							+ ")" // dow
							+ " AND ("
							+ hour
							+ "*60 + "
							+ minute
							+ " - HostTime.minute - HostTime.hour*60 < HostTime.validMinutes)" // valid
							+ " AND ("
							+ hour
							+ "*60 + "
							+ minute
							+ " - HostTime.minute - HostTime.hour*60 >= 0)" // time
							+ " AND HostImage.hostID="
							+ hostID
							+ " AND HostImage.imageID = image.id"
							+ " AND image.storageID=storage.id"
							+ " AND HostImage.id=HostTime.hostImageID"
							+ " ORDER BY HostImage.priority DESC" + " LIMIT 1;");
			while (resultSet.next()) {
				HostImage = true;
				priority = resultSet.getInt("priority");
				imageID = resultSet.getInt("id");
				// System.out.println("HostImage time - HostImageID:  " +
				// imageID
				// + " priority: " + priority);
			}
			// IF an image has been found
			if (priority > -1) {
				// if (HostImage) System.out.println("HostImageID : " +
				// imageID);
				// else System.out.println("GroupImageID: " + imageID);
				// System.out.println("Priority: " + priority);
				String query;
				if (HostImage) {
					query = "SELECT HostImage.bootParameter, storage.baseURL, image.directory, image.script"
							+ " FROM HostImage, storage, image"
							+ " WHERE HostImage.id = "
							+ imageID
							+ " AND HostImage.imageID = image.id"
							+ " AND image.storageID = storage.id";
				} else {
					query = "SELECT GroupImage.bootParameter, storage.baseURL, image.directory, image.script"
							+ " FROM GroupImage, storage, image"
							+ " WHERE GroupImage.id = "
							+ imageID
							+ " AND GroupImage.imageID = image.id"
							+ " AND image.storageID = storage.id";
				}
				resultSet = statement.executeQuery(query);
				while (resultSet.next()) {
					String baseURL = resultSet.getString("baseURL");
					String directory = resultSet.getString("directory");
					String script = resultSet.getString("script");
					String bootParameter = resultSet.getString("bootParameter");
					script = script.replace("$URL$", baseURL + "/" + directory
							+ "/");
					script = script.replace("$BootParam$", bootParameter);
					response.getWriter().print(script);
				}
			}
			// IF no image has been found but host exists
			else {
				// SELECT the HostName
				resultSet.close();
				resultSet = statement
						.executeQuery("SELECT host.name FROM host WHERE host.ID = "
								+ hostID);
				while (resultSet.next()) {
					HostName = resultSet.getString("name");
				}
				response.getWriter().println(
						"menu No host image has been assigned!");
				response.getWriter().println("item shutdown Shutdown");
				response.getWriter().println("item --gap");
				response.getWriter()
						.println("item --gap Hostname: " + HostName);
				response.getWriter().println("item --gap MAC: ${net0/mac}");
				response.getWriter()
						.println(
								"choose --default shutdown --timeout 30000 target && goto ${target}");
				response.getWriter().println(":shutdown");
				response.getWriter().println("poweroff");
			}
		} catch (IOException e) {
			System.err.println("output");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			output_error(response);
		} catch (SQLException e) {
			System.err.println("output");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			output_error(response);
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			System.err.println("output");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			output_error(response);
		} finally {
			try {
				statement.close();
				connection.close();
			} catch (Exception e) {
				System.err.println("output");
				System.err.println(e.getClass().getName() + ": "
						+ e.getMessage());
				output_error(response);
			}
		}
	}

	/**
	 * Produces the ipxe script for not validated hosts. Sends a short
	 * information that the host could be validated through the web-frontend,
	 * then powers of the machine.
	 * 
	 * @param response
	 * @param macAddr
	 * @throws IOException
	 */
	private void output_notValidated(HttpServletResponse response,
			String macAddr) throws IOException {
		response.getWriter().println("#!ipxe");
		response.getWriter().println("menu Host has not been validated yet.");
		response.getWriter().println(
				"item --gap Please validate the host via the frontend.");
		response.getWriter().println("item shutdown Shutdown");
		response.getWriter()
				.println(
						"choose --default shutdown --timeout 30000 target && goto ${target}");
		response.getWriter().println("item --gap");
		response.getWriter().println("item --gap MAC: ${net0/mac}");
		response.getWriter().println(":shutdown");
		response.getWriter().println("poweroff");
	}

	/**
	 * Produces the ipxe script for hosts who are not in the database.
	 * 
	 * @param response
	 * @param URL
	 * @throws IOException
	 */
	private void output_notInDatabase(HttpServletResponse response, String URL)
			throws IOException {
		response.getWriter().println("#!ipxe");
		response.getWriter().println("menu Host has not been added yet.");
		response.getWriter().println(
				"item --gap Please add the host to your database.");
		if (addingEnabled) {
			response.getWriter().println("item add Add now");
		}
		response.getWriter().println("item shutdown Shutdown");
		response.getWriter().println("item --gap");
		response.getWriter().println("item --gap MAC: ${net0/mac}");
		response.getWriter()
				.println("item --gap Product: ${product:uristring}");
		response.getWriter().println("item --gap UUID: ${uuid}");
		response.getWriter().println("item --gap Serial: ${serial}");
		response.getWriter().println("item --gap Asset: ${asset:uristring}");
		response.getWriter()
				.println(
						"choose --default shutdown --timeout 30000 target && goto ${target}");
		response.getWriter().println(":shutdown");
		response.getWriter().println("poweroff");
		if (addingEnabled) {
			response.getWriter().println(":add");
			response.getWriter().println("sleep 1");
			response.getWriter()
					.println(
							"chain "
									+ URL
									+ "?mac=${net0/mac}&add=true&product=${product:uristring}&uuid=${uuid}&serial=${serial}&asset=${asset:uristring}");
		}
	}

	/**
	 * Produces the HTML Site if an invalid MAC is choosen. This website
	 * redirects the user to the frontend.
	 * 
	 * @param response
	 * @throws IOException
	 */
	private void output_invalidMac(HttpServletResponse response,
			HttpServletRequest request) throws IOException {
		response.getWriter()
				.println(
						"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"");
		response.getWriter().println(
				"       \"http://www.w3.org/TR/html4/loose.dtd\">");
		if (ssl) {
			response.getWriter()
					.println(
							"<html><head><meta http-equiv=\"refresh\" content=\"0; URL=https://"
									+ request.getLocalAddr()
									+ ":"
									+ frontendPort
									+ "\"><h1>NetworkBoot</h1> You will be redirected to the frontend.</head><body></body></html>");
		} else {
			response.getWriter()
					.println(
							"<html><head><meta http-equiv=\"refresh\" content=\"0; URL=http://"
									+ request.getLocalAddr()
									+ ":"
									+ frontendPort
									+ "\"><h1>NetworkBoot</h1> You will be redirected to the frontend.</head><body></body></html>");
		}
	}

	/**
	 * Produces the ipxe script if an error occures.
	 * 
	 * @param response
	 * @throws IOException
	 */
	private void output_error(HttpServletResponse response) throws IOException {
		response.getWriter().println("#!ipxe");
		response.getWriter().println("menu Error");
		response.getWriter()
				.println(
						"item --gap Ooops an error occured. Please contact your local administrator for advice.");
		response.getWriter().println("item shutdown Shutdown");
		response.getWriter()
				.println(
						"choose --default shutdown --timeout 30000 target && goto ${target}");
		response.getWriter().println(":shutdown");
		response.getWriter().println("poweroff");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jetty.server.Handler#handle(java.lang.String,
	 * org.eclipse.jetty.server.Request, javax.servlet.http.HttpServletRequest,
	 * javax.servlet.http.HttpServletResponse)
	 */
	public void handle(String target, Request baseRequest,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		response.setContentType("text/html;charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);
		baseRequest.setHandled(true);
		String macAddr = getMac(request);
		if (macAddr != null) { // Valid mac or not?
			if (addingEnabled) {
				// Check if to add the host.
					addHostToDB(macAddr, request);
			}
			int hostID = checkMacInDatabase(macAddr);
			switch (hostID) {
			case -2: // Error occurred (database not readable)
				output_error(response);
				break;
			case -1: // Host not in database
				output_notInDatabase(
						response,
						"http://" + request.getServerName() + ":"
								+ request.getServerPort() + "/add.php");
				break;
			case 0: // Host not validated
				output_notValidated(response, macAddr);
				break;
			default: // Host in database
				output(response, hostID);
				break;
			}
		} else { // not valid mac
			output_invalidMac(response, request);
		}
	}
}