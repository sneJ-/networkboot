package uk.ac.lsbu.networkboot.frontend;

import java.text.DecimalFormat;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class TimeConstraint {
	@NotNull @Min(value = 0) @Max(value = 23)
	private Integer beginHour;
	@NotNull @Min(value = 0) @Max(value = 59)
	private Integer beginMinute;
	@NotNull @Min(value = 0) @Max(value = 24)
	private Integer endHour;
	@NotNull @Min(value = 0) @Max(value = 59)
	private Integer endMinute;
	@Min(value = 1) @Max(value = 31)
	private Integer dom;
	@Min(value = 1)	@Max(value = 12)
	private Integer month;
	@Min(value = 0)	@Max(value = 6)
	private Integer dow;
	private int id;

	// Constructor
	public TimeConstraint(Integer beginMin, Integer beginHour, Integer endMin, Integer endHour, Integer dom, Integer month,
			Integer dow, int id) {
		this.beginMinute = beginMin;
		this.beginHour = beginHour;
		this.endMinute = endMin;
		this.endHour = endHour;
		this.dom = dom;
		this.month = month;
		this.dow = dow;
		this.id = id;
	}
	
	public TimeConstraint(){
		this.month = null;
		this.dow = null;
		this.dom = null;
	}

	// Getter
	public Integer getBeginHour() {
		return beginHour;
	}

	public Integer getBeginMinute() {
		return beginMinute;
	}

	public Integer getEndHour(){
		return endHour;
	}
	
	public Integer getEndMinute(){
		return endMinute;
	}

	public Integer getDom() {
		return dom;
	}

	public Integer getMonth() {
		return month;
	}

	public Integer getDow() {
		return dow;
	}

	public int getId() {
		return id;
	}

	// Setter
	public void setBeginHour(Integer h) {
		beginHour = h;
	}

	public void setBeginMinute(Integer m) {
		beginMinute = m;
	}

	public void setEndHour(Integer h){
		endHour = h;
	}
	
	public void setEndMinute(Integer m){
		endMinute = m;
	}

	public void setDom(Integer dom) {
		this.dom = dom;
	}

	public void setMonth(Integer mo) {
		month = mo;
	}

	public void setDow(Integer dow) {
		this.dow = dow;
	}

	/**
	 * Generates the output to display the days in the image table.
	 * 
	 * @return human readable time constraint
	 */
	public String getTableOutputDays() {
		StringBuilder output = new StringBuilder();
		// If a day of week is chosen add him to the string.
		if (dow != null) {
			switch (dow) {
			case 0:
				output.append("Sundays ");
				break;
			case 1:
				output.append("Mondays ");
				break;
			case 2:
				output.append("Tuesdays ");
				break;
			case 3:
				output.append("Wednesdays ");
				break;
			case 4:
				output.append("Thursdays ");
				break;
			case 5:
				output.append("Fridays ");
				break;
			case 6:
				output.append("Saturdays ");
				break;
			default:
				break;
			}
		}
		// If whether the day of month or month is chosen add them to the
		// string.
		DecimalFormat format = new DecimalFormat("00");
		if (dom != null || month != null) {
			if (dom == null) {
				output.append("xx.");
			} else {
				output.append(format.format(dom) + ".");
			}
			if (month == null) {
				output.append("xx ");
			} else {
				output.append(format.format(month) + " ");
			}
		}
		// If no date has been specified return daily
		if (output.toString().equals("")){
			output.append("daily");
		}
		return output.toString();
	}

	/**
	 * Generates the output to display the minutes in the image table.
	 * 
	 * @return human readable time constraint
	 */
	public String getTableOutputTime() {
		StringBuilder output = new StringBuilder();
		DecimalFormat format = new DecimalFormat("00");
		// Add the valid time: from - until
		output.append(format.format(beginHour) + ":" + format.format(beginMinute) + " - "
				+ format.format(endHour) + ":" + format.format(endMinute));
		return output.toString();
	}
	
	/**
	 * Calculates the valid minutes from the begin date and end date.
	 * @return The number of valid minutes.
	 */
	public int getValidMinutes() {
		int validMinutes = (endHour - beginHour)*60 +(endMinute - beginMinute);
		return validMinutes;
	}

	/**
	 * Calculates the endHour based on the begin hour and validMinutes
	 * @return endHour
	 */
	public static int calculateEndHour(int beginHour, int beginMinute, int validMinutes){
		return beginHour + ((validMinutes+beginMinute)/60);
	}
	
	/**
	 * Calculates the endMinute based on the begin minute and validMinutes
	 * @return endHour
	 */
	public static int calculateEndMinute(int beginMinute, int validMinutes){
		return (beginMinute + validMinutes) % 60;
	}
}
