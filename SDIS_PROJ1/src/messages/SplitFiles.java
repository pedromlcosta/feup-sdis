package messages;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;

import chunk.Chunk;

public class SplitFiles {
	private static final int CHUNK_SIZE = 64000;

	public SplitFiles() {
	}

	public ArrayList<byte[]> splitFile(String fileName) throws IOException {
		File file = new File(fileName);
		ArrayList<byte[]> chunks = new ArrayList<byte[]>();

		if (file.exists()) {
			FileInputStream fileReader = new FileInputStream(fileName);

			while (fileReader.available() > 0) {
				byte[] chunk = new byte[CHUNK_SIZE];
				int bytesRead = fileReader.read(chunk);
				chunks.add(Arrays.copyOf(chunk, bytesRead));
			}
			fileReader.close();
		} else
			return null;
		System.out.println(chunks.size());
		for (byte[] chunk : chunks) {
			System.out.println(chunk.length);
		}
		return chunks;
	}

	public byte[] splitFile(String fileName, int StartPos) throws IOException {
		File file = new File(fileName);

		if (file.exists()) {
			FileInputStream fileReader = new FileInputStream(fileName);
			byte[] chunk = new byte[CHUNK_SIZE];
			int bytesRead = fileReader.read(chunk, StartPos, CHUNK_SIZE);
			fileReader.close();
			if (bytesRead == 0)
				return null;
			else
				return Arrays.copyOf(chunk, bytesRead);

		} else
			return null;
	}

	public void joinChunks(ArrayList<byte[]> chunks, String name) throws IOException {
		FileOutputStream out = new FileOutputStream(name);
		for (byte[] chunk : chunks) {
			out.write(chunk);
		}
		out.close();
	}

	public void joinChunk(byte[] chunk, String name, int pos) throws IOException {

		RandomAccessFile fileOut = new RandomAccessFile(name, "rw");
		fileOut.write(chunk, pos, chunk.length);
		fileOut.close();
	}

	public boolean joinChunk(Chunk chunk, String name) throws IOException {

		RandomAccessFile fileOut = new RandomAccessFile(name, "rw");
		byte[] data = chunk.getData();
		int id = chunk.getId().getChunkNumber();
		int pos;
		
		if (data.length == 0) {
			fileOut.close();
			return true;
		} else if (data.length == CHUNK_SIZE)
			pos = CHUNK_SIZE * id;
		else if (data.length < CHUNK_SIZE) {
			pos = CHUNK_SIZE * (id - 1);
		} else {
			fileOut.close();
			return false;
		}

		fileOut.write(data, pos, data.length);
		fileOut.close();
		return true;
	}
}