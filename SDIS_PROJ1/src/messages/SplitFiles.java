package messages;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;

import chunk.Chunk;

//TODO STATIC OR NOT??
public class SplitFiles {
	private static final int CHUNK_SIZE = 64000;
	private File file;
	private FileInputStream fileReader;
	private RandomAccessFile fileOut;

	public SplitFiles(String fileName) {
		changeFileToSplit(fileName);
		changeFileToSplit(fileName);
	}

	public SplitFiles() {
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

	// public ArrayList<byte[]> splitFile() throws IOException {
	// ArrayList<byte[]> chunks = new ArrayList<byte[]>();
	//
	// if (file.exists()) {
	//
	// while (fileReader.available() > 0) {
	// byte[] chunk = new byte[CHUNK_SIZE];
	// int bytesRead = fileReader.read(chunk);
	// chunks.add(Arrays.copyOf(chunk, bytesRead));
	// }
	// } else
	// return null;
	// System.out.println(chunks.size());
	// for (byte[] chunk : chunks) {
	// System.out.println(chunk.length);
	// }
	// return chunks;
	// }

	public byte[] splitFile() throws IOException {

		if (file.exists()) {
			byte[] chunk = new byte[CHUNK_SIZE];
			int bytesRead = fileReader.read(chunk, 0, CHUNK_SIZE);
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
			pos = CHUNK_SIZE * id;
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

	public static int getChunkSize() {
		return CHUNK_SIZE;
	}

}