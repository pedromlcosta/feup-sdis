package service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.NotSerializableException;
import java.io.PrintWriter;
import java.lang.ProcessBuilder.Redirect;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import channels.MCReceiver;
import channels.MDBReceiver;
import channels.MDRReceiver;
import channels.UDPConnection;
import chunk.Chunk;
import chunk.ChunkID;
import data.FileID;
import data.PeerData;
import extra.Extra;
import extra.FileHandler;
import protocol.BackupProtocol;
import protocol.CheckChunksProtocol;
import protocol.DeleteProtocol;
import protocol.ReclaimProtocol;
import protocol.RestoreProtocol;
import protocol.WakeProtocol;

//TODO add sendData to every event saveData?

//SINGLETON SYNCRONIZE ALL THREADS HAVE ACESS TO IT
public class Peer implements Invocation {
	/**
	 * Creates the Peer singleton
	 */
	static Peer instance = new Peer();

	/**
	 * 
	 * @return the Peer singleton
	 */
	public static Peer getInstance() {
		return instance;
	}

	private PeerData data;
	private static boolean connectedTracker = true; // TELLS IF THE PEER IS
													// CONNECTED TO THE TRACKER

	private MCReceiver controlChannel;
	private MDBReceiver dataChannel;
	private MDRReceiver restoreChannel;
	private static Integer serverID;
	private static Registry rmiRegistry;
	private static String rmiName;
	private String folderPath;
	private boolean runningRestart = false;
	private boolean runningCheckChunks = false;
	private boolean runningWakeUp = false;
	// connection to monitor need port since addr will be localhost
	private UDPConnection monitorConnection;

	// Monitor Information
	private static boolean connectionAlive = false;
	private static final String FROM_PEER = "PEER_BEEP";
	private static final String FROM_MONITOR = "MONITOR_BEEP";
	private static final int LIMIT_OF_ATTEMPTS = 3;
	private static PrintWriter bout = null;
	private static BufferedReader bin = null;
	private static boolean monitorAlive = false;
	private static boolean monitorResurrectedAttempted = false;
	private static int nTries = 0;
	private static String[] peerMainArgs;
	private static Process monitorProcess = null;

	// Tracker connection data fields
	private PeerTCPHandler trackerConnection;

	private SecretKey encryptionKey = null;
	static int serverPort;
	static InetAddress serverAddress;

	/**
	 * Default Peer constructor. Initializes receiver servers and PeerData
	 * container.
	 */
	public Peer() {

		controlChannel = new MCReceiver(this);
		dataChannel = new MDBReceiver(this);
		restoreChannel = new MDRReceiver(this);
		data = new PeerData();
	}

	/**
	 * Creates the folder for this peer, if not yet existent
	 * 
	 * @throws IOException
	 */
	public void createPeerFolder() throws IOException {
		folderPath = Extra.createDirectory(Integer.toString(serverID));
	}

