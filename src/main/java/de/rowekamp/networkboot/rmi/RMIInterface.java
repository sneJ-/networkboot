package de.rowekamp.networkboot.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

public interface RMIInterface extends Remote{
	//WakeOnLANScheduler Functions
	public boolean addWolHostJob(int id, String cronSchedule, String mac ) throws RemoteException;
	public boolean deleteWolHostJob(int id) throws RemoteException;
	public boolean addWolGroupJob(int id, String cronSchedule, ArrayList<String> macList ) throws RemoteException;
	public boolean deleteWolGroupJob(int id) throws RemoteException;
	
	//Authentication Functions
	public int frontendAuthentication(String username, String password) throws RemoteException;
	public String generateHashedPassword(String password) throws RemoteException, NoSuchAlgorithmException, InvalidKeySpecException;
}
