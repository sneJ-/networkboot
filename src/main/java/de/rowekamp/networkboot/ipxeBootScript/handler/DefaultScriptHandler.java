package de.rowekamp.networkboot.ipxeBootScript.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class DefaultScriptHandler implements HttpHandler {

	private final String htmlLocation = "/de/rowekamp/networkboot/ipxeBootScript/handler/html";

	public void handle(HttpExchange exc) throws IOException {

		String requestedPath = exc.getRequestURI().getPath().replace("/..", "/");
		if (requestedPath.equals("/")) requestedPath = "/index.html";
		URL url = getClass().getResource(htmlLocation + requestedPath);
		if (url == null) {
			url = getClass().getResource(htmlLocation + "/index.html");
		}

		exc.sendResponseHeaders(200, 0);
		InputStream in = url.openStream();
		OutputStream os = exc.getResponseBody();

		final byte[] buffer = new byte[0x10000];
		int count = 0;
		while ((count = in.read(buffer)) >= 0) {
			os.write(buffer, 0, count);
		}

		in.close();
		os.close();
	}
}