package service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import messages.FileHandler;
import chunk.ChunkID;
import extra.Extra;
import file.FileID;

public class PeerData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3129093348473298612L;
	private static final String CURRENT_VERSION = "1.0";
	private ArrayList<ChunkID> stored;
	private HashMap<String, ArrayList<FileID>> filesSent;
	private HashMap<ChunkID, ArrayList<Integer>> serverAnsweredCommand;
	private final static long DISK_SIZE = 64000 * 1000000;
	private static String dataPath = "";
	private static final String fileName = "PeerData.dat";

	// CONSTRUCTOR

	public PeerData() {
		stored = new ArrayList<ChunkID>();
		filesSent = new HashMap<String, ArrayList<FileID>>();
		serverAnsweredCommand = new HashMap<ChunkID, ArrayList<Integer>>();
	}

	// SERIAL FUNCTIONS
	public void savePeerData() throws FileNotFoundException, IOException {

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

	public PeerData loadPeerData() throws FileNotFoundException, IOException, ClassNotFoundException, NotSerializableException {

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

	public static String getCurrentVersion() {
		return CURRENT_VERSION;
	}

	public static String getFilename() {
		return fileName;
	}

}
