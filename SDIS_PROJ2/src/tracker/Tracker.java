package tracker;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import data.PeerData;
import extra.Extra;
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

	public static void main(String[] args) throws IOException{
		// Check if args are all ok and well written
		if (args.length != 1) {
			System.out.println("Proper argument usage is: <srvc_port>");
			System.exit(0);
		} else {
			
			if(!Extra.isNumeric(args[0])){
				System.out.println("<srvc_port> must be an integer.");
				System.exit(0);
			}
			
			// Call the server
			try{
				instance = new Tracker(Integer.parseInt(args[0]));
			}catch (IOException e){
				return;
			}
			
			instance.serverStart();
		}		
	}
	
	public Tracker(InetAddress addrbackup, int backupPort, InetAddress addrPeer, int portPeer, InetAddress addrMonitor, int portMonitor, boolean backupFlag, boolean activeFlag) throws IOException {
		
	}
	
	public Tracker(int port) throws IOException {
		serverSocket = new ServerSocket(port); // PORT 444 JUST TO TEST
	}

	/*
	 * Starts accepting connections
	 */
	public void serverStart(){
		
		while(!serverSocket.isClosed()){
			
			Socket remoteSocket;
			try {
				remoteSocket = serverSocket.accept();
				System.out.println("Accepted new connection. Waiting for messages.");
			} catch (IOException e) {
				continue;
			}
			
			Thread serverListener;
			try {
				serverListener = new ServerListener(remoteSocket);
			} catch (IOException e) {
				continue;
			}
			
			serverListener.start();
			
		}
		
	}
	
	public HashMap<Integer, PeerData> getPeerDataList() {
		return peerDataList;
	}

	public void setPeerDataList(HashMap<Integer, PeerData> peerDataList) {
		this.peerDataList = peerDataList;
	}

	public HashMap<Integer, Monitor> getMonitorList() {
		return monitorList;
	}

	public void setMonitorList(HashMap<Integer, Monitor> monitorList) {
		this.monitorList = monitorList;
	}
}
