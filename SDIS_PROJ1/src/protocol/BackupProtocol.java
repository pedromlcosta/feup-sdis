package protocol;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import chunk.Chunk;
import chunk.ChunkID;
import extra.Extra;
import file.FileID;
import messages.FileHandler;
import messages.Message;
import messages.PutChunkMsg;
import service.Peer;

public class BackupProtocol extends Thread {

	private static final int SLEEP_TIME = 401;
	private static final int INITIAL_WAITING_TIME = 1;
	// A peer must never store the chunks of its own files.
	private Peer peer;
	private String fileName;
	private int wantedRepDegree;
	private String version;

	public BackupProtocol(String fileName, int wantedRepDegree, String version, Peer instance) {
		this.fileName = fileName;
		this.wantedRepDegree = wantedRepDegree;
		this.version = version;
		this.peer = instance;
	}

	public BackupProtocol(Peer instance) {
		this.peer = instance;
	}

	// TODO check version && multiple case | Check as object or dataMember |
	/**
	 * 
	 */
	public void run() {
		System.out.println(fileName);
		FileHandler split = new FileHandler();
		split.changeFileToSplit(fileName);
		FileID fileID = new FileID(fileName);
		fileID.setDesiredRepDegree(wantedRepDegree);

		// If already in hash file already Send, "fileID" must match with one
		// already in the hashmap
		HashMap<String, FileID> sentFiles = peer.getFilesSent();
		synchronized (sentFiles) {
			if (sentFiles.containsKey(fileID.getID()))
				return;
			sentFiles.put(fileID.getID(), fileID);
		}
		backupFile(split, fileID);
		System.out.println("End of backupFile");
	}

