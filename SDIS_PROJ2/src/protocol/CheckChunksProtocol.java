package protocol;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;

import channels.MCReceiver;
import chunk.ChunkID;
import data.FileID;
import extra.Extra;
import extra.FileHandler;
import messages.CheckChunkMsg;
import messages.Message;
import messages.StoredMsg;
import service.Peer;

public class CheckChunksProtocol extends Protocol {
	private static final int LOCAL_MAX = 3;
	private Peer peer = Peer.getInstance();
	private String StoredChunksFolderPath;
	private String version;

	public CheckChunksProtocol() {
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
		sendCheckChunks();

	}

	public synchronized void sendCheckChunks() {
		HashMap<String, ArrayList<FileID>> filesSent = peer.getFilesSent();
		Set<String> keySet = filesSent.keySet();
		HashMap<ChunkID, ArrayList<Integer>> chunksStored = peer.getAnsweredCommand();
		Set<ChunkID> chunkIDs = chunksStored.keySet();
		peer.resetChunkData();
		for (String key : keySet) {
			ArrayList<FileID> file = filesSent.get(key);
			int size = file.size();
			if (size > 0) {
				FileID updated = file.get(size - 1);
				// CHECKCHUNK <Version> <SenderId> <FileId> <CRLF><CRLF>
				Message msg = new CheckChunkMsg();
				String args[] = new String[3];
				args[0] = getVersion();
				args[1] = peer.getServerID().toString();
				args[2] = updated.getID();
				new Thread(() -> {
					sendCheckChunksMsg(msg, args);
				}).start();

			}
		}

		for (ChunkID c : chunkIDs) {
			c.getFileID();
			Message msg = new CheckChunkMsg();
			String args[] = new String[3];
			args[0] = getVersion();
			args[1] = peer.getServerID().toString();
			args[2] = c.getFileID();
			new Thread(() -> {
				sendCheckChunksMsg(msg, args);
			}).start();

		}

	}

	// CHECKCHUNK <Version> <SenderId> <FileId><CRLF><CRLF>
	public synchronized void receiveCheckChunks(Message CheckChunksMsg) {
		Message msg = new StoredMsg();
		String fileID = CheckChunksMsg.getFileId();

		File dir = new File(StoredChunksFolderPath);
		if (!dir.isDirectory())
			throw new IllegalStateException("Not a dir");
		String prefix = fileID + "_";
		for (File file : dir.listFiles()) {

			String fileName = file.getName();
			if (fileName.startsWith(prefix)) {
				// STORED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
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

				sendStoredMsg(msg, args);
			}
		}
		// if not ignore
	}

	public void sendCheckChunksMsg(Message msg, String[] args) {
		msg.createMessage(null, args);
		Peer peer = Peer.getInstance();
		int delay;

		MCReceiver control = peer.getControlChannel();
		// TODO check if copies are well done
		byte[] msgBytes = msg.getMessageBytes();
		int messageSize = msgBytes.length;
		byte copyOfMessage[] = new byte[messageSize];

		for (int i = 0; i < LOCAL_MAX; i++) {

			System.arraycopy(msgBytes, 0, copyOfMessage, 0, messageSize);
			DatagramPacket msgPacket = control.createDatagramPacket(copyOfMessage); //
			// send message
			control.writePacket(msgPacket);
			// 0 and 400 ms random delay
			delay = randomSeed.nextInt(SLEEP_TIME);
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
	}

	public String getVersion() {
		return version;
	}

	public String getStoredChunksFolderPath() {
		return StoredChunksFolderPath;
	}

	public void setStoredChunksFolderPath(String storedChunksFolderPath) {
		StoredChunksFolderPath = storedChunksFolderPath;
	}

	public static int getSleepTime() {
		return SLEEP_TIME;
	}

	public void setPeer(Peer peer) {
		this.peer = peer;
	}

	public void setVersion(String version) {
		this.version = version;
	}

}
