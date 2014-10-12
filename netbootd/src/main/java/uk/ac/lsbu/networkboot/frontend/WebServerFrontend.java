package uk.ac.lsbu.networkboot.frontend;

import java.io.File;
import java.util.Collections;

import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;

import uk.ac.lsbu.networkboot.ConfigFile;
import uk.ac.lsbu.networkboot.NetworkBoot;

/**
 * Starts and configures the jetty frontend server.
 * Hosts the HTTP management environment.
 * 
 *@author Jens Röwekamp Last modified: 17.04.2014
 */
public class WebServerFrontend {
	// Default configuration
	private String databaseFile;
	private String frontendHost;
	private String storageDir;
	private String tempDir;
	private String warFile;
	private String passwdFile;
	private String keyStoreFile;
	private String keyStorePassword;
	private String keyManagerPassword;
	private String configFile;
	private int frontendPort;
	private int threads;
	private boolean ssl;
	private String maxUploadFileSize;

	private Server webserver = null;
	private ServerConnector frontend = null;

	/**
	 * Constructor
	 */
	public WebServerFrontend() {
		databaseFile = "./../config/mapping.db3";
		tempDir = "./../storage/temp";
		storageDir = "./../storage";
		frontendHost = "0.0.0.0";
		frontendPort = 8443;
		warFile = "./../frontend/frontend_netbootd.war";
		passwdFile = "./../config/authentification.properties";
		threads = 256;
		ssl = false;
		configFile = "./../config/main.cfg";
		maxUploadFileSize = "600";
	}

	/**
	 * Overwrites the default configuration with the values from the configuration file.
	 * @param configDir
	 */
	public void loadConfigurationFile(String configDir) {
		ConfigFile conf;
		configFile = configDir;
		try {
			conf = new ConfigFile(configDir);
			if (conf.checkProperty("frontendHost"))
				frontendHost = conf.getProperty("frontendHost");
			if (conf.checkProperty("frontendPort"))
				frontendPort = conf.getPropertyInt("frontendPort");
			if (conf.checkProperty("databaseFile"))
				databaseFile = conf.getProperty("databaseFile");
			if (conf.checkProperty("storageDir"))
				storageDir = conf.getProperty("storageDir");
			if (conf.checkProperty("tempDir"))
				tempDir = conf.getProperty("tempDir");
			if (conf.checkProperty("warFile"))
				warFile = conf.getProperty("warFile");
			if (conf.checkProperty("passwdFile"))
				passwdFile = conf.getProperty("passwdFile");
			if (conf.checkProperty("frontendThreatPool"))
				threads = conf.getPropertyInt("frontendThreatPool");
			if (conf.checkProperty("sslEnable"))
				ssl = conf.getPropertyBool("sslEnable");
			if (conf.checkProperty("keyStoreFile"))
				keyStoreFile = conf.getProperty("keyStoreFile");
			if (conf.checkProperty("keyStorePassword"))
				keyStorePassword = conf.getProperty("keyStorePassword");
			if (conf.checkProperty("keyManagerPassword"))
				keyManagerPassword = conf.getProperty("keyManagerPassword");
			if (conf.checkProperty("maxUploadFileSize"))
				maxUploadFileSize = conf.getProperty("maxUploadFileSize");
		} catch (Exception e) {
			System.err.println("netbootd - Frontend configuration Error.");
			e.printStackTrace();
		}
	}
	
