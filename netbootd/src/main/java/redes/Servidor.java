package redes;

import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import edu.bucknell.net.JDHCP.DHCPMessage;
import edu.bucknell.net.JDHCP.DHCPOptions;
import edu.bucknell.net.JDHCP.DHCPSocket;

public class Servidor extends Hilo {
	private static class IPRange {

		private int[] ipInicial;
		private int[] ipFinal;

		public IPRange(byte[] i, byte[] f) {
			ipInicial = byteAaIntA(i);
			ipFinal = byteAaIntA(f);
		}

		public boolean rango(byte[] ip) {
			int[] i_ip = byteAaIntA(ip);

			for (int i = 0; i < 4; i++) {
				if (i_ip[i] < ipInicial[i] || i_ip[i] > ipFinal[i])
					return false;
			}

			return true;
		}

		public byte[] siguienteIp(byte[] ip) {
			int[] array = { 0xFF & ip[0], 0xFF & ip[1], 0xFF & ip[2],
					0xFF & ip[3] };
			array[3]++;

			if (array[3] > ipFinal[3]) {
				array[3] = 1;
				array[2]++;
			}

			if (array[2] > ipFinal[2]) {
				array[2] = 0;
				array[1]++;
			}

			if (array[1] > ipFinal[1]) {
				array[1] = 0;
				array[0]++;
			}

			byte[] temp = { (byte) array[0], (byte) array[1], (byte) array[2],
					(byte) array[3] };

			if (rango(temp))
				return temp;
			else
				return new byte[] { 0, 0, 0, 0 };
		}
	}

	private static class Lease {

		private int lease_time = byteAInt(DHCP.getLeaseTime()) * 1000;
		private byte[] ip_byte;
		private String ip_string;
		private long leased;

		public Lease(byte[] ip) {
			ip_byte = ip.clone();
			leased = System.currentTimeMillis() + 600000;

			ip_string = byteAIp(ip_byte);
		}

		public byte[] getIp() {
			return ip_byte;
		}

		public String getIpString() {
			return ip_string;
		}

		public boolean isValid() {
			return (leased + lease_time >= System.currentTimeMillis());
		}

		public void refreshLease() {
			leased = System.currentTimeMillis() + 1000;
		}

		public String toString() {
			return getIpString() + ", asignada hasta: "
					+ dateTime(leased + lease_time);
		}
	}

	private class LeaseTimer extends Hilo {

		public LeaseTimer() {
			// nombre del Hilo
			setNombre("Lease_Timer");
		}

		/**
		 * Chequea y elimina
		 */
		private void check() {
			ArrayList<String> eliminar = new ArrayList<String>();

			synchronized (direcciones) {
				for (String mac : direcciones.keySet()) {
					if (!direcciones.get(mac).isValid()) {
						eliminar.add(mac);
					}
				}

				for (String mac : eliminar) {
					mensaje("Arrendamiento de : "
							+ direcciones.get(mac).getIpString() + " expirado");
					direcciones.remove(mac);
				}
			}
		}

