package service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import chunk.ChunkID;
import messages.ChunkMsg;
import messages.DeleteMsg;
import messages.GetChunkMsg;
import messages.Message;
import messages.PutChunkMsg;
import messages.RemovedMsg;
import messages.StoredMsg;
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
		// Costa

		// 1o - Verificar se o Chunk pertence a um ficheiro em restore
		
		// 2o - Se pertencer, guardar em chunksBeingReceived
		
		// 3o - Se não pertencer, guardar só a informação que foi recebido um chunk
		// So ver se e valido e adicionar aos chunks esperados desse ficheiro...
	}

	private void getChunkHandler() {
		// Costa

		// Ciclo com timer 0-400ms -> como sei se chegou um chunk? e tem de ser
		// chunk do mesmo ficheiro, para esperar?
		
		ChunkID chunkID = new ChunkID(msg.getFileId(), msg.getChunkNo());
		
		if(Peer.getInstance().hasChunkStored(chunkID)){
			try {
				Thread.sleep((new Random()).nextInt(401));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			

			if(Peer.getInstance().getRestoreChannel().receivedForeignChunk(chunkID)){

			}
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
	}

	private void putChunkHandler() {
		new BackupProtocol(Peer.getInstance()).putchunkReceive(this.msg);
		// Filipe -> putchunk call the function it needs to handle the putuch
	}

	private void removeHandler() {

	}

}
