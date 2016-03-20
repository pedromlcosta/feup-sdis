package channels;

import java.io.IOException;
import java.net.InetAddress;

import service.Peer;

public class MDRReceiver extends ReceiverServer {
	Peer user;

	public MDRReceiver(String[] args) throws NumberFormatException, IOException {
		super();
	}

	public MDRReceiver(boolean quitFlag, int serverID, InetAddress addr, int port) {
		super(quitFlag, serverID, addr, port);
	}
}
