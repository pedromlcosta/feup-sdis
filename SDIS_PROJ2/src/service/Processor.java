package service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import channels.MDRReceiver;
import chunk.Chunk;
import chunk.ChunkID;
import extra.Extra;
import extra.FileHandler;
import file.FileID;
import messages.ChunkMsg;
import messages.DeleteMsg;
import messages.GetChunkMsg;
import messages.Message;
import messages.PutChunkMsg;
import messages.RemovedMsg;
import messages.StoredMsg;
import messages.WakeMsg;
import protocol.BackupProtocol;
import protocol.WakeProtocol;

public class Processor extends Thread {

	private static final int MAX_WAIT = 400;
	private static ArrayList<ChunkID> waitLookup = new ArrayList<ChunkID>();
	private final long GETCHUNK_WAITING_NANO = TimeUnit.MILLISECONDS.toNanos(400); // In
	// milliseconds
	private String messageString;
	// TODO either this or turn parseHeader Static
	private Message msg;
	private byte[] messageBody = null;
	private String[] messageFields = null;

	/**
	 * Processor constructor
	 * 
	 * @param messageString
	 *            string received, corresponding to a message
	 */
	public Processor(String messageString) {
		this.messageString = messageString;
	}

	// Criado por causa das mudanças no receiver e evitar fazer o parseHeader 2x
	/**
	 * Processor constructor
	 * 
	 * @param headerArgs
	 *            arguments of the header of the message
	 * @param body
	 *            body of the message
	 */
	public Processor(String[] headerArgs, byte[] body) {
		this.messageFields = headerArgs;
		this.messageBody = body;
	}

	/**
	 * Processor constructor
	 * 
	 * @param header
	 *            String that represents the header of the message
	 * @param body
	 *            body of the message
	 */
	public Processor(String header, byte[] body) {
		this.messageString = header;
		this.messageBody = body;
	}

