package channels;

import java.io.IOException;
import java.net.InetAddress;


public class MCReceiver extends ReceiverServer {

	public MCReceiver(String[] args) throws NumberFormatException, IOException {
		super();
	}

	public MCReceiver(boolean quitFlag, int serverID, InetAddress addr, int port) {
		super(quitFlag, serverID, addr, port);
	}
}
