package redes;

import java.io.Closeable;
import java.io.IOException;

public class DHCP {

	protected static byte[] ipInicial;
	protected static byte[] ipFinal;
	protected static byte[] mascara;
	protected static byte[] servidor;
	protected static byte[] gateway;
	protected static byte[] dns;
	protected static byte[] tiempo_arriendo;
	protected static byte[] tiempo_renovacion;
	protected static byte[] tftpServerIp;
	protected static byte[] pxeFile;
	protected static byte [] bootFile = new byte[128];

	private static DHCP instancia = null;

	public static DHCP getInstance() {
		if (instancia == null)
			instancia = new DHCP();

		return instancia;
	}

	public static byte[] getIpInicial() {
		return ipInicial;
	}

	public static byte[] getIpFinak() {
		return ipFinal;
	}

	public static byte[] getLeaseTime() {
		return tiempo_arriendo;
	}

	/**
	 * @return mascara
	 */
	public static byte[] getMascara() {
		return mascara;
	}

	/**
	 * @return gateway
	 */
	public static byte[] getGateway() {
		return gateway;
	}

	/**
	 * @return tiempo para renovar el lease
	 */
	public static byte[] getRenewalTime() {
		// return intAByte(100000);
		return tiempo_renovacion;
	}

	/**
	 * @return Direccion IP Servidor
	 */
	public static byte[] getServidor() {
		return servidor;
	}

	/**
	 * @return Direccion DNS
	 */
	public static byte[] getDNS() {
		return dns;
	}

	/*
	 * @return PXE Server IP
	 */
	public static byte[] getTftpServerIp(){
		return tftpServerIp;
	}
	
	/*
	 * @return PXE Boot File
	 */
	
	public static byte[] getPxeFile(){
		return pxeFile;
	}
	
	/**
	 * 
	 */
	private static Servidor serv = null;


	/**
	 * Constructor
	 */
	public DHCP() {
		System.out.println("");
	}

	/**
	 * Limpia toda la tabla de datos de ips (HASHtable)
	 */
	protected void limpiaDatos() {
		serv.liberarTodos();
	}

	// ---------------------------------------------//
	// Metodos estaticos de trasnformar valores //
	// ---------------------------------------------//
	public static int byteAInt(byte[] buffer) {
		int x = (0xFF & buffer[0]) << 24;
		x |= (0xFF & buffer[1]) << 16;
		x |= (0xFF & buffer[2]) << 8;
		x |= (0xFF & buffer[3]);

		return x;
	}

	public static String byteAIp(byte[] b) {
		StringBuilder temp = new StringBuilder();

		for (int i = 0; i < 4; i++) {
			if (i > 0) {
				temp.append(".");
			}
			String n = String.valueOf(0xFF & b[i]);
			temp.append(n);
		}

		return temp.toString();
	}

	public static String byteAMac(byte[] b) {
		StringBuilder temp = new StringBuilder();

		for (int i = 0; i < 6; i++) {
			if (i > 0)
				temp.append(":");
			String n = Integer.toHexString(0xFF & b[i]);
			if (n.length() == 1)
				n = "0" + n;

			temp.append(n);
		}

		return temp.toString();
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
		String[] partes = ip.split("\\.");
		byte[] back = new byte[4];

		if (partes.length != 4) {
			return back;
		}

		for (int i = 0; i < 4; i++) {
			int valor = Integer.parseInt(partes[i]);
			if (valor < 0 || valor > 255) {
				return new byte[4];
			}
			valor -= 256;
			back[i] = (byte) valor;
		}

		return back;
	}

	public static int[] byteAaIntA(byte[] data) {
		int[] temp = new int[data.length];

		for (int i = 0; i < data.length; i++)
			temp[i] = 0xFF & data[i];

		return temp;
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
}
