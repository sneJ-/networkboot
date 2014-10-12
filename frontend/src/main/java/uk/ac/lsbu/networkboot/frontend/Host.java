package uk.ac.lsbu.networkboot.frontend;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class Host {

	// Variables of the host object
	private int id;
	@NotNull
	private String name;
	@NotNull
	@Pattern(regexp = "[a-fA-F0-9]{12}")
	private String mac;
	@NotNull
	private boolean validated;

	//Constructor
	public Host(){
		this.name = "New host...";
		this.validated = false;
	}
	
	public Host(int id, String name, String mac, boolean validated){
		this.name = name;
		this.id = id;
		this.mac = mac;
		this.validated = validated;
	}
	
	// Getters and Setters
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public boolean isValidated() {
		return validated;
	}

	public void setValidated(boolean validated) {
		this.validated = validated;
	}

	public int getId() {
		return id;
	}
	
//	public void setId(int id){
//		this.id = id;
//	}
}