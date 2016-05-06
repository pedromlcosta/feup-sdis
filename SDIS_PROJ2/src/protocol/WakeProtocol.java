package protocol;

import service.Peer;

public class WakeProtocol extends Thread {
	// needs Access to Peer
	// will go through Peer Data and send the WakeUpMessages
	private Peer peer = Peer.getInstance();

	public Peer getPeer() {
		return peer;
	}

	public void run() {
	
	}

	public void setPeer(Peer peer) {
		this.peer = peer;
	}

	public void receiveWakeUp() {
		//Received a wakeUp 
		//Need to check if chunk exists
		//if so send msg
		//if not ignore
	}

	public void sendWakeUp() {
		//what do I need for a wakeup msg 
		//version 1.0? or higher 
		//need senderID
		//need chunkID
		//need chunkNo
		//need fileID
		//No bodies 
	}

}
