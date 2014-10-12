package uk.ac.lsbu.networkboot.frontend;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;

@ManagedBean
public class PreferencesBean {
	public class Teammember {
		private String name;
		private String function;
		private String mail;

		public Teammember(String name, String function, String mail) {
			this.name = name;
			this.function = function;
			this.mail = mail;
		}

		public String getName() {
			return name;
		}

		public String getfunction() {
			return function;
		}

		public String getMail() {
			return mail;
		}
	}

	private final String frontendVersion = "0.1.0";
	private final String frontendBuildDate = "12.10.2014";
	private String configFile = "";
	private String databaseFile = "";
	private String authenticationFile = "";
	private String tempStorage = "";
	private String freeTempMemory = "";
	private String storageDir = "";
	private boolean HTTPs = false;
	private String certificateStorage = "";
	private boolean addingEnabled = true;
	private String backendDaemon = "";
	private String DHCPDaemon = "false";
	private String TFTPDaemon = "false";
	private String TFTPDir = "";
	private String HTTPImageDaemon = "false";
	private String HTTPImageDir = "";
	private String backendVersion = "";
	private String backendBuildDate = "";
	private String iPXEVersion = "";
	private String iPXEBuildDate = "";
	private String backendHost = "localhost";
	private List<Teammember> team = new ArrayList<Teammember>();
	private int backendPort = 55555;
	private int maxUploadSize = 600;

	@PostConstruct
	public void init() {
		// Fill the team
		team.add(new Teammember("Jens Röwekamp ", "Programmer",
				"mailto:snej@networkboot.me"));

		// Get data from the backend
		FacesContext ctx = FacesContext.getCurrentInstance();
		if (ctx.getExternalContext().getInitParameter("configFile") != null) {
			configFile = ctx.getExternalContext()
					.getInitParameter("configFile");
		}
		if (ctx.getExternalContext().getInitParameter("databaseFile") != null) {
			databaseFile = ctx.getExternalContext().getInitParameter(
					"databaseFile");
		}
		if (ctx.getExternalContext().getInitParameter("tempDirectory") != null) {
			tempStorage = ctx.getExternalContext().getInitParameter(
					"tempDirectory");
		}
		if (ctx.getExternalContext().getInitParameter("storageDirectory") != null) {
			storageDir = ctx.getExternalContext().getInitParameter(
					"storageDirectory");
		}
		if (ctx.getExternalContext().getInitParameter("backendVersion") != null) {
			backendVersion = ctx.getExternalContext().getInitParameter(
					"backendVersion");
		}
		if (ctx.getExternalContext().getInitParameter("backendBuildDate") != null) {
			backendBuildDate = ctx.getExternalContext().getInitParameter(
					"backendBuildDate");
		}
		if (ctx.getExternalContext().getInitParameter("iPXEVersion") != null) {
			iPXEVersion = ctx.getExternalContext().getInitParameter(
					"iPXEVersion");
		}
		if (ctx.getExternalContext().getInitParameter("iPXEBuildDate") != null) {
			iPXEBuildDate = ctx.getExternalContext().getInitParameter(
					"iPXEBuildDate");
		}
		if (ctx.getExternalContext().getInitParameter("maxUploadFileSize") != null) {
			maxUploadSize = Integer.parseInt(ctx.getExternalContext()
					.getInitParameter("maxUploadFileSize"));
		}

		// Get data from the configuration file
		if (!configFile.equals("")) {
			ConfigFile conf;
			try {
				conf = new ConfigFile(configFile);
				if (conf.checkProperty("passwdFile"))
					authenticationFile = conf.getProperty("passwdFile");
				if (conf.checkProperty("sslEnable"))
					HTTPs = conf.getPropertyBool("sslEnable");
				if (conf.checkProperty("keyStoreFile"))
					certificateStorage = conf.getProperty("keyStoreFile");
				if (conf.checkProperty("addingEnabled"))
					addingEnabled = conf.getPropertyBool("addingEnabled");
				if (conf.checkProperty("tftpDir"))
					TFTPDir = conf.getProperty("tftpDir");
				if (conf.checkProperty("imageDir"))
					HTTPImageDir = conf.getProperty("imageDir");
				if (conf.checkProperty("tftpd"))
					TFTPDaemon = conf.getProperty("tftpd");
				if (conf.checkProperty("dhcpd"))
					DHCPDaemon = conf.getProperty("dhcpd");
				if (conf.checkProperty("backendPort"))
					backendPort = conf.getPropertyInt("backendPort");
				if (conf.checkProperty("imageServer"))
					HTTPImageDaemon = conf.getProperty("imageServer");
				if (conf.checkProperty("backendHost"))
					backendHost = conf.getProperty("backendHost");

			} catch (Exception e) {
			}
		}

		// Retrieve other data
		freeTempMemory = fetchFreeMemory(tempStorage);
		backendDaemon = checkBackendStatus(backendPort);
		if (DHCPDaemon.equals("true")) {
			DHCPDaemon = "enabled";
		} else {
			DHCPDaemon = "disabled";
		}
		if (TFTPDaemon.equals("true")) {
			TFTPDaemon = "enabled";
		} else {
			TFTPDaemon = "disabled";
		}
		if (HTTPImageDaemon.equals("true")) {
			HTTPImageDaemon = checkHTTPImageDaemon(backendPort);
		} else {
			HTTPImageDaemon = "disabled";
		}
	}

