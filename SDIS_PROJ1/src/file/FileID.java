package file;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

import chunk.Chunk;
import extra.Extra;

public class FileID implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final int MAX_NUMBER_OF_CHUNKS = 1000000;
	private long fileSize;
	private int nChunks;
	private String fileName;
	private int desiredRepDegree;
	private int homeServer;
	private String ID;
	private boolean multiple = false;
	// TODO anyone uses this?
	private ArrayList<Chunk> fileChunks = new ArrayList<Chunk>();

	public FileID() {

	}

	public FileID(String fileName) throws Exception {
		// TODO multiple Nchunks bellow
		File file = new File(fileName);
		String absPath = file.getAbsolutePath();
		Path path = file.toPath();
		fileSize = file.length();
		this.fileName = file.getName();
		// TODO case where fileSize multiple of ChunkSize
		this.nChunks = (int) Math.ceil((1.0 * fileSize) / Chunk.getChunkSize());
		// each file must have at least 1 chunck
		this.nChunks = ((this.nChunks == 0 && fileSize > 0) ? 1 : this.nChunks);
		if (nChunks > MAX_NUMBER_OF_CHUNKS) {
			System.out.println("File is to large to be backed up");
			throw new Exception("File Too large to be backed up");
		}
		// In case fileSize is a multiple of ChunkSize
		if (fileSize % Chunk.getChunkSize() == 0) {
			multiple = true;
		}
		try {
			BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
			// O ID criado com Path,Owner e data da ultima modificação
			this.ID = Extra.SHA256(absPath + Files.getOwner(path).toString() + attr.lastModifiedTime().toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		// System.out.println(
		// "FileID create\nFileSize: " + file.length() + "\n" + "Nchunks: " +
		// this.nChunks + "\n ID: " + this.ID);
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public int getnChunks() {
		return nChunks;
	}

	public ArrayList<Chunk> getFileChunks() {
		return fileChunks;
	}

	public void setFileChunks(ArrayList<Chunk> fileChunks) {
		this.fileChunks = fileChunks;
	}

	public void setnChunks(int nChunks) {
		this.nChunks = nChunks;
	}

	public int getDesiredRepDegree() {
		return desiredRepDegree;
	}

	public void setDesiredRepDegree(int desiredRepDegree) {
		this.desiredRepDegree = desiredRepDegree;
	}

	public int getHomeServer() {
		return homeServer;
	}

	public void setHomeServer(int homeServer) {
		this.homeServer = homeServer;
	}

	public String getID() {
		return ID;
	}

	public void setID(String ID) {
		this.ID = ID;
	}

	public boolean isMultiple() {
		return multiple;
	}

	public void setMultiple(boolean multiple) {
		this.multiple = multiple;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public static int getMaxNumberOfChunks() {
		return MAX_NUMBER_OF_CHUNKS;
	}

}
