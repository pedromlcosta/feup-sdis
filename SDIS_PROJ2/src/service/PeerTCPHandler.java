package service;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.Arrays;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import messages.Message;
import data.PeerData;
import extra.Extra;
import extra.FileHandler;

public class PeerTCPHandler extends Thread {

	// Final Data fields
	final static String pathToStorage = System.getProperty("user.dir") + "\\storage\\";
	final static String keyStoreFile = "client.keys";
	final static String passwd = "123456";
	final boolean debug = false;

	private Peer peerInstance;

	private int serverPort;
	private InetAddress serverAddress;
	private SSLSocket remoteSocket;

	private ByteArrayOutputStream os;
	private DataInputStream in;
	private DataOutputStream out;
	private byte[] messageByte;

	PeerTCPHandler(Peer peer, InetAddress serverAddress, int port) {
		setSystemProperties();

		peerInstance = peer;
		this.serverAddress = serverAddress;
		this.serverPort = port;

		os = new ByteArrayOutputStream();
		messageByte = new byte[64000];
	}

	public void initializeConnection() throws IOException {
		// Initialize socket
		SSLSocketFactory socketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
		remoteSocket = (SSLSocket) socketFactory.createSocket(serverAddress, serverPort);

		// Initialize streams
		in = new DataInputStream(remoteSocket.getInputStream());
		out = new DataOutputStream(remoteSocket.getOutputStream());
	}

	public void run() {
		try {
			
			while (!remoteSocket.isClosed()) {
				// RECEIVE SERVER MESSAGE

				// TODO: Needs synchronized or something?
				
				int bytesRead = in.read(messageByte);
				String answer = new String(messageByte, 0, bytesRead);
				processServerMessage(answer, bytesRead);

			}
			System.out.println("Socket was closed.");

		} catch (Exception e) {
			// Close stuff
			try {
				close();
			} catch (IOException e1) {
				System.out.println("Failed at closing streams. They are already closed, maybe socket was closed?");
			}
			e.printStackTrace();
			System.out.println("Socket was closed.");
			// DO SOCKET CLOSED STUFF HERE.
		}

	}

	private void setSystemProperties() {
		String trustFileName = pathToStorage + "/" + keyStoreFile;

		// TODO: CHANGE TO TRUSTFILENAME AND PASSWD
		System.setProperty("javax.net.ssl.keyStore", "client.keys");
		System.setProperty("javax.net.ssl.keyStorePassword", "123456");
		System.setProperty("javax.net.ssl.trustStore", "truststore");
		System.setProperty("javax.net.ssl.trustStorePassword", "123456");

		if (debug) {
			System.setProperty("javax.net.debug", "all");
		}

	}

	public void close() throws IOException {
		out.close();
		in.close();
		remoteSocket.close();
	}

	private String endHeader() {
		return Message.EOL + Message.EOL;
	}

	public void sendData() {

		byte[] message = null;
		// prepare message
		try {
			os.write(("STORE" + " " + peerInstance.getServerID() + endHeader()).getBytes());
			os.write(peerInstance.getData().getData());
			message = os.toByteArray();
			os.reset();

			// send message
			out.write(message);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}

	}

	public void requestData() {

		// prepare message
		byte[] message = ("DATAREQUEST" + " " + peerInstance.getServerID() + endHeader()).getBytes();

		// send message
		try {
			out.write(message);
		} catch (IOException e1) {
			System.out.println("Error writing to socket");
		}

	}

	public void requestKey() {

		// prepare message
		byte[] message = ("KEYREQUEST" + " " + peerInstance.getServerID() + endHeader()).getBytes();

		// send message
		try {
			out.write(message);
		} catch (IOException e1) {
			System.out.println("Error writing to socket");
		}

	}

	public void verifyPeerData(byte[] peerData) {

		PeerData tmpPeerData = PeerData.getPeerData(peerData);
		if (tmpPeerData != null) {
			if (peerInstance.getData() == null) {
				peerInstance.setData(tmpPeerData);
				return;
			}

			else if (peerInstance.getData().oldest(tmpPeerData)) {
				String dirPath = "";
				try {
					dirPath = Extra.createDirectory(Integer.toString(Peer.getInstance().getServerID()) + File.separator
							+ FileHandler.BACKUP_FOLDER_NAME);
				} catch (IOException e) {
					System.out.println("Couldn't create or use directory");
				}
				peerInstance.getData().cleanupLocal(tmpPeerData, dirPath);
				tmpPeerData.cleanupData(peerInstance.getData(), dirPath);
				peerInstance.setData(tmpPeerData);
			}
			// else keep current peerData
		}
	}

	public void processServerMessage(String request, int length) {

		System.out.println("Request:" + request);

		int index = request.indexOf(endHeader());
		if (index == -1) {
			System.out.println("Tracker answer: No header especified");
			return;
		}

		String header = request.substring(0, index);
		int interval = endHeader().getBytes().length;
		byte[] body = Arrays.copyOfRange(messageByte, index + interval, length);
		String[] tokens = header.split(" ");

		if (tokens[0] != null) {
			switch (tokens[0]) {
			case "STORE":
				if (tokens[1] == null || tokens[1] == "ERROR")
					System.out.println("Error storing peerdata in tracker");
				break;
			case "DATAREQUEST":
				if (tokens[1] == null || tokens[1] == "ERROR")
					System.out.println("Error requesting peerdata in tracker");
				else
					verifyPeerData(body);
				break;
			case "KEYREQUEST":
				if (tokens[1] == null || tokens[1] == "ERROR")
					System.out.println("Error requesting key in tracker");
				else {
					// TODO keySave - key will be in body in byte[]
				}
				break;
			default:
				System.out.println("Tracker answer: Tracker couldn't find request function");
			}
		} else
			System.out.println("Tracker answer: Header format incorrect");
	}

	public void sendMessageToTrackerTest(String message) {
		
		BufferedReader input = null;
		PrintWriter output = null;
				 
		 try {
			input = new BufferedReader(new InputStreamReader(remoteSocket.getInputStream()));
			output = new PrintWriter(remoteSocket.getOutputStream(), true);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		if (!remoteSocket.isClosed()) {

			// SEND CLIENT REQUEST
			System.out.println("Sending message:" + message);
			System.out.println("Peer remotesocket port: " + remoteSocket.getPort());
			try {
				output.println("hi");
				//output.flush();
								
				//remoteSocket.close();
				//out.write(message.getBytes());
			} catch (Exception e) {
				System.out.println("Error writing to out stream.");
			}
			System.out.println("Message sent");

			/*
			// RECEIVE SERVER REPLY
			try {
				String receivedString = input.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// System.out.println("Client received: " + receivedString);
			*/
		}

	}

}
