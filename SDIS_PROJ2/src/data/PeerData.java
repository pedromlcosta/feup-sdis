package data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import chunk.Chunk;
import chunk.ChunkID;
import extra.Extra;

public class PeerData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3129093348473298612L;
	private static final String CURRENT_VERSION = "1.0";
	private ArrayList<ChunkID> stored;
	private HashMap<String, ArrayList<FileID>> filesSent;
	private HashMap<ChunkID, ArrayList<Integer>> serverAnsweredCommand;
	private HashMap<ChunkID, Integer> deleted;
	private ArrayList<ChunkID> removeLookup = new ArrayList<ChunkID>();
	private final static long DISK_SIZE = Chunk.getChunkSize() * 100000;
	private static String dataPath = "";
	private static final String fileName = "PeerData.dat";

	// CONSTRUCTOR

	/**
	 * Constructor for the PeerData, initializes the data members that are
	 * necessary
	 */
	public PeerData() {
		stored = new ArrayList<ChunkID>();
		filesSent = new HashMap<String, ArrayList<FileID>>();
		serverAnsweredCommand = new HashMap<ChunkID, ArrayList<Integer>>();
		deleted = new HashMap<ChunkID, Integer>();
	}

	// SERIAL FUNCTIONS
	/**
	 * Serializes this object into a file
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public synchronized void savePeerData() throws FileNotFoundException, IOException {

		String dirPath = "";

		try {
			dirPath = Extra.createDirectory(dataPath);
		} catch (IOException e1) {
			throw new IOException(e1.getMessage() + " Couldn't create directory.");
		}

		FileOutputStream fileOut = new FileOutputStream(dirPath + File.separator + fileName);
		ObjectOutputStream objOut = new ObjectOutputStream(fileOut);

		objOut.writeObject(this);
		fileOut.close();
		objOut.close();

	}

	/**
	 * Deserializes this object, from a previously created file in a directory,
	 * if it exists Otherwise, just creates the directory and/or file, as needed
	 * 
	 * @return the PeerData object loaded from the file
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws NotSerializableException
	 */
	public synchronized PeerData loadPeerData() throws FileNotFoundException, IOException, ClassNotFoundException, NotSerializableException {

		String dirPath = "";

		try {
			dirPath = Extra.createDirectory(dataPath);
		} catch (IOException e1) {
			throw new IOException(e1.getMessage() + " Couldn't create directory: " + dirPath);
		}

		// Read from disk using FileInputStream -> may throw file not found
		// exception ->
		FileInputStream fileIn = null;

		fileIn = new FileInputStream(dirPath + File.separator + fileName);

		// Read object using ObjectInputStream
		ObjectInputStream objIn = new ObjectInputStream(fileIn);

		// Read an object
		Object obj = objIn.readObject();

		fileIn.close();
		objIn.close();

		if (obj instanceof PeerData) {
			PeerData data = (PeerData) obj;
			System.out.println("Finished loading PeerData");
			return data;
		} else {
			throw new ClassNotFoundException("Object read from PeerData.dat isn't a valid PeerData object.");
		}

	}

	// GETTERS AND SETTERS

	public ArrayList<ChunkID> getStored() {
		return stored;
	}

	public HashMap<ChunkID, ArrayList<Integer>> getServerAnsweredCommand() {
		return serverAnsweredCommand;
	}

	public void setServerAnsweredCommand(HashMap<ChunkID, ArrayList<Integer>> serverAnsweredCommand) {
		this.serverAnsweredCommand = serverAnsweredCommand;
	}

	public void setStored(ArrayList<ChunkID> stored) {
		this.stored = stored;
	}

	public HashMap<String, ArrayList<FileID>> getFilesSent() {
		return filesSent;
	}

	public void setFilesSent(HashMap<String, ArrayList<FileID>> filesSent) {
		this.filesSent = filesSent;
	}

	// OTHER FUNCTIONS

	public void addChunk(ChunkID id) {
		stored.add(id);
	}

	public void removeStoredEntry(String fileId) {
		stored.remove(fileId);
	}

	/**
	 * Sorts the stored chunks by difference in their actual-desired degrees
	 */
	public synchronized void sortStored() {
		Collections.sort(stored);
	}

	public synchronized void removeFilesSentEntry(String filePath) {
		filesSent.remove(filePath);
	}

	public synchronized void removeChunkPeers(ChunkID chunk) {
		serverAnsweredCommand.remove(chunk);
	}

	public synchronized void removeChunkPeer(ChunkID chunk, Integer peer) {
		ArrayList<Integer> ids = serverAnsweredCommand.get(chunk);
		if (ids != null) {
			ids.remove(peer);
		}
	}

	public boolean hasChunkStored(ChunkID chunkID) {
		return stored.contains(chunkID);
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public static long getDiskSize() {
		return DISK_SIZE;
	}

	public static String getDataPath() {
		return dataPath;
	}

	public static void setDataPath(String path) {
		dataPath = path;
	}

	public static void setDataPath(Integer serverID) {
		PeerData.dataPath = Integer.toString(serverID) + File.separator + "PeerData";
	}

	public HashMap<ChunkID, Integer> getDeleted() {
		return deleted;
	}

	public void setDeleted(HashMap<ChunkID, Integer> deleted) {
		this.deleted = deleted;
	}

	public void addChunkDeleted(ChunkID chunk) {
		this.deleted.put(chunk, 0);
	}

	public void removeChunkDeleted(ChunkID chunk) {
		this.deleted.remove(chunk);
	}

	public void incChunkDeleted(ChunkID chunk) {
		this.deleted.put(chunk, this.deleted.get(chunk) + 1);
	}

	public static String getCurrentVersion() {
		return CURRENT_VERSION;
	}

	public static String getFilename() {
		return fileName;
	}

	public int removeCheck(String fileId, int chunkNo) {

		ChunkID chunk = new ChunkID(fileId, chunkNo);
		int index = removeLookup.indexOf(chunk);

		if (index != -1)
			removeLookup.remove(index);
		return index;
	}

	public String getData() throws IOException{
		
		String dirPath = "";

		try {
			dirPath = Extra.createDirectory(dataPath);
		} catch (IOException e1) {
			throw new IOException(e1.getMessage() + " Couldn't create directory.");
		}

		File file = new File(dirPath + File.separator + fileName);
		return new String(Files.readAllBytes(file.toPath()));
	}
	
 	public ArrayList<ChunkID> getRemoveLookup() {
		return removeLookup;
	}

	public void setRemoveLookup(ArrayList<ChunkID> removeLookup) {
		this.removeLookup = removeLookup;
	}

	/*
	public static void main(String[] args){
		
		PeerData data = new PeerData();
		PeerData.setDataPath(2);
		data.dataPath = "bin\\" + data.dataPath;
		try {
			data.getData();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	*/
}
