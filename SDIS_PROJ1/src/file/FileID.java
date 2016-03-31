package file;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;

import chunk.Chunk;
import chunk.ChunkID;
import extra.Extra;

public class FileID implements Serializable, Comparable<FileID> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int MAX_NUMBER_OF_CHUNKS = 1000000;
	private long fileSize;
	private String fileName;
	private FileTime lastChange;
	private int nChunks;
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
		this.fileName = fileName;

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
			this.lastChange = attr.lastModifiedTime();
			this.ID = Extra.SHA256(absPath + Files.getOwner(path).toString() + lastChange.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		// System.out.println(
		// "FileID create\nFileSize: " + file.length() + "\n" + "Nchunks: " +
		// this.nChunks + "\n ID: " + this.ID);
	}
	
	@Override
	public int compareTo(FileID file) {
		return lastChange.compareTo(file.getLastChange());

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ID == null) ? 0 : ID.hashCode());
		result = prime * result + desiredRepDegree;
		result = prime * result + ((fileChunks == null) ? 0 : fileChunks.hashCode());
		result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
		result = prime * result + (int) (fileSize ^ (fileSize >>> 32));
		result = prime * result + homeServer;
		result = prime * result + ((lastChange == null) ? 0 : lastChange.hashCode());
		result = prime * result + (multiple ? 1231 : 1237);
		result = prime * result + nChunks;
		return result;
	}


	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public FileTime getLastChange() {
		return lastChange;
	}

	public void setLastChange(FileTime lastChange) {
		this.lastChange = lastChange;
	}

	public static int getMaxNumberOfChunks() {
		return MAX_NUMBER_OF_CHUNKS;
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

}
