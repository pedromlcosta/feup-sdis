package service;

import java.io.IOException;
import java.net.InetAddress;

import channels.MulticastServer;

public class Tracker {
	private MulticastServer monitorConnection;
	private MulticastServer peerConnection;
	private Tracker backup;
	private boolean backupFlag;
	private boolean activeFlag;

	public Tracker(InetAddress addrPeer, int portPeer, InetAddress addrMonitor, int portMonitor, boolean backupFlag, boolean activeFlag) throws IOException {
		super();
		this.peerConnection = new MulticastServer(false, addrPeer, portPeer);
		this.peerConnection.createSocket();
		this.peerConnection.joinMulticastGroup();
		this.monitorConnection = new MulticastServer(false, addrMonitor, portMonitor);
		this.monitorConnection.createSocket();
		this.monitorConnection.joinMulticastGroup();
		this.backupFlag = backupFlag;
		this.activeFlag = activeFlag;

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

}
