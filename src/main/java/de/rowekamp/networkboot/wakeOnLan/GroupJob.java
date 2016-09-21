package de.rowekamp.networkboot.wakeOnLan;

import java.util.ArrayList;

public class GroupJob{
	private int id;
	private String cronSchedule;
	private ArrayList<String> macArray;
	
	public GroupJob(int id, String cronSchedule, ArrayList<String> macArray){
		this.id = id;
		this.cronSchedule = cronSchedule;
		this.macArray = macArray;
	}
	
	public int getId(){
		return id;
	}
	
	public String getCronSchedule(){
		return cronSchedule;
	}
	
	public ArrayList<String> getMacArray(){
		return macArray;
	}
}