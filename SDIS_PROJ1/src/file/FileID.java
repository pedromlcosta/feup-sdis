package file;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

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

	/**
	 * Empty Constructor for FileID
	 */
	public FileID() {

	}

	/**
	 * Constructor for FileID
	 * 
	 * @param fileName
	 * @throws Exception
	 *             -> if the file has a ChunkNumber bigger than what is allowed
	 */
	public FileID(String fileName) throws Exception {
		File file = new File(fileName);
		// get path for file
		String absPath = file.getAbsolutePath();
		Path path = file.toPath();
		// get size of file
		fileSize = file.length();
		// get file Name for example if the path was src/file.txt the name would
		// be file.txt
		this.fileName = file.getName();

		this.nChunks = (int) Math.ceil((1.0 * fileSize) / Chunk.getChunkSize());
		// each file must have at least 1 chunk if fileSize > 0
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

	}

	/**
	 * Hash code for the FileID object
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ID == null) ? 0 : ID.hashCode());
		result = prime * result + desiredRepDegree;
		result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
		result = prime * result + (int) (fileSize ^ (fileSize >>> 32));
		result = prime * result + homeServer;
		result = prime * result + (multiple ? 1231 : 1237);
		result = prime * result + nChunks;
		return result;
	}

	/***
	 * return true if FileIDs are equal
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;

		FileID other = (FileID) obj;
		if (!ID.equals(other.ID))
			return false;
		if (desiredRepDegree != other.desiredRepDegree)
			return false;
		if (!fileName.equals(other.fileName))
			return false;
		if (fileSize != other.fileSize)
			return false;
		if (homeServer != other.homeServer)
			return false;
		if (nChunks != other.nChunks)
			return false;
		return true;
	}

	/**
	 * 
	 * @return size of the file which fileID represents
	 */
	public long getFileSize() {
		return fileSize;
	}

	/**
	 * set the fileSize
	 * 
	 * @param fileSize
	 */
	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	/**
	 * 
	 * @return the number of chunks that file has
	 */
	public int getnChunks() {
		return nChunks;
	}

	/**
	 * sets the number of chunks
	 * 
	 * @param nChunks
	 */
	public void setnChunks(int nChunks) {
		this.nChunks = nChunks;
	}

	/**
	 * 
	 * @return the desired Degree for all the chunks in the file
	 */
	public int getDesiredRepDegree() {
		return desiredRepDegree;
	}

	/**
	 * sets the desired representation degree
	 * 
	 * @param desiredRepDegree
	 */
	public void setDesiredRepDegree(int desiredRepDegree) {
		this.desiredRepDegree = desiredRepDegree;
	}

	/**
	 * 
	 * @return the server who backed up this file, its "home"
	 */
	public int getHomeServer() {
		return homeServer;
	}

	/**
	 * sets the homeServer
	 * 
	 * @param homeServer
	 */
	public void setHomeServer(int homeServer) {
		this.homeServer = homeServer;
	}

	/**
	 * 
	 * @return the fileID
	 */
	public String getID() {
		return ID;
	}

	/**
	 * sets the fileID
	 * 
	 * @param ID
	 */
	public void setID(String ID) {
		this.ID = ID;
	}

	/**
	 * 
	 * @return if the file has a size multiple of Chunk.Size
	 */
	public boolean isMultiple() {
		return multiple;
	}

	/**
	 * set the field multiple
	 * 
	 * @param multiple
	 */
	public void setMultiple(boolean multiple) {
		this.multiple = multiple;
	}

	/**
	 *
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * sets the fileName
	 * 
	 * @param fileName
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * 
	 * @return the max number of chunks a file is allowed to have
	 */
	public static int getMaxNumberOfChunks() {
		return MAX_NUMBER_OF_CHUNKS;
	}

}
