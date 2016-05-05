package service;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;

import channels.MulticastServer;

public class Tracker extends Thread {
	private MulticastServer monitorConnection;
	private MulticastServer peerConnection;
	private Tracker backup;
	private boolean backupFlag;
	private boolean activeFlag;
	// Record of monitors
	private HashMap<Integer, Monitor> monitorList;
	// Record of Peers
	private HashMap<Integer, PeerData> peerDataList;
	// probation Peer
	private HashMap<Integer, PeerData> probationPeerList;
	// probation Monitor
	private HashMap<Integer, Monitor> probationMonitorList;

	public Tracker(InetAddress addrPeer, int portPeer, InetAddress addrMonitor, int portMonitor, boolean backupFlag, boolean activeFlag) throws IOException {
		super();
		this.peerConnection = new MulticastServer(false, addrPeer, portPeer);
		this.peerConnection.createSocket();
		this.peerConnection.joinMulticastGroup();
		this.monitorConnection = new MulticastServer(false, addrMonitor, portMonitor);
		this.monitorConnection.createSocket();
		this.monitorConnection.joinMulticastGroup();
		this.backupFlag = backupFlag;
		//should only the main one read the network and send X in X the info?
		this.activeFlag = activeFlag;
		this.backup = new Tracker(addrPeer, portPeer, addrMonitor, portMonitor, true, false);

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

	public Tracker getBackup() {
		return backup;
	}

	public void setBackup(Tracker backup) {
		this.backup = backup;
	}

	public boolean isBackupFlag() {
		return backupFlag;
	}

	public void setBackupFlag(boolean backupFlag) {
		this.backupFlag = backupFlag;
	}

	public boolean isActiveFlag() {
		return activeFlag;
	}

	public void setActiveFlag(boolean activeFlag) {
		this.activeFlag = activeFlag;
	}

	public HashMap<Integer, Monitor> getMonitorList() {
		return monitorList;
	}

	public void setMonitorList(HashMap<Integer, Monitor> monitorList) {
		this.monitorList = monitorList;
	}

	public HashMap<Integer, PeerData> getPeerDataList() {
		return peerDataList;
	}

	public void setPeerDataList(HashMap<Integer, PeerData> peerDataList) {
		this.peerDataList = peerDataList;
	}

	public HashMap<Integer, PeerData> getProbationPeerList() {
		return probationPeerList;
	}

	public void setProbationPeerList(HashMap<Integer, PeerData> probationPeerList) {
		this.probationPeerList = probationPeerList;
	}

	public HashMap<Integer, Monitor> getProbationMonitorList() {
		return probationMonitorList;
	}

	public void setProbationMonitorList(HashMap<Integer, Monitor> probationMonitorList) {
		this.probationMonitorList = probationMonitorList;
	}

}