	/**
	 * Starts the jetty webserver for the frontend
	 * @throws Exception
	 */
	public void start() throws Exception {
		//Limitate the threads of the webserver
		QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setMaxThreads(threads);
		webserver = new Server(threadPool);
		
		//Authentication preparation
		HashLoginService loginService = new HashLoginService();
		loginService.setName("netbootd authentification");
		loginService.setConfig(passwdFile);
		webserver.addBean(loginService);
		ConstraintSecurityHandler security = new ConstraintSecurityHandler();
        webserver.setHandler(security);
        Constraint constraint = new Constraint();
        constraint.setName("auth");
        constraint.setAuthenticate( true );
        constraint.setRoles(new String[]{"admin"});
        ConstraintMapping mapping = new ConstraintMapping();
        mapping.setPathSpec( "/*" );
        mapping.setConstraint( constraint );
        security.setConstraintMappings(Collections.singletonList(mapping));
        security.setAuthenticator(new BasicAuthenticator());
        security.setLoginService(loginService);
        
		//Webapp
		WebAppContext webapp = new WebAppContext();
		webapp.setContextPath("/");
		webapp.setWar(warFile);
		webapp.setParentLoaderPriority(true);  //So that sqlite only gets loaded once from the parent classloader, to avoid the java.lang.UnsatisfiedLinkError
		webapp.setInitParameter("configFile", new File(configFile).getAbsolutePath());
		webapp.setInitParameter("databaseFile", new File(databaseFile).getAbsolutePath());
		webapp.setInitParameter("tempDirectory", new File(tempDir).getAbsolutePath());
		webapp.setInitParameter("storageDirectory", new File(storageDir).getAbsolutePath());
		webapp.setInitParameter("backendVersion", NetworkBoot.backendVersion);
		webapp.setInitParameter("backendBuildDate", NetworkBoot.backendBuildDate);
		webapp.setInitParameter("iPXEVersion", NetworkBoot.iPXEVersion);
		webapp.setInitParameter("iPXEBuildDate", NetworkBoot.iPXEBuildDate);
		webapp.setInitParameter("maxUploadFileSize", maxUploadFileSize);
        String[] configuration = new String[]{
        		"org.eclipse.jetty.webapp.WebInfConfiguration",
        	    "org.eclipse.jetty.webapp.WebXmlConfiguration",
        	    "org.eclipse.jetty.webapp.MetaInfConfiguration",
        	    "org.eclipse.jetty.webapp.FragmentConfiguration",
        	    "org.eclipse.jetty.plus.webapp.EnvConfiguration",
        	    "org.eclipse.jetty.plus.webapp.PlusConfiguration",
        	    "org.eclipse.jetty.annotations.AnnotationConfiguration",
        	    "org.eclipse.jetty.webapp.JettyWebXmlConfiguration",
        	    "org.eclipse.jetty.webapp.TagLibConfiguration"
        };
        webserver.setAttribute("org.eclipse.jetty.webapp.configuration", configuration);
		
		//HTTPs Options
        if (ssl && !keyStoreFile.equals("") && !keyStorePassword.equals("")){
        	SslContextFactory sslContextFactory = new SslContextFactory();
        	sslContextFactory.setKeyStorePath(keyStoreFile);
        	sslContextFactory.setKeyStorePassword(keyStorePassword);
        	sslContextFactory.setKeyManagerPassword(keyManagerPassword);
        	sslContextFactory.setExcludeCipherSuites("SSL_RSA_WITH_DES_CBC_SHA", "SSL_DHE_RSA_WITH_DES_CBC_SHA", "SSL_DHE_DSS_WITH_DES_CBC_SHA", "SSL_RSA_EXPORT_WITH_RC4_40_MD5", "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA", "SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA", "SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA");
		
        	HttpConfiguration https_config = new HttpConfiguration();
        	https_config.setSecureScheme("https");
        	https_config.setSecurePort(frontendPort);
        	https_config.setOutputBufferSize(32768);
        	https_config.setRequestHeaderSize(8192);
        	https_config.setResponseHeaderSize(8192);
        	https_config.setSendServerVersion(true);
        	https_config.setSendDateHeader(false);
        	https_config.addCustomizer(new SecureRequestCustomizer());
        	
        	frontend = new ServerConnector(webserver, new SslConnectionFactory(sslContextFactory, "http/1.1"), new HttpConnectionFactory(https_config));
        }else{
        	//HTTP Webserver
            frontend = new ServerConnector(webserver);
        }
        
        //Port definition
		frontend.setPort(frontendPort);
		frontend.setIdleTimeout(120000);
		frontend.setHost(frontendHost);
		
		//Map the authentication to the webapp
		security.setHandler(webapp);
		
		//Webserver
		webserver.addConnector(frontend);
		webserver.start();
		System.out.println("netbootd: Frontend started on Port "
				+ frontendPort);
//		webserver.join();
	}
	
	/**
	 * Stops the frontend webserver.
	 * @throws Exception
	 */
	public void stop() throws Exception{
		webserver.stop();
		System.out.println("netbootd: Frontend stopped");
	}
}