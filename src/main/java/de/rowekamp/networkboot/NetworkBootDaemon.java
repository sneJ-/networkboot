package de.rowekamp.networkboot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.SQLException;
import java.util.Properties;

import org.jvnet.hudson.proxy_dhcp.ProxyDhcpService;
import org.openthinclient.tftp.tftpd.TFTPServer;
import org.quartz.SchedulerException;

import de.rowekamp.networkboot.database.InitDatabase;
import de.rowekamp.networkboot.frontendServletContainer.WebServerFrontend;
import de.rowekamp.networkboot.ipxeBootScript.IPXEBootScriptService;
import de.rowekamp.networkboot.rmi.RMIServer;
import de.rowekamp.networkboot.wakeOnLan.WakeOnLANScheduler;
import sun.security.tools.keytool.CertAndKeyGen;
import sun.security.x509.X500Name;

public class NetworkBootDaemon {
	
	private TFTPServer TFTPDaemon;
	private ProxyDhcpService DHCPProxy;
	private IPXEBootScriptService IPXEScriptingDaemon;
	private WakeOnLANScheduler wolSched;
	private RMIServer rmiServer;
	private WebServerFrontend frontend;
	
	private String tftpDirectory;
	private String ip;
	private URL bootServer;
	private String ipxeFile;
	private String ipxeBootUrl;
	private boolean backendHttps;
	private int backendPort;
	private boolean addingEnabled;
	private File dbFile;
	private int maxFailedLoginAttempts;
	private int blockingTimeIncreaseMinutes;
	private File backendKeyStore;
	private String backendKeyStorePassword;
	private int RMI_PORT;
	private String RMI_ID;
	private boolean frontendHttps;
	private int frontendPort;
	private File warFile;
	private File frontendKeyStore;
	private String frontendKeyStorePassword;
	private Properties readProperties;
	
	private final String configurationFile;
	private final String bootUrlAppendix = "/boot?mac=${net0/mac}";
	
	public NetworkBootDaemon(String configurationFile) throws IOException, UnrecoverableKeyException, KeyManagementException, KeyStoreException, NoSuchAlgorithmException, CertificateException, SchedulerException, SQLException, InvalidKeyException, NoSuchProviderException, SignatureException{
		this.configurationFile = configurationFile;
		
		//Write initial files / configurations
		if(!new File(configurationFile).exists()){
			createConfigurationFile(configurationFile); //determine IP
		}
		readConfigurationFile(configurationFile); //set local variables
		if(!dbFile.exists()){
			createDatabase();
		}else{
			updateBootscriptServerVariablesInDb(); //BOOTSCRIPT_SERVER_PROTOCOL, BOOTSCRIPT_SERVER_IP and BOOTSCRIPT_SERVER_PORT
		}
		if(!frontendKeyStore.exists()){
			createFrontendKeyStore(); //incl. update in configuration file (password)
		}
		if(!backendKeyStore.exists()){
			createBackendKeyStore(); //incl. update in configuration file (password)
		}
		
		//Initialize the services
		TFTPDaemon = new TFTPServer(tftpDirectory, TFTPServer.DEFAULT_TFTP_PORT);
		DHCPProxy = new ProxyDhcpService((Inet4Address) InetAddress.getByName(bootServer.getHost()),ipxeFile,ipxeBootUrl);
		if (backendHttps)IPXEScriptingDaemon = new IPXEBootScriptService(addingEnabled, bootServer, dbFile, maxFailedLoginAttempts, blockingTimeIncreaseMinutes, backendKeyStore, backendKeyStorePassword);
		else IPXEScriptingDaemon = new IPXEBootScriptService(addingEnabled, bootServer, dbFile, maxFailedLoginAttempts, blockingTimeIncreaseMinutes);
		wolSched = new WakeOnLANScheduler();
		wolSched.initializeFromDb(dbFile);
		rmiServer = new RMIServer(RMI_PORT, RMI_ID, dbFile, maxFailedLoginAttempts, blockingTimeIncreaseMinutes, wolSched);
		if (frontendHttps) frontend = new WebServerFrontend(dbFile, frontendPort, warFile, RMI_PORT, RMI_ID, frontendKeyStore, frontendKeyStorePassword);
		else frontend = new WebServerFrontend(dbFile, frontendPort, warFile, RMI_PORT, RMI_ID); 
	}


