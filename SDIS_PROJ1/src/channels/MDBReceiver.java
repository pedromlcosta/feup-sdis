package channels;

import java.io.IOException;
import java.net.InetAddress;

import service.Peer;

public class MDBReceiver extends ReceiverServer {
	Peer user;

	public MDBReceiver(String[] args) throws NumberFormatException, IOException {
		super();
	}

	public MDBReceiver(boolean quitFlag, int serverID, InetAddress addr, int port) {
		super(quitFlag, serverID, addr, port);
	}
}
