package channels;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;

import chunk.Chunk;
import chunk.ChunkID;

public class MDRReceiver extends MulticastServer {
	
	// Must be volatile so that 2 restores dont access it at the same time for the same file!
	private volatile HashMap<String, ArrayList<Chunk> > restoreChunksReceived = new  HashMap<String, ArrayList<Chunk> >();
	private volatile HashMap<ChunkID, Boolean> foreignChunksReceived = new HashMap<ChunkID, Boolean>();
	
	/**
	 * Default constructor for this subclass
	 */
	public MDRReceiver(){
		
	}
	
	/**
	 * 
	 * @param args
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public MDRReceiver(String[] args) throws NumberFormatException, IOException {
		super();
	}

	/**
	 * 
	 * @param quitFlag flag for the infinite run cycle that receives the messages
	 * @param serverID Identifier of the peer this receiver belongs to
	 * @param addr Multicast IP address of this receiver
	 * @param port Multicast Port of this receiver
	 */
	public MDRReceiver(boolean quitFlag, int serverID, InetAddress addr, int port) {
		super(quitFlag, serverID, addr, port);
	}

	/**
	 * Getter for the HashMap restoreChunksReceived.
	 * @return returns the hashmap with chunks that are part of a file awaiting a restoration
	 */
	public HashMap<String, ArrayList<Chunk> > getRestoreChunksReceived() {
		return restoreChunksReceived;
	}

	/**
	 * Setter for the HashMap restoreChunksReceived.
	 * @param restoreChunksReceived
	 */
	public void setRestoreChunksReceived(HashMap<String, ArrayList<Chunk> > restoreChunksReceived) {
		this.restoreChunksReceived = restoreChunksReceived;
	}
	
	/**
	 * Is ran when starting a restore. If a file isn't yet being restored, puts an entry
	 * into the hashmap with it's fileID, marking the file as in Restoration and creating
	 * an ArrayList for it's chunks to be put in.
	 * 
	 * @param fileID identifier of the file to start restoring
	 * @return true if the file was already being restored, false if not
	 */
	// Synchronized to avoid 2 restores at once interleaving the functions used inside
	public synchronized boolean startRestore(String fileID){
		if (restoreChunksReceived.containsKey(fileID) == false){
			restoreChunksReceived.put(fileID, new ArrayList<Chunk>());
			return false;
		}
		else{
			return true;
		}
	}
	
	/**
	 * 
	 * Is ran when a restore has finished. Removes the file entry from the hashmap,
	 * which indicates it is no longer in restoration. 
	 * 
	 * @param fileID identifier of the file to stop restoring
	 */
	public synchronized void finishRestore(String fileID){
		System.out.println("finish restore called");
		restoreChunksReceived.remove(fileID);
	}
	
	/**
	 * 
	 * @param fileID identifier of a file
	 * @return true if there is a file with that fileID currently being restored
	 */
	public synchronized boolean expectingRestoreChunks(String fileID){
		return restoreChunksReceived.containsKey(fileID);
	}
	
	/**
	 * 
	 * Adds a chunk to the arraylist of restored chunks of the file being restored
	 * 
	 * @param fileID identifier of file being restored
	 * @param chunk Chunk object associated with file being restored
	 */
	public synchronized void addRestoreChunk(String fileID, Chunk chunk){
		restoreChunksReceived.get(fileID).add(chunk);
	}

	/**
	 * Getter for this data member
	 * @return
	 */
	public HashMap<ChunkID, Boolean> getForeignChunksReceived() {
		return foreignChunksReceived;
	}

	/**
	 * Setter for this data member
	 * @param foreignChunksReceived
	 */
	public void setForeignChunksReceived(HashMap<ChunkID, Boolean> foreignChunksReceived) {
		this.foreignChunksReceived = foreignChunksReceived;
	}
	
	/**
	 * Starts or stops expecting certain chunks associated with the chunkID
	 * When starting to expect a chunk, the value "false" is associated with it,
	 * meaning it still hasn't been received
	 * 
	 * @param chunkID identifier of the chunk to start/stop awaiting
	 * @param awaitingChunk true to start expecting, false to stop
	 */
	public void expectingForeignChunk(ChunkID chunkID, boolean awaitingChunk){
		if(awaitingChunk)
			foreignChunksReceived.put(chunkID, false);
		else
			foreignChunksReceived.remove(chunkID); //No longer awaiting chunk, no need to be in the hashmap
	}
	
	/**
	 * When a foreign chunk was being awaited and is received, it's value on the
	 * hashmap turns from false to true
	 * 
	 * @param chunkID identifier of chunk to recognize as received
	 */
	public void receivedForeignChunk(ChunkID chunkID){
		// If it was being awaited... goes from false to true
		if(foreignChunksReceived.containsKey(chunkID))
			foreignChunksReceived.put(chunkID, true);
	}
	
	/**
	 * 
	 * 
	 * @param chunkID identifier of chunk to check
	 * @return true, if a foreign chunk with chunkID is present on the hashmap and associated with the value true
	 */
	public synchronized boolean wasForeignChunkReceived(ChunkID chunkID) {
		
		if(foreignChunksReceived.containsKey(chunkID)){
			Boolean received = foreignChunksReceived.get(chunkID);
			if( received!= null && received == true)
				return true;
		}
		return false;
	}
	
}