	/**
	 * Starts running the peer, validating arguments and registering the RMI
	 * 
	 * @param args
	 *            Peer arguments: <server_id> <MC_addr> <MC_port> <MDB_addr>
	 *            <MDB_port> <MDR_addr> <MDR_port>
	 */
	public static void main(String[] args) {

		Peer peer = Peer.getInstance();
		MCReceiver controlChannel = peer.getControlChannel();
		MDBReceiver dataChannel = peer.getDataChannel();
		MDRReceiver restoreChannel = peer.getRestoreChannel();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					rmiRegistry.unbind(rmiName);

					// UnicastRemoteObject.unexportObject(instance, true);

				} catch (RemoteException | NotBoundException e) {
					System.out.println("Problem unbinding the RMI");
				} catch (Exception e) {

				}
			}
		});

		// Validate args and associate them with the variables

		if (!validArgs(args)) {
			System.exit(0);
		}
		peerMainArgs = args;

		// OPEN TCP TRACKER SOCKET

		rmiName = args[0];
		peer.setServerID(Integer.parseInt(args[0]));
		peer.getData();

		// Attempt to initialize the communication channels
		try {
			controlChannel.setAddr(InetAddress.getByName(args[1]));
			controlChannel.setPort(Integer.parseInt(args[2]));
			controlChannel.createSocket();
			controlChannel.joinMulticastGroup();

			dataChannel.setAddr(InetAddress.getByName(args[3]));
			dataChannel.setPort(Integer.parseInt(args[4]));
			dataChannel.createSocket();
			dataChannel.joinMulticastGroup();

			restoreChannel.setAddr(InetAddress.getByName(args[5]));
			restoreChannel.setPort(Integer.parseInt(args[6]));
			restoreChannel.createSocket();
			restoreChannel.joinMulticastGroup();
		} catch (IOException e) {
			System.out.println("Couldn't bind IP:ports to peer");
			return;
		} catch (Exception e) {
			System.out
					.println("Invalid Args. Ports must be between 1 and 9999 and IP must be a valid multicast address.");
			return;
		}

		try {
			peer.createPeerFolder();
		} catch (IOException e2) {
			return;
		}

		while (connectedTracker) {
			// TCP Handler Initialization
			try {
				System.out.println("Connecting");
				serverAddress = InetAddress.getByName(args[7]);
				int port = Integer.parseInt(args[8]);

				PeerTCPHandler tcpHandler = new PeerTCPHandler(instance,
						serverAddress, port);
				instance.setTrackerConnection(tcpHandler);
				tcpHandler.initializeConnection();
				tcpHandler.start();

				// Reaches this point, is ok
				connectedTracker = false;

			} catch (ConnectException e2) {
				System.out
						.println("Problem connecting to server (wrong address or port?). Will retry in 2 seconds.");

				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} catch (IOException e3) {

				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				System.out
						.println("Problem connecting to server. Error creating input streams. Will retry in 2 seconds.");
				System.out.println("Missing key files?");
			}
		}
		System.out.println("Connected to tracker.");

		peer.getRestoreChannel().start();
		peer.getDataChannel().start();
		peer.getControlChannel().start();

		// LOAD PEER DATA

		try {
			PeerData.setDataPath(peer.getServerID());
			peer.loadData();
		} catch (FileNotFoundException e) {
			System.out
					.println("There wasn't a peerData file, creating one now");
			System.out.println(e.getMessage());
			// e.printStackTrace(); // Remove these stack traces after
			// There wasn't a file, so we're creating one now!
			try {
				peer.saveData();
			} catch (FileNotFoundException e1) {
				System.out.println("FileNotFoundException for saveData");
			} catch (IOException e1) {
				System.out.println("IOException for saveData");
			}
		} catch (NotSerializableException e) {
			System.out.println("PeerData is not Serializable");
		} catch (IOException e) {
			System.out
					.println("IOException while loading PeerData for the first time");
			System.out.println(e.getMessage());
			// e.printStackTrace();
			return;
		} catch (ClassNotFoundException e) {
			System.out.println("PeerData - a Class was not found");
			System.out.println(e.getMessage());
			// e.printStackTrace();
			return;
		}

		peer.trackerConnection.requestKey();

		while (instance.encryptionKey == null) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}

		registerRMI();
		for (ChunkID c : Peer.getInstance().getAnsweredCommand().keySet())
			System.out.println("Size: "
					+ Peer.getInstance().getAnsweredCommand().get(c).size()
					+ "  " + c.getActualRepDegree() + " " + c.getFileID() + "_"
					+ c.getChunkNumber());

		registerRMI();
		for (ChunkID c : Peer.getInstance().getAnsweredCommand().keySet())
			System.out.println("Size: "
					+ Peer.getInstance().getAnsweredCommand().get(c).size()
					+ "  " + c.getActualRepDegree() + " " + c.getFileID() + "_"
					+ c.getChunkNumber());
		if (args.length == 10 && args[9] == "RESTART") {
			try {
				peer.getInstance().restartProtocol();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		(new MonitorProcess()).start();

	}

	/**
	 * Validates the arguments received
	 * 
	 * @param args
	 *            arguments received from main
	 * @return true if they are in a valid from, false otherwise
	 */
	/**
	 * Validates the arguments received
	 * 
	 * @param args
	 *            arguments received from main
	 * @return true if they are in a valid from, false otherwise
	 */
	public static boolean validArgs(String[] args) {

		if (args.length != 9) {
			if ((args.length == 10 && (!args[9].equals("RESTART")))) {
				return true;
			}
			System.out.println(args[9]);
			System.out.println("Incorrect number of args." + " You gave: "
					+ args.length);
			System.out
					.println("Correct usage is: <server_id> <MC_addr> <MC_port> <MDB_addr> <MDB_port> <MDR_addr> <MDR_port> <Tracker_host> <Tracker_port>");
			return false;
		}

		if (!Extra.isNumeric(args[0]) || !Extra.isNumeric(args[2])
				|| !Extra.isNumeric(args[4]) || !Extra.isNumeric(args[6])
				|| !Extra.isNumeric(args[8])) {
			System.out.println("Server ID and ports must be valid numbers");
			System.out
					.println("Correct usage is: <server_id> <MC_addr> <MC_port> <MDB_addr> <MDB_port> <MDR_addr> <MDR_port> <Tracker_host> <Tracker_port>");
			return false;
		}

		return true;

	}

	public void closeConnectionToTracker() {
		try {
			trackerConnection.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// Methods
	/**
	 * registers RMI with the remote name
	 */
	public static void registerRMI() {
		// Create and export object
		try {

			Invocation stub = (Invocation) UnicastRemoteObject.exportObject(
					instance, 0);

			// Register object to rmi registry
			rmiRegistry = LocateRegistry.getRegistry();

			try {
				rmiRegistry.bind(rmiName, stub);
			} catch (Exception e) {
				System.out
						.println("Couldnt bind, try another remote name, this one is in use");
				e.printStackTrace();
			}

		} catch (Exception e) {
			System.out.println("Unexpected Exception");
			e.printStackTrace();
		}
	}

	/**
	 * Backup function for the RMI call - starts the backup protocol
	 */
	@Override
	public synchronized String backup(String filePath, int desiredRepDegree)
			throws RemoteException {
		// O dispatcher vai ter as cenas do socket necessarias
		// e os metodos para enviar para os canais que queremos as cenas

		// Call backup protocol through dispatcher
		System.out.println(desiredRepDegree);
		new BackupProtocol(filePath, desiredRepDegree, "1.0",
				Peer.getInstance()).start();

		System.out.println("backup called");
		return "backup sent";
	}

	/**
	 * Restore function for the RMI call - starts the restore protocol
	 */
	@Override
	public String restore(String filePath) throws RemoteException {
		// Call restore protocol

		Thread restore = new RestoreProtocol(filePath);
		restore.start();
		System.out.println("restore called");
		return "restore sent";
	}

	/**
	 * Delete function for the RMI call - starts the delete protocol
	 */
	@Override
	public synchronized String delete(String filePath) throws RemoteException {
		// Call delete protocol
		System.out.println("Before calling delete");
		(new DeleteProtocol(filePath)).start();
		System.out.println("delete called");
		return "delete sent";
	}

	/**
	 * Reclaim function for the RMI call - starts the reclaim protocol
	 */
	@Override
	public String reclaim(int reclaimSpace) throws RemoteException {
		// Call reclaim protocol
		Thread reclaim = new ReclaimProtocol(reclaimSpace);
		reclaim.start();
		System.out.println("reclaim called");
		return "reclaim sent";
	}

	// TODO 2 instances when restart can be called
	// actually just one, after we ask for PD ( PeerData ) from tracker we will
	// check
	// if they match if not give prioraty to local peerdata
	// and sent it to tracker and start the restart process
	// if no local PD we use the PD tracker gave us

	public String wakeUp() throws RemoteException {

		if (isRunningWakeUp() || isRunningRestart()) {
			System.out.println("WakeUp Called");
			Thread wakeup = new WakeProtocol();
			wakeup.start();
			return "WakeUp";
		} else
			return "Already running WakeUp or Restart";

	}

	public String checkChunks() throws RemoteException {

		if (isRunningCheckChunks() || isRunningRestart()) {
			System.out.println("Check Chunks Called");
			resetChunkData();
			Thread checkChunks = new CheckChunksProtocol();
			checkChunks.start();
			return "check Chunks";
		} else
			return "Already running CheckChunks or Restart";
	}

	public String restartProtocol() throws RemoteException {
		if (isRunningRestart()) {
			resetChunkData();
			wakeUp();
			checkChunks();
			return "restart";
		} else
			return "Already running one restartProtocol";
	}

	/**
	 * responsible for making the actualRepDegree = 0 and take the servers who
	 * answered
	 */
	public synchronized void resetChunkData() {
		data.resetChunkData();
	}

	public ArrayList<ChunkID> getStored() {
		return data.getStored();
	}

	public void addChunk(ChunkID id) {
		data.addChunk(id);
	}

	public void setStored(ArrayList<ChunkID> stored) {
		data.setStored(stored);
	}

	public HashMap<String, ArrayList<FileID>> getFilesSent() {
		return data.getFilesSent();
	}

	public void setFilesSent(HashMap<String, ArrayList<FileID>> filesSent) {
		data.setFilesSent(filesSent);
	}

	public HashMap<ChunkID, ArrayList<Integer>> getAnsweredCommand() {
		return data.getServerAnsweredCommand();
	}

	public void setAnsweredCommand(
			HashMap<ChunkID, ArrayList<Integer>> answeredCommand) {
		data.setServerAnsweredCommand(answeredCommand);
	}

	public MCReceiver getControlChannel() {
		return controlChannel;
	}

	public void setControlChannel(MCReceiver controlChannel) {
		this.controlChannel = controlChannel;
	}

	public MDBReceiver getDataChannel() {
		return dataChannel;
	}

	public void setDataChannel(MDBReceiver dataChannel) {
		this.dataChannel = dataChannel;
	}

	public MDRReceiver getRestoreChannel() {
		return restoreChannel;
	}

	public void setRestoreChannel(MDRReceiver restoreChannel) {
		this.restoreChannel = restoreChannel;
	}

	public static Registry getRmiRegistry() {
		return rmiRegistry;
	}

	public static void setRmiRegistry(Registry rmiRegistry) {
		Peer.rmiRegistry = rmiRegistry;
	}

	public static String getRmiName() {
		return rmiName;
	}

	public static void setRmiName(String rmiName) {
		Peer.rmiName = rmiName;
	}

	public HashMap<ChunkID, ArrayList<Integer>> getServerAnsweredCommand() {
		return data.getServerAnsweredCommand();
	}

	public void setServerAnsweredCommand(
			HashMap<ChunkID, ArrayList<Integer>> serverAnsweredCommand) {
		data.setServerAnsweredCommand(serverAnsweredCommand);
	}

	public static void setInstance(Peer instance) {
		Peer.instance = instance;
	}

	public Integer getServerID() {
		return serverID;
	}

	public void setServerID(Integer serverID) {
		this.serverID = serverID;
		try {
			createPeerFolder();
			Extra.createDirectory(Integer.toString(this.serverID)
					+ File.separator + FileHandler.BACKUP_FOLDER_NAME);
		} catch (IOException e1) {
			System.out
					.println("IOException when creating the Peer and Backup folders.");
			e1.printStackTrace();
		}
	}

	public void removeStoredEntry(String fileId) {
		data.removeStoredEntry(fileId);
	}

	public synchronized void sortStored() {
		data.sortStored();
	}

	public synchronized void removeFilesSentEntry(String filePath) {
		data.removeFilesSentEntry(filePath);
	}

	public synchronized void removeChunkPeers(ChunkID chunk) {
		data.removeChunkPeers(chunk);
	}

	public synchronized void removeChunkPeer(ChunkID chunk, Integer peer) {
		data.removeChunkPeer(chunk, peer);
	}

	public boolean hasChunkStored(ChunkID id) {
		return data.hasChunkStored(id);
	}

	public PeerData getData() {
		return data;
	}

	public void setData(PeerData data) {
		this.data = data;
	}

	/**
	 * Loads the PeerData object from a file, if it exists
	 * 
	 * @throws FileNotFoundException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public void loadData() throws FileNotFoundException,
			ClassNotFoundException, IOException {
		this.data = data.loadPeerData();
	}

	/**
	 * Saves the PeerData object to a file
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void saveData() throws FileNotFoundException, IOException {
		data.savePeerData();
	}

	public String getFolderPath() {
		return folderPath;
	}

	public void setFolderPath(String workingDirPath) {
		this.folderPath = workingDirPath;
	}

	public static String getCurrentVersion() {
		return PeerData.getCurrentVersion();
	}

	/**
	 * 
	 * @param chunkID
	 * @param senderID
	 */
	public void addSenderToAnswered(ChunkID chunkID, int senderID) {
		HashMap<ChunkID, ArrayList<Integer>> command = getAnsweredCommand();
		ArrayList<Integer> answered = command.get(chunkID);
		synchronized (command) {
			if (answered == null) {
				answered = new ArrayList<Integer>();
				synchronized (answered) {
					command.put(chunkID, answered);
					// System.out.println("Added CHUNKID");
				}
			}
			synchronized (answered) {

				if (answered.isEmpty() || !answered.contains(senderID)) {
					answered.add(senderID);
					for (ChunkID toUpdate : command.keySet())
						if (toUpdate.equals(chunkID))
							toUpdate.increaseRepDegree();
				}

			}
		}
		// Save alterations to peer data
		try {
			saveData();
		} catch (FileNotFoundException e) {
			System.out.println("FileNotFoundException for saveData");
		} catch (IOException e) {
			System.out.println("IOException for saveData");
		}
	}

	/**
	 * Starts to reclaim disk space when the data size is bigger than the backup
	 * size left
	 * 
	 * @param backupFolderSize
	 *            size available on the backup folder
	 * @param dataSize
	 *            size of the data to write
	 * @return true if there is enough space
	 */
	public boolean reclaimDiskSpace(long backupFolderSize, long dataSize) {
		// System.out.println("Testing " + PeerData.getDiskSize() + " " +
		// backupFolderSize + " " + dataSize);
		if (PeerData.getDiskSize() - (backupFolderSize + dataSize) < 0) {
			System.out.println("!!Starting Disk Reclaim!!  "
					+ PeerData.getDiskSize() + "   " + backupFolderSize);
			return (new ReclaimProtocol(Chunk.getChunkSize()))
					.nonPriorityReclaim();
		}
		return true;
	}

	/**
	 * 
	 * @param fileID
	 * @return true if the fileID belongs to a file Already backed up by this
	 *         peer
	 */
	public boolean fileAlreadySent(String fileID) {
		HashMap<String, ArrayList<FileID>> filesSent = data.getFilesSent();
		for (String key : filesSent.keySet()) {
			for (FileID member : filesSent.get(key)) {
				if (member.getID().equals(fileID))
					return true;
			}
		}
		return false;
	}

	public UDPConnection getMonitorConnection() {
		return monitorConnection;
	}

	public void setMonitorConnection(UDPConnection monitorConnection) {
		this.monitorConnection = monitorConnection;
	}

	public boolean isMonitorAlive() {
		return monitorAlive;
	}

	public void setMonitorAlive(boolean monitorAlive) {
		this.monitorAlive = monitorAlive;
	}

	public boolean isMonitorResurrectedAttempted() {
		return monitorResurrectedAttempted;
	}

	public void setMonitorResurrectedAttempted(
			boolean monitorResurrectedAttempted) {
		this.monitorResurrectedAttempted = monitorResurrectedAttempted;
	}

	public int getnTries() {
		return nTries;
	}

	public void setnTries(int nTries) {
		this.nTries = nTries;
	}

	public Set<FileID> getFilesDeleted() {
		return data.getFilesDeleted();
	}

	public void setFilesDeleted(Set<FileID> filesDeleted) {
		data.setFilesDeleted(filesDeleted);
	}

	public int getLIMIT_OF_ATTEMPTS() {
		return LIMIT_OF_ATTEMPTS;
	}

	public PeerTCPHandler getTrackerConnection() {
		return trackerConnection;
	}

	public void setTrackerConnection(PeerTCPHandler trackerConnection) {
		this.trackerConnection = trackerConnection;
	}

	public boolean isRunningRestart() {
		return runningRestart;
	}

	public void setRunningRestart(boolean runningRestart) {
		this.runningRestart = runningRestart;
	}

	public boolean isRunningCheckChunks() {
		return runningCheckChunks;
	}

	public void setRunningCheckChunks(boolean runningCheckChunks) {
		this.runningCheckChunks = runningCheckChunks;
	}

	public boolean isRunningWakeUp() {
		return runningWakeUp;
	}

	public void setRunningWakeUp(boolean runningWakeUp) {
		this.runningWakeUp = runningWakeUp;
	}

	public static int getServerPort() {
		return serverPort;
	}

	public static void setServerPort(int serverPort) {
		Peer.serverPort = serverPort;
	}

	public static InetAddress getServerAddress() {
		return serverAddress;
	}

	public static void setServerAddress(InetAddress serverAddress) {
		Peer.serverAddress = serverAddress;
	}

	public void sendMessageToTrackerTest(String message) throws IOException {

		// trackerConnection.sendMessageToTrackerTest(message);
	}

	@Override
	public String testTCP() throws RemoteException {

		try {
			sendMessageToTrackerTest("test message");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "";
	}

	public void setKey(byte[] key) {

		encryptionKey = new SecretKeySpec(key, 0, key.length, "AES");
	}

	public SecretKey getEncryptionKey() {
		return encryptionKey;
	}

	public void setEncryptionKey(SecretKey encryptionKey) {
		this.encryptionKey = encryptionKey;
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
		System.out.println(args[0] + " " + args[1] + " " + args[2] + " "
				+ args[3] + " " + args[4] + " " + args[5] + " " + args[6] + " "
				+ args[7] + " " + args[8]);
		System.out.println(beepPort);
		builder = new ProcessBuilder(javaBin, "-cp", classpath, className,
				"PEER", beepPORT, args[0], args[1], args[2], args[3], args[4],
				args[5], args[6], args[7], args[8]);

		File oldLog = new File("monitor_logs\\server" + serverID
				+ "_monitor_log");
		oldLog.delete();
		File log = new File("monitor_logs\\server" + serverID + "_monitor_log");

		builder.redirectErrorStream(true);
		builder.redirectOutput(Redirect.appendTo(log));
		monitorProcess = builder.start();
		assert builder.redirectOutput().file() == log;
		// process.waitFor();
		// return process.exitValue();
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
					createMonitorProcess(beepServerPort, peerMainArgs);
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
				String fromPeer, fromMonitor;
				Thread.sleep(4000);
				while (connectionAlive) {
					if (bin.ready()) { // buffer has something
						if ((fromMonitor = bin.readLine()) != null) {
							System.out.println("received: " + fromMonitor);
							fromPeer = "PEER_BEEP";
							Thread.sleep(1000);
							monitorAlive = true;
							bout.println(fromPeer);
							System.out.println("sent: " + fromPeer);
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
							System.out.println("failed To Ressurect Peer\n");
							return;
							// Action to take??
						} else {
							if (nTries >= LIMIT_OF_ATTEMPTS) {
								monitorAlive = false;
								monitorResurrectedAttempted = true;
								System.out
										.println("attempting ressurection \nCreating new Monitor process");
								createMonitorProcess(beepServerPort,
										peerMainArgs);
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

}
