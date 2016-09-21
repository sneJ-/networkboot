package de.rowekamp.networkboot.ipxeBootScript;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

import de.rowekamp.networkboot.ipxeBootScript.handler.AddScriptHandler;
import de.rowekamp.networkboot.ipxeBootScript.handler.AuthScriptHandler;
import de.rowekamp.networkboot.ipxeBootScript.handler.BootScriptHandler;
import de.rowekamp.networkboot.ipxeBootScript.handler.DefaultScriptHandler;

public class IPXEBootScriptService {
	private HttpServer server;
	private HttpsServer httpsServer;
	
	public IPXEBootScriptService(boolean addingEnabled, URL bootServer, File dbFile, int  maxFailedLoginAttempts, int blockingTimeIncreaseMinutes) throws IOException{
		 server = HttpServer.create(new InetSocketAddress(bootServer.getPort()), 0);
		 server.createContext("/boot", new BootScriptHandler(addingEnabled, bootServer, dbFile));
		 server.createContext("/add", new AddScriptHandler(addingEnabled, bootServer, dbFile));
		 server.createContext("/auth", new AuthScriptHandler(bootServer, dbFile, maxFailedLoginAttempts, blockingTimeIncreaseMinutes ));
		 server.createContext("/", new DefaultScriptHandler());
	     server.setExecutor(null); // creates a default executor
	}
	
	public IPXEBootScriptService(boolean addingEnabled, URL bootServer, File dbFile, int  maxFailedLoginAttempts, int blockingTimeIncreaseMinutes, File keyStoreFile, String keyStorePassword) throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException, KeyManagementException{
		 //initialize the HTTPS server
		 httpsServer = HttpsServer.create(new InetSocketAddress(bootServer.getPort()), 0);
		 SSLContext sslContext = SSLContext.getInstance ( "TLS" );
		 
		 //initialize the keystore
		 char[] password = keyStorePassword.toCharArray ();
		 KeyStore ks = KeyStore.getInstance ( "JKS" );
		 FileInputStream fis = new FileInputStream ( keyStoreFile );
		 ks.load ( fis, password );
		 
		 // setup the key manager factory
		 KeyManagerFactory kmf = KeyManagerFactory.getInstance ( "SunX509" );
		 kmf.init ( ks, password );

		 // setup the trust manager factory
		 TrustManagerFactory tmf = TrustManagerFactory.getInstance ( "SunX509" );
		 tmf.init ( ks );
		 
		// setup the HTTPS context and parameters
		sslContext.init ( kmf.getKeyManagers (), tmf.getTrustManagers (), null );
		httpsServer.setHttpsConfigurator ( new HttpsConfigurator( sslContext )
			{
		        public void configure ( HttpsParameters params )
		        {
		            try
		            {
		                // initialise the SSL context
		                SSLContext c = SSLContext.getDefault ();
		                SSLEngine engine = c.createSSLEngine ();
		                params.setNeedClientAuth ( false );
		                params.setCipherSuites ( engine.getEnabledCipherSuites () );
		                params.setProtocols ( engine.getEnabledProtocols () );

		                // get the default parameters
		                SSLParameters defaultSSLParameters = c.getDefaultSSLParameters ();
		                params.setSSLParameters ( defaultSSLParameters );
		            }
		            catch ( Exception ex )
		            {
		                ex.printStackTrace();
		            }
		        }
		 } );
		 
		 //initialize the handlers
		 httpsServer.createContext("/boot", new BootScriptHandler(addingEnabled, bootServer, dbFile));
		 httpsServer.createContext("/add", new AddScriptHandler(addingEnabled, bootServer, dbFile));
		 httpsServer.createContext("/auth", new AuthScriptHandler(bootServer, dbFile, maxFailedLoginAttempts, blockingTimeIncreaseMinutes ));
		 httpsServer.createContext("/", new DefaultScriptHandler());
		 httpsServer.setExecutor(null); // creates a default executor
	}
	
	public void start(){
		if (httpsServer != null) httpsServer.start();
		if (server != null) server.start();
	}
	
	public void shutdown(){
		if (httpsServer != null) httpsServer.stop(0);
		if (server != null) server.stop(0);
	}
}
