package tracker;

//TODO definir melhor os intervalos de envio de keys

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.ProcessBuilder.Redirect;
import java.net.ServerSocket;
import java.net.Socket;
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
	final static String pathToStorage = System.getProperty("user.dir");
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
	// Record of Peers
	private HashMap<Integer, PeerData> peerDataList;
	private static String dataPath;
	private HashMap<Integer, ServerListener> listeners;
	private static int listenerIDgenerator = 1;

	// monitor variables
	private static boolean connectionAlive = false;
	private static final int LIMIT_OF_ATTEMPTS = 3;
	private static PrintWriter bout = null;
	private static BufferedReader bin = null;
	private static boolean monitorAlive = false;
	private static boolean monitorResurrectedAttempted = false;
	private static int nTries = 0;
	private static String[] trackerMainArgs;

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
			trackerMainArgs = args;
			// Call the server
			try {
				instance = new Tracker(Integer.parseInt(args[0]));
			} catch (IOException e) {
				return;
			} catch (NumberFormatException e) {
				e.printStackTrace();
				return;
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
				return;
			}

			(new MonitorProcess()).start();
			instance.serverStart();
		}
	}

	public Tracker(int port) throws IOException, NoSuchAlgorithmException {

		listeners = new HashMap<Integer, ServerListener>();
		generatePeerEncryptionKey();
		setSystemProperties();
		sendKeys();

		this.port = port;
		try {
			SSLServerSocketFactory serverSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory
					.getDefault();
			sslServerSocket = (SSLServerSocket) serverSocketFactory
					.createServerSocket(port);
		} catch (IOException e) {
			System.out
					.println("Couldn't create socket. Check key files and/or try another port.");
			throw e;
			// e.printStackTrace();
		}

		sslServerSocket.setNeedClientAuth(true);
		dataPath = "TRACKER" + File.separator + "PeerData";
	}

	private void sendKeys() {

		Thread t = new Thread() {

			private long TIMER = TimeUnit.MINUTES
					.toMillis(MINUTES_REDISTRIBUTE_KEY);
			private boolean run = true;

			public void run() {

				while (run) {
					try {

						Thread.sleep(TIMER);
						generatePeerEncryptionKey();

						for (Entry<Integer, ServerListener> entry : listeners
								.entrySet()) {
							ServerListener sl = entry.getValue();
							sl.sendKey(getKey());
						}

					} catch (InterruptedException e) {
						System.out
								.println("Error in waiting between key messages delivering");
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
		keyGen.init(128, random);
		peerEncryptionKey = keyGen.generateKey();

		// System.out.println(Base64.getEncoder().encodeToString(peerEncryptionKey.getEncoded()));
		// System.out.println(peerEncryptionKey.getEncoded().length);
	}

	private void setSystemProperties() {
		String storeFileName = pathToStorage + "/" + keyStoreFile;
		String trustFileName = pathToStorage + "/" + trustStoreFile;

		System.setProperty("javax.net.ssl.keyStore", storeFileName);
		System.setProperty("javax.net.ssl.keyStorePassword", passwd);
		System.setProperty("javax.net.ssl.trustStore", trustFileName);
		System.setProperty("javax.net.ssl.trustStorePassword", passwd);
		if (debug)
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
				System.out
						.println("Accepted new connection. Waiting for messages.");
			} catch (IOException e) {
				continue;
			}
			ServerListener serverListener;
			try {
				serverListener = new ServerListener(remoteSocket, instance,
						listenerIDgenerator);
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

	public boolean store(String peerID, byte[] body) {

		String dirPath = "";

		try {
			dirPath = Extra.createDirectory(dataPath);

			File file = new File(dirPath + File.separator + peerID
					+ "_PeerData.dat");
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

			File file = new File(dirPath + File.separator + peerID
					+ "_PeerData.dat");
			if (file.exists())
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

	public static class MonitorProcess extends Thread {
		public void run() {

			System.out.println("Entered Task Thread");
			int beepServerPort = 4445;
			boolean portEmpty = false;
			while (!portEmpty) {
				try { // SEE IF THIS WORKS
					System.out.println("Trying port " + beepServerPort + "\n");
					ServerSocket serverSocket = new ServerSocket(beepServerPort);
					System.out.println("creating Monitor process");
					createMonitorProcess(beepServerPort, trackerMainArgs);
					System.out.println("Monitor process created");
					Socket clientSocket = serverSocket.accept();
					System.out.println("client accepted | Created sockets");
					portEmpty = true;
					// Monitor monitor = new Monitor(beepServerPort); // NOT
					// THIS:
					// MUST CREATE A PROCESS
					bout = new PrintWriter(clientSocket.getOutputStream(), true);
					bin = new BufferedReader(new InputStreamReader(
							clientSocket.getInputStream()));
					System.out.println("created in and out streams\n");
					connectionAlive = true;
				} catch (IOException e) {
					beepServerPort++;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			try {
				System.out.println("start listening");
				String fromTracker, fromMonitor;
				Thread.sleep(4000);
				while (connectionAlive) {
					if (bin.ready()) { // buffer has something
						if ((fromMonitor = bin.readLine()) != null) {
							System.out.println("received: " + fromMonitor);
							fromTracker = "TRACKER_BEEP";
							Thread.sleep(1000);
							monitorAlive = true;
							bout.println(fromTracker);
							System.out.println("sent: " + fromTracker);
						} else
							System.out.println("null readline");
					} else {
						nTries++;
						monitorAlive = false;
						int triesLeft = LIMIT_OF_ATTEMPTS - nTries;
						System.out.println("Trying to reconect " + triesLeft
								+ "more time(s)");
						Thread.sleep(500);
					}

					if (monitorAlive) {
						System.out.println("Monitor alive");
						nTries = 0;
						monitorAlive = false;
						monitorResurrectedAttempted = false;
					} else {
						if (monitorResurrectedAttempted) {
							monitorResurrectedAttempted = false;
							System.out.println("failed To Ressurect Monitor\n");
							return;
							// Action to take??
						} else {
							if (nTries >= LIMIT_OF_ATTEMPTS) {
								monitorAlive = false;
								monitorResurrectedAttempted = true;
								System.out
										.println("attempting ressurection \nCreating new Monitor process");
								createMonitorProcess(beepServerPort,
										trackerMainArgs);
								monitorAlive = true;
								// attemptMonitorResurrection();
							}
						}
					}
					Thread.sleep(1000);
				}
				System.out.println("finished listening");
			} catch (IOException e) {
				connectionAlive = false;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Port nr: " + beepServerPort);
			System.out.println("Finish main");
		}
	}

	public static void createMonitorProcess(int beepPort, String[] args)
			throws IOException, InterruptedException {
		String javaHome = System.getProperty("java.home");
		String javaBin = javaHome + File.separator + "bin" + File.separator
				+ "java";
		String classpath = System.getProperty("java.class.path");
		Class monitorClass = service.Monitor.class;
		String className = monitorClass.getCanonicalName();
		ProcessBuilder builder;
		String beepPORT = beepPort + "";
		builder = new ProcessBuilder(javaBin, "-cp", classpath, className,
				"TRACKER", beepPORT, args[0]);

		File peerDirectory = new File(System.getProperty("user.dir")
				+ File.separator + "logs");
		File fileDirectory = new File(peerDirectory, "monitor_logs");
		if (!fileDirectory.exists())
			fileDirectory.mkdirs();
		String fileName = "tracker_monitor_log";
		File oldlog = new File(fileDirectory, fileName);
		oldlog.delete();
		File log = new File(fileDirectory, fileName);


		builder.redirectErrorStream(true);
		builder.redirectOutput(Redirect.appendTo(log));
		Process process = builder.start();
		assert builder.redirectOutput().file() == log;
		// process.waitFor();
		// return process.exitValue();
	}
}
