package service;

import java.io.IOException;
import java.net.InetAddress;

import channels.MulticastServer;

public class Tracker {
	private MulticastServer monitorConnection;
	private MulticastServer peerConnection;

	public Tracker(InetAddress addrPeer, int portPeer, InetAddress addrMonitor, int portMonitor) throws IOException {
		super();
		this.peerConnection = new MulticastServer(false, addrPeer, portPeer);
		this.peerConnection.createSocket();
		this.peerConnection.joinMulticastGroup();
		this.monitorConnection = new MulticastServer(false, addrMonitor, portMonitor);
		this.monitorConnection.createSocket();
		this.monitorConnection.joinMulticastGroup();

	}

	public MulticastServer getMonitorConnection() {
		return monitorConnection;
	}

	public void setMonitorConnection(MulticastServer monitorConnection) {
		this.monitorConnection = monitorConnection;
	}

	public MulticastServer getPeerConnection() {
		return peerConnection;
	}

	public void setPeerConnection(MulticastServer peerConnection) {
		this.peerConnection = peerConnection;
	}

}
