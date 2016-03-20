package channels;

import java.io.IOException;
import java.net.InetAddress;

import service.Peer;
import service.Server;

public class MDBReceiver extends Server {
	Peer user;

	public MDBReceiver(String[] args) throws NumberFormatException, IOException {
		super();
	}

	public MDBReceiver(boolean quitFlag, int serverID, InetAddress addr, int port) {
		super(quitFlag, serverID, addr, port);
	}
}
