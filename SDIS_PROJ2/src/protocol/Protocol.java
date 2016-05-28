package protocol;

import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.Random;

import chunk.Chunk;
import chunk.ChunkID;
import messages.Message;
import service.Peer;

public class Protocol extends Thread {
	private static final int ONE_SEC_IN_MILI_SEC = 1000;
	static final int SLEEP_TIME = 401;
	static final int MAX_MESSAGES_TO_SEND = 5;
	static final int INITIAL_WAITING_TIME = 1;
	Peer peer = Peer.getInstance();
	Random randomSeed = new Random();
	String filePath;

	public static boolean checkTrackerStatus() {
		// PeerFlag
		if (!Peer.getInstance().getTrackerConnection().isConnectionOnline()) {
			System.out.println("Cannot start new Protocol Tracker is offline");
			return false;
		}
		return true;
	}

	/**
	 * Sends a stored message to the control channel
	 * 
	 * @param msg
	 * @param args
	 */
	public void sendStoredMsg(Message msg, String[] args) {
		// create message and packets
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

	/**
	 * waits to see if it has enough stored msg per chunk
	 * 
	 * @param chunkToSend
	 * @param chunkToSendID
	 * @param waitTime
	 * @return
	 */
	public void waitForStoredMsg(Chunk chunkToSend, ChunkID chunkToSendID, long waitTime) {
		long startTime = System.nanoTime();
		ArrayList<Integer> serverWhoAnswered;
		do {
			if ((serverWhoAnswered = Peer.getInstance().getAnsweredCommand().get(chunkToSendID)) != null && !serverWhoAnswered.isEmpty()) {
				synchronized (serverWhoAnswered) {
					int size = serverWhoAnswered.size();
					if (chunkToSend.getDesiredRepDegree() == size) {
						chunkToSend.setActualRepDegree(size);
						break;
					}
				}
			}
		} while ((System.nanoTime() - startTime) < waitTime);
	}

	public Peer getPeer() {
		return peer;
	}

	public void setPeer(Peer peer) {
		this.peer = peer;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public static int getSleepTime() {
		return SLEEP_TIME;
	}

}
