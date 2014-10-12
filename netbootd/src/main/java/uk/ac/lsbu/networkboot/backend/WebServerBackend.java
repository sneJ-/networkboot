package uk.ac.lsbu.networkboot.backend;

import java.io.File;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import uk.ac.lsbu.networkboot.ConfigFile;

/**
 * Starts and configures the jetty backend server.
 * Hosts the ipxe scripts and http images.
 * 
 *@author Jens Röwekamp Last modified: 19.01.2014
 */
public class WebServerBackend {
	// Default configuration
	private String databaseFile;
	private int backendPort;
	private String backendHost;
	private int frontendPort;
	private boolean addingEnabled;
	private int threatPoolNumber;
	private boolean imageServer;
	private String imageDir;
	private boolean ssl;

	private Server webserver = null;
	private ServerConnector backend = null;
	private QueuedThreadPool threatPool = null;
	private ContextHandler context = null;
	private ContextHandler contextImage = null;
	private ContextHandlerCollection contexts = null;
	private ResourceHandler imageHandler = null;

	public WebServerBackend() {
		databaseFile = "./../config/mapping.db3";
		backendPort = 55555;
		backendHost = "0.0.0.0";
		frontendPort = 8443;
		addingEnabled = true;
		threatPoolNumber = 256;
		imageServer = false;
		imageDir = "./../storage/HTTP";
		ssl = false;
	}

	/**
	 * Overwrites the default configuration with the values from the configuration file.
	 * @param configDir
	 */
	public void loadConfigurationFile(String configDir) {
		ConfigFile conf;
		try {
			conf = new ConfigFile(configDir);
			if (conf.checkProperty("backendPort"))
				backendPort = conf.getPropertyInt("backendPort");
			if (conf.checkProperty("backendHost"))
				backendHost = conf.getProperty("backendHost");
			if (conf.checkProperty("frontendPort"))
				frontendPort = conf.getPropertyInt("frontendPort");
			if (conf.checkProperty("addingEnabled"))
				addingEnabled = conf.getPropertyBool("addingEnabled");
			if (conf.checkProperty("databaseFile"))
				databaseFile = conf.getProperty("databaseFile");
			if (conf.checkProperty("backendThreatPool"))
				threatPoolNumber = conf.getPropertyInt("backendThreatPool");
			if (conf.checkProperty("imageServer")) imageServer = conf.getPropertyBool("imageServer");
			if (conf.checkProperty("imageDir")) imageDir = conf.getProperty("imageDir");
			if (conf.checkProperty("sslEnable") && conf.checkProperty("keyStoreFile") && conf.checkProperty("keyStorePassword")){
				ssl = conf.getPropertyBool("sslEnable");
			}
		} catch (Exception e) {
			System.err.println("netbootd - Backend configuration Error.");
			e.printStackTrace();
		}
	}

	/**
	 * Checks if the database is accessible.
	 * @return returns true if yes, false if not.
	 */
	public boolean checkDatabaseFile(){
		boolean returns = false;
		File file = new File(databaseFile);
		if (file.canRead() && !addingEnabled) returns = true;
		if (file.canWrite() && addingEnabled) returns = true;
		if (!returns){
			System.err.println("netbootd - Backend Error: Database file " + databaseFile + " isn't accessable.");
		}
		return returns;
	}
	
	/**
	 * Starts the jetty webserver for the backend (script and image host)
	 * @throws Exception
	 */
	public void start() throws Exception {
		threatPool = new QueuedThreadPool();
		threatPool.setMaxThreads(threatPoolNumber);
		webserver = new Server(threatPool);
		webserver.manage(threatPool);
//      webserver.setDumpAfterStart(false);
//      webserver.setDumpBeforeStop(false);
//		webserver.dumpStdErr();
		context = new ContextHandler("/");
		context.setContextPath("/");
		context.setHandler(new BootHandler(databaseFile, addingEnabled, frontendPort, ssl));
		if (imageServer) contextImage = new ContextHandler("/images");
		imageHandler = new ResourceHandler();
		imageHandler.setDirectoriesListed(false);
		imageHandler.setWelcomeFiles(new String [] { "index.html" });
		imageHandler.setResourceBase(imageDir);
        if (imageServer) contextImage.setHandler(imageHandler);
        contexts = new ContextHandlerCollection();
        if (imageServer) contexts.setHandlers(new Handler[] { context, contextImage });
        else contexts.setHandlers(new Handler [] { context });
		backend = new ServerConnector(webserver);
		backend.setPort(backendPort);
		backend.setIdleTimeout(30000);
		backend.setHost(backendHost);
		webserver.addConnector(backend);
		webserver.setHandler(contexts);
		webserver.start();
		System.out.println("netbootd: Backend started on port "
				+ backendPort);
//		webserver.join();
	}
	
	/**
	 * Stops the backend webserver.
	 * @throws Exception
	 */
	public void stop() throws Exception{
		webserver.stop();
		System.out.println("netbootd: Backend stopped");
	}
	
}