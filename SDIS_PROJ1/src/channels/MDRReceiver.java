package channels;

import java.io.IOException;
import java.net.InetAddress;

import service.Peer;
import service.Server;

public class MDRReceiver extends Server {
	Peer user;

	public MDRReceiver(String[] args) throws NumberFormatException, IOException {
		super();
	}

	public MDRReceiver(boolean quitFlag, int serverID, InetAddress addr, int port) {
		super(quitFlag, serverID, addr, port);
	}
}
