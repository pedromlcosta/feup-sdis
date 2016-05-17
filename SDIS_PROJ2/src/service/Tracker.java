package service;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;

import channels.MulticastServer;
import messages.Message;

public class Tracker extends Thread {
	private MulticastServer monitorConnection;
	private MulticastServer peerConnection;
	private MulticastServer backupConnection;
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
	// ID of files deleted by the peers
	private ArrayList<String> deletedFiles;

	public Tracker(InetAddress addrbackup, int backupPort, InetAddress addrPeer, int portPeer, InetAddress addrMonitor, int portMonitor, boolean backupFlag, boolean activeFlag) throws IOException {
		// multicastGroup to listen to the peers
		this.peerConnection = new MulticastServer(false, addrPeer, portPeer);
		this.peerConnection.createSocket();
		this.peerConnection.joinMulticastGroup();
		// multicastGroup for the monitor
		this.monitorConnection = new MulticastServer(false, addrMonitor, portMonitor);
		this.monitorConnection.createSocket();
		this.monitorConnection.joinMulticastGroup();
		// multicastGroup for the backup
		this.monitorConnection = new MulticastServer(false, addrbackup, backupPort);
		this.monitorConnection.createSocket();
		this.monitorConnection.joinMulticastGroup();
		this.backupFlag = backupFlag;
		// should only the main one read the network and send X in X the info?
		this.activeFlag = activeFlag;
		this.backup = new Tracker(addrbackup, backupPort, addrPeer, portPeer, addrMonitor, portMonitor, true, false);

	}

	public synchronized void handleDelete(Message msg) {
		String id = msg.getFileId();
		if (id != null && !id.isEmpty()) {
			if (!deletedFiles.contains(id))
				deletedFiles.add(id);
		}
	}

	public synchronized boolean checkIfFileDeleted(String IDToCheck) {
		for (String check : deletedFiles)
			if (check.equals(IDToCheck))
				return true;
		return false;
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

	public ArrayList<String> getDeletedFiles() {
		return deletedFiles;
	}

	public void setDeletedFiles(ArrayList<String> deletedFiles) {
		this.deletedFiles = deletedFiles;
	}

	public MulticastServer getBackupConnection() {
		return backupConnection;
	}

	public void setBackupConnection(MulticastServer backupConnection) {
		this.backupConnection = backupConnection;
	}

}
