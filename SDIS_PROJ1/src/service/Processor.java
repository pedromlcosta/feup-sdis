package service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import channels.MDRReceiver;
import chunk.Chunk;
import chunk.ChunkID;
import extra.Extra;
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
	private Message msg;

	public Processor(String messageString) {
		this.messageString = messageString;
	}

	public Processor(String header, byte[] body) {
	}

	public void run() {
		// HANDLE MESSAGES HERE
		if (msg != null && messageString != null) {
			String[] messageFields = msg.parseMessage(messageString);
			messageString = ""; // Empty, so as not to fill unnecessary space

			switch (messageFields[0]) {
			case "PUTCHUNK":
				msg = new PutChunkMsg(messageFields);

				// Unreserve the now unneeded array space, while the processor
				// handles the message
				messageFields = null;
				reclaimCheck(msg.getFileId(),msg.getChunkNo());
				putChunkHandler();
				break;
			case "STORED":
				msg = new StoredMsg(messageFields);

				messageFields = null;
				storedHandler();
				break;
			case "GETCHUNK":
				msg = new GetChunkMsg(messageFields);

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
				msg = new ChunkMsg(messageFields);

				messageFields = null;
				chunkHandler();
				break;
			case "DELETE":
				msg = new DeleteMsg(messageFields);

				messageFields = null;
				deleteHandler();
				break;
			case "REMOVED":
				msg = new RemovedMsg(messageFields);

				messageFields = null;
				removeHandler();
				break;
			default:
				break;
			}
		}
	}

	private int reclaimCheck(String fileId, int chunkNo) {
		
		ChunkID chunk = new ChunkID(fileId,chunkNo);
		int index = waitLookup.indexOf(chunk);
		
		if(index != -1)
			waitLookup.remove(index);
		return index;
	}

	private void deleteHandler() {

		String fileId = msg.getFileId();
		String dirPath = "";

		try {
			dirPath = Extra.createDirectory(FileHandler.BACKUP_FOLDER_NAME);
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (Iterator<ChunkID> it = Peer.getInstance().getStored().iterator(); it.hasNext();) {
			ChunkID chunk = it.next();
			String idToConfirm = chunk.getFileID();
			// if chunk belongs to file delete chunk and stored
			if (fileId.equals(idToConfirm)) {
				File file = new File(dirPath + File.separator + idToConfirm + "_" + chunk.getChunkNumber());
				file.delete();
				it.remove();
			}
		}
	}

	private void chunkHandler() {

		ChunkID chunkID = new ChunkID(msg.getFileId(), msg.getChunkNo());
		MDRReceiver restoreChannel = Peer.getInstance().getRestoreChannel();

		// Received a chunk and was expecting it for a restore
		if (restoreChannel.expectingRestoreChunks(chunkID.getFileID())) {
			restoreChannel.addRestoreChunk(chunkID.getFileID(), new Chunk(chunkID, msg.getBody()));
		} else { // Received a chunk whose file wasn't being restored
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

				byte[] chunkBody = fileHandler.loadChunkBody(chunkID);
				String[] args = { "1.0", Peer.getInstance().getServerID(), chunkID.getFileID(), Integer.toString(chunkID.getChunkNumber()) };

				// byte[] chunkBody = new byte[64];
				Message chunkMsg = new ChunkMsg();
				if (chunkMsg.createMessage(chunkBody, args) == true) {
					DatagramPacket packet = restore.createDatagramPacket(chunkMsg.getMessageBytes());
					restore.writePacket(packet);
				} else {
					System.out.println("Wasn't able to create and send chunk message");
				}

			}

			// Stop waiting for chunks with this ID
			restore.expectingForeignChunk(chunkID, false);

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
		ArrayList<Integer> answered = Peer.getInstance().getAnsweredCommand().get(chunkID);
		synchronized (answered) {
			int senderID = Integer.parseInt(this.msg.getSenderID());
			if (answered == null) {
				answered = new ArrayList<Integer>();

				Peer.getInstance().getAnsweredCommand().put(chunkID, answered);

			}
			if (answered.isEmpty() || !answered.contains(senderID))
				answered.add(senderID);
		}
	}

	private void putChunkHandler() {
		System.out.println("Putchunk");
		new BackupProtocol(Peer.getInstance()).putChunkReceive(this.msg);
		// Filipe -> putchunk call the function it needs to handle the putuch
	}

	private void removeHandler() {
		
		Peer peer = Peer.getInstance();
		//check if chunkId exist in database
		ChunkID tmp = new ChunkID(msg.getFileId(), msg.getChunkNo());
		int index = peer.getStored().indexOf(tmp);
		if(index==-1)
			return;
		
		//update actualRepDegree
		peer.getStored().get(index).decreaseRepDegree();
		
		int actualRepDegree = peer.getStored().get(index).getActualRepDegree();
		int desiredRepDegree = peer.getStored().get(index).getDesiredRepDegree();
		
		if(desiredRepDegree < actualRepDegree)
			return;
		//sleep between 0 to 400 ms
		waitLookup.add(tmp);
		int sleepTime = new Random().nextInt(MAX_WAIT);
		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//if launch is -1, mean that chunk has already gone through putChunk
		int launch = reclaimCheck(tmp.getFileID(),tmp.getChunkNumber());
		if(launch==-1)
			return;
		
		//if not received putChunk, launch
		//peer.backup(filePath, desiredRepDegree);
		//backupChunk(tmp.getFileID(), byte[] chunkData, tmp.getChunkNumber(), desiredRepDegree, "1.0");
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

}
