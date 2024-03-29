package protocol;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Random;
import java.util.Set;

import channels.MCReceiver;
import data.FileID;
import extra.Extra;
import extra.FileHandler;
import messages.Message;
import messages.WakeMsg;
import service.Peer;

public class WakeProtocol extends Protocol {
	// needs Access to Peer
	// will go through Peer Data and send the WakeUpMessages
	private String StoredChunksFolderPath;
	private static final int LOCAL_MAX = 3;
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

	// synchronized
	public void run() {
		// TODO CHUNKS DOS "NOSSOS" FILES
		// Find out about the chunks from other files
		// HashMap<String, Boolean> chunkStored = new HashMap<String,
		// Boolean>();
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
				// if (chunkStored.get(fileID) == null) {
				// chunkStored.put(fileID, true);
				String args[] = new String[3];
				args[0] = Peer.getCurrentVersion();
				args[1] = Integer.toString(peer.getServerID());
				args[2] = fileID;
				sendWakeUp(args);
				// The rest of the work need to be done at the processor
				// }
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
		MCReceiver control = peer.getControlChannel();
		// TODO check if copies are well done
		byte[] msgBytes = msg.getMessageBytes();
		int messageSize = msgBytes.length;
		byte copyOfMessage[] = new byte[messageSize];

		for (int i = 0; i < LOCAL_MAX; i++) {
			// Copia conteudo (por causa da encrpita��o ele ser� alterado
			System.arraycopy(msgBytes, 0, copyOfMessage, 0, messageSize);
			DatagramPacket msgPacket = control.createDatagramPacket(copyOfMessage); //
			control.writePacket(msgPacket);

			int delay = randomSeed.nextInt(SLEEP_TIME);
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	public synchronized void receiveWakeUp(Message msg) {

		FileID fileID = new FileID(msg.getFileId(), true);
		Set<FileID> filesDeleted = peer.getFilesDeleted();
		synchronized (filesDeleted) {
			for (FileID id : filesDeleted) {
				if (id.equals(fileID))
					(new DeleteProtocol()).sendDeleteMsg(fileID.getID());
			}
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
