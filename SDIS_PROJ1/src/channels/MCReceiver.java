package channels;

import java.io.IOException;
import java.net.InetAddress;

import service.Peer;
import service.Server;

public class MCReceiver extends ReceiverServer {
	Peer user;

	public MCReceiver(String[] args) throws NumberFormatException, IOException {
		super();
	}

	public MCReceiver(boolean quitFlag, int serverID, InetAddress addr, int port) {
		super(quitFlag, serverID, addr, port);
	}
}