	/**
	 * Main processor thread. After the constructor is called, this method
	 * accesses the message that was stored in the datamembers and calls the
	 * adequate handlers
	 */
	public void run() {
		// HANDLE MESSAGES HERE
		// TODO msg != null &&
		if (messageString != null || messageFields != null) {
			// legacy reasons (in case some function uses this one)
			if (messageFields == null)
				messageFields = Message.parseHeader(messageString);
			messageString = ""; // Empty, so as not to fill unnecessary space

			// System.out.println(messageFields[0]);

			switch (messageFields[0]) {
			case "PUTCHUNK": {
				msg = new PutChunkMsg(messageFields, messageBody);

				// Unreserve the now unneeded array space, while the processor
				// handles the message
				messageFields = null;
				reclaimCheck(msg.getFileId(), msg.getChunkNo());
				// ignore message if chunk is set to deleted
				if (ignoreMessage())
					break;
				putChunkHandler();
				break;
			}
			case "STORED":
				msg = new StoredMsg(messageFields, messageBody);
				messageFields = null;
				storedHandler();
				break;
			case "GETCHUNK":
				msg = new GetChunkMsg(messageFields, messageBody);

				messageFields = null;
				try {
					getChunkHandler();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case "CHUNK":
				msg = new ChunkMsg(messageFields, messageBody);

				messageFields = null;
				chunkHandler();
				break;
			case "DELETE":
				msg = new DeleteMsg(messageFields, messageBody);

				messageFields = null;
				deleteHandler();
				break;
			case "REMOVED":
				msg = new RemovedMsg(messageFields, messageBody);
				messageFields = null;
				Peer.getInstance().getData().removeCheck(msg.getFileId(), msg.getChunkNo());
				removeHandler();
				break;
			case "WAKEUP":
				msg = new WakeMsg(messageFields, messageBody);
				messageFields = null;
				wakeupHandler(msg);
				break;

			default:
				break;
			}
		}
	}

	/**
	 * check if received a putchunk for a chunk while waiting before starting
	 * Backup Protocol
	 * 
	 * @param fileId
	 *            - identifier of file
	 * @param chunkNo
	 *            - number of chunk
	 * @return -1 if received a putchunk and shouldn't start Backup Protocol,
	 *         any number otherwise
	 */
	private int reclaimCheck(String fileId, int chunkNo) {

		ChunkID chunk = new ChunkID(fileId, chunkNo);
		int index = waitLookup.indexOf(chunk);

		if (index != -1)
			waitLookup.remove(index);
		return index;
	}

	/**
	 * ignores up to 5 messages putchunk messages if the identifier of chunk in
	 * that message is in the container of chunks to be ignored
	 * 
	 * @return true if message shall be ignored, false otherwise
	 */
	private boolean ignoreMessage() {
		ChunkID tmp = new ChunkID(msg.getFileId(), msg.getChunkNo());
		if (Peer.getInstance().getData().getDeleted().get(tmp) != null) {
			Peer.getInstance().getData().incChunkDeleted(tmp);
			if (Peer.getInstance().getData().getDeleted().get(tmp) <= 5)
				return true;
		}
		return false;
	}

	/**
	 * Handles DELETE messages
	 */
	private void deleteHandler() {

		Peer peer = Peer.getInstance();
		String fileId = msg.getFileId();
		String dirPath = "";

		try {
			dirPath = Extra.createDirectory(Integer.toString(peer.getServerID()) + File.separator + FileHandler.BACKUP_FOLDER_NAME);
		} catch (IOException e) {
			System.out.println("Couldn't create or use directory");
		}

		ArrayList<ChunkID> chunks = peer.getStored();
		synchronized (chunks) {
			for (Iterator<ChunkID> it = chunks.iterator(); it.hasNext();) {
				ChunkID chunk = it.next();
				String idToConfirm = chunk.getFileID();
				// if chunk belongs to file delete chunk and stored
				if (fileId.equals(idToConfirm)) {
					File file = new File(dirPath + File.separator + fileId + "_" + chunk.getChunkNumber());
					file.delete();
					it.remove();
					peer.removeChunkPeers(chunk);
				}
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

		File file = new File(dirPath);
		Extra.recursiveDelete(file);
	}

	/**
	 * Handles CHUNK messages
	 */
	private void chunkHandler() {

		ChunkID chunkID = new ChunkID(msg.getFileId(), msg.getChunkNo());
		MDRReceiver restoreChannel = Peer.getInstance().getRestoreChannel();

		// Received a chunk and was expecting it for a restore
		if (restoreChannel.expectingRestoreChunks(chunkID.getFileID())) {
			restoreChannel.addRestoreChunk(chunkID.getFileID(), new Chunk(chunkID, msg.getBody()));

		} else {
			// Received a chunk whose file wasn't being restored

			restoreChannel.receivedForeignChunk(chunkID);
		}

	}

	/**
	 * Handles GETCHUNK messages
	 * 
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	private void getChunkHandler() throws ClassNotFoundException, IOException {

		ChunkID chunkID = new ChunkID(msg.getFileId(), msg.getChunkNo());
		MDRReceiver restore = Peer.getInstance().getRestoreChannel();
		FileHandler fileHandler = new FileHandler();

		if (Peer.getInstance().hasChunkStored(chunkID)) {

			// Start waiting for chunks with this ID
			restore.expectingForeignChunk(chunkID, true);

			try {
				Thread.sleep((new Random()).nextInt(401));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			// Check if any chunks expected arrived while sleeping
			if (!restore.wasForeignChunkReceived(chunkID)) {
				// enviar mensagem com o chunk

				System.out.println("No foreign chunk received! Going to send, as requests, chunk nr. " + chunkID.getChunkNumber());

				byte[] chunkBody = new byte[0];
				try {
					chunkBody = fileHandler.loadChunkBody(chunkID);
				} catch (IOException e) {
					e.getMessage();
					e.printStackTrace();
					System.out.println("Wasn't able to load chunk nr. " + chunkID.getChunkNumber() + " from file id: " + chunkID.getFileID());
					chunkBody = new byte[0];
				}

				String[] args = { "1.0", Integer.toString(Peer.getInstance().getServerID()), chunkID.getFileID(), Integer.toString(chunkID.getChunkNumber()) };

				// byte[] chunkBody = new byte[64];
				Message chunkMsg = new ChunkMsg();
				if (chunkMsg.createMessage(chunkBody, args) == true) {
					DatagramPacket packet = restore.createDatagramPacket(chunkMsg.getMessageBytes());
					restore.writePacket(packet);
				} else {
					System.out.println("Wasn't able to create and send chunk message");
				}

			} else {
				System.out.println("Received a foreign chunk with nr: " + chunkID.getChunkNumber());
			}

			// Stop waiting for chunks with this ID
			restore.expectingForeignChunk(chunkID, false);

		} else {
			System.out.println("Dont have it stored, sorry!");
		}

	}

	/**
	 * Handles STORED messages
	 */
	private void storedHandler() {

		ChunkID chunkID = new ChunkID(this.msg.getFileId(), this.msg.getChunkNo());
		Peer.getInstance().addSenderToAnswered(chunkID, Integer.parseInt(this.msg.getSenderID()));

	}

	/**
	 * Handles PUTCHUNK messages
	 */
	private void putChunkHandler() {
		boolean fullBackup;
		String dirPath = "";
		try {
			dirPath = Extra.createDirectory(Integer.toString(Peer.getInstance().getServerID()) + File.separator + FileHandler.BACKUP_FOLDER_NAME);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		long backupFolderSize = Extra.getFolderSize(dirPath);
		boolean canBackup = Peer.getInstance().reclaimDiskSpace(backupFolderSize, this.msg.getBody().length);

		ChunkID chunkID = new ChunkID(this.msg.getFileId(), this.msg.getChunkNo());
		ArrayList<Integer> answered = Peer.getInstance().getAnsweredCommand().get(chunkID);
		if (answered != null) {
			synchronized (answered) {
				ArrayList<ChunkID> stored = Peer.getInstance().getStored();
				// if a chunk has stored msg associated to him, it means it must
				// also be stored
				synchronized (stored) {
					int index = stored.indexOf(chunkID);
					// Only lets store chunks with actualDegree lower than
					// desiredDegree
					if (index >= 0 && answered.size() < stored.get(index).getDesiredRepDegree()) {
						fullBackup = true;
						System.out.println("Peer: " + Peer.getInstance().getServerID());
						System.out.println("Checking available space: " + (PeerData.getDiskSize() - backupFolderSize));
						System.out.println(PeerData.getDiskSize() + "   " + backupFolderSize);
						// Is the disk full? if not backup if it is full,was
						// able to
						// free some space?
						if (canBackup)
							new BackupProtocol(Peer.getInstance()).putChunkReceive(this.msg, fullBackup);
					} else
						System.out.println("enough chunks,all already saved");
					if (index >= 0) {
						fullBackup = false;
						canBackup = true;
					} else
						fullBackup = true;
					if (canBackup)
						new BackupProtocol(Peer.getInstance()).putChunkReceive(this.msg, fullBackup);
				}
			}
		} else {
			fullBackup = true;
			if (canBackup)
				new BackupProtocol(Peer.getInstance()).putChunkReceive(this.msg, fullBackup);
		}

	}

	/**
	 * Handles REMOVE messages
	 */
	private void removeHandler() {

		Peer peer = Peer.getInstance();
		// check if chunkId exist in database
		ChunkID tmp = new ChunkID(msg.getFileId(), msg.getChunkNo());
		ArrayList<ChunkID> stored = peer.getStored();
		int index, desiredRepDegree;
		synchronized (stored) {
			index = peer.getStored().indexOf(tmp);
			if (index == -1)
				return;

			// report loss of chunk
			peer.removeChunkPeer(tmp, Integer.valueOf(msg.getSenderID()));

			// update actualRepDegree
			peer.getStored().get(index).decreaseRepDegree();

			int actualRepDegree = peer.getStored().get(index).getActualRepDegree();
			desiredRepDegree = peer.getStored().get(index).getDesiredRepDegree();

			if (desiredRepDegree <= actualRepDegree)
				return;
			// sleep between 0 to 400 ms
			waitLookup.add(tmp);
			int sleepTime = new Random().nextInt(MAX_WAIT);
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				System.out.println("Unexpected wake up of a thread sleeping");
			}
		}

		// if launch is -1, mean that chunk has already gone through putChunk
		int launch = reclaimCheck(tmp.getFileID(), tmp.getChunkNumber());
		if (launch == -1)
			return;

		// if not received putChunk, launch
		FileID fileId = new FileID();
		fileId.setID(tmp.getFileID());

		try {
			Extra.createDirectory(FileHandler.BACKUP_FOLDER_NAME);
		} catch (IOException e) {
			System.out.println("Couldn't create or use directory");
		}

		FileHandler fileHandler = new FileHandler();

		byte[] chunkBody = new byte[0];
		try {
			chunkBody = fileHandler.loadChunkBody(tmp);
		} catch (SocketException | ClassNotFoundException e) {
			System.out.println("Couldn't load chunk");
		} catch (IOException e) {
			// If fails, chunkBody will be empty
			System.out.println("Wasn't able to load chunk nr. " + tmp.getChunkNumber() + " from file id: " + fileId);
			chunkBody = new byte[0];
		}

		try {
			new BackupProtocol(Peer.getInstance()).backupChunk(fileId, chunkBody, tmp.getChunkNumber(), desiredRepDegree, "1.0");
		} catch (SocketException | InterruptedException e) {
			System.out.println("Error launching Backup Protocol in reclaiming");
		}
	}
	/**
	 * Handles WakeMsgs 
	 * @param wakeupMSG
	 */
	public void wakeupHandler(Message wakeupMSG) {
		(new WakeProtocol()).receiveWakeUp(wakeupMSG);
	}

	public String getMessageString() {
		return messageString;
	}

	public void setMessageString(String messageString) {
		this.messageString = messageString;
	}

	public Message getMsg() {
		return msg;
	}

	public void setMsg(Message msg) {
		this.msg = msg;
	}

	public long getGETCHUNK_WAITING_NANO() {
		return GETCHUNK_WAITING_NANO;
	}

	public static ArrayList<ChunkID> getWaitLookup() {
		return waitLookup;
	}

	public static void setWaitLookup(ArrayList<ChunkID> waitLookup) {
		Processor.waitLookup = waitLookup;
	}

	public byte[] getMessageBody() {
		return messageBody;
	}

	public void setMessageBody(byte[] messageBody) {
		this.messageBody = messageBody;
	}

	public static int getMaxWait() {
		return MAX_WAIT;
	}
}
