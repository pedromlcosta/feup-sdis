package service;

import java.rmi.Remote;
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
	private ArrayList<FileID> filesSent;

	private MCReceiver controlChannel;
	private MDBReceiver dataChannel;
	private MDRReceiver restoreChannel;

	private Registry rmiRegistry;
	private final String rmiName = "123";
	private Dispatcher commandDispatcher = new Dispatcher();
	// TODO change names and check structures
	// TODO servers that replay to command
	// TODO check connection between channel an peers
	HashMap<ChunkID, ArrayList<Integer>> serverAnsweredCommand;

	public HashMap<ChunkID, Chunk> getStored() {
		return stored;
	}

	public void addChunk(ChunkID id, Chunk chunk) {
		stored.put(id, chunk);
	}

	public void setStored(HashMap<ChunkID, Chunk> stored) {
		this.stored = stored;
	}

	public ArrayList<FileID> getFilesSent() {
		return filesSent;
	}

	public void setFilesSent(ArrayList<FileID> filesSent) {
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

	public void registerRMI() {
		// Create and export object
		try {
			Invocation stub = (Invocation) UnicastRemoteObject.exportObject(instance, 0);

			// Register object to rmi registry
			rmiRegistry = LocateRegistry.getRegistry();
			rmiRegistry.bind(rmiName, stub);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String backup(String exampleArg) throws RemoteException {
		// O dispatcher vai ter as cenas do socket necessarias
		// e os metodos para enviar para os canais que queremos as cenas

		// Call backup protocol through dispatcher
		return null;
	}

	@Override
	public String restore(String exampleArg) throws RemoteException {
		// Call restore protocol
		return null;
	}

	@Override
	public String delete(String exampleArg) throws RemoteException {
		// Call delete protocol
		return null;
	}

	@Override
	public String reclaim(String exampleArg) throws RemoteException {
		// Call reclaim protocol
		return null;
	}

}
