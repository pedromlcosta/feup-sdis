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
import data.FileID;
import extra.Extra;
import extra.FileHandler;
import messages.Message;
import messages.PutChunkMsg;
import messages.StoredMsg;
import service.Peer;

public class BackupProtocol extends Thread {

	private static final int SLEEP_TIME = 401;
	private static final int INITIAL_WAITING_TIME = 1;
	// A peer must never store the chunks of its own files.
	private Peer peer;
	private String filePath;
	private int wantedRepDegree;
	private String version = "1.0";

	public BackupProtocol(String fileName, int wantedRepDegree, String version, Peer instance) {
		this.filePath = fileName;
		this.wantedRepDegree = wantedRepDegree;
		this.version = version;
		this.peer = instance;
	}

	public BackupProtocol(Peer instance) {
		this.peer = instance;
	}

	// TODO check version && multiple case | Check as object or dataMember |
	/**
	 * starts running a backup from the file who has a path stored in filePath
	 */
	public void run() {
		System.out.println(filePath);
		FileHandler split = new FileHandler();
		split.changeFileToSplit(filePath);
		FileID fileID;
		try {
			fileID = new FileID(filePath);
		} catch (Exception e) {
			e.printStackTrace();
			fileID = null;
			split = null;
			return;
		}
		fileID.setDesiredRepDegree(wantedRepDegree);

		// If already in hash file already Send, "fileID" must match with one
		// already in the hashmap
		HashMap<String, ArrayList<FileID>> sentFiles = peer.getFilesSent();
		ArrayList<FileID> fileList;
		synchronized (sentFiles) {
			if (sentFiles.containsKey(filePath)) {
				// "File already in List";
				fileList = sentFiles.get(filePath);
				if (fileList != null && fileID != null)
					synchronized (fileList) {
						// File added
						if (!fileList.contains(fileID)) {
							fileList.add(fileID);
						} else {
							System.out.println("File already backed up");
							return;
						}
					}
			} else {
				// System.out.println("File not in List");
				fileList = new ArrayList<FileID>();
				fileList.add(fileID);
				sentFiles.put(filePath, fileList);
			}

			try {
				backupFile(split, fileID);
			} catch (Exception e) {
				e.printStackTrace();
			}

			// Finished backing up, save?
			try {
				peer.saveData();
				//peer.sendData();
			} catch (FileNotFoundException e) {
				System.out.println("File to save Data not found");
			} catch (IOException e) {
				System.out.println("IO error saving to file");
			}
		}
		System.out.println("End of backupFile");
	}

	/**
	 * backups said file by spliting the file into chunks and backing up
	 * aformentioned chunks
	 * 
	 * @param split
	 * @param fileID
	 * @throws Exception
	 */
	public void backupFile(FileHandler split, FileID fileID) throws Exception {
		System.out.println("Backup Stuff");
		byte[] chunkData;
		int currentPos = 0;
		int chunkNumber = 0;
		boolean fileDoesNotExist = false;
		try {
			do {
				// Get chunk
				chunkData = split.splitFile();
				if (chunkData == null) {
					fileDoesNotExist = true;
					break;
				}
				currentPos = chunkData.length;
				// update Chunk Number
				chunkNumber++;
				System.out.println("Chunk Number: " + chunkNumber + " ChunkData length: " + chunkData.length);
				// Send putChunk msg
				backupChunk(fileID, chunkData, chunkNumber, wantedRepDegree, version);

			} while (chunkData.length > 0 && checkNChunks(fileID, chunkNumber));

			// Empty body when the file has a size multiple of the ChunkSize
			if (fileID.isMultiple() && !fileDoesNotExist) {
				chunkNumber++;
				fileID.setnChunks(chunkNumber);
				System.out.println(currentPos + "  " + chunkNumber);
				backupChunk(fileID, new byte[0], chunkNumber, wantedRepDegree, version);
			}

		} catch (IOException e) {
			throw new Exception("IOException in backupFile");
		} catch (InterruptedException e) {
			throw new Exception("Interrupted exception in backupFile");
		}
		try {
			split.closeInputStream();
		} catch (IOException e) {
			throw new Exception("Closure of FileInputStream failed");
		}
	}

	/**
	 * 
	 * @param fileID
	 * @param chunkNumber
	 * @return false of the current Chunk number is different than the desired
	 */
	public boolean checkNChunks(FileID fileID, int chunkNumber) {
		if (fileID.getnChunks() == chunkNumber)
			return false;
		return true;
	}

