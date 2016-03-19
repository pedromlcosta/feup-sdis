package channels;

import java.io.IOException;
import java.net.InetAddress;

import service.Peer;
import service.Server;

public class MDR extends Server {
	Peer user;

	public MDR(String[] args) throws NumberFormatException, IOException {
		super();
	}

	public MDR(boolean quitFlag, int serverID, InetAddress addr, int port) {
		super(quitFlag, serverID, addr, port);
	}
}
