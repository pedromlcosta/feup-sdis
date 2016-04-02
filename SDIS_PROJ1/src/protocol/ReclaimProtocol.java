package protocol;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Iterator;
import java.util.Random;

import channels.MCReceiver;
import chunk.ChunkID;
import extra.Extra;
import messages.FileHandler;
import messages.Message;
import messages.RemovedMsg;
import service.Peer;

public class ReclaimProtocol extends Thread {

	private static final int SLEEP_TIME = 1000;
	private int reclaimSpace;
	private int amountReclaimed;
	private static Peer peer = Peer.getInstance();

	public ReclaimProtocol(int reclaimSpace) {
		this.reclaimSpace = reclaimSpace;
		this.amountReclaimed = 0;
	}

	public void run() {

		String dirPath = "";

		try {
			dirPath = Extra.createDirectory(Integer.toString(peer.getServerID()) + File.separator + FileHandler.BACKUP_FOLDER_NAME);
		} catch (IOException e) {
			e.printStackTrace();
		}

		peer.sortStored();
		synchronized (peer.getStored()) {
			Iterator<ChunkID> it = Peer.getInstance().getStored().iterator();

			while (this.amountReclaimed < this.reclaimSpace && it.hasNext()) {
				ChunkID chunk = it.next();
				reclaim(dirPath, it, chunk);
				it.remove();
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

	public boolean nonPriorityReclaim() {
		boolean chunksRemoved = false;
		String dirPath = "";

		try {
			dirPath = Extra.createDirectory(Integer.toString(peer.getServerID()) + File.separator + FileHandler.BACKUP_FOLDER_NAME);
		} catch (IOException e) {
			e.printStackTrace();
		}

		peer.sortStored();
		synchronized (peer.getStored()) {
			Iterator<ChunkID> it = Peer.getInstance().getStored().iterator();
			System.out.println("Start cycle");
			while (this.amountReclaimed < this.reclaimSpace && it.hasNext()) {
				ChunkID chunk = it.next();
				if (chunk.getActualRepDegree() > chunk.getDesiredRepDegree()) {

					try {
						System.in.read();
					} catch (IOException e) {
						e.printStackTrace();
					}
					chunksRemoved = reclaim(dirPath, it, chunk);
					it.remove();
				}
				System.out.println(chunk.getActualRepDegree() + "    " + chunk.getDesiredRepDegree());
			}
		}
		// Save alterations to peer data
		if (chunksRemoved)
			try {
				System.out.println("FileDeleted");
				peer.saveData();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		System.out.println("End of reclaim");
		if (this.amountReclaimed >= this.reclaimSpace)
			return true;
		else
			return false;
	}

	public synchronized boolean reclaim(String dirPath, Iterator<ChunkID> it, ChunkID chunk) {
		File file = new File(dirPath + File.separator + chunk.getFileID() + "_" + chunk.getChunkNumber());

		// create message
		Message msg = new RemovedMsg();
		String[] args = { "1.0", Integer.toString(peer.getServerID()), chunk.getFileID(), Integer.toString(chunk.getChunkNumber()) };
		msg.createMessage(null, args);

		MCReceiver mc = peer.getControlChannel();
		DatagramPacket msgPacket = mc.createDatagramPacket(msg.getMessageBytes());
		mc.writePacket(msgPacket);

		peer.getData().addChunkDeleted(chunk);

		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		peer.getData().removeChunkDeleted(chunk);

		this.amountReclaimed += file.length();

		file.delete();
		peer.removeChunkPeers(chunk);
		return true;
	}
}
