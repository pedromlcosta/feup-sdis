package service;

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
import chunk.ChunkID;
import extra.Extra;
import file.FileID;
import protocol.BackupProtocol;
import protocol.DeleteProtocol;
import protocol.ReclaimProtocol;
import protocol.RestoreProtocol;

//SINGLETON SYNCRONIZE ALL THREADS HAVE ACESS TO IT
public class Peer implements Invocation {
	static Peer instance = new Peer();

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
	private Dispatcher commandDispatcher = new Dispatcher();
	private String folderPath;
	// TODO change names and check structures
	// TODO servers that replay to command
	// TODO check connection between channel an peers

	public Peer() {

		controlChannel = new MCReceiver();
		dataChannel = new MDBReceiver();
		restoreChannel = new MDRReceiver();
		data = new PeerData();

	}

	public void createPeerFolder() throws IOException {
		folderPath = Extra.createDirectory(Integer.toString(serverID));
	}

	public static void main(String[] args) {

		Peer peer = Peer.getInstance();
		
		for (String a : args)
			System.out.println(a);

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					rmiRegistry.unbind(rmiName);

					// UnicastRemoteObject.unexportObject(instance, true);

				} catch (RemoteException | NotBoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println("Problem unbinding");
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
			

			peer.getControlChannel().setAddr(InetAddress.getByName(args[1]));
			peer.getControlChannel().setPort(Integer.parseInt(args[2]));
			peer.getControlChannel().createSocket();
			peer.getControlChannel().joinMulticastGroup();

			peer.getDataChannel().setAddr(InetAddress.getByName(args[3]));
			peer.getDataChannel().setPort(Integer.parseInt(args[4]));
			peer.getDataChannel().createSocket();
			peer.getDataChannel().joinMulticastGroup();

			peer.getRestoreChannel().setAddr(InetAddress.getByName(args[5]));
			peer.getRestoreChannel().setPort(Integer.parseInt(args[6]));
			peer.getRestoreChannel().createSocket();
			peer.getRestoreChannel().joinMulticastGroup();

			peer.createPeerFolder();

			peer.getRestoreChannel().start();
			peer.getDataChannel().start();
			peer.getControlChannel().start();
		} catch (IOException e) {
			System.out.println("Couldn't bind IP:ports to peer");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Invalid Args. Ports must be between 1 and 9999 and IP must be a valid multicast address.");
			return;
		}
		
		// LOAD PEER DATA
		
		try{
			PeerData.setDataPath(peer.getServerID());
			peer.loadData();
		}catch(FileNotFoundException e){
			System.out.println("There wasn't a peerData file, creating one now");
			System.out.println(e.getMessage());   
			//e.printStackTrace();                           // Remove these stack traces after
			//There wasn't a file, so we're creating one now!
			try {
				peer.saveData();
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}catch (NotSerializableException e){
			System.out.println("wtf is going on here?");
		}catch(IOException e){
			System.out.println(e.getMessage());
			e.printStackTrace();
			return;
		} catch (ClassNotFoundException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return;
		}

		registerRMI();

	}

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

	public static void registerRMI() {
		// Create and export object
		try {

			Invocation stub = (Invocation) UnicastRemoteObject.exportObject(instance, 0);

			// Register object to rmi registry
			rmiRegistry = LocateRegistry.getRegistry();
			/*
			 * try{ rmiRegistry = LocateRegistry.createRegistry(1099);
			 * }catch(Exception e){ System.out.println("Caught ya, bitch");
			 * rmiRegistry = LocateRegistry.getRegistry(); }
			 */
			try {
				rmiRegistry.bind(rmiName, stub);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Couldnt bind, try another remote name, this one is in use");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

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

	@Override
	public String restore(String filePath) throws RemoteException {
		// Call restore protocol

		Thread restore = new RestoreProtocol(filePath);
		restore.start();
		System.out.println("restore called");
		return "restore sent";
	}

	@Override
	public synchronized String delete(String filePath) throws RemoteException {
		// Call delete protocol
		System.out.println("Before calling delete");
		Thread delete = new DeleteProtocol(filePath);
		delete.start();
		System.out.println("delete called");
		return "delete sent";
	}

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

	public HashMap<String, FileID> getFilesSent() {
		return data.getFilesSent();
	}

	public void setFilesSent(HashMap<String, FileID> filesSent) {
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

	public Dispatcher getCommandDispatcher() {
		return commandDispatcher;
	}

	public void setCommandDispatcher(Dispatcher commandDispatcher) {
		this.commandDispatcher = commandDispatcher;
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
	
	public void loadData() throws FileNotFoundException, ClassNotFoundException, IOException {
		this.data = data.loadPeerData();
	}

	public void saveData() throws FileNotFoundException, IOException {
		data.savePeerData();
	}
	
	public String getFolderPath() {
		return folderPath;
	}

	public void setFolderPath(String workingDirPath) {
		this.folderPath = workingDirPath;
	}

	

}
