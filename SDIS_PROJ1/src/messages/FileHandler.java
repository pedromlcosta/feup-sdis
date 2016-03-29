package messages;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;

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

		if (!f.exists() && !f.isDirectory()) {
			try {
				fileWriter = new FileOutputStream(filePath);
				return true;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			return false;
		}

		return false;
	}

	public void writeToFile(byte[] fileData) throws IOException {
		fileWriter.write(fileData);
		// writePos++;
	}

	public void openOutStream() {

	}

	// TODO check this part
	public byte[] loadChunkBody(ChunkID chunkID) throws ClassNotFoundException {
		String path;
		try {
			path = Extra.createDirectory(BACKUP_FOLDER_NAME);
			FileInputStream fileOut = new FileInputStream(path + File.pathSeparator + chunkID.getFileID() + "_" + chunkID.getChunkNumber());
			ObjectInputStream out = new ObjectInputStream(fileOut);
			Chunk storedChunk = (Chunk) out.readObject();
			out.close();
			return storedChunk.getData();
		} catch (IOException e) {
			System.out.println("Wasn't able to load chunk nr. " + chunkID.getChunkNumber() + " from file id: " + chunkID.getFileID());
			e.printStackTrace();
		}
		// incase of error
		return new byte[0];
	}

}