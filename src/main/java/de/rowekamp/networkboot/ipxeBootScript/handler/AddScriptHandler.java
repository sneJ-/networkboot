package de.rowekamp.networkboot.ipxeBootScript.handler;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.sql.SQLException;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import de.rowekamp.networkboot.database.BootScriptDatabase;

public class AddScriptHandler implements HttpHandler {

	private ParseFunctions pf = new ParseFunctions();
	private String forwardToBootScriptHandler;
	private String addHost;
	private String response;
	private URL bootServer;
	private String errorMac;
	private boolean addingEnabled;
	private File dbFile;

	public AddScriptHandler(boolean addingEnabled, URL bootServer, File dbFile) {
		this.addingEnabled = addingEnabled;
		this.bootServer = bootServer;
		forwardToBootScriptHandler = pf.parseScript("forwardToBootScriptHandler.ipxe", new String[]{bootServer.toExternalForm()});
		addHost = pf.parseScript("addHost.ipxe", new String[] { bootServer.toExternalForm() });
		errorMac = pf.parseScript("error.ipxe", new String[] {"Transferred MAC is invalid.", bootServer.toExternalForm() });
		this.dbFile = dbFile;
	}

	/*
	 * Handles the HTTP request
	 * (non-Javadoc)
	 * @see com.sun.net.httpserver.HttpHandler#handle(com.sun.net.httpserver.HttpExchange)
	 */
	public void handle(HttpExchange exc) throws IOException {
		
		Map<String, String> getParameter = pf.parseGetParameter(exc
				.getRequestURI());

		if (addingEnabled) {
			String mac = pf.parseMac(getParameter.get("mac"));
			if (mac != null) {
				BootScriptDatabase db = new BootScriptDatabase(dbFile);
				if (db.getHostId(mac) == -1) {
					String hostname = getParameter.get("hostname");
					if (hostname != null) {
						try {
							db.addHost(mac, hostname);
							response = pf.parseScript("addHostSuccessful.ipxe",
									new String[] { bootServer.toExternalForm(), hostname }); //Host successful added to db --> Screen information to validate host
						} catch (SQLException e) {
//							e.printStackTrace();
							response = pf.parseScript(
									"addHostUnsuccessful.ipxe", new String[] {
											bootServer.toExternalForm(), hostname }); //Hostname exists in database --> Screen error and menu to add host
						} finally {
							db.close();
						}
					} else {
						response = addHost; //Host doesn't exist in database --> Screen menu to add host
					}
				} else {
					response = forwardToBootScriptHandler; //Host exists in database --> Forward to bootScriptHandler 
				}
			} else {
				response = errorMac; //Transferred mac is invalid --> Screen error message
			}
		} else {
			response = forwardToBootScriptHandler; //Adding is Disabled --> Forward to bootScriptHandler
		}
		exc.sendResponseHeaders(200, response.length());
		OutputStream os = exc.getResponseBody();
		os.write(response.getBytes());
		os.close();
	}
}