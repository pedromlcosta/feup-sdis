package protocol;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.HashMap;
import channels.MCReceiver;
import extra.Extra;
import extra.FileHandler;
import messages.Message;
import messages.WakeMsg;
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
		// TODO CHUNKS DOS "NOSSOS" FILES
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
					String args[] = new String[3];
					args[0] = Peer.getCurrentVersion();
					args[1] = Integer.toString(peer.getServerID());
					args[2] = fileID;
					sendWakeUp(args);
					// The rest of the work need to be done at the processor
				}
			}
		}
	}

	public void setPeer(Peer peer) {
		this.peer = peer;
	}

	public void sendWakeUp(String args[]) {
		// will send the msg to the network
		Message msg = new WakeMsg();
		msg.createMessage(null, args);
		System.out.println(msg.getMessageToSend());
		MCReceiver control = peer.getControlChannel();
		DatagramPacket msgPacket = control.createDatagramPacket(msg.getMessageBytes()); //
		control.writePacket(msgPacket);
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

	public void receiveWakeUp(Message msg) {
		 
		String fileID =msg.getFileId();
		// defeats the purpose of being a Set
		if (peer.getFilesDeleted().contains(fileID)) {
			// send a delete
			(new DeleteProtocol()).sendDeleteMsg(fileID);

		}
	}
}
