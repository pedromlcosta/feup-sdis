package service;

import java.net.DatagramPacket;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import channels.MDRReceiver;
import chunk.Chunk;
import chunk.ChunkID;
import messages.ChunkMsg;
import messages.DeleteMsg;
import messages.GetChunkMsg;
import messages.Message;
import messages.PutChunkMsg;
import messages.RemovedMsg;
import messages.StoredMsg;
import messages.Message.MESSAGE_TYPE;
import protocol.BackupProtocol;

public class Processor extends Thread {

	private final long GETCHUNK_WAITING_NANO = TimeUnit.MILLISECONDS.toNanos(400); // In milliseconds
	private String messageString;
	private Message msg;

	public Processor(String messageString) {
		this.messageString = messageString;
	}

	public void run() {
		// HANDLE MESSAGES HERE
		String[] messageFields = msg.parseMessage(messageString);
		messageString = ""; // Empty, so as not to fill unnecessary space

		switch (messageFields[0]) {
		case "PUTCHUNK":
			msg = new PutChunkMsg(messageFields);

			// Unreserve the now unneeded array space, while the processor
			// handles the message
			messageFields = null;
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
			getChunkHandler();
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

	private void deleteHandler() {
		// Rui
	}

	private void chunkHandler() {

		ChunkID chunkID = new ChunkID(msg.getFileId(), msg.getChunkNo());
		MDRReceiver restoreChannel = Peer.getInstance().getRestoreChannel();
		
		// Received a chunk and was expecting it for a restore
		if(restoreChannel.expectingRestoreChunks(chunkID.getFileID())){
			restoreChannel.addRestoreChunk(chunkID.getFileID(), new Chunk(chunkID, msg.getBody()));
		}else{ // Received a chunk whose file wasn't being restored
			restoreChannel.receivedForeignChunk(chunkID);
		}
		

		// 1o - Verificar se o Chunk pertence a um ficheiro em restore
		
		// 2o - Se pertencer, guardar em chunksBeingReceived
		
		// 3o - Se não pertencer, guardar só a informação que foi recebido um chunk
		// So ver se e valido e adicionar aos chunks esperados desse ficheiro...
	}

	private void getChunkHandler() {
		
		ChunkID chunkID = new ChunkID(msg.getFileId(), msg.getChunkNo());
		MDRReceiver restore = Peer.getInstance().getRestoreChannel();
		
		if(Peer.getInstance().hasChunkStored(chunkID)){
			
			// Start waiting for chunks with this ID
			restore.expectingForeignChunk(chunkID, true);
			
			try {
				Thread.sleep((new Random()).nextInt(401));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			// Check if any chunks expected arrived while sleeping
			if(!restore.wasForeignChunkReceived(chunkID)){
				// enviar mensagem com o chunk
				
				//Byte[] chunkBody = fileHandler.loadChunkBody(chunkID);
				String[] args= {"1.0", Integer.toString(Peer.getInstance().getServerID()), chunkID.getFileID(), Integer.toString(chunkID.getChunkNumber())};
				
				byte[] chunkBody = new byte[64];
				Message chunkMsg = new Message();
				if(chunkMsg.createMessage(MESSAGE_TYPE.CHUNK, args, chunkBody) == true){
					DatagramPacket packet = restore.createDatagramPacket(chunkMsg.getMessageBytes());
					restore.writePacket(packet);
				}else{
					System.out.println("Wasn't able to create and send chunk message");
				}

			}
			
			// Stop waiting for chunks with this ID
			restore.expectingForeignChunk(chunkID, false);
			
		}

	}

	private void storedHandler() {
		new BackupProtocol(Peer.getInstance()).putchunkReceive(this.msg);
		;
		// Filipe places the message in a queue? that will be read by the
		// protocole handling the putcunk Message creation // concorrent?
		// guardar no Peer?
	}

	private void putChunkHandler() {
		// Filipe -> putchunk call the function it needs to handle the putuch
	}

	private void removeHandler() {

	}

}
