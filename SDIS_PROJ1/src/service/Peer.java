package service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.NotSerializableException;
import java.net.InetAddress;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;

import channels.MCReceiver;
import channels.MDBReceiver;
import channels.MDRReceiver;
import chunk.Chunk;
import chunk.ChunkID;
import extra.Extra;
import extra.FileHandler;
import file.FileID;
import protocol.BackupProtocol;
import protocol.DeleteProtocol;
import protocol.ReclaimProtocol;
import protocol.RestoreProtocol;

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

	private MCReceiver controlChannel;
	private MDBReceiver dataChannel;
	private MDRReceiver restoreChannel;
	private Integer serverID;
	private static Registry rmiRegistry;
	private static String rmiName;
	private String folderPath;

	/**
	 * Default Peer constructor. Initializes receiver servers and PeerData
	 * container.
	 */
	public Peer() {

		controlChannel = new MCReceiver();
		dataChannel = new MDBReceiver();
		restoreChannel = new MDRReceiver();
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
	 *            Peer arguments:
	 *            <server_id> <MC_addr> <MC_port> <MDB_addr> <MDB_port>
	 *            <MDR_addr> <MDR_port>
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
			return;
		}

		try {

			rmiName = args[0];
			peer.setServerID(Integer.parseInt(args[0]));
			peer.getData();

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

			peer.createPeerFolder();

			System.out.println(controlChannel.getAddr().toString());
			System.out.println(controlChannel.getPort());
			System.out.println(dataChannel.getAddr().toString());
			System.out.println(dataChannel.getPort());
			System.out.println(restoreChannel.getAddr().toString());
			System.out.println(restoreChannel.getPort());

			peer.getRestoreChannel().start();
			peer.getDataChannel().start();
			peer.getControlChannel().start();
		} catch (IOException e) {
			System.out.println("Couldn't bind IP:ports to peer");
		} catch (Exception e) {
			System.out.println("Invalid Args. Ports must be between 1 and 9999 and IP must be a valid multicast address.");
			return;
		}

		// LOAD PEER DATA

		try {
			PeerData.setDataPath(peer.getServerID());
			peer.loadData();
		} catch (FileNotFoundException e) {
			System.out.println("There wasn't a peerData file, creating one now");
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
			System.out.println("IOException while loading PeerData for the first time");
			System.out.println(e.getMessage());
			// e.printStackTrace();
			return;
		} catch (ClassNotFoundException e) {
			System.out.println("PeerData - a Class was not found");
			System.out.println(e.getMessage());
			// e.printStackTrace();
			return;
		}

		registerRMI();
		for (ChunkID c : Peer.getInstance().getAnsweredCommand().keySet())
			System.out.println("Size: " + Peer.getInstance().getAnsweredCommand().get(c).size() + "  " + c.getActualRepDegree() + " " + c.getFileID() + "_" + c.getChunkNumber());

	}

	/**
	 * Validates the arguments received
	 * 
	 * @param args
	 *            arguments received from main
	 * @return true if they are in a valid from, false otherwise
	 */
	public static boolean validArgs(String[] args) {

		if (args.length != 7) {
			System.out.println("Incorrect number of args." + " You gave: " + args.length);
			System.out.println("Correct usage is: <server_id> <MC_addr> <MC_port> <MDB_addr> <MDB_port> <MDR_addr> <MDR_port>");
			return false;
		}

		if (!Extra.isNumeric(args[0]) || !Extra.isNumeric(args[2]) || !Extra.isNumeric(args[4]) || !Extra.isNumeric(args[6])) {
			System.out.println("Server ID and ports must be valid numbers");
			System.out.println("Correct usage is: <server_id> <MC_addr> <MC_port> <MDB_addr> <MDB_port> <MDR_addr> <MDR_port>");
			return false;
		}

		return true;

	}

	// Methods
	/**
	 * registers RMI with the remote name
	 */
	public static void registerRMI() {
		// Create and export object
		try {

			Invocation stub = (Invocation) UnicastRemoteObject.exportObject(instance, 0);

			// Register object to rmi registry
			rmiRegistry = LocateRegistry.getRegistry();
			try {
				rmiRegistry.bind(rmiName, stub);
			} catch (Exception e) {
				System.out.println("Couldnt bind, try another remote name, this one is in use");
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
	public synchronized String backup(String filePath, int desiredRepDegree) throws RemoteException {
		// O dispatcher vai ter as cenas do socket necessarias
		// e os metodos para enviar para os canais que queremos as cenas

		// Call backup protocol through dispatcher
		System.out.println(desiredRepDegree);
		new BackupProtocol(filePath, desiredRepDegree, "1.0", Peer.getInstance()).start();

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
		Thread delete = new DeleteProtocol(filePath);
		delete.start();
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

	public void setAnsweredCommand(HashMap<ChunkID, ArrayList<Integer>> answeredCommand) {
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

	public void setServerAnsweredCommand(HashMap<ChunkID, ArrayList<Integer>> serverAnsweredCommand) {
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
			Extra.createDirectory(Integer.toString(this.serverID) + File.separator + FileHandler.BACKUP_FOLDER_NAME);
		} catch (IOException e1) {
			System.out.println("IOException when creating the Peer and Backup folders.");
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
	public void loadData() throws FileNotFoundException, ClassNotFoundException, IOException {
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
			System.out.println("!!Starting Disk Reclaim!!  " + PeerData.getDiskSize() + "   " + backupFolderSize);
			return (new ReclaimProtocol(Chunk.getChunkSize())).nonPriorityReclaim();
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

}
