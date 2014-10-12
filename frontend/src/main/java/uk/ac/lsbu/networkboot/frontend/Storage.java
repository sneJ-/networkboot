package uk.ac.lsbu.networkboot.frontend;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class Storage {
	private int id;
	@NotNull
	private String name;
	@NotNull
	private String type;
	@NotNull
	private String baseURL;
	@NotNull @Pattern(regexp = "[A-Za-z0-9_\\-]*")
	private String directory;
	
	//Constructor
	public Storage(){
		id = 0;
		name = "New storage...";
	}
	
	public Storage(int id, String name, String type, String baseURL, String directory){
		this.id = id;
		this.name = name;
		this.type = type;
		this.baseURL = baseURL;
		this.directory = directory;
	}

	//Getter and Setter
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getBaseURL() {
		return baseURL;
	}

	public void setBaseURL(String baseURL) {
		this.baseURL = baseURL;
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}
	
	//Functions
	
	
}
