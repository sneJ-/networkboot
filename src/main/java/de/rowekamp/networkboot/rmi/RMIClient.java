package de.rowekamp.networkboot.rmi;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

import javax.faces.context.FacesContext;

public class RMIClient implements RMIInterface{

	private Registry registry;
	private RMIInterface remote;
	private int rmiPort = 1099;
	private String rmiId = "networkboot";
	
	public RMIClient() throws RemoteException, NotBoundException{
		//Get the RMI parameters passed from jetty
		FacesContext ctx = FacesContext.getCurrentInstance();
		if (ctx.getExternalContext().getInitParameter("RMI_PORT") != null) {
			rmiPort = Integer.parseInt(ctx.getExternalContext().getInitParameter("RMI_PORT"));
		}
		if (ctx.getExternalContext().getInitParameter("RMI_ID") != null) {
			rmiId = ctx.getExternalContext().getInitParameter("RMI_ID");
		}
		
		registry = LocateRegistry.getRegistry(rmiPort);
		remote = (RMIInterface) registry.lookup(rmiId);
	}
	
	public boolean addWolGroupJob(int id, String cronSchedule, ArrayList<String> macList) throws RemoteException{
		return remote.addWolGroupJob(id, cronSchedule, macList);
	}
	
	public boolean addWolHostJob(int id, String cronSchedule, String mac) throws RemoteException{
		return remote.addWolHostJob(id, cronSchedule, mac);
	}
	
	public boolean deleteWolHostJob(int id) throws RemoteException{
		return remote.deleteWolHostJob(id);
	}
	
	public boolean deleteWolGroupJob(int id) throws RemoteException{
		return remote.deleteWolGroupJob(id);
	}
	
	public int frontendAuthentication(String username, String password) throws RemoteException{
		return remote.frontendAuthentication(username, password);
	}
	
	public String generateHashedPassword(String password) throws RemoteException, NoSuchAlgorithmException, InvalidKeySpecException{
		return remote.generateHashedPassword(password);
	}
}