	// GETTER
	public String getConfigFile() {
		return configFile;
	}

	public String getDatabaseFile() {
		return databaseFile;
	}

	public String getAuthenticationFile() {
		return authenticationFile;
	}

	public String getTempStorage() {
		return tempStorage;
	}

	public String getFreeTempMemory() {
		return freeTempMemory;
	}

	public String getStorageDir() {
		return storageDir;
	}

	public boolean isHTTPs() {
		return HTTPs;
	}

	public String getCertificateStorage() {
		return certificateStorage;
	}

	public boolean isAddingEnabled() {
		return addingEnabled;
	}

	public String getBackendDaemon() {
		return backendDaemon;
	}

	public String getDHCPDaemon() {
		return DHCPDaemon;
	}

	public String getTFTPDaemon() {
		return TFTPDaemon;
	}

	public String getTFTPDir() {
		return TFTPDir;
	}

	public String getHTTPImageDaemon() {
		return HTTPImageDaemon;
	}

	public String getHTTPImageDir() {
		return HTTPImageDir;
	}

	public String getFrontendVersion() {
		return frontendVersion;
	}

	public String getFrontendBuildDate() {
		return frontendBuildDate;
	}

	public String getBackendVersion() {
		return backendVersion;
	}

	public String getBackendBuildDate() {
		return backendBuildDate;
	}

	public String getiPXEVersion() {
		return iPXEVersion;
	}

	public String getiPXEBuildDate() {
		return iPXEBuildDate;
	}

	public List<Teammember> getTeam() {
		return team;
	}

	public int getMaxUploadSize() {
		return maxUploadSize;
	}

	// PUBLIC FUNCTIONS
	/**
	 * Clears the image files stored in the temporary directory. Forwards to
	 * preferences.xhtml
	 * 
	 * @return forward page
	 */
	public String clearTemp() {
		File file = new File(tempStorage);
		String files[] = file.list();
		for (String temp : files) {
			File fileDelete = new File(file, temp);
			if (fileDelete.isFile()
					&& fileDelete.getName().startsWith("netbootd-")) {
				if (!fileDelete.delete()){
					System.err.println("Error: File + " + fileDelete.getAbsolutePath() + " couldn't be deleted.");
				}
			}
		}
		return null;
	}

	// PRIVATE FUNCTIONS
	/**
	 * Calculates the free memory of the directory
	 * 
	 * @return the free temp memory in GB
	 */
	private String fetchFreeMemory(String dir) {
		float freeSpace = new File(dir).getUsableSpace() / (float) 1024 / (float) 1024 / (float) 1024;
		BigDecimal bd = new BigDecimal(Float.toString(freeSpace));
		bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
		return bd.floatValue() + " GiB";
	}

	/**
	 * Checks if the HTTPImageDaemon is running.
	 * 
	 * @param backendPort
	 * @return running or error
	 */
	private String checkHTTPImageDaemon(int backendPort) {
		String out = "error";
		try {
			URL url = new URL("http://" + backendHost + ":" + backendPort + "/images/");
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setRequestMethod("GET");
			int responseCode = connection.getResponseCode();
			connection.disconnect();
			if (responseCode == 403) {
				out = "running";
			}
		} catch (Exception e) {
		}
		return out;
	}

	/**
	 * Checks if the BackendDaemon is running
	 * 
	 * @param backendPort
	 * @return running or error
	 */
	private String checkBackendStatus(int backendPort) {
		String out = "error";
		try {
			URL url = new URL("http://" + backendHost + ":" + backendPort
					+ "/test.php?mac=abcdefabcdef");
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setRequestMethod("GET");
			int responseCode = connection.getResponseCode();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			String firstLine = in.readLine();
			in.close();
			connection.disconnect();
			if (responseCode == 200 && firstLine.equals("#!ipxe")) {
				out = "running";
			}
		} catch (Exception e) {
		}
		return out;
	}
}
