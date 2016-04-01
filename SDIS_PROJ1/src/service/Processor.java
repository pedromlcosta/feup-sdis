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
import file.FileID;
import messages.ChunkMsg;
import messages.DeleteMsg;
import messages.FileHandler;
import messages.GetChunkMsg;
import messages.Message;
import messages.PutChunkMsg;
import messages.RemovedMsg;
import messages.StoredMsg;
import protocol.BackupProtocol;

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

	public Processor(String messageString) {
		this.messageString = messageString;
	}

	// Criado por causa das mudanças no receiver e evitar fazer o parseHeader 2x
	public Processor(String[] headerArgs, byte[] body) {
		this.messageFields = headerArgs;
		this.messageBody = body;
	}

	public Processor(String header, byte[] body) {
		this.messageString = header;
		this.messageBody = body;
	}

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
			case "PUTCHUNK":
				msg = new PutChunkMsg(messageFields, messageBody);

				// Unreserve the now unneeded array space, while the processor
				// handles the message
				messageFields = null;
				reclaimCheck(msg.getFileId(), msg.getChunkNo());
			// ignore message if chunk is set to deleted
			{
				ChunkID tmp = new ChunkID(msg.getFileId(), msg.getChunkNo());
				if (Peer.getInstance().getData().getDeleted().contains(tmp))
					break;
			}
				putChunkHandler();
				break;
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
				removeHandler();
				break;
			default:
				break;
			}
		}
	}

	private int reclaimCheck(String fileId, int chunkNo) {

		ChunkID chunk = new ChunkID(fileId, chunkNo);
		int index = waitLookup.indexOf(chunk);

		if (index != -1)
			waitLookup.remove(index);
		return index;
	}

	private void deleteHandler() {

		Peer peer = Peer.getInstance();
		String fileId = msg.getFileId();
		String dirPath = "";

		try {
			dirPath = Extra.createDirectory(Integer.toString(peer.getServerID()) + File.separator + FileHandler.BACKUP_FOLDER_NAME);
		} catch (IOException e) {
			e.printStackTrace();
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		File file = new File(dirPath);
		Extra.recursiveDelete(file);
	}

	private void chunkHandler() {

		ChunkID chunkID = new ChunkID(msg.getFileId(), msg.getChunkNo());
		MDRReceiver restoreChannel = Peer.getInstance().getRestoreChannel();

		// Received a chunk and was expecting it for a restore
		if (restoreChannel.expectingRestoreChunks(chunkID.getFileID())) {
			restoreChannel.addRestoreChunk(chunkID.getFileID(), new Chunk(chunkID, msg.getBody()));
			System.out.println("Received an expected chunk.");
		} else { // Received a chunk whose file wasn't being restored
			System.out.println("Received a foreign chunk.");
			restoreChannel.receivedForeignChunk(chunkID);
		}

		// 1o - Verificar se o Chunk pertence a um ficheiro em restore

		// 2o - Se pertencer, guardar em chunksBeingReceived

		// 3o - Se não pertencer, guardar só a informação que foi recebido um
		// chunk
		// So ver se e valido e adicionar aos chunks esperados desse ficheiro...
	}

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
					System.out.println("Wasn't able to create and send chunk message");
				}

			} else {
				System.out.println("Received a foreign chunk with nr: " + chunkID.getChunkNumber());
			}

			// Stop waiting for chunks with this ID
			restore.expectingForeignChunk(chunkID, false);

		} else {
			/*
			 * System.out.println("Stored size: " +
			 * Peer.getInstance().getData().getStored().size());
			 * 
			 * for(int i = 0; i<
			 * Peer.getInstance().getData().getStored().size(); i++){
			 * System.out.println("Chunk nr. " +
			 * Peer.getInstance().getData().getStored().get(i).getChunkNumber()
			 * +" with fileID: " +
			 * Peer.getInstance().getData().getStored().get(i).getFileID()); }
			 * 
			 * System.out.println(
			 * "And our chunkID we were comparing to has chunk nr. " +
			 * chunkID.getChunkNumber()+ " and fileID " + chunkID.getFileID() );
			 */
			System.out.println("Dont have it stored, sorry!");
		}

	}

	private void storedHandler() {
		// I got a stored now I need to send them to the queue, we should have a
		// Task that goes through that List and checks the Lists and such
		// Maybe I do not need to keep on reading but just wait the time and
		// then wait for a few secs and check the number of ppl who replied ?
		// Filipe places the message in a queue? that will be read by the
		// protocole handling the putcunk Message creation // concorrent?
		// guardar no Peer?

		ChunkID chunkID = new ChunkID(this.msg.getFileId(), this.msg.getChunkNo());
		// TODO check this part
		ArrayList<Integer> answered = Peer.getInstance().getAnsweredCommand().get(chunkID);
		if (answered == null) {
			answered = new ArrayList<Integer>();
			Peer.getInstance().getAnsweredCommand().put(chunkID, answered);
		}
		synchronized (answered) {
			int senderID = Integer.parseInt(this.msg.getSenderID());

			if (answered.isEmpty() || !answered.contains(senderID)) {
				answered.add(senderID);
				ArrayList<ChunkID> stored = Peer.getInstance().getStored();
				int index = stored.indexOf(chunkID);
				if (index != -1)
					stored.get(index).increaseRepDegree();

				// Save alterations to peer data
				// try {
				// peer.saveData();
				// } catch (FileNotFoundException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// } catch (IOException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// }
			}
		}
	}

	private void putChunkHandler() {
		ChunkID chunkID = new ChunkID(this.msg.getFileId(), this.msg.getChunkNo());
		ArrayList<Integer> answered = Peer.getInstance().getAnsweredCommand().get(chunkID);
		if (answered != null) {
			synchronized (answered) {
				ArrayList<ChunkID> stored = Peer.getInstance().getStored();
				int index = stored.indexOf(chunkID);
				if (answered.size() < stored.get(index).getDesiredRepDegree())
					new BackupProtocol(Peer.getInstance()).putChunkReceive(this.msg);
			}
		}
	}

	private void removeHandler() {

		Peer peer = Peer.getInstance();
		// check if chunkId exist in database
		ChunkID tmp = new ChunkID(msg.getFileId(), msg.getChunkNo());
		int index = peer.getStored().indexOf(tmp);
		if (index == -1)
			return;

		// report loss of chunk
		peer.removeChunkPeer(tmp, Integer.valueOf(msg.getSenderID()));

		// update actualRepDegree
		peer.getStored().get(index).decreaseRepDegree();

		int actualRepDegree = peer.getStored().get(index).getActualRepDegree();
		int desiredRepDegree = peer.getStored().get(index).getDesiredRepDegree();

		if (desiredRepDegree <= actualRepDegree)
			return;
		// sleep between 0 to 400 ms
		waitLookup.add(tmp);
		int sleepTime = new Random().nextInt(MAX_WAIT);
		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			e.printStackTrace();
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
			e.printStackTrace();
		}

		FileHandler fileHandler = new FileHandler();

		byte[] chunkBody = new byte[0];
		try {
			chunkBody = fileHandler.loadChunkBody(tmp);
		} catch (SocketException | ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// If fails, chunkBody will be empty
			System.out.println("Wasn't able to load chunk nr. " + tmp.getChunkNumber() + " from file id: " + fileId);
			chunkBody = new byte[0];
		}

		try {
			new BackupProtocol(Peer.getInstance()).backupChunk(fileId, chunkBody, tmp.getChunkNumber(), desiredRepDegree, "1.0");
		} catch (SocketException | InterruptedException e) {
			e.printStackTrace();
		}
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
