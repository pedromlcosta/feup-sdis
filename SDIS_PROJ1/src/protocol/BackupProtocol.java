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
import messages.StoredMsg;
import service.Peer;

public class BackupProtocol extends Thread {

	private static final int SLEEP_TIME = 401;
	private static final int INITIAL_WAITING_TIME = 1;
	// A peer must never store the chunks of its own files.
	private Peer peer;
	private String fileName;
	private int wantedRepDegree;
	private String version = "1.0";

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
		FileID fileID;
		try {
			fileID = new FileID(fileName);
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
			if (sentFiles.containsKey(fileID.getID())) {
				// System.out.println("File already in List");
				fileList = sentFiles.get(fileID.getID());
				synchronized (fileList) {
					if (!fileList.contains(fileID)) {
						fileList.add(fileID);
						// System.out.println("File added");
					} else {
						System.out.println("File already backed up");
						return;
					}
				}
			} else {
				// System.out.println("File not in List");
				fileList = new ArrayList<FileID>();
				fileList.add(fileID);
				sentFiles.put(fileID.getID(), fileList);
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

			backupFile(split, fileID);

			// Finished backing up, save?
			try {
				peer.saveData();
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		System.out.println("End of backupFile");
	}

	/**
	 * 
	 * @param split
	 * @param fileID
	 */
	public void backupFile(FileHandler split, FileID fileID) {
		System.out.println("Backup Stuff");
		byte[] chunkData;
		int currentPos;
		int chunkNumber = 0;
		try {
			do {
				// Get chunk
				chunkData = split.splitFile();
				currentPos = chunkData.length;
				// update Chunk Number
				chunkNumber++;
				System.out.println("Chunk Number: " + chunkNumber + " ChunkData length: " + chunkData.length);
				// Send putChunk msg
				backupChunk(fileID, chunkData, chunkNumber, wantedRepDegree, version);

			} while (chunkData.length > 0 && checkNChunks(fileID, chunkNumber));

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

		// TODO when should we clean ArrayList to avoid having "false positives"
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

			System.out.println("N Message: " + nMessagesSent + " N stored chunks: " + chunkToSend.getActualRepDegree());

			waitTime *= 2;
		} while (nMessagesSent < 5 && chunkToSend.getActualRepDegree() != chunkToSend.getDesiredRepDegree());
		System.out.println("End Backup Of Chunk");
		if (nMessagesSent >= 5 || chunkToSend.getActualRepDegree() != chunkToSend.getDesiredRepDegree()) {
			System.out.println("The backup of the file of the chunk Number: " + chunkNumber + " has failed to reach the desired Replication Degree: " + wantedRepDegree
					+ " instead the actual degree is: " + chunkToSend.getActualRepDegree());
			chunkStatus = false;
		}

		return chunkStatus;
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
						System.out.println("Got enough stored. Breaking out at " + (System.nanoTime() - startTime));
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
	public void putChunkReceive(Message putchunkMSG, boolean fullBackup) {
		Message msg = new StoredMsg();
		String dirPath = "";
		String args[] = new String[4];
		String fileID = putchunkMSG.getFileId();
		// What happens when two peers from the same PC try to backup the same
		// file
		if (Peer.getInstance().getFilesSent().get(fileID) != null) {
			System.out.println("backingup Own file");
			return;
		} else {
			System.out.println("NOT OWN File: " + putchunkMSG.getFileId() + "    " + putchunkMSG.getSenderID());
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
		// TODO good idea?
		if (fullBackup) {
			System.out.println("Fullbackup");
			Chunk chunk = new Chunk(new ChunkID(fileID, putchunkMSG.getChunkNo()), msgData);
			chunk.setDesiredRepDegree(putchunkMSG.getReplicationDeg());
			ChunkID id = chunk.getId();
			int index;
			ArrayList<ChunkID> storedList = peer.getStored();
			// TODO Check if condition makes sense, check here
			synchronized (storedList) {
				if ((index = storedList.indexOf(id)) < 0) {
					// Not in the list so added
					chunk.setDesiredRepDegree(putchunkMSG.getReplicationDeg());
					chunk.setActualRepDegree(0);
					peer.addChunk(chunk.getId());
				} else {
					// just need to increment Rep Degree
					storedList.get(index).increaseRepDegree();
				}
			}
			Peer.getInstance().addSenderToAnswered(id, peer.getServerID());

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

			writeChunk(dirPath, chunk, id);
		} else
			System.out.println("Just send MSG");
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
		//
		try {

			FileOutputStream fileWriter = new FileOutputStream(dirPath + File.separator + id.getFileID() + "_" + id.getChunkNumber());
			ObjectOutputStream out = new ObjectOutputStream(fileWriter);
			// TODO OR out.writeObject(chunk.getData());
			System.out.println("Data to Write: " + chunk.getData().length);
			byte[] arr = new byte[3];
			arr[0] = 'l';
			arr[1] = 'a';
			arr[2] = 'c';
			// out.writeObject(chunk.getData());
			out.writeObject(chunk);

			out.close();
		} catch (FileNotFoundException e1) {
			System.out.println("FileNotFound");
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return;
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