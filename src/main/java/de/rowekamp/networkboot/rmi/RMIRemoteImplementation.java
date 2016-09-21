package de.rowekamp.networkboot.rmi;

import java.io.File;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

import org.quartz.SchedulerException;

import de.rowekamp.networkboot.authentication.Authenticator;
import de.rowekamp.networkboot.authentication.PasswordHash;
import de.rowekamp.networkboot.wakeOnLan.WakeOnLANScheduler;

public class RMIRemoteImplementation extends UnicastRemoteObject implements RMIInterface{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private File dbFile;
	private int maxFailedLoginAttempts;
	private int blockingTimeIncreaseMinutes;
	private WakeOnLANScheduler wolSched;
	
	protected RMIRemoteImplementation(File dbFile, int maxFailedLoginAttempts, int blockingTimeIncreaseMinutes, WakeOnLANScheduler wolSched) throws RemoteException {
		super();
		this.dbFile = dbFile;
		this.maxFailedLoginAttempts = maxFailedLoginAttempts;
		this.blockingTimeIncreaseMinutes = blockingTimeIncreaseMinutes;
		this.wolSched = wolSched;
	}

	/**
	 * Adds a WOL host to the quartz scheduler.
	 * @return true if succeeded, otherwise false.
	 */
	public boolean addWolHostJob(int id, String cronSchedule, String mac) throws RemoteException {
		try {
			wolSched.addWolHostJob(id, cronSchedule, mac);
			return true;
		} catch (SchedulerException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Deletes a WOL host from the quartz scheduler.
	 * @return true if succeeded, otherwise false.
	 */
	public boolean deleteWolHostJob(int id) throws RemoteException {
		try {
			wolSched.deleteWolHostJob(id);
			return true;
		} catch (SchedulerException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Adds a WOL group to the quartz scheduler.
	 * @return true if succeeded, otherwise false.
	 */
	public boolean addWolGroupJob(int id, String cronSchedule, ArrayList<String> macList) throws RemoteException {
		try {
			wolSched.addWolGroupJob(id, cronSchedule, macList);
			return true;
		} catch (SchedulerException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Deletes a WOL group from the quartz scheduler.
	 * @return true if succeeded, otherwise false.
	 */
	public boolean deleteWolGroupJob(int id) throws RemoteException {
		try {
			wolSched.deleteWolGroupJob(id);
			return true;
		} catch (SchedulerException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Authenticates a user from the frontend.
	 * @return 0 if login succeeded, 1 if username/password were wrong, 2 if user tried to login too often, 3 if user wasn't allowed to login, 4 if username and password weren't transmitted correctly, 5 and 6 if errors occurred during authentication, 7 if an database error occurred
	 */
	public int frontendAuthentication(String username, String password) throws RemoteException {
		Authenticator auth = new Authenticator(dbFile, maxFailedLoginAttempts, blockingTimeIncreaseMinutes);
		int returns = auth.frontendAuthentication(username, password);
		auth.close();
		return returns;
	}

    /**
     * Returns a salted PBKDF2 hash of the password.
     *
     * @param   password    the password to hash
     * @return              a salted PBKDF2 hash of the password
     */
	public String generateHashedPassword(String password) throws RemoteException, NoSuchAlgorithmException, InvalidKeySpecException {
		return PasswordHash.createHash(password);
	}
	
	/**
	 * Stops the rmi service
	 * @throws NoSuchObjectException
	 */
	public void stop() throws NoSuchObjectException{
		UnicastRemoteObject.unexportObject(this, true);
	}
}
