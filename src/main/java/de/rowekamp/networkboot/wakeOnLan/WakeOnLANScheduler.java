package de.rowekamp.networkboot.wakeOnLan;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import de.rowekamp.networkboot.database.WakeOnLanDatabase;

public class WakeOnLANScheduler {

	private SchedulerFactory schedFact;
	private Scheduler sched;

	public WakeOnLANScheduler() throws SchedulerException {
		schedFact = new org.quartz.impl.StdSchedulerFactory();
		sched = schedFact.getScheduler();
	}

	public void start() throws SchedulerException {
		sched.start();
	}

	public void shutdown(boolean a) throws SchedulerException {
		sched.shutdown(a);
	}

	public void addWolHostJob(int id, String cronSchedule, String mac ) throws SchedulerException {
		JobDetail job = JobBuilder.newJob(WakeOnLanJob.class)
				.withIdentity("wol host job id "+id, "wol host group").build();
		job.getJobDataMap().put("mac", mac);
		Trigger trigger = TriggerBuilder.newTrigger()
				.withIdentity("wol host job id "+ id, "wol host group")
				.withSchedule(CronScheduleBuilder.cronSchedule(cronSchedule))
				.build();
		sched.scheduleJob(job,trigger);
	}
	
	public void deleteWolHostJob(int id) throws SchedulerException{
		sched.deleteJob(new JobKey("wol host job id "+id, "wol host group"));
	}
	
	public void addWolGroupJob(int id, String cronSchedule, ArrayList<String> macList ) throws SchedulerException {
		JobDetail job = JobBuilder.newJob(WakeOnLanJob.class)
				.withIdentity("wol group job id "+id, "wol group group").build();
		job.getJobDataMap().put("macList", macList);
		Trigger trigger = TriggerBuilder.newTrigger()
				.withIdentity("wol group job id "+ id, "wol group group")
				.withSchedule(CronScheduleBuilder.cronSchedule(cronSchedule))
				.build();
		sched.scheduleJob(job,trigger);
	}
	
	public void deleteWolGroupJob(int id) throws SchedulerException{
		sched.deleteJob(new JobKey("wol group job id "+id, "wol group group"));
	}
	
	public void initializeFromDb(File dbFile) throws SchedulerException, SQLException{
		WakeOnLanDatabase db = new WakeOnLanDatabase(dbFile);
		ArrayList<HostJob> hostJobs = db.getHostJobs();
		ArrayList<GroupJob> groupJobs = db.getGroupJobs();
		db.close();
		for(HostJob hj : hostJobs){
			addWolHostJob(hj.getId(), hj.getCronSchedule(), hj.getMac());
		}
		for(GroupJob gj : groupJobs){
			addWolGroupJob(gj.getId(), gj.getCronSchedule(), gj.getMacArray());
		}
	}
}
