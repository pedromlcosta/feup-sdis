package channels;

import java.io.IOException;
import java.net.InetAddress;

import service.Peer;
import service.Server;

public class MDB extends Server {
	Peer user;

	public MDB(String[] args) throws NumberFormatException, IOException {
		super();
	}

	public MDB(boolean quitFlag, int serverID, InetAddress addr, int port) {
		super(quitFlag, serverID, addr, port);
	}
}
