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
	private volatile HashMap<FileID, ArrayList<Chunk> > restoreChunksReceived = new  HashMap<FileID, ArrayList<Chunk> >();
	private volatile HashMap<ChunkID, Boolean> foreignChunksReceived = new HashMap<ChunkID, Boolean>();
	
	public MDRReceiver(String[] args) throws NumberFormatException, IOException {
		super();
	}

	public MDRReceiver(boolean quitFlag, int serverID, InetAddress addr, int port) {
		super(quitFlag, serverID, addr, port);
	}

	public HashMap<FileID, ArrayList<Chunk> > getRestoreChunksReceived() {
		return restoreChunksReceived;
	}

	public void setRestoreChunksReceived(HashMap<FileID, ArrayList<Chunk> > restoreChunksReceived) {
		this.restoreChunksReceived = restoreChunksReceived;
	}
	
	// Synchronized to avoid 2 restores at once interleaving the functions used inside
	public synchronized boolean isBeingRestoredAlready(FileID file){
		if (restoreChunksReceived.containsKey(file) == false){
			restoreChunksReceived.put(file, new ArrayList<Chunk>());
			return false;
		}
		else{
			return true;
		}
	}

	public HashMap<ChunkID, Boolean> getForeignChunksReceived() {
		return foreignChunksReceived;
	}

	public void setForeignChunksReceived(HashMap<ChunkID, Boolean> foreignChunksReceived) {
		this.foreignChunksReceived = foreignChunksReceived;
	}

	public synchronized boolean receivedForeignChunk(ChunkID chunkID) {
		/*
		if(foreignChunksReceived.containsKey(chunkID)){
			Boolean
			if(foreignChunksReceived.get(chunkID) != null && foreignChunksReceived.get(chunkID) == true)
		}
		*/
		return false;
	}
}
