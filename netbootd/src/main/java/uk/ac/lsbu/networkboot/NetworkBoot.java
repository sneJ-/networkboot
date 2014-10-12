package uk.ac.lsbu.networkboot;

import java.io.File;
import java.io.IOException;

import uk.ac.lsbu.networkboot.backend.WebServerBackend;
import uk.ac.lsbu.networkboot.frontend.WebServerFrontend;

import org.openthinclient.tftp.tftpd.TFTPServer;

import uk.ac.lsbu.networkboot.backend.dhcpd.Dhcpd;

/**
 * Starts and stops the network boot daemon - netbootd.
 * 
 *@author Jens Röwekamp 
 *		  Last modified: 15.04.2014
 */
public class NetworkBoot {

	public static final String backendVersion = "0.1.0";
	public static final String backendBuildDate = "22.04.2014";
	public static final String iPXEVersion = "1.0.0+";
	public static final String iPXEBuildDate = "03.04.2014";
	private WebServerBackend backendServer = null;
	private WebServerFrontend frontendServer = null;
	private TFTPServer TFTPDaemon = null;
	private Dhcpd DHCPDaemon = null;
	private String configFile = "./../config/main.cfg";
	private boolean tftpd = false;
	private boolean dhcpd = false;
	private String tftpDir = "./../storage/TFTP";
	private String bootFile = "/ipxe.kpxe";
	private String dhcpIp = "192.168.42.1";
	private String dnsIp = "8.8.8.8";
	private String gatewayIp = "192.168.42.1";
	private String initialIp = "192.168.42.100";
	private String finalIp = "192.168.42.200";
	private int leaseTime = 3600;
	private String nextServerIp = "192.168.42.1";
	private int renewTime = 3600;
	private String subnetMask = "255.255.255.0";
	
	/**
	 * Sets the configuration file.
	 * @param file
	 */
	public void setConfigFile(String file){
		this.configFile = file;
	}
	
	/**
	 * Starts the netbootd daemons
	 */
	public void start(){
		//If the kill signal comes, shut down the daemon gracefully
        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
            	try {
					backendServer.stop();
					if (tftpd){
						TFTPDaemon.shutdown();
						System.out.println("netbootd: TFTP Daemon stopped");
					}
					if (dhcpd){
						DHCPDaemon.stop();
						System.out.println("netbootd: DHCP Daemon stopped");
					}
					frontendServer.stop();
				} catch (Exception e) {
					System.err.println("netbootd - Error: Wasn't able to shut the process properly.");
					e.printStackTrace();
				}
            }
        });
		
		//checking the config file
		File file = new File(configFile);
		if (!file.exists()){
			System.err.println("netbootd - Error: Configuration file " + configFile + " does not exist");
			return;
		}
		if (!file.canRead()){
			System.err.println("netbootd - Error: Configuration file " + configFile +" is not readable");
			return;
		}
		ConfigFile conf;
		try {
			conf = new ConfigFile(configFile);
		} catch (IOException e) {
			System.err.println("netbootd - Error: Config file " + configFile + " couldn't be loaded");
			e.printStackTrace();
			return;
		}
		
		//starting the backend Server
		backendServer = new WebServerBackend();
		backendServer.loadConfigurationFile(configFile);
		if (backendServer.checkDatabaseFile())
			try {
				backendServer.start();
			} catch (Exception e) {
				System.err.println("netbootd - Error: Backend server couldn't be started.");
				e.printStackTrace();
				return;
			}
		
		//starting the TFTP Server if enabled in the config file
		if (conf.checkProperty("tftpd")) tftpd = conf.getPropertyBool("tftpd");
		if (conf.checkProperty("tftpDir")) tftpDir = conf.getProperty("tftpDir");
		if (conf.checkProperty("boot-file")) bootFile = conf.getProperty("boot-file");
		if (tftpd){
			try {
				TFTPDaemon = new TFTPServer(tftpDir, TFTPServer.DEFAULT_TFTP_PORT, bootFile);
			} catch (IOException e) {
				System.err.println("TFTPServer couldn't be initialized");
				e.printStackTrace();
			}
			TFTPDaemon.start();
			System.out.println("netbootd: TFTP Daemon started on Port 69");
		}
		//starting the DHCP Daemon if enabled in the config file
		if (conf.checkProperty("dhcpd")) dhcpd = conf.getPropertyBool("dhcpd");
		if (conf.checkProperty("dhcpIp")) dhcpIp = conf.getProperty("dhcpIp");
		if (conf.checkProperty("dnsIp")) dnsIp = conf.getProperty("dnsIp");
		if (conf.checkProperty("gatewayIp")) gatewayIp = conf.getProperty("gatewayIp");
		if (conf.checkProperty("initialIp")) initialIp = conf.getProperty("initialIp");
		if (conf.checkProperty("finalIp")) finalIp = conf.getProperty("finalIp");
		if (conf.checkProperty("leaseTime")) leaseTime = conf.getPropertyInt("leaseTime");
		if (conf.checkProperty("nextServerIp")) nextServerIp = conf.getProperty("nextServerIp");
		if (conf.checkProperty("renewTime")) renewTime = conf.getPropertyInt("renewTime");
		if (conf.checkProperty("subnetMask")) subnetMask = conf.getProperty("subnetMask");
		if (dhcpd){
			DHCPDaemon = new Dhcpd();
			DHCPDaemon.setBootFile(bootFile);
			DHCPDaemon.setDhcpIp(dhcpIp);
			DHCPDaemon.setDnsIp(dnsIp);
			DHCPDaemon.setGatewayIp(gatewayIp);
			DHCPDaemon.setIpInicial(initialIp);
			DHCPDaemon.setIpFinal(finalIp);
			DHCPDaemon.setLeaseTime(leaseTime);
			DHCPDaemon.setNextServer(nextServerIp);
			DHCPDaemon.setRenewTime(renewTime);
			DHCPDaemon.setSubnetMask(subnetMask);
			DHCPDaemon.updateRange();
			DHCPDaemon.start();
			System.out.println("netbootd: DHCP Daemon started on UDP Port 67");
		}
		
		//Starting the frontend Server
		frontendServer = new WebServerFrontend();
		frontendServer.loadConfigurationFile(configFile);
		try {
			frontendServer.start();
		} catch (Exception e) {
			System.err.println("netbootd - Error: Frontend couldn't be started.");
			e.printStackTrace();
		}
	}
	
	
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		NetworkBoot netbootd = new NetworkBoot();
		//setting the configuration variable if argument given
		if (args.length != 0){
			String configFile = args[0];
			netbootd.setConfigFile(configFile);
		}
		netbootd.start();
	}
}
