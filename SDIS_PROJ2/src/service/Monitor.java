package service;

import java.io.IOException;
import java.net.InetAddress;

import channels.MulticastServer;
import channels.UDPConnection;

public class Monitor extends Thread {
	private UDPConnection peerConnection;
	private MulticastServer trackerConnection;
	private Integer peerID;

	public Monitor(InetAddress addrUDP, int portUDP, InetAddress addrMC, int portMC) throws IOException {
		super();
		this.peerConnection = new UDPConnection(addrUDP, portUDP);
		this.trackerConnection = new MulticastServer(false, addrMC, portMC);
		this.trackerConnection.createSocket();
		this.trackerConnection.joinMulticastGroup();

	}

	public void run() {
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

	public Integer getPeerID() {
		return peerID;
	}

	public void setPeerID(Integer peerID) {
		this.peerID = peerID;
	}

}