	public void start() throws Exception{
		//If the kill signal comes, shut down the daemon gracefully
        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
            	try {
            		frontend.shutdown();
        			TFTPDaemon.shutdown();
        			DHCPProxy.shutdown();
        			IPXEScriptingDaemon.shutdown();
        			rmiServer.shutdown();
        			wolSched.shutdown(false);
				} catch (Exception e) {
					e.printStackTrace();
				}
            }
        });
        
        //Start the neworkboot services
        TFTPDaemon.start();
        DHCPProxy.start();
        IPXEScriptingDaemon.start();
		wolSched.start();
		rmiServer.start();
		frontend.start();
	}
	
	private void createConfigurationFile(String configurationFile) throws IOException {
		Properties properties = new Properties();
		properties.setProperty("tftpDirectory","./TFTP");
		properties.setProperty("ip",Inet4Address.getLocalHost().getHostAddress());
		properties.setProperty("ipxeFile","/ipxe.kpxe");
		properties.setProperty("backendHttps","false");
		properties.setProperty("backendPort", "8080");
		properties.setProperty("addingEnabled","true");
		properties.setProperty("dbFile","db.sqlite");
		properties.setProperty("maxFailedLoginAttempts","3");
		properties.setProperty("blockingTimeIncreaseMinutes","5");
		properties.setProperty("backendKeyStore","backend.keystore");
		properties.setProperty("backendKeyStorePassword","");
		properties.setProperty("RMI_PORT","1099");
		properties.setProperty("RMI_ID","networkboot");
		properties.setProperty("frontendHttps","false");
		properties.setProperty("frontendPort","80");
		properties.setProperty("warFile","frontend.war");
		properties.setProperty("frontendKeyStore","frontend.keystore");
		properties.setProperty("frontendKeyStorePassword","");
		File configFile = new File(configurationFile);
		FileOutputStream fileOut = new FileOutputStream(configFile);
		properties.store(fileOut, configFile.getAbsolutePath());
		fileOut.close();
	}
	
	private void readConfigurationFile(String configurationFile) throws IOException {
		ConfigFile conf = new ConfigFile(configurationFile);
		if(conf.checkProperty("tftpDirectory")) tftpDirectory = conf.getProperty("tftpDirectory");
		if(conf.checkProperty("ip")) ip = conf.getProperty("ip");
		if(conf.checkProperty("ipxeFile")) ipxeFile = conf.getProperty("ipxeFile");
		if(conf.checkProperty("backendHttps")) backendHttps = conf.getPropertyBool("backendHttps");
		if(conf.checkProperty("backendPort")) backendPort = conf.getPropertyInt("backendPort");
		if(conf.checkProperty("addingEnabled")) addingEnabled = conf.getPropertyBool("addingEnabled");
		if(conf.checkProperty("dbFile")) dbFile = new File (conf.getProperty("dbFile"));
		if(conf.checkProperty("maxFailedLoginAttempts")) maxFailedLoginAttempts = conf.getPropertyInt("maxFailedLoginAttempts");
		if(conf.checkProperty("blockingTimeIncreaseMinutes")) blockingTimeIncreaseMinutes = conf.getPropertyInt("blockingTimeIncreaseMinutes");
		if(conf.checkProperty("backendKeyStore")) backendKeyStore = new File(conf.getProperty("backendKeyStore"));
		if(conf.checkProperty("backendKeyStorePassword")) backendKeyStorePassword = conf.getProperty("backendKeyStorePassword");
		if(conf.checkProperty("RMI_PORT")) RMI_PORT = conf.getPropertyInt("RMI_PORT");
		if(conf.checkProperty("RMI_ID")) RMI_ID = conf.getProperty("RMI_ID");
		if(conf.checkProperty("frontendHttps")) frontendHttps = conf.getPropertyBool("frontendHttps");
		if(conf.checkProperty("frontendPort")) frontendPort = conf.getPropertyInt("frontendPort");
		if(conf.checkProperty("warFile")) warFile = new File (conf.getProperty("warFile"));
		if(conf.checkProperty("frontendKeyStore")) frontendKeyStore = new File(conf.getProperty("frontendKeyStore"));
		if(conf.checkProperty("frontendKeyStorePassword")) frontendKeyStorePassword = conf.getProperty("frontendKeyStorePassword");
		if (backendHttps) bootServer = new URL("https://"+ip+":"+backendPort);
		else bootServer = new URL("http://"+ip+":"+backendPort);
		ipxeBootUrl = bootServer.toExternalForm()+bootUrlAppendix;
		readProperties = conf.getProperties();
	}
	
	private void createDatabase() {
		InitDatabase db = new InitDatabase(dbFile);
		try {
			db.initializeTables();
			db.insertSystemVariables(backendHttps,ip,backendPort);
			db.close();
		} catch (SQLException e) {
			dbFile.delete();
			e.printStackTrace();
		}
	}

	private void updateBootscriptServerVariablesInDb() throws SQLException {
		InitDatabase db = new InitDatabase(dbFile);
		db.updateSystemVariables(backendHttps,ip,backendPort);
		db.close();
	}

	private void createFrontendKeyStore() throws IOException, KeyStoreException, InvalidKeyException, NoSuchAlgorithmException, CertificateException, NoSuchProviderException, SignatureException {
		frontendKeyStorePassword = createKeyStore(frontendKeyStore);
		frontendPort = 443;
		frontendHttps = true;
		readProperties.setProperty("frontendKeyStorePassword", frontendKeyStorePassword);
		readProperties.setProperty("frontendPort", "443");
		readProperties.setProperty("frontendHttps", "true");
		File configFile = new File(configurationFile);
		FileOutputStream fileOut = new FileOutputStream(configFile, false);
		readProperties.store(fileOut, configFile.getAbsolutePath());
		fileOut.close();
	}

	private void createBackendKeyStore() throws IOException, KeyStoreException, InvalidKeyException, NoSuchAlgorithmException, CertificateException, NoSuchProviderException, SignatureException {
		backendKeyStorePassword = createKeyStore(backendKeyStore);
		readProperties.setProperty("backendKeyStorePassword", backendKeyStorePassword);
		File configFile = new File(configurationFile);
		FileOutputStream fileOut = new FileOutputStream(configFile, false);
		readProperties.store(fileOut, configFile.getAbsolutePath());
		fileOut.close();
	}
	
	private String createKeyStore(File keyStoreFile) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, NoSuchProviderException, InvalidKeyException, SignatureException {
		//generate random keyStorePassword
		SecureRandom random = new SecureRandom();
		String password = new BigInteger(130, random).toString(32);
		System.out.println("password: " + password);
		
		//generate keyStore
		KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		keyStore.load(null, password.toCharArray());
		
		//generate root certificate
		CertAndKeyGen keyGen = new CertAndKeyGen("RSA","SHA256WithRSA",null);
		keyGen.generate(2048);
		PrivateKey rootPrivateKey = keyGen.getPrivateKey();
		X509Certificate rootCertificate = keyGen.getSelfCertificate(new  X500Name( "CN=networkboot.me" ), ( long )  3 * 365   *  24   *  60   *  60 );
		X509Certificate[] chain = new X509Certificate[1];
		chain[0] = rootCertificate;
		
		//add certificates to keystore
		keyStore.setKeyEntry("networkboot.me", rootPrivateKey, password.toCharArray(), chain);
		
		//write keystore in file to disk
		FileOutputStream output = new FileOutputStream(keyStoreFile);
		keyStore.store(output, password.toCharArray());
		output.close();
		
		return password;
	}
}