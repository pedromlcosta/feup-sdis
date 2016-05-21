package protocol;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;

import channels.MCReceiver;
import data.FileID;
import extra.Extra;
import extra.FileHandler;
import messages.CheckChunkMsg;
import messages.Message;
import messages.StoredMsg;
import service.Peer;

public class CheckChunksProtocol extends Thread {
	private static final int SLEEP_TIME = 400;
	private static final int N_MESSAGES_SENT = 5;
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

	public void sendCheckChunks() {
		HashMap<String, ArrayList<FileID>> filesSent = peer.getFilesSent();
		Set<String> keySet = filesSent.keySet();
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
	}

	// CHECKCHUNK <Version> <SenderId> <FileId><CRLF><CRLF>
	public void receiveCheckChunks(Message CheckChunksMsg) {
		System.out.println("Received a CHECKCHUNK");
		Message msg = new StoredMsg();
		String fileID = CheckChunksMsg.getFileId();

		File dir = new File(StoredChunksFolderPath);
		if (!dir.isDirectory())
			throw new IllegalStateException("Not a dir");
		String prefix = fileID + "_";
		System.out.println("Prefix: " + prefix);
		for (File file : dir.listFiles()) {

			String fileName = file.getName();
			System.out.println("FIle: " + fileName);
			if (fileName.startsWith(prefix)) {
				// STORED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
				System.out.println("Found Chunk");
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

	public void sendCheckChunksMsg(Message msg, String[] args) {
		msg.createMessage(null, args);
		Peer peer = Peer.getInstance();
		DatagramPacket packet = peer.getControlChannel().createDatagramPacket(msg.getMessageBytes());
		MCReceiver control = peer.getControlChannel();
		for (int i = 0; i < N_MESSAGES_SENT; i++) {
			// 0 and 400 ms random delay
			int delay = new Random().nextInt(SLEEP_TIME);
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// send message
			control.writePacket(packet);
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

	public static int getnMessagesSent() {
		return N_MESSAGES_SENT;
	}

	public void setPeer(Peer peer) {
		this.peer = peer;
	}

	public void setVersion(String version) {
		this.version = version;
	}

}
