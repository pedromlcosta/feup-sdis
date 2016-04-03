package extra;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;

import service.Peer;
import chunk.Chunk;
import chunk.ChunkID;

public class FileHandler {
	public static final String BACKUP_FOLDER_NAME = "backup";
	public static final String RESTORE_FOLDER_NAME = "restore";
	private File file;
	private FileInputStream fileReader;
	private FileOutputStream fileWriter;
	private RandomAccessFile fileOut;

	/**
	 * Constructor of this class
	 * 
	 * @param fileName - name of the file
	 */
	public FileHandler(String fileName) {
		changeFileToSplit(fileName);
	}

	/**
	 * Default constructor
	 */
	public FileHandler() {
	}
	
	/**
	 * Closes the File Input Stream
	 * 
	 * @throws IOException
	 */
	public void closeInputStream() throws IOException {
		fileReader.close();
	}
	
	/**
	 * Closes the File Output Stream
	 * 
	 * @throws IOException
	 */
	public void closeOutputStream() throws IOException {
		fileWriter.close();
	}

	/**
	 * Closes the Random Access File
	 * 
	 * @throws IOException
	 */
	public void closeRandomAcess() throws IOException {
		fileOut.close();
	}

	/**
	 * Updates the File Input Stream and File used to split
	 * 
	 * @param fileName name of the file
	 */
	public void changeFileToSplit(String fileName) {
		try {
			fileReader = new FileInputStream(fileName);
			file = new File(fileName);

		} catch (FileNotFoundException e) {
			System.out.println("Couldn't find the file: " + fileName);
		}
	}

	/**
	 * Updates the Random Acess File and File used to merge
	 * 
	 * @param fileName name of the file
	 */
	public void changeFileToMerge(String fileName) {

		try {
			fileOut = new RandomAccessFile(fileName, "w");
		} catch (FileNotFoundException e) {
			System.out.println("Couldn't find the file: " + fileName);
		}
	}

	/**
	 * splits the file in 64K chunks
	 * 
	 * @return an array of bytes from a chunk
	 * @throws IOException
	 */
	public byte[] splitFile() throws IOException {

		if (file.exists()) {
			byte[] chunk = new byte[Chunk.getChunkSize()];
			int bytesRead = fileReader.read(chunk, 0, Chunk.getChunkSize());
			// TODO return null or return byte[0]
			if (bytesRead <= 0)
				return new byte[0];
			else
				return Arrays.copyOf(chunk, bytesRead);

		} else
			return null;
	}

	/**
	 * write a chunk back to a file
	 * 
	 * @param chunk - chunk to be written
	 * @param name - name of the file where chuck will be written
	 * @return true if successful, false otherwise
	 * @throws IOException
	 */
	public boolean joinChunk(Chunk chunk, String name) throws IOException {

		byte[] data = chunk.getData();
		int id = chunk.getId().getChunkNumber();
		int pos;

		if (data.length == 0) {
			fileOut.close();
			return true;
		} else {
			pos = Chunk.getChunkSize() * id;
			fileOut.write(data, pos, data.length);
			return true;
		}
	}

	/**
	 * 
	 * @return the file in use
	 */
	public File getFile() {
		return file;
	}

	/**
	 * set the file used
	 * 
	 * @param file new file to be used
	 */
	public void setFile(File file) {
		this.file = file;
	}

	/**
	 * 
	 * @return the file Input Stream in use
	 */
	public FileInputStream getFileReader() {
		return fileReader;
	}


	/**
	 * set the file Input Steam
	 * 
	 * @param fileReader new file Input Stream to be used
	 */
	public void setFileReader(FileInputStream fileReader) {
		this.fileReader = fileReader;
	}

	/**
	 * 
	 * @return the file Random Access File in use
	 */
	public RandomAccessFile getFileOut() {
		return fileOut;
	}

	/**
	 * set the file Random Access
	 * 
	 * @param fileOut new file Random Access to be used
	 */
	public void setFileOut(RandomAccessFile fileOut) {
		this.fileOut = fileOut;
	}

	/**
	 * set the new file Output Stream
	 * 
	 * @param filePath - name of the file
	 * @return
	 */
	public synchronized boolean createFile(String filePath) {

		try {
			fileWriter = new FileOutputStream(filePath);
			return true;
		} catch (FileNotFoundException e) {
			System.out.println("here");
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * writes an array of bytes to the file using current File Output Stream
	 * 
	 * @param fileData bytes to be written to a file
	 * @throws IOException
	 */
	public void writeToFile(byte[] fileData) throws IOException {
		fileWriter.write(fileData);
	}
	
	/**
	 * writes only len bytes to the file using current File Output Stream
	 * 
	 * @param fileData bytes to be written to a file
	 * @len number of bytes to be written
	 * @throws IOException
	 */
	public void writeToFile(byte[] fileData, int len) throws IOException {
		fileWriter.write(fileData, 0, len);
	}

	/**
	 * read from a chunk
	 * 
	 * @param chunkID - identifier of chunk
	 * @return bytes from that file
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public byte[] loadChunkBody(ChunkID chunkID) throws ClassNotFoundException, IOException {
		String path;
		String serverID = Integer.toString(Peer.getInstance().getServerID());
		
		path = Extra.createDirectory(serverID + File.separator + BACKUP_FOLDER_NAME);
		FileInputStream fileOut = new FileInputStream(path + File.separator + chunkID.getFileID() + "_" + chunkID.getChunkNumber());
		ObjectInputStream out = new ObjectInputStream(fileOut);
		Chunk storedChunk = (Chunk) out.readObject();
		fileOut.close();
		out.close();
		return storedChunk.getData();

	}

}