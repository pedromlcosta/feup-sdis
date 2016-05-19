package tracker;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import protocol.RestoreProtocol;
import data.PeerData;
import messages.Message;
import monitor.Monitor;

public class Tracker extends Thread {
	/**
	 * Creates the Tracker singleton
	 */
	static Tracker instance;

	
	/**
	 * 
	 * @return the Tracker singleton
	 */
	public static Tracker getInstance() {
		return instance;
	}

	boolean serverEnd = false;
	ServerSocket serverSocket;
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

	public static void main(String[] args) throws IOException{
		// Check if args are all ok and well written
		// Check if args are all ok and well written
		if (args.length != 1) {
			System.out.println("Proper Usage is: java Server <srvc_port>");
			System.exit(0);
		} else {
			// Call the server
			try{
				instance = new Tracker();
			}catch (IOException e){
				return;
			}
			
			instance.serverStart();
		}		
	}
	
	public Tracker(InetAddress addrbackup, int backupPort, InetAddress addrPeer, int portPeer, InetAddress addrMonitor, int portMonitor, boolean backupFlag, boolean activeFlag) throws IOException {
		
	}
	
	public Tracker() throws IOException {
		serverSocket = new ServerSocket(444); // PORT 444 JUST TO TEST
	}

	public void serverStart(){
		
		
		while(!serverSocket.isClosed()){
			
			Socket remoteSocket;
			try {
				remoteSocket = serverSocket.accept();
			} catch (IOException e) {
				continue;
			}
			
			Thread serverListener = new ServerListener(remoteSocket);
			serverListener.start();
			
		}
		
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

	public ArrayList<String> getDeletedFiles() {
		return deletedFiles;
	}

	public void setDeletedFiles(ArrayList<String> deletedFiles) {
		this.deletedFiles = deletedFiles;
	}


}
