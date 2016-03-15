package channels;

import java.util.ArrayList;
import java.util.HashMap;
import chunk.Chunk;
import chunk.ChunkID;
import file.FileID;

//SINGLETON SYNCRONIZE ALL THREADS HAVE ACESS TO IT
public class Peer extends Server {
	static Peer instance = new Peer();

	static Peer getInstance() {
		return instance;
	}

	private HashMap<ChunkID, Chunk> stored;
	private ArrayList<FileID> filesSent;
	private MC controlChannel;
	private MDB dataChannel;
	private MDR restoreChannel;
	// TODO change names and check structures
	// TODO servers that replay to command
	// TODO check connection between channel an peers
	HashMap<ChunkID, ArrayList<Integer>> serverAnsweredCommand;

	public HashMap<ChunkID, Chunk> getStored() {
		return stored;
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

	public MC getControlChannel() {
		return controlChannel;
	}

	public void setControlChannel(MC controlChannel) {
		this.controlChannel = controlChannel;
	}

	public MDB getDataChannel() {
		return dataChannel;
	}

	public void setDataChannel(MDB dataChannel) {
		this.dataChannel = dataChannel;
	}

	public MDR getRestoreChannel() {
		return restoreChannel;
	}

	public void setRestoreChannel(MDR restoreChannel) {
		this.restoreChannel = restoreChannel;
	}

}
