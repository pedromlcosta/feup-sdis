package channels;

import java.net.InetAddress;

import service.Peer;


public class MCReceiver extends MulticastServer {

	/**
	 * Default constructor for this subclass
	 */
	public MCReceiver(Peer peer){
		user = peer;
	}

	/**
	 * 
	 * @param quitFlag flag for the infinite run cycle that receives the messages
	 * @param serverID Identifier of the peer this receiver belongs to
	 * @param addr Multicast IP address of this receiver
	 * @param port Multicast Port of this receiver
	 */
	public MCReceiver(boolean quitFlag, int serverID, InetAddress addr, int port) {
		super(quitFlag, serverID, addr, port);
	}
}
