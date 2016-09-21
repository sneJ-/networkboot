package de.rowekamp.networkboot;

public class NetworkBoot {
	
	public static void main(String[] args) throws Exception {
		
		NetworkBootDaemon daemon;
		if (args.length > 0 && !args[0].equals("")){
			daemon = new NetworkBootDaemon(args[0]);
		}else{
			daemon = new NetworkBootDaemon("networkboot.properties");
		}
		daemon.start();
		
	}
}