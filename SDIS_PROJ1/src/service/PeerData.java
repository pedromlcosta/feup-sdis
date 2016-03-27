package service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import chunk.ChunkID;
import file.FileID;

public class PeerData implements Serializable{
	
	private ArrayList<ChunkID> stored;
	private HashMap<String, FileID> filesSent;
	private HashMap<ChunkID, ArrayList<Integer>> serverAnsweredCommand;
	
	// CONSTRUCTOR
	
	public PeerData(){
		stored = new ArrayList<ChunkID>();
		filesSent = new HashMap<String, FileID>();
		serverAnsweredCommand = new HashMap<ChunkID, ArrayList<Integer>>();
	}
	
	// OTHER FUNCTIONS
	
	public void addChunk(ChunkID id) {
		stored.add(id);
	}
	
	public void removeStoredEntry(String fileId){
		stored.remove(fileId);
	}
	
	public synchronized  void sortStored(){
		Collections.sort(stored);
	}
	
	public synchronized void removeFilesSentEntry(String filePath){
		filesSent.remove(filePath);
	}
	
	public synchronized void removeChunkPeers(ChunkID chunk){
		serverAnsweredCommand.remove(chunk);
	}
	
	public synchronized void removeChunkPeer(ChunkID chunk, Integer peer){
		ArrayList<Integer> ids = serverAnsweredCommand.get(chunk);
		if(ids != null){
			ids.remove(peer);
		}
	}

	public boolean hasChunkStored(String fileID) {
		return false;
	}
	
	
	
	// GETTERS AND SETTERS
	
	public ArrayList<ChunkID> getStored() {
		return stored;
	}
	public HashMap<ChunkID, ArrayList<Integer>> getServerAnsweredCommand() {
		return serverAnsweredCommand;
	}
	public void setServerAnsweredCommand(
			HashMap<ChunkID, ArrayList<Integer>> serverAnsweredCommand) {
		this.serverAnsweredCommand = serverAnsweredCommand;
	}
	public void setStored(ArrayList<ChunkID> stored) {
		this.stored = stored;
	}
	public HashMap<String, FileID> getFilesSent() {
		return filesSent;
	}
	public void setFilesSent(HashMap<String, FileID> filesSent) {
		this.filesSent = filesSent;
	}

}
