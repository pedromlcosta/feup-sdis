package channels;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;

import chunk.Chunk;
import chunk.ChunkID;
import file.FileID;
import service.Peer;

public class MDRReceiver extends ReceiverServer {
	private Peer user;
	// Must be volatile so that 2 restores dont access it at the same time for the same file!
	private volatile HashMap<String, ArrayList<Chunk> > restoreChunksReceived = new  HashMap<String, ArrayList<Chunk> >();
	private volatile HashMap<ChunkID, Boolean> foreignChunksReceived = new HashMap<ChunkID, Boolean>();
	
	public MDRReceiver(String[] args) throws NumberFormatException, IOException {
		super();
	}

	public MDRReceiver(boolean quitFlag, int serverID, InetAddress addr, int port) {
		super(quitFlag, serverID, addr, port);
	}

	public HashMap<String, ArrayList<Chunk> > getRestoreChunksReceived() {
		return restoreChunksReceived;
	}

	public void setRestoreChunksReceived(HashMap<String, ArrayList<Chunk> > restoreChunksReceived) {
		this.restoreChunksReceived = restoreChunksReceived;
	}
	
	// Synchronized to avoid 2 restores at once interleaving the functions used inside
	public synchronized boolean isBeingRestoredAlready(String file){
		if (restoreChunksReceived.containsKey(file) == false){
			restoreChunksReceived.put(file, new ArrayList<Chunk>());
			return false;
		}
		else{
			return true;
		}
	}
	
	public synchronized boolean expectingRestoreChunks(String fileID){
		return restoreChunksReceived.containsKey(fileID);
	}
	
	public synchronized void addRestoreChunk(String fileID, Chunk chunk){
		restoreChunksReceived.get(fileID).add(chunk);
	}

	public HashMap<ChunkID, Boolean> getForeignChunksReceived() {
		return foreignChunksReceived;
	}

	public void setForeignChunksReceived(HashMap<ChunkID, Boolean> foreignChunksReceived) {
		this.foreignChunksReceived = foreignChunksReceived;
	}
	
	public void expectingForeignChunk(ChunkID chunkID, boolean awaitingChunk){
		if(awaitingChunk)
			foreignChunksReceived.put(chunkID, false);
		else
			foreignChunksReceived.remove(chunkID); //No longer awaiting chunk, no need to be in the hashmap
	}
	
	public void receivedForeignChunk(ChunkID chunkID){
		// If it was being awaited... goes from false to true
		if(foreignChunksReceived.containsKey(chunkID))
			foreignChunksReceived.put(chunkID, true);
	}
	
	public synchronized boolean wasForeignChunkReceived(ChunkID chunkID) {
		
		if(foreignChunksReceived.containsKey(chunkID)){
			Boolean received = foreignChunksReceived.get(chunkID);
			if( received!= null && received == true)
				return true;
		}
		return false;
	}
}