		public void run() {
			while (continueTarea()) {
				check();

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

//	InetAddress SendSocketIp;

	/**
	 * Timer para control de arredamientos
	 */
	private LeaseTimer lease_timer;

	/**
	 * Hashmap de mac y arrendamiento
	 */
	private HashMap<String, Lease> direcciones;

	/**
	 * Rango de direcciones
	 */
	private IPRange rango;

	/**
	 * Socket
	 */
	private DHCPSocket socket;
//	private DatagramSocket sendSocket;

	/**
	 * Constructor
	 */
	public Servidor() {
		cargarInfo();
		direcciones = new HashMap<String, Lease>();
		socket = null;
		rango = new IPRange(DHCP.getIpInicial(), DHCP.getIpFinak());

		lease_timer = new LeaseTimer();

		// Nombre Hilo
		setNombre("Servidor");
	}

	private void cargarInfo() {
		/*
		 * DHCP.ipInicial = ipAByte("192.168.1.75"); DHCP.ipFinal =
		 * ipAByte("192.168.1.85"); DHCP.servidor = ipAByte("192.168.1.25");
		 * DHCP.dns = ipAByte("190.157.2.140"); DHCP.mascara =
		 * ipAByte("255.255.255.0"); DHCP.gateway = ipAByte("192.168.1.1");
		 * DHCP.tiempo_arriendo = intAByte(25); DHCP.tiempo_renovacion =
		 * intAByte(25);
		 */

		setIpInicial("192.168.42.100");
		setIpFinal("192.168.42.200");
		setDhcpIp("192.168.42.1");
		setDnsIp("8.8.8.8");
		setSubnetMask("255.255.255.0");
		setGatewayIp("192.168.42.254");
		setLeaseTime(3600);
		setRenewTime(3600);
		setGatewayIp("192.168.42.1");
		setTftpServerIp("192.168.42.1");
		setPxeFile("ipxe.kpxe");
		setBootFile("ipxe.kpxe");
		// cargarArchivo();
	}

	public void setIpInicial(String ip) {
		DHCP.ipInicial = ipAByte(ip);
	}

	public void setIpFinal(String ip) {
		DHCP.ipFinal = ipAByte(ip);
	}

	public void setDhcpIp(String ip) {
		DHCP.servidor = ipAByte(ip);
	}

	public void setDnsIp(String ip) {
		DHCP.dns = ipAByte(ip);
	}

	public void setSubnetMask(String mask) {
		DHCP.mascara = ipAByte(mask);
	}

	public void setGatewayIp(String ip) {
		DHCP.gateway = ipAByte(ip);
	}

	public void setLeaseTime(int time) {
		DHCP.tiempo_arriendo = intAByte(time);
	}

	public void setRenewTime(int time) {
		DHCP.tiempo_renovacion = intAByte(time);
	}

	public void setTftpServerIp(String ip) {
		DHCP.tftpServerIp = ipAByte(ip);
	}

	public void setPxeFile(String file) {
		DHCP.pxeFile = stringAByte(file);
	}

	public void setBootFile(String file) {
		byte[] temp = stringAByte(file);
		byte[] returns = new byte[128];
		for (int i = 0; i < temp.length; i++) {
			returns[i] = temp[i];
		}
		DHCP.bootFile = returns;
	}
	
	public void updateRange(){
		rango = new IPRange(DHCP.getIpInicial(), DHCP.getIpFinak());
	}

	@Override
	public void aborte() {
		lease_timer.aborte();
		super.aborte();
	}

	/**
	 * Agrega una mac nueva con el lease
	 * 
	 * @param mac
	 * @param ip
	 */
	public void nuevoCliente(String mac, String ip) {
		synchronized (direcciones) {
			direcciones.put(mac, new Lease(ipAByte(ip)));
		}
	}

	/**
	 * DHCPREQUEST
	 * 
	 * @param m
	 *            DHCPMessage
	 */
	private void respuestaRequest(DHCPMessage m) {
		String mac = byteAMac(m.getChaddr());

		if (direcciones.containsKey(mac)) {
			envioAck(m, direcciones.get(mac).getIp(), false);
			direcciones.get(mac).refreshLease();
			return;
		}

		byte[] ip = m.getOption(DHCPOptions.OPTION_DHCP_IP_ADRESS_REQUESTED);

		if (ip == null || !ipLibre(byteAIp(ip))) {
			ip = new byte[4];

			envioNAck(m, ip);
		} else {
			envioAck(m, ip, false);

			synchronized (direcciones) {
				direcciones.put(byteAMac(m.getChaddr()), new Lease(ip));
			}
		}

		return;
	}

	/**
	 * Devuelve la siguiente direccion libre o 0.0.0.0 si no quedan si la mac
	 * tiene ip devuelve la ip ya asiganda
	 * 
	 * @param mac
	 *            direccion mac
	 * @return ip o vacio si no quedan
	 */
	private synchronized byte[] sieguienteIpLibre(String mac) {
		synchronized (direcciones) {
			if (direcciones.containsKey(mac))
				return direcciones.get(mac).getIp();
		}

		byte[] back = DHCP.getIpInicial().clone();
		String ip = byteAIp(back);

		while (!ipLibre(ip)) {
			back = rango.siguienteIp(back);
			ip = byteAIp(back);
		}

		if (rango.rango(back))
			return back;
		else
			return new byte[] { 0, 0, 0, 0 };
	}

	public String siguienteIp() {
		return byteAIp(sieguienteIpLibre(null));
	}

	/**
	 * @return tabla de hash con las mac y las ip
	 */
	public HashMap<String, Object> getTable() {
		HashMap<String, Object> temp = new HashMap<String, Object>();

		synchronized (direcciones) {
			for (String mac : direcciones.keySet()) {
				temp.put(mac, direcciones.get(mac));
			}
		}

		return temp;
	}

	/**
	 * Maneja el mensaje despendiendo del tipo
	 * 
	 * @param m
	 *            the message to interprete
	 */
	private void manejoRequest(DHCPMessage m) {

			byte message_type = m
					.getOption(DHCPOptions.OPTION_DHCP_MESSAGE_TYPE)[0];
			String mac = byteAMac(m.getChaddr());
			String hostname;
			if (m.getOption((DHCPOptions.OPTION_HOSTNAME)) == null) {
				hostname = "no hostname";
			} else {
				hostname = new String(m.getOption(DHCPOptions.OPTION_HOSTNAME));
			}

			String identificador = mac;

			if (hostname != null && hostname.length() > 0)
				identificador += " (" + hostname + ")";

			switch (message_type) {
			case DHCPMessage.DHCPDISCOVER:
				mensaje("DHCPDISCOVER " + identificador);
				envioDiscover(m);
				break;
			case DHCPMessage.DHCPREQUEST:
				mensaje("DHCPREQUEST " + identificador);
				respuestaRequest(m);
				break;
			case DHCPMessage.DHCPDECLINE:
				mensaje("DHCPDECLINE " + identificador);
				direcciones.put(String.valueOf(System.currentTimeMillis()),
						new Lease(ipAByte(byteAIp(m.getCiaddr()))));
				direcciones.remove(byteAMac(m.getChaddr()));
				break;
			case DHCPMessage.DHCPRELEASE:
				mensaje("DHCPRELEASE " + identificador);
				direcciones.remove(byteAMac(m.getChaddr()));
				break;
			case DHCPMessage.DHCPINFORM:
				envioAck(m, new byte[4], true);
				break;
			default:
				mensaje("ERROR Tipo de mensaje: " + (int) message_type);
				break;
			}
	}

	/**
	 * Checks wheter the given ip adress is not in use. Does a lookup in our
	 * HashTable {@link #direcciones}.
	 * 
	 * @param ip
	 *            the ip adress to check
	 * @return is the given ip free?
	 */
	private boolean ipLibre(String ip) {
		if (ip == null || ip.equals("0.0.0.0"))
			return false;

		for (Lease akt : direcciones.values()) {
			if (akt.getIpString().equals(ip) && akt.isValid()) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Observer Model notifica al observador
	 * 
	 * @param m
	 *            mensaje
	 */
	public void mensaje(String m) {
		setChanged();
		notifyObservers(m);
	}

	/**
	 * Libera todas las direcciones
	 */
	public void liberarTodos() {
		synchronized (direcciones) {
			direcciones.clear();
		}
	}

	/**
	 * Libera Ip
	 * 
	 * @param c
	 *            ip a eliminar
	 */
	public void liberaIp(String c) {
		synchronized (direcciones) {
			if (c.contains(":")) { // MAC
				Lease lease = direcciones.get(c);

				if (lease != null) {
					direcciones.remove(c);
					mensaje("Cliente " + c + " (" + lease.getIpString()
							+ ") eliminado.");
				}
			} else { // IP
				String toDelete = null;
				for (String mac : direcciones.keySet()) {
					Lease lease = direcciones.get(mac);

					if (lease.getIpString().equals(c)) {
						toDelete = mac;
					}
				}

				if (toDelete != null) {
					direcciones.remove(toDelete);
					mensaje("Cliente " + toDelete + " (" + c + ") eliminado.");
				}
			}
		}
	}

	@Override
	public void run() {
		mensaje("" + byteAIp(DHCP.getServidor()));
		try {
			socket = new DHCPSocket(DHCPMessage.SERVER_PORT);
			socket.setBroadcast(true);
			direcciones.clear();
		} catch (SocketException e) {
			setChanged();
			notifyObservers(e);

			mensaje("Colapso " + byteAIp(DHCP.getServidor()));
			return;
		}

		lease_timer.inicie();
		while (continueTarea()) {
			DHCPMessage anfrage = new DHCPMessage();
			try{
				if (socket.receive(anfrage))
					manejoRequest(anfrage);
			} catch (Exception e){ }
		}
		lease_timer.aborte();

		socket.close();
		socket = null;
		direcciones.clear();
		direcciones = null;
		rango = null;

		mensaje("Servidor DOWN : (LINEA 457 Servidor)"
				+ byteAIp(DHCP.getServidor()));
	}

	/**
	 * DHCPACK
	 * 
	 * @param m
	 *            DHCPMessage
	 * @param ip
	 *            ip del cliente
	 * @param unicast
	 *            a todos o solo a uno
	 */
	private void envioAck(DHCPMessage m, byte[] ip, boolean unicast) {
		DHCPMessage temp = new DHCPMessage();

		temp.setOp(DHCPMessage.OP_REPLY);
		temp.setHtype(m.getHtype());
		temp.setHlen(m.getHlen());
		temp.setHops((byte) 0);
		temp.setXid(m.getXid());
		temp.setFlags(m.getFlags());
		temp.setYiaddr(ip);
		temp.setSiaddr(DHCP.tftpServerIp); // Set the next-server value
		temp.setFile(DHCP.bootFile); // Set the boot file value
		temp.setChaddr(m.getChaddr());
		temp.setOption(DHCPOptions.OPTION_NETMASK, DHCP.getMascara());
		if (!unicast) {
			temp.setOption(DHCPOptions.OPTION_ROUTERS, DHCP.getGateway());
			temp.setOption(DHCPOptions.OPTION_DHCP_IP_LEASE_TIME,
					DHCP.getLeaseTime());
			temp.setOption(DHCPOptions.OPTION_DHCP_RENEWAL_TIME,
					DHCP.getRenewalTime());
			temp.setOption(DHCPOptions.OPTION_NEXT_SERVER,
					DHCP.getTftpServerIp());
			temp.setOption(DHCPOptions.OPTION_PXE_FILE, DHCP.getPxeFile());
		}
		temp.setOption(DHCPOptions.OPTION_DHCP_MESSAGE_TYPE,
				new byte[] { DHCPMessage.DHCPACK });

		temp.setOption(DHCPOptions.OPTION_DNS_SERVERS, DHCP.getDNS());

		try {
			byte[] data = temp.externalize();

			if (!unicast) {
				socket.send(new DatagramPacket(data, data.length,
						DHCPMessage.getBroadCastAddress(), DHCPMessage.CLIENT_PORT));
			} else {
				InetAddress unicast_addr = null;

				try {
					unicast_addr = InetAddress.getByAddress(ip);
				} catch (UnknownHostException e) {
					setChanged();
					notifyObservers(e);
				}
				socket.send(new DatagramPacket(data, data.length, unicast_addr,
						DHCPMessage.CLIENT_PORT));
			}
		} catch (IOException e) {
			setChanged();
			notifyObservers(e);

			return;
		}

		mensaje("DHCPACK " + byteAMac(m.getChaddr()) + " " + byteAIp(ip));
	}

	/**
	 * DHCPDISCOVER
	 * 
	 * @param m
	 *            mensaje DHCPMessage
	 */
	private void envioDiscover(DHCPMessage m) {
		DHCPMessage temp = new DHCPMessage();

		temp.setOp(DHCPMessage.OP_REPLY);
		temp.setHtype(m.getHtype());
		temp.setHlen(m.getHlen());
		temp.setHops((byte) 0);
		temp.setXid(m.getXid());
		temp.setFlags(m.getFlags());
		temp.setYiaddr(sieguienteIpLibre(byteAMac(m.getChaddr())));
		temp.setSiaddr(DHCP.tftpServerIp); // Set the next-server value
		temp.setFile(DHCP.bootFile); // Set the boot file value
		temp.setChaddr(m.getChaddr());
		temp.setOption(DHCPOptions.OPTION_NETMASK, DHCP.getMascara());
		temp.setOption(DHCPOptions.OPTION_DHCP_MESSAGE_TYPE,
				new byte[] { DHCPMessage.DHCPOFFER });

		temp.setOption(DHCPOptions.OPTION_DNS_SERVERS, DHCP.getDNS());

		if (temp.getYiaddr()[0] == (byte) 0) {
			mensaje("ERROR: Direccion se acabaron " + byteAMac(m.getChaddr()));
			return;
		}

		try {
			byte[] data = temp.externalize();
			socket.send(new DatagramPacket(data, data.length,
					DHCPMessage.getBroadCastAddress(), DHCPMessage.CLIENT_PORT));
		} catch (IOException e) {
			setChanged();
			notifyObservers(e);
			return;
		}

		synchronized (direcciones) {
			direcciones.put(byteAMac(m.getChaddr()),
					new Lease(temp.getYiaddr()));
		}

		mensaje("DHCPOFFER : " + byteAMac(m.getChaddr()) + " IP "
				+ byteAIp(temp.getYiaddr()));

		return;
	}

	/**
	 * DHCHNACK
	 * 
	 * @param m
	 *            mensjae DHCPMessage
	 * @param ip
	 *            ip destino
	 */
	private void envioNAck(DHCPMessage m, byte[] ip) {
		DHCPMessage back = new DHCPMessage();

		back.setOp(DHCPMessage.OP_REPLY);
		back.setHtype(m.getHtype());
		back.setHlen(m.getHlen());
		back.setHops((byte) 0);
		back.setXid(m.getXid());
		back.setFlags(m.getFlags());
		back.setYiaddr(ip);
		back.setChaddr(m.getChaddr());
		back.setSiaddr(DHCP.tftpServerIp); // Set the next-server value
		back.setFile(DHCP.bootFile); // Set the boot file value
		back.setOption(DHCPOptions.OPTION_DHCP_MESSAGE_TYPE,
				new byte[] { DHCPMessage.DHCPNAK });

		try {
			byte[] data = back.externalize();
			socket.send(new DatagramPacket(data, data.length,
					DHCPMessage.getBroadCastAddress(), DHCPMessage.CLIENT_PORT));
		} catch (IOException e) {
			setChanged();
			notifyObservers(e);

			return;
		}

		mensaje("DHCPNACK to " + byteAMac(m.getChaddr()));
	}

	// ----------------------------------------------//
	// Metodos estaticos de trasnformar valores //
	// ----------------------------------------------//
	public static int byteAInt(byte[] buffer) {
		if (buffer.length != 4) {
			throw new IllegalArgumentException("debe ser de 5");
		}

		int x = (0xFF & buffer[0]) << 24;
		x |= (0xFF & buffer[1]) << 16;
		x |= (0xFF & buffer[2]) << 8;
		x |= (0xFF & buffer[3]);

		return x;
	}

	public static String byteAIp(byte[] b) {
		StringBuilder back = new StringBuilder();

		for (int i = 0; i < 4; i++) {
			if (i > 0)
				back.append(".");
			String neu = String.valueOf(0xFF & b[i]);

			back.append(neu);
		}

		return back.toString();
	}

	public static String byteAMac(byte[] b) {
		StringBuilder back = new StringBuilder();

		for (int i = 0; i < 6; i++) {
			if (i > 0)
				back.append(":");
			String neu = Integer.toHexString(0xFF & b[i]);
			if (neu.length() == 1)
				neu = "0" + neu;

			back.append(neu);
		}

		return back.toString();
	}

	public static byte[] intAByte(int val) {
		byte[] buffer = new byte[4];

		buffer[0] = (byte) (val >>> 24);
		buffer[1] = (byte) (val >>> 16);
		buffer[2] = (byte) (val >>> 8);
		buffer[3] = (byte) val;

		return buffer;
	}

	public static byte[] ipAByte(String ip) {
		String[] parts = ip.split("\\.");
		byte[] back = new byte[4];

		if (parts.length != 4)
			return back;

		for (int i = 0; i < 4; i++) {
			int val = Integer.parseInt(parts[i]);
			if (val < 0 || val > 255)
				return new byte[4];

			val -= 256;
			back[i] = (byte) val;
		}

		return back;
	}

	public static int[] byteAaIntA(byte[] data) {
		int[] back = new int[data.length];

		for (int i = 0; i < data.length; i++)
			back[i] = 0xFF & data[i];

		return back;
	}

	// Converts the String to ASCII Bytes
	public static byte[] stringAByte(String string) {
		byte[] back;
		try {
			back = string.getBytes("US-ASCII");
		} catch (UnsupportedEncodingException e) {
			back = new byte[] { 101, 114, 114, 111, 114 };
			e.printStackTrace();
		}
		return back;
	}

	public static void close(Closeable c) {
		if (c == null) {
			return;
		}

		try {
			c.close();
		} catch (IOException e) {
		}
	}

	public static String dateTime(long s) {
		Date d = new Date(s);
		String FORMATO_HORA = "HH:mm:ss";
		String FORMATO_FECHA = "MM-dd";
		SimpleDateFormat HORA = new SimpleDateFormat(FORMATO_HORA);
		SimpleDateFormat FECHA = new SimpleDateFormat(FORMATO_FECHA);

		return FECHA.format(d) + " " + HORA.format(d);
	}
}
