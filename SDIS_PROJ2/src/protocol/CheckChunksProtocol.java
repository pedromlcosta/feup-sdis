package protocol;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;

import data.FileID;
import extra.Extra;
import extra.FileHandler;
import messages.CheckChunkMsg;
import messages.Message;
import service.Peer;

public class CheckChunksProtocol extends Thread {
	private static final int SLEEP_TIME = 400;
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
		HashMap<String, ArrayList<FileID>> filesSent = peer.getFilesSent();
		Set<String> keySet = filesSent.keySet();
		for (String key : keySet) {
			ArrayList<FileID> file = filesSent.get(key);
			int size = file.size();
			if (size > 0) {
				FileID updated = file.get(size - 1);
				// CHECKCHUNK <Version> <SenderId> <FileId> <CRLF><CRLF>

				String args[] = new String[3];
				args[0] = getVersion();
				args[1] = peer.getServerID().toString();
				args[2] = updated.getID();

			}
		}

	}

	// Not the receiver I need to do xD
	public void receiveWakeUp(Message CheckChunksMsg) {
		Message msg = new CheckChunkMsg();
		String fileID = CheckChunksMsg.getFileId();
		// CHECKCHUNK <Version> <SenderId> <FileId><CRLF><CRLF>
		// In here we check if we chunks of said File
		// Need to check if chunk exists
		File dir = new File(StoredChunksFolderPath);
		if (!dir.isDirectory())
			throw new IllegalStateException("Not a dir");

		for (File file : dir.listFiles()) {
			String prefix = fileID + "_" + Integer.toString(CheckChunksMsg.getChunkNo());
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

				sendCheckChunksMsg(msg, args);
			}
		}
		// if not ignore
	}

	public void sendCheckChunksMsg(Message msg, String[] args) {
		msg.createMessage(null, args);
		Peer peer = Peer.getInstance();
		DatagramPacket packet = peer.getControlChannel().createDatagramPacket(msg.getMessageBytes());

		// 0 and 400 ms random delay
		int delay = new Random().nextInt(SLEEP_TIME);
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// send message
		peer.getControlChannel().writePacket(packet);
	}

	public String getVersion() {
		return version;
	}

}
