package uk.ac.lsbu.networkboot.frontend;

import javax.validation.constraints.NotNull;

public class Group {
	
	private int id;
	@NotNull
	private String name;
	private String description;
	
	/**
	 * Constructor
	 */
	public Group(int id, String name, String description){
		this.id = id;
		this.name = name;
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public int getId() {
		return id;
	}
	
	public String getDescription(){
		return description;
	}
	
	public void setName(String n){
		this.name = n;
	}
	
//	public void setId(int id){
//		this.id = id;
//	}
	
	public void setDescription(String d){
		this.description = d;
	}
}
