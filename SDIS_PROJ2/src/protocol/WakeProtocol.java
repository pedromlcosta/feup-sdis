package protocol;

import java.io.File;
import java.io.IOException;

import chunk.ChunkID;
import extra.Extra;
import extra.FileHandler;
import messages.Message;
import messages.StoredMsg;
import service.Peer;

public class WakeProtocol extends Thread {
	// needs Access to Peer
	// will go through Peer Data and send the WakeUpMessages
	private Peer peer = Peer.getInstance();
	private String StoredChunksFolderPath;
	private String version;

	public WakeProtocol() {
		version = "1.0";
		try {
			StoredChunksFolderPath = Extra.createDirectory(Integer.toString(peer.getServerID()) + File.separator + FileHandler.BACKUP_FOLDER_NAME);
		} catch (IOException e1) {
		}
	}

	public Peer getPeer() {
		return peer;
	}

	public void run() {

	}

	public void setPeer(Peer peer) {
		this.peer = peer;
	}

	// Received a wakeUp
	public void receiveWakeUp(Message wakeupMSG) {
		Message msg = new StoredMsg();

		String fileID = wakeupMSG.getFileId();
		ChunkID id = new ChunkID(fileID, wakeupMSG.getChunkNo());
		// Need to check if chunk exists
		if (FileHandler.checkIfFileExits(StoredChunksFolderPath + File.separator + id.getFileID() + "_" + id.getChunkNumber())) {
			// if so send msg
			String args[] = new String[4];
			// Version
			args[0] = getVersion();
			// SenderID
			args[1] = Integer.toString(peer.getServerID());
			// FileID
			args[2] = fileID;
			// Chunk No
			args[3] = Integer.toString(wakeupMSG.getChunkNo());
			BackupProtocol.sendStoredMsg(msg, args);
		}
		// if not ignore
	}

	public void sendWakeUp() {
		//should I go through each file 
		//need to know who has each chunk of Files it sent
		//need to let ppl know which chunks it has
		// what do I need for a wakeup msg
		// version 1.0? or higher
		// need senderID
		// need chunkID
		// need chunkNo
		// need fileID
		// No bodies
		//what happens when he send a I have Chunk X of File Y but Chunk X/FileY should not be in the system??
	}

	public String getStoredChunksFolderPath() {
		return StoredChunksFolderPath;
	}

	public void setStoredChunksFolderPath(String storedChunksFolderPath) {
		StoredChunksFolderPath = storedChunksFolderPath;
	}

	public String getVersion() {
		return version;
	}
}
