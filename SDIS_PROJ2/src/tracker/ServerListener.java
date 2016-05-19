package tracker;

import java.net.Socket;

public class ServerListener extends Thread{
	
	private Socket remoteSocket;
	
	public ServerListener(Socket remoteSocket){
		this.setRemoteSocket(remoteSocket);
	}

	public Socket getRemoteSocket() {
		return remoteSocket;
	}

	public void setRemoteSocket(Socket remoteSocket) {
		this.remoteSocket = remoteSocket;
	}
}
