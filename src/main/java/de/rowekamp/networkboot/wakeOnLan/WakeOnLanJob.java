package de.rowekamp.networkboot.wakeOnLan;

import java.util.ArrayList;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class WakeOnLanJob implements Job{

	@SuppressWarnings("unchecked")
	public void execute(JobExecutionContext ctxt) throws JobExecutionException {
		
		JobDataMap data = ctxt.getJobDetail().getJobDataMap();
		
		if (data.containsKey("mac") && data.getString("mac") instanceof String){
			String mac = data.getString("mac");
			WakeOnLAN.wol(mac);
		}

		if (data.containsKey("macList") && data.get("macList") instanceof ArrayList<?>){
			ArrayList<String> macList = (ArrayList<String>) data.get("macList");
			WakeOnLAN.wol(macList);
		}
	}
}
