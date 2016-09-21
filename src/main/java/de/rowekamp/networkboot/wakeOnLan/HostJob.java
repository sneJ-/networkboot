package de.rowekamp.networkboot.wakeOnLan;

public class HostJob{
	private int id;
	private String cronSchedule;
	private String mac;
	
	public HostJob(int id, String cronSchedule, String mac){
		this.id = id;
		this.cronSchedule = cronSchedule;
		this.mac = mac;
	}
	
	public int getId(){
		return id;
	}
	
	public String getCronSchedule(){
		return cronSchedule;
	}
	
	public String getMac(){
		return mac;
	}
}