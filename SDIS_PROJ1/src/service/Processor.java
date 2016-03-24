package service;


import java.util.concurrent.LinkedBlockingQueue;

import messages.ChunkMsg;
import messages.DeleteMsg;
import messages.GetChunkMsg;
import messages.Message;
import messages.PutChunkMsg;
import messages.RemovedMsg;
import messages.StoredMsg;



public class Processor extends Thread{

	String messageString;
	Message msg;
	
	public Processor(String messageString){
		this.messageString = messageString;
	}
	
	public void run(){
		// HANDLE MESSAGES HERE
		String[] messageFields = msg.parseMessage(messageString);
		messageString = ""; // Empty, so as not to fill unnecessary space
		
		switch(messageFields[0]){
		case "PUTCHUNK":
			msg = new PutChunkMsg(messageFields);
			
			// Unreserve the now unneeded array space, while the processor handles the message
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
		
		//So ver se e valido e adicionar aos chunks esperados desse ficheiro...
	}

	private void getChunkHandler() {
		// Costa
		
		// Ciclo com timer 0-400ms -> como sei se chegou um chunk? e tem de ser chunk do mesmo ficheiro, para esperar?
	}

	private void storedHandler() {
		// Filipe
	}

	private void putChunkHandler() {
		// Filipe
	}
	
	private void removeHandler() {

	}
	
}