	/**
	 * Is responsible for backing up a chunk
	 * 
	 * @param file
	 * @param chunkData
	 * @param chunkNumber
	 * @param wantedRepDegree
	 * @param version
	 * @throws SocketException
	 * @throws InterruptedException
	 *             most 5 PUTCHUNK messages per chunk
	 */
	public boolean backupChunk(FileID file, byte[] chunkData, int chunkNumber, int wantedRepDegree, String version) throws SocketException, InterruptedException {
		System.out.println("Backup Chunk");
		Message msg = new PutChunkMsg();
		int nMessagesSent = 0;
		boolean chunkStatus = true;
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

		HashMap<ChunkID, ArrayList<Integer>> seversAnswers = peer.getAnsweredCommand();
		synchronized (seversAnswers) {
			if (seversAnswers.containsKey(chunkToSendID)) { // Place
				System.out.println("Adding chunk");
				seversAnswers.put(chunkToSendID, new ArrayList<Integer>());
			}
		}
		long waitTime = TimeUnit.SECONDS.toNanos(INITIAL_WAITING_TIME);
		do {
			System.out.println("Wait for STORED");
			// send Message
			peer.getDataChannel().writePacket(msgPacket);
			nMessagesSent++;
			waitForStoredMsg(chunkToSend, chunkToSendID, waitTime);

			// Double the waiting time
			waitTime *= 2;
		} while (nMessagesSent < 5 && chunkToSend.getActualRepDegree() != chunkToSend.getDesiredRepDegree());
		System.out.println("End Backup Of Chunk");

		// Failed the chunk backup
		// TODO remove chunks here when it fails
		if (nMessagesSent >= 5 || chunkToSend.getActualRepDegree() != chunkToSend.getDesiredRepDegree()) {
			System.out.println("The backup of the file of the chunk Number: " + chunkNumber + " has failed to reach the desired Replication Degree: " + wantedRepDegree
					+ " instead the actual degree is: " + chunkToSend.getActualRepDegree());
			chunkStatus = false;
		}

		return chunkStatus;
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

	/**
	 * 
	 * Receives a putChunk message
	 * 
	 * @param putchunkMSG
	 * @param fullBackup
	 *            -> if we are supposed to backup the chunk or just send the
	 *            stored message
	 */
	public void putChunkReceive(Message putchunkMSG, boolean fullBackup) {
		Message msg = new StoredMsg();
		String dirPath = "";
		String args[] = new String[4];
		String fileID = putchunkMSG.getFileId();

		if (Peer.getInstance().fileAlreadySent(fileID)) {
			System.out.println("backingup Own file");
			return;
		}

		try {
			peer.createPeerFolder();
		} catch (IOException e2) {
		}

		try {
			dirPath = Extra.createDirectory(Integer.toString(peer.getServerID()) + File.separator + FileHandler.BACKUP_FOLDER_NAME);
		} catch (IOException e1) {
		}

		// Version
		args[0] = getVersion();
		// SenderID
		args[1] = Integer.toString(peer.getServerID());
		// FileID
		args[2] = fileID;
		// Chunk No
		args[3] = Integer.toString(putchunkMSG.getChunkNo());
		byte msgData[] = putchunkMSG.getMessageData();

		// starts the chunk backup
		if (fullBackup) {
			Chunk chunk = new Chunk(new ChunkID(fileID, putchunkMSG.getChunkNo()), msgData);
			chunk.setDesiredRepDegree(putchunkMSG.getReplicationDeg());
			ChunkID id = chunk.getId();
			int index;
			ArrayList<ChunkID> storedList = peer.getStored();
			// Update chunk instances
			synchronized (storedList) {
				if ((index = storedList.indexOf(id)) < 0) {
					// Not in the list so added
					chunk.setDesiredRepDegree(putchunkMSG.getReplicationDeg());
					chunk.setActualRepDegree(0);
					peer.addChunk(chunk.getId());
				} else {
					storedList.get(index).increaseRepDegree();
				}
			}
			Peer.getInstance().addSenderToAnswered(id, peer.getServerID());

			// Save alterations to peer data
			try {
				peer.saveData();
				//peer.sendData();
				peer.requestData();
			} catch (FileNotFoundException e) {
				System.out.println("File to save Data not found");
			} catch (IOException e) {
				System.out.println("IO error saving to file");
			}
			writeChunk(dirPath, chunk, id);
		}

		sendStoredMsg(msg, args);

	}

	/**
	 * writes in disk a chunk
	 * 
	 * @param dirPath
	 * @param chunk
	 * @param id
	 */
	public void writeChunk(String dirPath, Chunk chunk, ChunkID id) {
		try {

			FileOutputStream fileWriter = new FileOutputStream(dirPath + File.separator + id.getFileID() + "_" + id.getChunkNumber());
			ObjectOutputStream out = new ObjectOutputStream(fileWriter);

			out.writeObject(chunk);
			out.close();
		} catch (FileNotFoundException e1) {
			System.out.println("FileNotFound in writeChunk");
			e1.printStackTrace();
		} catch (IOException e) {
			System.out.println("IOException in writeChunk");
			e.printStackTrace();
		}
	}

	/**
	 * Sends a stored message to the control channel
	 * 
	 * @param msg
	 * @param args
	 */
	// TODO CHECK CHANGE TO STATIC
	public static void sendStoredMsg(Message msg, String[] args) {
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

	// return the number of server who have answered the putchunk msg
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
		return filePath;
	}

	public void setFileName(String fileName) {
		this.filePath = fileName;
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