package file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import chunk.Chunk;
import extra.Extra;

public class FileID {
	private int nChunks;
	private int desiredRepDegree;
	private int homeServer;
	private String ID;

	public FileID(String fileName) {
		// TODO multiple Nchunks bellow
		File file = new File(fileName);
		String absPath = file.getAbsolutePath();
		Path path = file.toPath();
		long fileSize = file.length();
		// TODO case where fileSize multiple of ChunkSize
		this.nChunks = (int) Math.ceil((1.0 * fileSize) / Chunk.getChunkSize());
		// each file must have at least 1 chunck
		this.nChunks = ((this.nChunks == 0 && fileSize > 0) ? 1 : this.nChunks);
		try {
			BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
			// O ID  criado com Path,Owner e data da ultima modificação
			this.ID = Extra.SHA256(absPath + Files.getOwner(path).toString() + attr.lastModifiedTime().toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("FileSize: " + file.length() + "\n" + "Nchunks: " + this.nChunks + "\n ID: " + this.ID);
	}

	public int getnChunks() {
		return nChunks;
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

}
