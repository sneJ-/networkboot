package uk.ac.lsbu.networkboot.frontend;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class ImageMapping {
	private int id;
	@NotNull
	private int hostId;
	@NotNull
	private Image image;
	@NotNull
	private boolean grouped;
	@NotNull
	private boolean timed;
	private String bootParameter;
	@NotNull
	@Min (value = 0)
	private int priority;
	private List<TimeConstraint> times = null;
	
	//Getter and Setter
//	public int getHostId() {
//		return hostId;
//	}
//	public void setHostId(int hostId) {
//		this.hostId = hostId;
//	}
	public boolean isGrouped() {
		return grouped;
	}
//	public void setGrouped(boolean grouped) {
//		this.grouped = grouped;
//	}
	public boolean isTimed() {
		return timed;
	}
	public void setTimed(boolean timed) {
		this.timed = timed;
	}
	public String getBootParameter() {
		return bootParameter;
	}
	public void setBootParameter(String bootParameter) {
		this.bootParameter = bootParameter;
	}
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}
	public int getId() {
		return id;
	}
	public void setImage(Image a){
		image = a;
	}
	public Image getImage(){
		return image;
	}
	
	public List<TimeConstraint> getTimes() {
		return times;
	}
//	public void setTimes(List<TimeConstraint> times) {
//		this.times = times;
//	}
	
	//Constructor
	public ImageMapping(int id, int hostId, boolean grouped, Image image,
			boolean timed, String bootParameter, int priority, List<TimeConstraint> times) {
		this.id = id;
		this.hostId = hostId;
		this.image = image;
		this.grouped = grouped;
		this.timed = timed;
		this.bootParameter = bootParameter;
		this.priority = priority;
		this.times = times;
	}
	
	public ImageMapping(boolean grouped){
		id = 0;
		this.hostId = 0;
		image = new Image();
		this.grouped = grouped;
		timed = false;
		bootParameter = "";
		priority = 0;
		times = new ArrayList<TimeConstraint>();
	}
	
}
