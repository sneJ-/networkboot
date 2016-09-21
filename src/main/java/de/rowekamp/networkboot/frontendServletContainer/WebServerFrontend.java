package de.rowekamp.networkboot.frontendServletContainer;

import java.io.File;

import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Starts and configures the jetty server, which hosts the frontend. 
 * 
 */
public class WebServerFrontend {

	private File databaseFile;
	private File warFile;
	private String RMI_ID;
	private Integer RMI_PORT;

	private Server webserver = null;

	/**
	 * Constructor
	 */
	public WebServerFrontend(File databaseFile, int port, File warFile, int RMI_PORT, String RMI_ID) {
		this.databaseFile = databaseFile;
		this.warFile = warFile;
		this.RMI_ID = RMI_ID;
		this.RMI_PORT = RMI_PORT;
		webserver = new Server(port);
	}
	
	public WebServerFrontend(File databaseFile, int port, File warFile, int RMI_PORT, String RMI_ID, File keyStoreFile, String keyStorePassword) {
		this.databaseFile = databaseFile;
		this.warFile = warFile;
		this.RMI_ID = RMI_ID;
		this.RMI_PORT = RMI_PORT;
		webserver = new Server();
		
		SslContextFactory sslContextFactory = new SslContextFactory();
    	sslContextFactory.setKeyStorePath(keyStoreFile.getAbsolutePath());
    	sslContextFactory.setKeyStorePassword(keyStorePassword);
    	sslContextFactory.setKeyManagerPassword(keyStorePassword);
    	sslContextFactory.setExcludeCipherSuites("SSL_RSA_WITH_DES_CBC_SHA", "SSL_DHE_RSA_WITH_DES_CBC_SHA", "SSL_DHE_DSS_WITH_DES_CBC_SHA", "SSL_RSA_EXPORT_WITH_RC4_40_MD5", "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA", "SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA", "SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA");
	
    	HttpConfiguration https_config = new HttpConfiguration();
    	https_config.setSecureScheme("https");
    	https_config.setSecurePort(port);
    	https_config.setOutputBufferSize(32768);
    	https_config.addCustomizer(new SecureRequestCustomizer());
    	
    	ServerConnector https = new ServerConnector(webserver, new SslConnectionFactory(sslContextFactory,HttpVersion.HTTP_1_1.asString()),new HttpConnectionFactory(https_config));
        https.setPort(port);
        https.setIdleTimeout(500000);
        
        webserver.setConnectors(new Connector[]{https});
	}
	
	public void start() throws Exception{
		WebAppContext webapp = new WebAppContext();
		webapp.setContextPath("/");
		webapp.setWar(warFile.getAbsolutePath());
		webapp.setParentLoaderPriority(true);  //So that sqlite only gets loaded once, from the parent classloader, to avoid java.lang.UnsatisfiedLinkError
		webapp.setInitParameter("databaseFile", databaseFile.getAbsolutePath());
		webapp.setInitParameter("RMI_ID", RMI_ID);
		webapp.setInitParameter("RMI_PORT", RMI_PORT.toString());
		
		webserver.setStopAtShutdown(true);
		webserver.setHandler(webapp);
		webserver.start();
//		webserver.join();
		System.err.println("frontend started");
	}
	
	/**
	 * Stops the frontend webserver.
	 * @throws Exception
	 */
	public void shutdown() throws Exception{
		webserver.stop();
		System.err.println("frontend stopped");
	}
}