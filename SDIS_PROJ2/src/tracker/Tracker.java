package tracker;

//TODO definir melhor os intervalos de envio de keys

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
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
	final static String pathToStorage = System.getProperty("user.dir") ;
	final static String keyStoreFile = "server.keys";
	final static String trustStoreFile = "truststore";
	final static String passwd = "123456";
	final static boolean debug = false;
	final static int MINUTES_REDISTRIBUTE_KEY = 120;
	
	// Normal Data fields
	boolean serverEnd = false;
	int port;
	SSLServerSocket sslServerSocket;
	SecretKey peerEncryptionKey;
	// Record of monitors
	private HashMap<Integer, Monitor> monitorList;
	// Record of Peers
	private HashMap<Integer, PeerData> peerDataList;
	private static String dataPath;
	private HashMap<Integer,ServerListener> listeners;
	private static int listenerIDgenerator = 1;
	
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
			} catch (NumberFormatException e){ 
				e.printStackTrace();
				return;
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
				return;
			}
			
			instance.serverStart();
		}
	}

	public Tracker(int port) throws IOException, NoSuchAlgorithmException {
		
		listeners = new HashMap<Integer,ServerListener>();
		generatePeerEncryptionKey();
		setSystemProperties();
		sendKeys();
		
		this.port = port;
		try{
			SSLServerSocketFactory serverSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
			sslServerSocket = (SSLServerSocket) serverSocketFactory.createServerSocket(port);
		}
		catch(IOException e){
			System.out.println("Couldn't create socket. Check key files and/or try another port.");
			throw e;
			//e.printStackTrace();
		}
		
		sslServerSocket.setNeedClientAuth(true);
		dataPath = "TRACKER" + File.separator + "PeerData";
	}

	private void sendKeys() {
		
		Thread t = new Thread(){
			
			private long TIMER = TimeUnit.MINUTES.toMillis(MINUTES_REDISTRIBUTE_KEY);
			private boolean run = true;
			
			public void run() {
		        
				while(run){
					try {
			
						Thread.sleep(TIMER);
						generatePeerEncryptionKey();
						
				        for(Entry<Integer, ServerListener> entry: listeners.entrySet()){
				        	ServerListener sl = entry.getValue();
				        	sl.sendKey(getKey());
				        }
				       
				        
					} catch (InterruptedException e) {
						System.out.println("Error in waiting between key messages delivering");
						run = false;
					} catch (NoSuchAlgorithmException e) {
						System.out.println("Error creating new key");
					}
				}      	
		    }
		};
		
		t.start();
	}

	private void generatePeerEncryptionKey() throws NoSuchAlgorithmException {
		
		KeyGenerator keyGen = KeyGenerator.getInstance("AES");
	    SecureRandom random = new SecureRandom(); // cryptograph. secure random 
	    keyGen.init(128,random); 
	    peerEncryptionKey = keyGen.generateKey();
	    
	    //System.out.println(Base64.getEncoder().encodeToString(peerEncryptionKey.getEncoded()));
	    //System.out.println(peerEncryptionKey.getEncoded().length);
	}

	private void setSystemProperties() {
		String storeFileName = pathToStorage + "/" + keyStoreFile;
		String trustFileName = pathToStorage + "/" + trustStoreFile;
		
		System.setProperty("javax.net.ssl.keyStore", storeFileName);
		System.setProperty("javax.net.ssl.keyStorePassword", passwd);
		System.setProperty("javax.net.ssl.trustStore", trustFileName);
		System.setProperty("javax.net.ssl.trustStorePassword", passwd);
		if(debug)
			System.setProperty("javax.net.debug", "all");
	}

	/*
	 * Starts accepting connections
	 */
	public void serverStart() {

		while (!sslServerSocket.isClosed()) {

			SSLSocket remoteSocket;

			try {
				remoteSocket = (SSLSocket) sslServerSocket.accept();
				System.out.println("Accepted new connection. Waiting for messages.");
			} catch (IOException e) {
				continue;
			}
			ServerListener serverListener;
			try {
				serverListener = new ServerListener(remoteSocket, instance, listenerIDgenerator);
				listeners.put(listenerIDgenerator, serverListener);
				listenerIDgenerator++;
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

	public boolean store(String peerID, byte[] body) {
		
		String dirPath = "";

		try {
			dirPath = Extra.createDirectory(dataPath);
			
			File file = new File(dirPath + File.separator + peerID + "_PeerData.dat");
			FileOutputStream outputStream = new FileOutputStream(file);
			outputStream.write(body);
			outputStream.close();
			return true;
		} catch (IOException e1) {
			System.out.println(e1.getMessage() + " Couldn't create directory.");
		}
		
		return false;
	}

	public byte[] getPeerData(String peerID) {
		
		String dirPath = "";

		try {
			dirPath = Extra.createDirectory(dataPath);
			
			File file = new File(dirPath + File.separator + peerID + "_PeerData.dat");
			if(file.exists())
				return Files.readAllBytes(file.toPath());
		} catch (IOException e1) {
			System.out.println(e1.getMessage() + " Couldn't create directory.");
		}
		
		return null;
	}

	public byte[] getKey() {
		
		return peerEncryptionKey.getEncoded();
	}

	public void removeListener(int id) {
		
		listeners.remove(id);
	}
}
