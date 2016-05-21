package tracker;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

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

	// Final Data fields
	final static String pathToStorage = System.getProperty("user.dir") + "\\storage\\";
	final static String keyStoreFile = "server.keys";
	final static String passwd = "123456";
	
	// Normal Data fields
	boolean debug = false;
	boolean serverEnd = false;
	int port;
	SSLServerSocket sslServerSocket;
	// Record of monitors
	private HashMap<Integer, Monitor> monitorList;
	// Record of Peers
	private HashMap<Integer, PeerData> peerDataList;

	public static void main(String[] args) throws IOException {
		// Check if args are all ok and well written
		if (args.length != 1) {
			System.out.println("Proper argument usage is: <srvc_port>");
			System.exit(0);
		} else {

			if (!Extra.isNumeric(args[0])) {
				System.out.println("<srvc_port> must be an integer.");
				System.exit(0);
			}

			// Call the server
			try {
				instance = new Tracker(Integer.parseInt(args[0]));
			} catch (IOException e) {
				return;
			}
			
			instance.serverStart();
		}
	}

	public Tracker(InetAddress addrbackup, int backupPort,
			InetAddress addrPeer, int portPeer, InetAddress addrMonitor,
			int portMonitor, boolean backupFlag, boolean activeFlag)
			throws IOException {

	}

	public Tracker(int port) throws IOException {
		
		String trustFileName = pathToStorage + "/" + keyStoreFile;
		System.setProperty("javax.net.ssl.keyStore", "server.keys");
		System.setProperty("javax.net.ssl.keyStorePassword", "123456");
		System.setProperty("javax.net.ssl.trustStore", "truststore");
		System.setProperty("javax.net.ssl.trustStorePassword", "123456");
		if(debug)
			System.setProperty("javax.net.debug", "all");
		
		this.port = port;
		
		SSLServerSocketFactory serverSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory
				.getDefault();
		sslServerSocket = (SSLServerSocket) serverSocketFactory
				.createServerSocket(port);

		sslServerSocket.setNeedClientAuth(true);
		
	}

	/*
	 * Starts accepting connections
	 */
	public void serverStart() {

		/*
		for (String s : sslServerSocket.getEnabledCipherSuites())
        {
            System.out.println(s);
        }
        */
		
		while (!sslServerSocket.isClosed()) {

			SSLSocket remoteSocket;

			try {
				remoteSocket = (SSLSocket) sslServerSocket.accept();
				System.out
						.println("Accepted new connection. Waiting for messages.");
			} catch (IOException e) {
				continue;
			}
			
			for (String s : sslServerSocket.getEnabledCipherSuites())
	        {
	            System.out.println("serverSocket: " + s);
	        }
			
			for (String s : remoteSocket.getEnabledCipherSuites())
	        {
	            System.out.println("remoteSocket: " + s);
	        }

			Thread serverListener;
			try {

				// System.out.println("HandShake went well?");
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
