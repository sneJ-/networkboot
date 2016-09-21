package de.rowekamp.networkboot.wakeOnLan;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

public class WakeOnLAN {

	private static final int PORT = 9;

	/**
	 * Wakes up a PC using the Magic Packet.
	 * 
	 * @param mac 12 digits from 0-F (ABCDeF123456)
	 */
	public static void wol(String mac) {
		
		//Check if the MAC address is valid.
		if (mac.length() == 12 && mac.matches("[abcdef0123456789]*")) {
			
			byte[] message = new byte[102];
			
			//First 6 times 0xff in the magic packet
			for (int i = 0; i < 6; i++) {
				message[i] = (byte) 0xff;
			}
			
			//Afterwards 16times the MAC address
			for (int i = 6; i < 102; i++) {
				message[i] = (byte) Integer.parseInt(mac.substring(0, 2),16);
				message[++i] = (byte) Integer.parseInt(mac.substring(2, 4),16);
				message[++i] = (byte) Integer.parseInt(mac.substring(4, 6),16);
				message[++i] = (byte) Integer.parseInt(mac.substring(6, 8),16);
				message[++i] = (byte) Integer.parseInt(mac.substring(8, 10),16);
				message[++i] = (byte) Integer.parseInt(mac.substring(10, 12),16);
			}

			//Define the Broadcast IPv4 Address 255.255.255.255 as Destination.
			byte[] address = new byte[] { (byte) 0xff, (byte) 0xff,
					(byte) 0xff, (byte) 0xff };
			InetAddress addr;
			
			try {
				addr = InetAddress.getByAddress(address);
				//Prepare the UDP Packet
				DatagramPacket packet = new DatagramPacket(message,
						message.length, addr, PORT);
				
				//Open a UDP Socket
				DatagramSocket socket = new DatagramSocket();
				//Send the WOL Packet
				socket.send(packet);
				//Close the UDP Socket
				socket.close();
				
			} catch (Exception e) {
				System.err.println("WOL Error");
				e.printStackTrace();
			}
		}else{
			System.err.println("Error: Invalid mac "+mac+" to wake up!");
		}
	}
	
	/**
	 * Wakes up a list of PCs using the Magic Packet.
	 * 
	 * @param mac 12 digits from 0-F (ABCDeF123456)
	 */
	public static void wol(ArrayList<String> macList) {
		WOLList wollist = new WOLList(macList);
		Thread t = new Thread(wollist);
		t.start();
	}
	
	/**
	 * Thread which wakes up the Hosts in the macList.
	 * Every Second another host gets woken up.
	 */
	private static class WOLList implements Runnable{
		ArrayList<String> macList;
		
		public WOLList(ArrayList<String> macList){
			this.macList = macList;
		}
		
		public void run() {
			for (String mac : macList){
				wol(mac);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
