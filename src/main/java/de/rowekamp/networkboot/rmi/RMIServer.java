package de.rowekamp.networkboot.rmi;

import java.io.File;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import de.rowekamp.networkboot.wakeOnLan.WakeOnLANScheduler;

public class RMIServer {
	
	private String rmiId;
	private RMIRemoteImplementation rmi;
	private Registry registry;
	
	public RMIServer(int port, String rmiId, File dbFile, int maxFailedLoginAttempts, int blockingTimeIncreaseMinutes, WakeOnLANScheduler wolSched) throws RemoteException{
		this.rmiId = rmiId;
		rmi = new RMIRemoteImplementation(dbFile, maxFailedLoginAttempts, blockingTimeIncreaseMinutes, wolSched);
		registry = LocateRegistry.createRegistry(port);
	}

	public void start() throws RemoteException, AlreadyBoundException{
		registry.bind(rmiId, rmi);
		System.err.println("rmi server started");
	}
	
	public void shutdown() throws RemoteException, NotBoundException{
		registry.unbind(rmiId);
		rmi.stop();
		System.err.println("rmi server stopped");
	}
}
