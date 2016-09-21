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

public class BootScriptHandler implements HttpHandler {
	
	private final boolean addingEnabled;
	private final String errorMac;
	private final String errorDb;
	private final String notInDbAddingEnabled;
	private final String notInDbAddingDisabled;
	private final String hostNotValidated;
	private final String noImageAssigned;
	private final String ipxeGenerationPrefix; 
	private final File dbFile;
	private ParseFunctions pf = new ParseFunctions();
	
	public BootScriptHandler(boolean addingEnabled, URL bootServer, File dbFile){
		this.addingEnabled = addingEnabled;
		this.dbFile = dbFile;
		errorMac = pf.parseScript("error.ipxe", new String[]{"Transferred MAC is invalid.",bootServer.toExternalForm()});
		errorDb = pf.parseScript("error.ipxe", new String[]{"Couln't read the database.",bootServer.toExternalForm()});
		notInDbAddingEnabled = pf.parseScript("notInDbAddingEnabled.ipxe", new String[]{bootServer.toExternalForm()});
		notInDbAddingDisabled = pf.parseScript("notInDbAddingDisabled.ipxe", new String[]{bootServer.toExternalForm()});
		hostNotValidated = pf.parseScript("hostNotValidated.ipxe", new String[]{bootServer.toExternalForm()});
		noImageAssigned = pf.parseScript("noImageAssigned.ipxe", new String[]{bootServer.toExternalForm()});
		ipxeGenerationPrefix = "#!ipxe\n\n"+"console --picture "+ bootServer.toExternalForm()+"/background.png --left 32 --right 32 --top 32 --bottom 48\n\n";
	}

	/*
	 * Handles the HTTP Request
	 * (non-Javadoc)
	 * @see com.sun.net.httpserver.HttpHandler#handle(com.sun.net.httpserver.HttpExchange)
	 */
	public void handle(HttpExchange exc) throws IOException {
		
		Map<String, String> getParameter = pf.parseGetParameter(exc.getRequestURI());
		String response;
		
		String mac = pf.parseMac(getParameter.get("mac"));
		
		if (mac == null){ //Mac invalid --> Error
			response = errorMac;
		}
		else{ //Mac valid --> database queries
			BootScriptDatabase db = new BootScriptDatabase(dbFile);
			int hostId = db.getHostId(mac);
			switch(hostId){
			case -2: // Error occurred (database not readable)
				response = errorDb;
				break;
			case -1: // Host not in DB
				if (addingEnabled) response = notInDbAddingEnabled;
				else response = notInDbAddingDisabled;
				break;
			case 0: // Host not validated
				response = hostNotValidated;
				break;
			default: //Host in DB
				response = generateBootScript(hostId, db);
				break;
			}
			db.close();
		}
		exc.sendResponseHeaders(200, response.length());
        OutputStream os = exc.getResponseBody();
        os.write(response.getBytes());
        os.close();
	}
	
	/**
	 * Generates the iPXE boot script depending on the system time.
	 * @param hostId
	 * @param db
	 * @return
	 */
	private String generateBootScript(int hostId, BootScriptDatabase db){
		String bootScript;
		
		try{
			//Find the assigned image with the highest priority, depending on the system time.
			Object[] image = db.findImage(hostId);
			
			boolean hostImage = (Boolean) image[0];
			int imageId = (Integer) image[1];
			int imageMappingId = (Integer) image[2];
		
			if(imageId != 0){ //If an image was found, generate the corresponding iPXE bootscript
				String rootURL = db.getRootURL(imageId);
				bootScript = ipxeGenerationPrefix+db.getImageScript(imageId);
				Map<String, String> globalVariables = db.getGlobalVariables();
				Map<String, String> imageVariables = db.getImageVariables(imageId);
				Map<String, String> imageMappingVariables;
				if(hostImage) imageMappingVariables = db.getHostImageMappingVariables(imageMappingId);
				else imageMappingVariables = db.getGroupImageMappingVariables(imageMappingId);
				bootScript = pf.replaceInScript(bootScript, rootURL, imageMappingVariables, imageVariables, globalVariables);
			}else{ //If no image could be found screen corresponding notification message.
				bootScript = noImageAssigned;
			}
		}catch(SQLException e){
			e.printStackTrace();
			bootScript = errorDb;
		}
		return bootScript;
	}
}