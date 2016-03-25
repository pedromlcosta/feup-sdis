package chunk;

import java.io.Serializable;

public class Chunk implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;
	private static final int CHUNK_SIZE = 64000;
	private ChunkID id;
	private byte data[];

	public Chunk(String fileID, int number, byte[] data) {
		this.data = data;
		this.id = new ChunkID(fileID, number);
	}

	public Chunk(ChunkID id, byte[] data) {
		this.id = id;
		this.data = data;
	}

	public ChunkID getId() {
		return id;
	}

	public void setId(ChunkID id) {
		this.id = id;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public static int getChunkSize() {
		return CHUNK_SIZE;
	}

	public void increaseRepDegree() {
		this.id.increaseRepDegree();
	}

	public int getDesiredRepDegree() {
		return this.id.getDesiredRepDegree();
	}

	public int getActualRepDegree() {
		return this.id.getDesiredRepDegree();
	}

	public void setDesiredRepDegree(int replicationDeg) {
		this.id.setDesiredRepDegree(replicationDeg);
	}

	public void setActualRepDegree(int i) {
		this.id.setActualRepDegree(i);

	}

	public String toString() {
		return "ID: " + this.id + "Length Of Data: " + this.data.length;
	}

}
