package de.rowekamp.networkboot.ipxeBootScript.handler;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.sql.SQLException;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import de.rowekamp.networkboot.authentication.Authenticator;
import de.rowekamp.networkboot.database.BootScriptDatabase;

public class AuthScriptHandler implements HttpHandler {
	
	private final File dbFile;
	private final URL bootServer;
	private final String errorMac, errorDb, noImageAssigned, forwardToBootScriptHandler, loginFailed, loginPage, ipxeGenerationPrefix;
	private final int blockingTimeIncreaseMinutes, maxFailedLoginAttempts;
	private ParseFunctions pf = new ParseFunctions();
	
	public AuthScriptHandler(URL bootServer, File dbFile, int maxFailedLoginAttempts, int blockingTimeIncreaseMinutes){
		this.dbFile = dbFile;
		this.bootServer = bootServer;
		this.blockingTimeIncreaseMinutes = blockingTimeIncreaseMinutes;
		this.maxFailedLoginAttempts = maxFailedLoginAttempts;
		errorMac = pf.parseScript("error.ipxe", new String[]{"Transferred MAC is invalid.",bootServer.toExternalForm()});
		errorDb = pf.parseScript("error.ipxe", new String[]{"Couln't read the database.",bootServer.toExternalForm()});
		noImageAssigned = pf.parseScript("noImageAssigned.ipxe", new String[]{bootServer.toExternalForm()});
		forwardToBootScriptHandler = pf.parseScript("forwardToBootScriptHandler.ipxe", new String[]{bootServer.toExternalForm()});
		loginFailed = pf.parseScript("loginFailed.ipxe", new String[]{bootServer.toExternalForm(), "Invalid username or password", "", "Please try to login again"});
		loginPage = pf.parseScript("loginPage.ipxe", new String[]{bootServer.toExternalForm()});
		ipxeGenerationPrefix = "#!ipxe\n\n"+"console --picture "+ bootServer.toExternalForm()+"/background.png --left 32 --right 32 --top 32 --bottom 48\n\n";
	}
	
	@SuppressWarnings("unused")
	public void handle(HttpExchange exc) throws IOException {

		Map<String, String> getParameter = pf.parseGetParameter(exc.getRequestURI());
		Map<String, String> postParameter = pf.parsePostParameter(exc);
		String response;
		
		String mac = pf.parseMac(getParameter.get("mac"));
		String username = null;
		String password = null;
		if(postParameter.get("username") != null && postParameter.get("password") != null){
			username = java.net.URLDecoder.decode(postParameter.get("username"), "UTF-8");
			password = java.net.URLDecoder.decode(postParameter.get("password"), "UTF-8");
		}
		
		Authenticator auth = new Authenticator(dbFile,maxFailedLoginAttempts, blockingTimeIncreaseMinutes);
		switch(auth.authenticate(mac, username, password)){
		case 0:	//login succeeded
			response = generateBootScript(username);
			break;
		case 1:	//user tried to login to often
			response = pf.parseScript("loginFailed.ipxe", new String[]{bootServer.toExternalForm(), "Your username "+username+" is blocked.", "You tried to login too often." ,"Please wait " + blockingTimeIncreaseMinutes + " minutes or contact your administrator to login again."});
			break;
		case 2:	//mac was invalid
			response = errorMac;
			break;
		case 3:	//username/password were wrong
			response = loginFailed;
			break;
		case 4: //errors occurred during authentication
			response = pf.parseScript("error.ipxe", new String[]{"Authentication failed. Something went wrong with the hashing algorithm.", bootServer.toExternalForm()});
			break;
		case 5: //errors occurred during authentication
			response = pf.parseScript("error.ipxe", new String[]{"Authentication failed. Something went wrong with the hashing algorithm.", bootServer.toExternalForm()});
			break;
		case 6: //username/password weren't transfered correctly
			response = loginPage;
			break;
		case 7:	//host wasn't allowed to login
			response = forwardToBootScriptHandler;
			break;
		case 8: //database error occurred
			response = errorDb;
			break;
		default: //unknown error occurred
			response: response = pf.parseScript("error.ipxe", new String[]{"Authentication failed. An unknown error occured.", bootServer.toExternalForm()});
			break;
		}
		auth.close();

		exc.sendResponseHeaders(200, response.length());
        OutputStream os = exc.getResponseBody();
        os.write(response.getBytes());
        os.close();
	}

	/**
	 * Generates the iPXE boot script depending on the system time.
	 * @param username
	 * @param db
	 * @return
	 */
	private String generateBootScript(String username) {
		String bootScript;
		BootScriptDatabase db = new BootScriptDatabase(dbFile);
		try{
			//Find the assigned image with the highest priority, depending on the system time.
			Object[] image = db.findUserImage(username);
			
			boolean userImage = (Boolean) image[0];
			int imageId = (Integer) image[1];
			int imageMappingId = (Integer) image[2];
		
			if(imageId != 0){ //If an image was found, generate the corresponding iPXE bootscript
				String rootURL = db.getRootURL(imageId);
				bootScript = ipxeGenerationPrefix+db.getImageScript(imageId);
				Map<String, String> globalVariables = db.getGlobalVariables();
				Map<String, String> imageVariables = db.getImageVariables(imageId);
				Map<String, String> imageMappingVariables;
				if(userImage) imageMappingVariables = db.getUserImageMappingVariables(imageMappingId);
				else imageMappingVariables = db.getUserGroupImageMappingVariables(imageMappingId);
				bootScript = pf.replaceInScript(bootScript, rootURL, imageMappingVariables, imageVariables, globalVariables);
			}else{ //If no image could be found screen corresponding notification message.
				bootScript = noImageAssigned;
			}
		}catch(SQLException e){
			e.printStackTrace();
			bootScript = errorDb;
		}finally{
			db.close();
		}
		return bootScript;
	}
}
