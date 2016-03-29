package messages;

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
import extra.Extra;

//TODO STATIC OR NOT?? Check if good practice the Chunk.getChunkSize()
public class FileHandler {
	public static final String BACKUP_FOLDER_NAME = "backup";
	public static final String RESTORE_FOLDER_NAME = "restore";
	private File file;
	private FileInputStream fileReader;
	private FileOutputStream fileWriter;
	private RandomAccessFile fileOut;

	public FileHandler(String fileName) {
		changeFileToSplit(fileName);
		changeFileToSplit(fileName);
	}

	public FileHandler() {
	}

	public void closeInputStream() throws IOException {
		fileReader.close();
	}
	
	public void closeOutputStream() throws IOException {
		fileWriter.close();
	}

	public void closeRandomAcess() throws IOException {
		fileOut.close();
	}

	public void changeFileToSplit(String fileName) {
		try {
			fileReader = new FileInputStream(fileName);
			file = new File(fileName);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void changeFileToMerge(String fileName) {

		try {
			fileOut = new RandomAccessFile(fileName, "w");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

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

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public FileInputStream getFileReader() {
		return fileReader;
	}

	public void setFileReader(FileInputStream fileReader) {
		this.fileReader = fileReader;
	}

	public RandomAccessFile getFileOut() {
		return fileOut;
	}

	public void setFileOut(RandomAccessFile fileOut) {
		this.fileOut = fileOut;
	}

	// PEDRO STUFF BELOW

	public synchronized boolean createFile(String filePath) {

		File f = new File(filePath);

		//if (!f.exists() && !f.isDirectory()) {
			try {
				System.out.println("Going to open the stream for " + filePath);
				fileWriter = new FileOutputStream(filePath);
				System.out.println("Just opened the stream for " + filePath);
				return true;
			} catch (FileNotFoundException e) {
				System.out.println("here");
				e.printStackTrace();
			}
		//} else {
			//return false;
		//}

		return false;
	}

	public void writeToFile(byte[] fileData) throws IOException {
		fileWriter.write(fileData);
	}
	
	public void writeToFile(byte[] fileData, int len) throws IOException {
		fileWriter.write(fileData, 0, len);
	}

	public void openOutStream() {

	}

	// TODO check this part
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