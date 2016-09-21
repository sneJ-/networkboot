package de.rowekamp.networkboot.ipxeBootScript.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;

public class ParseFunctions {

	/**
	 * Extracts the GET parameter from an URI
	 * 
	 * @param uri
	 * @return HashMap of GET parameter (K,V)
	 */
	public Map<String, String> parseGetParameter(URI uri) {
		Map<String, String> getParameter = new HashMap<String, String>();
		String query = uri.getQuery();
		if (query != null) {
			for (String param : query.split("&")) {
				String pair[] = param.split("=");
				if (pair.length > 1)
					getParameter.put(pair[0], pair[1]);
				else
					getParameter.put(pair[0], "");
			}
		}
		return getParameter;
	}

	/**
	 * Extracts the POST parameter from the HttpExchange.
	 * 
	 * @param exc
	 * @return HashMap of Post parameter (K,V)
	 */
	public Map<String, String> parsePostParameter(HttpExchange exc) {
		Map<String, String> postParameter = new HashMap<String, String>();
		
		try {
			InputStreamReader isr =  new InputStreamReader(exc.getRequestBody(), "utf-8");
			BufferedReader br = new BufferedReader(isr);
			String currentLine;
			while ((currentLine = br.readLine()) != null) {
				for (String param : currentLine.split("&")) {
					String pair[] = param.split("=");
					if (pair.length > 1)
						postParameter.put(pair[0], pair[1]);
					else
						postParameter.put(pair[0], "");
				}
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException ex){
			ex.printStackTrace();
		}
		
		return postParameter;
	}
	
	/**
	 * Parses the MAC and checks for integrity
	 * 
	 * @param macUnparsed
	 * @return parsed MAC or null if invalid
	 */
	public String parseMac(String macUnparsed) {
		if (macUnparsed != null) {
			String mac = macUnparsed.replace(":", "").toLowerCase();
			if (mac.length() == 12 && mac.matches("[abcdef0123456789]*")) {
				return mac;
			}
		}
		return null;
	}

	/**
	 * Parses the file for iPXE output. Transforms the file into a String and replaces variables regarding to the replaceVector.
	 * 
	 * @param file
	 * @param replaceVector
	 *            ($0$ --> [0], $1$ --> [1] ...)
	 * @return
	 */
	public String parseScript(String file, String[] replaceVector) {
		StringBuilder out = new StringBuilder();
		InputStream ipxeFile = getClass().getResourceAsStream(
				"/de/rowekamp/networkboot/ipxeBootScript/handler/scripts/" + file);
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				ipxeFile));
		String currentLine;
		try {
			while ((currentLine = reader.readLine()) != null) {
				if (replaceVector != null) {
					String currentLineModified;
					for (int i = 0; i < replaceVector.length; i++) {
						currentLineModified = currentLine.replace(
								"$" + i + "$", replaceVector[i]);
						currentLine = currentLineModified;
					}
				}
				out.append(currentLine + "\n");
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				ipxeFile.close();
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return out.toString();
	}
	
	/**
	 * Generates an iPXE boot script by replacing the imageMappingVariables and imageVariables.
	 * @param bootScript
	 * @param imageMappingVariables
	 * @param imageVariables
	 * @return iPXE boot script
	 */
	public String replaceInScript(String bootScript, String rootURL, Map<String,String> imageMappingVariables, Map<String,String> imageVariables, Map<String,String> globalVariables){
		String temporaryBootScript = bootScript.replace("$rootURL$", rootURL);
		bootScript = temporaryBootScript;
		for(Map.Entry<String, String> replaceEntry : imageMappingVariables.entrySet()){
			temporaryBootScript = bootScript.replace("$"+replaceEntry.getKey()+"$", replaceEntry.getValue());
			bootScript = temporaryBootScript;
		}
		for(Map.Entry<String, String> replaceEntry : imageVariables.entrySet()){
			temporaryBootScript = bootScript.replace("$"+replaceEntry.getKey()+"$", replaceEntry.getValue());
			bootScript = temporaryBootScript;
		}
		for(Map.Entry<String, String> replaceEntry : globalVariables.entrySet()){
			temporaryBootScript = bootScript.replace("$"+replaceEntry.getKey()+"$", replaceEntry.getValue());
			bootScript = temporaryBootScript;
		}
		return bootScript;
	}
}
