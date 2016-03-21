package service;

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
import file.FileID;

//SINGLETON SYNCRONIZE ALL THREADS HAVE ACESS TO IT
public class Peer implements Invocation {
	static Peer instance = new Peer();

	static Peer getInstance() {
		return instance;
	}

	private HashMap<ChunkID, Chunk> stored;
	private HashMap<String, FileID> filesSent;

	private MCReceiver controlChannel;
	private MDBReceiver dataChannel;
	private MDRReceiver restoreChannel;

	private static Registry rmiRegistry;
	private static String rmiName;
	private Dispatcher commandDispatcher = new Dispatcher();
	// TODO change names and check structures
	// TODO servers that replay to command
	// TODO check connection between channel an peers
	HashMap<ChunkID, ArrayList<Integer>> serverAnsweredCommand;

	public static void main(String[] args) {

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					System.out.println("YES");

					rmiRegistry.unbind(rmiName);
					// UnicastRemoteObject.unexportObject(instance,true);
				} catch (RemoteException | NotBoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println("Problem unbinding");
				}
			}
		});

		System.out.println("Hi");
		rmiName = args[0];
		registerRMI();

	}

	// Methods

	public HashMap<ChunkID, Chunk> getStored() {
		return stored;
	}

	public void addChunk(ChunkID id, Chunk chunk) {
		stored.put(id, chunk);
	}

	public void setStored(HashMap<ChunkID, Chunk> stored) {
		this.stored = stored;
	}

	public HashMap<String, FileID> getFilesSent() {
		return filesSent;
	}

	public void setFilesSent(HashMap<String, FileID> filesSent) {
		this.filesSent = filesSent;
	}

	public HashMap<ChunkID, ArrayList<Integer>> getAnsweredCommand() {
		return serverAnsweredCommand;
	}

	public void setAnsweredCommand(HashMap<ChunkID, ArrayList<Integer>> answeredCommand) {
		this.serverAnsweredCommand = answeredCommand;
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
	public String backup(String filePath, int desiredRepDegree) throws RemoteException {
		// O dispatcher vai ter as cenas do socket necessarias
		// e os metodos para enviar para os canais que queremos as cenas

		// Call backup protocol through dispatcher

		System.out.println("backup called");
		return "backup sent";
	}

	@Override
	public String restore(String exampleArg) throws RemoteException {
		// Call restore protocol
		System.out.println("restore called");
		return "restore sent";
	}

	@Override
	public String delete(String exampleArg) throws RemoteException {
		// Call delete protocol
		System.out.println("delete called");
		return "delete sent";
	}

	@Override
	public String reclaim(int reclaimSpace) throws RemoteException {
		// Call reclaim protocol
		System.out.println("reclaim called");
		return "reclaim sent";
	}

}
