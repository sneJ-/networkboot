package de.rowekamp.networkboot;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Reads a configuration file.
 * 
 */

public class ConfigFile {
	private Properties configFile;

	public ConfigFile(String locator) throws IOException {
		configFile = new java.util.Properties();
		final InputStream cfg = new FileInputStream(locator);
		configFile.load(cfg);
		cfg.close();
	}

	/**
	 * Checks if a property is set or not.
	 * @param Requires the String name of the property defined in the config file.
	 * @return Returns true if the property is set and false if isn't.
	 */
	public boolean checkProperty(String key){
		boolean returns = false;
		if (this.configFile.getProperty(key) != null) returns = true;
		return returns;
	}
	
	
	/**
	 * Gets the requested property value from the config file as String.
	 * @param key
	 * @return
	 */
	public String getProperty(String key) {
		String value = this.configFile.getProperty(key);
		return value;
	}

	/**
	 * Gets the requested property value from the config file as int.
	 * @param key
	 * @return
	 */
	public int getPropertyInt(String key) {
		String value = this.configFile.getProperty(key);
		return Integer.parseInt(value);
	}

	/**
	 * Gets the requested property value from the config file as boolean.
	 * @param key
	 * @return
	 */
	public boolean getPropertyBool(String key) {
		boolean returns = false;
		String value = this.configFile.getProperty(key);
		if (value.equals("true") || value.equals("TRUE"))
			returns = true;
		return returns;
	}
	
	/**
	 * Gets the whole properties.
	 * @return
	 */
	public Properties getProperties(){
		return configFile;
	}
}