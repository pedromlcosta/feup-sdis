package protocol;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.ArrayList;

import channels.MCReceiver;
import chunk.ChunkID;
import file.FileID;
import messages.DeleteMsg;
import messages.Message;
import service.Peer;

public class DeleteProtocol extends Thread {

	private static Peer peer = Peer.getInstance();
	private static FileID file;
	private String filePath;
	private static int MAX_SENT = 5;

	public DeleteProtocol(String filePath) {
		this.filePath = filePath;
	}

	public void run() {

		ArrayList<FileID> fileSentVersions = peer.getFilesSent().get(filePath);
		file = fileSentVersions.get(fileSentVersions.size()-1);
		if (file == null) {
			System.out.println(filePath + " not found");
			return;
		}

		// create message
		Message msg = new DeleteMsg();
		int nMessagesSent = 0;

		msg.createMessage(null, "1.0", Integer.toString(peer.getServerID()), file.getID());

		MCReceiver mc = peer.getControlChannel();
		DatagramPacket msgPacket = mc.createDatagramPacket(msg.getMessageBytes());
		while (nMessagesSent < MAX_SENT) {
			mc.writePacket(msgPacket);
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			nMessagesSent++;
		}
		// delete
		synchronized (peer.getStored()) {
			peer.removeFilesSentEntry(filePath);

			ChunkID chunk;
			for (int i = 0; i < file.getnChunks(); i++) {
				chunk = new ChunkID(file.getID(), i + 1);
				peer.removeChunkPeers(chunk);
			}
		}

		// Save alterations to peer data
		try {
			peer.saveData();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
