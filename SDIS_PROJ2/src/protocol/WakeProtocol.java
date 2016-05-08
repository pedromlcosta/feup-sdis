package protocol;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import chunk.ChunkID;
import extra.Extra;
import extra.FileHandler;
import file.FileID;
import messages.Message;
import messages.StoredMsg;
import service.Peer;

public class WakeProtocol extends Thread {
	private static final int N_ARGS = 4;
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

		// TODO CHUNKS DOS "NOSSOS" FILES
		HashMap<String, ArrayList<FileID>> filesSent = peer.getFilesSent();
		Set<String> keys = filesSent.keySet();
		for (String key : keys) {
			ArrayList<FileID> fileIDs = filesSent.get(key);
			int pos = fileIDs.size() - 1;
			if (pos >= 0) {
				FileID file = fileIDs.get(fileIDs.size() - 1);
				String args[] = new String[2];
				args[0] = file.getID();
				args[1] = Integer.toString(file.getnChunks());
				sendWakeUp(false, args);
			}
		}
		// Find out about the chunks from other files
		HashMap<String, Boolean> chunkStored = new HashMap<String, Boolean>();
		File dir = new File(StoredChunksFolderPath);
		if (!dir.isDirectory())
			throw new IllegalStateException("Not a directoray");
		for (File file : dir.listFiles()) {
			String sufix = "_\\d+";
			String fileName = file.getName();
			// Example: sadsadasdasdasdasda_10 it will
			// return sadsadasdasdasdasda
			String[] fileIDs = fileName.split(sufix);
			if (fileIDs.length > 0) {
				String fileID = fileIDs[0];
				if (chunkStored.get(fileID) != null) {
					chunkStored.put(fileID, true);
					String args[] = new String[1];
					args[0] = fileID;
					sendWakeUp(true, args);
					// The rest of the work need to be done at the processor
				}
			}
		}
	}

	public void setPeer(Peer peer) {
		this.peer = peer;
	}

	// Received a wakeUp
	public void receiveWakeUp(Message wakeupMSG) {
		Message msg = new StoredMsg();
		String fileID = wakeupMSG.getFileId();

		// WAKEUP <Version> <SenderId> <FileId> <CRLF><CRLF>
		// In here we check if we chunks of said File
		// Need to check if chunk exists
		File dir = new File(StoredChunksFolderPath);
		if (!dir.isDirectory())
			throw new IllegalStateException("Not a dir");

		for (File file : dir.listFiles()) {
			String prefix = fileID + "_";
			String fileName = file.getName();
			if (fileName.startsWith(prefix)) {
				// if so send msg
				String args[] = new String[4];
				// Version
				args[0] = getVersion();
				// SenderID
				args[1] = Integer.toString(peer.getServerID());
				// FileID
				args[2] = fileID;
				// Chunk No -> Example: sadsadasdasdasdasda_10 it will
				// return 10
				args[3] = fileName.substring(fileName.indexOf(prefix) + prefix.length(), fileName.length());
				BackupProtocol.sendStoredMsg(msg, args);
			}
		}
		// if not ignore
	}

	public void sendWakeUp(boolean tracker, String args[]) {
		if (tracker) {
			// send msg to monitor to send to tracker
		} else {
			// will send the msg to the network
		}

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
