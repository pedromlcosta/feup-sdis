package channels;

import java.io.IOException;
import java.net.InetAddress;

import service.Peer;
import service.Server;

public class MC extends Server {
	Peer user;

	public MC(String[] args) throws NumberFormatException, IOException {
		super();
	}

	public MC(boolean quitFlag, int serverID, InetAddress addr, int port) {
		super(quitFlag, serverID, addr, port);
	}
}
