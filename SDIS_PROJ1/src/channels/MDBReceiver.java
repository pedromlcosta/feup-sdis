package channels;

import java.io.IOException;
import java.net.InetAddress;

public class MDBReceiver extends ReceiverServer {

	public MDBReceiver(){
		
	}
	
	public MDBReceiver(String[] args) throws NumberFormatException, IOException {
		super();
	}

	public MDBReceiver(boolean quitFlag, int serverID, InetAddress addr, int port) {
		super(quitFlag, serverID, addr, port);
	}
}
