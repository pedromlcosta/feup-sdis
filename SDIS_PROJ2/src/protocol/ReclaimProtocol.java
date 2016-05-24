package protocol;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Iterator;

import channels.MCReceiver;
import chunk.ChunkID;
import extra.Extra;
import extra.FileHandler;
import messages.Message;
import messages.RemovedMsg;
import service.Peer;

public class ReclaimProtocol extends Thread {

	private static final int SLEEP_TIME = 1000;
	private static final long WAIT_STORED = 400;
	private int reclaimSpace;
	private int amountReclaimed;
	private static Peer peer = Peer.getInstance();

	/**
	 * Constructor for Reclaim protocol that deletes a file and sends REMOVED message
	 * 
	 * @param filePath name of the file to be deleted
	 */
	public ReclaimProtocol(int reclaimSpace) {
		this.reclaimSpace = reclaimSpace;
		this.amountReclaimed = 0;
	}

	/**
	 * Runs the Reclaim Protocol
	 */
	public void run() {

		String dirPath = "";

		try {
			dirPath = Extra.createDirectory(Integer.toString(peer.getServerID()) + File.separator + FileHandler.BACKUP_FOLDER_NAME);
		} catch (IOException e) {
			System.out.println("Couldn't create or use directory");
		}

		synchronized (peer.getStored()) {
			peer.sortStored();
			
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
			System.out.println("File to save Data not found");
		} catch (IOException e) {
			System.out.println("IO error saving to file");
		}
	}

	/**
	 * Tries to reclaim space
	 * 
	 * @return true if could reclaim the space necessary, false otherwise
	 */
	public boolean nonPriorityReclaim() {
		
		boolean chunksRemoved = false;
		String dirPath = "";

		try {
			dirPath = Extra.createDirectory(Integer.toString(peer.getServerID()) + File.separator + FileHandler.BACKUP_FOLDER_NAME);
		} catch (IOException e) {
			System.out.println("Couldn't create or use directory");
		}
	
		synchronized (peer.getStored()) {
			peer.sortStored();
			
			Iterator<ChunkID> it = Peer.getInstance().getStored().iterator();
			
			while (this.amountReclaimed < this.reclaimSpace && it.hasNext()) {
				ChunkID chunk = it.next();
				peer.getData().getRemoveLookup().add(chunk);
				
				try {
					Thread.sleep(WAIT_STORED);
				} catch (InterruptedException e1) {
					System.out.println("Unexpected wake up of a thread sleeping");
				}
				
				// if launch is -1, mean that chunk has already gone through reclaim
				int launch = peer.getData().removeCheck(chunk.getFileID(), chunk.getChunkNumber());
				if (launch == -1)
					break;
				
				if (chunk.getActualRepDegree() > chunk.getDesiredRepDegree()) {
					chunksRemoved = reclaim(dirPath, it, chunk);
					it.remove();
				}
			}
		}
		// Save alterations to peer data
		if (chunksRemoved)
			try {
				peer.saveData();
			} catch (FileNotFoundException e) {
				System.out.println("File to save Data not found");
			} catch (IOException e) {
				System.out.println("IO error saving to file");
			}
		if (this.amountReclaimed >= this.reclaimSpace)
			return true;
		else
			return false;
	}

	/**
	 * reclaim the space of occupied from a chunk
	 * 
	 * @param dirPath location of chunk
	 * @param it position of the chunk in Storage
	 * @param chunk the chunk to free storage space
	 * @return true always
	 */
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
		
		int prevMsg=-1; int currentMsg = 0;
		int sleepTime = SLEEP_TIME;
		
		//wait to see if received putchunk message
		while(prevMsg < currentMsg){
			
			prevMsg = currentMsg;
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				System.out.println("Unexpected wake up of a thread sleeping");
			}
			
			currentMsg = peer.getData().getDeleted().get(chunk);
			sleepTime *=2;
		}

		peer.getData().removeChunkDeleted(chunk);

		this.amountReclaimed += file.length();

		file.delete();
		peer.removeChunkPeers(chunk);
		return true;
	}
}