	/**
	 * 
	 * @param split
	 * @param fileID
	 */
	public void backupFile(FileHandler split, FileID fileID) {
		System.out.println("Backup Stuff");
		byte[] chunk;
		int currentPos;
		int chunkNumber = 0;
		try {
			do {
				// Get chunk
				chunk = split.splitFile();
				currentPos = chunk.length;
				// update Chunk Number
				chunkNumber++;
				System.out.println("Chunk Number: " + chunkNumber);
				// Send putChunk msg
				backupChunk(fileID, chunk, chunkNumber, wantedRepDegree, version);

			} while (chunk.length > 0 && checkNChunks(fileID, chunkNumber));

			// Empty body when the file has a size multiple of the ChunkSize
			if (fileID.isMultiple()) {
				chunkNumber++;
				fileID.setnChunks(chunkNumber);
				System.out.println(currentPos + "  " + chunkNumber);
				backupChunk(fileID, new byte[0], chunkNumber, wantedRepDegree, version);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param fileID
	 * @param chunkNumber
	 * @return
	 */
	public boolean checkNChunks(FileID fileID, int chunkNumber) {
		if (fileID.getnChunks() == chunkNumber)
			return false;
		return true;
	}

	// TODO most 5 PUTCHUNK messages per chunk. check about server ID
	/**
	 * 
	 * @param file
	 * @param chunkData
	 * @param chunkNumber
	 * @param wantedRepDegree
	 * @param version
	 * @throws SocketException
	 * @throws InterruptedException
	 */
	public void backupChunk(FileID file, byte[] chunkData, int chunkNumber, int wantedRepDegree, String version) throws SocketException, InterruptedException {
		System.out.println("Backup Chunk");
		Message msg = new PutChunkMsg();
		int nMessagesSent = 0;
		// Create Chunk
		Chunk chunkToSend = new Chunk(file.getID(), chunkNumber, chunkData);
		chunkToSend.getId().setDesiredRepDegree(wantedRepDegree);
		chunkToSend.getId().setActualRepDegree(0);
		ChunkID chunkToSendID = chunkToSend.getId();

		// createMessage
		String[] args = new String[5];
		args[0] = version;
		args[1] = Integer.toString(peer.getServerID());
		args[2] = file.getID();
		args[3] = Integer.toString(chunkNumber);
		args[4] = Integer.toString(wantedRepDegree);
		msg.createMessage(chunkData, args);

		// Send Mensage
		DatagramPacket msgPacket = peer.getDataChannel().createDatagramPacket(msg.getMessageBytes()); //
		// TODO when should we clean ArrayList to avoid having "false positives"
		HashMap<ChunkID, ArrayList<Integer>> seversAnswers = peer.getAnsweredCommand();
		synchronized (seversAnswers) {
			if (seversAnswers.containsKey(chunkToSendID)) { // Place
				System.out.println("Adding chunk");
				seversAnswers.put(chunkToSendID, new ArrayList<Integer>());
			} else // Replace
				seversAnswers.replace(chunkToSendID, new ArrayList<Integer>());
		}
		long waitTime = TimeUnit.SECONDS.toNanos(INITIAL_WAITING_TIME);
		do {
			System.out.println("Wait for STORED");
			// send Message
			peer.getDataChannel().writePacket(msgPacket);
			nMessagesSent++;
			waitForStoredMsg(chunkToSend, chunkToSendID, waitTime);

			System.out.println("N Message: " + nMessagesSent + " N stored chunks: " + chunkToSend.getActualRepDegree());

			waitTime *= 2;
		} while (nMessagesSent < 5 && chunkToSend.getActualRepDegree() != chunkToSend.getDesiredRepDegree());
		System.out.println("End Backup Of Chunk");
	}

	/**
	 * 
	 * @param chunkToSend
	 * @param chunkToSendID
	 * @param waitTime
	 * @return
	 */
	public long waitForStoredMsg(Chunk chunkToSend, ChunkID chunkToSendID, long waitTime) {
		long startTime = System.nanoTime();
		long elapsedTime;
		ArrayList<Integer> serverWhoAnswered;
		do {
			if ((serverWhoAnswered = Peer.getInstance().getAnsweredCommand().get(chunkToSendID)) != null && !serverWhoAnswered.isEmpty()) {
				synchronized (serverWhoAnswered) {
					int size = serverWhoAnswered.size();
					if (chunkToSend.getDesiredRepDegree() == size) {
						chunkToSend.setActualRepDegree(size);
						// TODO delete with System.out.println
						elapsedTime = -1;
						break;
					}
				}
			}
		} while ((elapsedTime = System.nanoTime() - startTime) < waitTime);
		return elapsedTime;
	}

	// 0 and 400 ms. delay
	/**
	 * 
	 * @param putchunkMSG
	 */
	public void putChunkReceive(Message putchunkMSG) {
		Message msg = new Message();
		String dirPath = "";
		String args[] = new String[4];
		try {
			dirPath = Extra.createDirectory(Integer.toString(peer.getServerID()) + File.pathSeparator + FileHandler.BACKUP_FOLDER_NAME);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		if (Integer.parseInt(putchunkMSG.getSenderID()) == peer.getServerID()) {
			System.out.println("Either the Msg was you tried to use the same server ");
			System.out.println(msg.getMessageToSend());
			return;
		}
		String fileID = putchunkMSG.getFileId();

		// Version
		args[0] = getVersion();
		// SenderID
		args[1] = Integer.toString(peer.getServerID());
		// FileID
		args[2] = fileID;
		// Chunk No
		args[3] = Integer.toString(putchunkMSG.getChunkNo());
		byte msgData[] = msg.getMessageData();

		// TODO good idea?
		Chunk chunk = new Chunk(new ChunkID(args[2], Integer.parseInt(args[2])), msgData);
		ChunkID id = chunk.getId();
		int index;
		ArrayList<ChunkID> storedList = peer.getStored();
		// TODO Check if condition makes sense, check here
		synchronized (storedList) {
			if ((index = storedList.indexOf(id)) < 0) {
				// Not in the list so added
				chunk.setDesiredRepDegree(putchunkMSG.getReplicationDeg());
				chunk.setActualRepDegree(1);
				peer.addChunk(chunk.getId());
			} else {
				// just need to increment Rep Degree
				storedList.get(index).increaseRepDegree();
			}
		}

		writeChunk(dirPath, chunk, id);
		sendStoredMsg(msg, args);

	}

	/**
	 * 
	 * @param dirPath
	 * @param chunk
	 * @param id
	 */
	public void writeChunk(String dirPath, Chunk chunk, ChunkID id) {
		// Write Chunk
		try {
			FileOutputStream fileWriter = new FileOutputStream(dirPath + File.separator + id.getFileID() + "_" + id.getChunkNumber());
			ObjectOutputStream out = new ObjectOutputStream(fileWriter);
			// TODO OR out.writeObject(chunk.getData());
			out.writeObject(chunk);
			fileWriter.close();
			out.close();
		} catch (FileNotFoundException e1) {
			System.out.println("FileNotFound");
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param msg
	 * @param args
	 */
	public void sendStoredMsg(Message msg, String[] args) {
		// create message and packets
		msg.createMessage(null, args);
		DatagramPacket packet = peer.getControlChannel().createDatagramPacket(msg.getMessageBytes());

		// get Random Delay
		int delay = new Random().nextInt(SLEEP_TIME);
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// send message
		peer.getControlChannel().writePacket(packet);
	}

	public int checkMessagesReceivedForChunk(ChunkID chunkToSendID) {
		ArrayList<Integer> servers = peer.getAnsweredCommand().get(chunkToSendID);
		if (servers != null)
			return servers.size();
		return 0;
	}

	// Gets and Sets
	public Peer getPeer() {
		return peer;
	}

	public void setPeer(Peer peer) {
		this.peer = peer;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public int getWantedRepDegree() {
		return wantedRepDegree;
	}

	public void setWantedRepDegree(int wantedRepDegree) {
		this.wantedRepDegree = wantedRepDegree;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public static int getInitialWaitingTime() {
		return INITIAL_WAITING_TIME;
	}

}