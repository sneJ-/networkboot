package org.jvnet.hudson.proxy_dhcp;

import static org.jvnet.hudson.proxy_dhcp.DHCPMessageType.DHCPOFFER;
import static org.jvnet.hudson.proxy_dhcp.DHCPOption.OPTION_DHCP_SERVER_IDENTIFIER;
import static org.jvnet.hudson.proxy_dhcp.DHCPPacket.OP_BOOTREQUEST;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.Level;

import static java.util.logging.Level.INFO;

import java.util.logging.Logger;

/**
 * Proxy DHCP service that works in conjunction with a real DHCP server to point PXEclients to the TFTP boot coordinates.
 *
 * @author Kohsuke Kawaguchi
 */
public class ProxyDhcpService implements Runnable {

    public final Inet4Address tftpServer;
    public final String bootFileName;
	public final String ipxeBootServer;
    private final DatagramSocket server;
    private final DatagramSocket replySocket;
    private boolean threadRunning = true;

	public ProxyDhcpService(Inet4Address tftpServer, String bootFileName) throws SocketException {
        this(tftpServer,bootFileName,null,null);
    }
	
    public ProxyDhcpService(Inet4Address tftpServer, String bootFileName, String ipxeBootServer) throws SocketException {
        this(tftpServer,bootFileName,ipxeBootServer,null);
    }
    
    public ProxyDhcpService(Inet4Address tftpServer, String bootFileName, String ipxeBootServer, InetAddress interfaceToListen) throws SocketException {
        this.tftpServer = tftpServer;
        this.bootFileName = bootFileName;
		this.ipxeBootServer = ipxeBootServer;

        server = new DatagramSocket(DHCP_SERVER_PORT,interfaceToListen);
        server.setBroadcast(true);

        if(interfaceToListen==null) {
            replySocket = server;
        } else {
            replySocket = new DatagramSocket(0,tftpServer);
            replySocket.setBroadcast(true);
        }

        LOGGER.info("TFTP server: "+tftpServer.getHostAddress());
        LOGGER.info("Boot file: "+bootFileName);
		if(ipxeBootServer!=null)
			LOGGER.info("iPXE Bootscript Server: "+ipxeBootServer);
    }

    public void run() {
        try {
            execute();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING,"IO exception in proxy DHCP service",e);
        }
    }

    public void execute() throws IOException {
        DatagramPacket datagram = new DatagramPacket(new byte[8192],8192);
        while(threadRunning) {
            server.receive(datagram);
            LOGGER.fine("Got a packet from "+datagram.getSocketAddress());
            try {
                handle(server, datagram);
            } catch (IOException e) {
                LOGGER.log(INFO,"Failed to handle a DHCP packet",e);
            }
        }
    }
    
    /**
     * Launch the server thread...
     */
    public void start() {
      new Thread(this, "Starting Proxy DHCP Server").start();
    }

    /**
     * ...And shut it down again.
     */
    public void shutdown() {
      LOGGER.log(INFO,"Shutting down Proxy DHCP Server.");
      server.close();
    }
    
    private void handle(DatagramSocket server, DatagramPacket datagram) throws IOException {
        DHCPPacket packet = new DHCPPacket(datagram);
        if(packet.op!=OP_BOOTREQUEST) {
            LOGGER.fine("Not a BOOT request: "+packet.op);
            return;
        }

        if(!packet.is(DHCPMessageType.DHCPDISCOVER)) {
            LOGGER.fine("Not a DHCPDISCOVER");
            return;
        }

        String vendorClass = packet.getVendorClassIdentifier();
        if(vendorClass==null || !vendorClass.startsWith("PXEClient")) {
            LOGGER.fine("Not a PXEClient: "+vendorClass);
            return;
        }

        if (!shallWeRespond(datagram,packet)) {
            LOGGER.fine("Ignoring this request");
            return;
        }

        // at this point we think someone is PXE booting and we need to tell it where the boot image is
        DHCPPacket reply = packet.createResponse();
        reply.siaddr = tftpServer;
        reply.chaddr = packet.chaddr;
        reply.file = bootFileName;
		if(ipxeBootServer != null && packet.getUserClass() != null && packet.getUserClass().equals("iPXE")) reply.file = ipxeBootServer; // if the second DHCP request is from iPXE, forward to the iPXE BootScript Server
        reply.options.add(DHCPOFFER.createOption()); // DHCP offer
        reply.options.add(new DHCPOption(OPTION_DHCP_SERVER_IDENTIFIER,tftpServer)); // set server identifier. not sure what to set to.
        reply.options.add(DHCPOption.createVendorClassIdentifier("PXEClient")); // set vendor class
        reply.options.add(PXEOptions.createDiscoveryControl());

        // send back the response
        datagram = reply.pack();
        datagram.setAddress(InetAddress.getByName("255.255.255.255"));
        datagram.setPort(DHCP_CLIENT_PORT);
        replySocket.send(datagram);
        LOGGER.fine("responded");
    }

    /**
     * Subtypes can override this method to selectively respond to the boot requests.
     *
     * By default, this method always return true, meaning it responds to every request.
     *
     * @param datagram
     *      The received DHCP DISCOVER packet.
     * @param packet
     *      Interpreted packet.
     */
    protected boolean shallWeRespond(DatagramPacket datagram, DHCPPacket packet) {
        return true;
    }

    public static final int DHCP_CLIENT_PORT = 68;
    public static final int DHCP_SERVER_PORT = 67;

    private static final Logger LOGGER = Logger.getLogger(ProxyDhcpService.class.getName());
}
