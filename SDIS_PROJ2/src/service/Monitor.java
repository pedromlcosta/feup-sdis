package service;

import java.io.IOException;
import java.net.InetAddress;

import channels.MulticastServer;
import channels.UDPConnection;

public class Monitor {
	private UDPConnection peerConnection;
	// TODO não vai ser esta mas para questões de estrutura vou deixar aqui
	private MulticastServer trackerConnection;

	public Monitor(InetAddress addrUDP, int portUDP, InetAddress addrMC, int portMC) throws IOException {
		super();
		this.peerConnection = new UDPConnection(addrUDP, portUDP);
		this.trackerConnection = new MulticastServer(false, addrMC, portMC);
		this.trackerConnection.createSocket();
		this.trackerConnection.joinMulticastGroup();

	}

	public UDPConnection getPeerConnection() {
		return peerConnection;
	}

	public void setPeerConnection(UDPConnection peerConnection) {
		this.peerConnection = peerConnection;
	}

	public MulticastServer getTrackerConnection() {
		return trackerConnection;
	}

	public void setTrackerConnection(MulticastServer trackerConnection) {
		this.trackerConnection = trackerConnection;
	}

}
