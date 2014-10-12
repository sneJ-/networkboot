/*
 * Starts and Stops the DHCP Daemon
 */

package uk.ac.lsbu.networkboot.backend.dhcpd;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import redes.Servidor;

public class Dhcpd implements Observer{
	
	private Servidor server = null;
	
	/*
	 * Constructor
	 */
	public Dhcpd(){
		server = new Servidor();
		this.setDhcpIp("192.168.42.1");
		server.addObserver(this);
	}
	
	/*
	 * Starts the dhcp daemon
	 */
	public void start(){
		server.inicie();
	}
	
	/*
	 * Stops the dhcp daemon
	 */
	public void stop(){
		server.aborte();
	}
	
	/*
	 * Sets values of the DHCP server
	 */
	public void setIpInicial(String ip){
		server.setIpInicial(ip);
	}

	public void setIpFinal(String ip){
		server.setIpFinal(ip);
	}
	
	public void setDhcpIp(String ip){
		server.setDhcpIp(ip);
	}
	
	public void setDnsIp(String ip){
		server.setDnsIp(ip);
	}
	
	public void setSubnetMask(String mask){
		server.setSubnetMask(mask);
	}
	
	public void setGatewayIp(String ip){
		server.setGatewayIp(ip);
	}
	
	public void setLeaseTime(int time){
		server.setLeaseTime(time);
	}
	
	public void setRenewTime(int time){
		server.setRenewTime(time);
	}
	
	public void setNextServer(String ip){
		server.setTftpServerIp(ip);
	}
	
	public void setBootFile(String file){
		server.setPxeFile(file);
		server.setBootFile(file);
	}
	
	public void updateRange(){
		server.updateRange();
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 * Outputs from the server, observes it...
	 */
	public void update(Observable o, Object arg) {
		synchronized (System.err) {
			if (arg instanceof Throwable) {
				System.err.println("{" + currentDateTime() + "} "
						+ ((Throwable) arg).getLocalizedMessage());
			} else {
				System.err.println("{" + currentDateTime() + "} "
						+ arg.toString());
			}
		}
	}
	
	/*
	 * Produces the time stamp for logging
	 */
	private String currentDateTime() {
		String FORMATO_HORA = "HH:mm:ss";
		String FORMATO_FECHA = "MM-dd";
		SimpleDateFormat TIME_FORMATER = new SimpleDateFormat(FORMATO_HORA);
		SimpleDateFormat DATE_FORMATER = new SimpleDateFormat(FORMATO_FECHA);			
		
		return DATE_FORMATER.format(new Date()) + " " +	TIME_FORMATER.format(new Date());
	}
}
