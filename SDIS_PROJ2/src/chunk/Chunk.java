package chunk;

import java.io.Serializable;

public class Chunk implements Serializable {
	private static final long serialVersionUID = 2L;
	private static final int CHUNK_SIZE = 36000;
	private ChunkID id;
	private byte data[];

	/**
	 * Constructor for chunk
	 * 
	 * @param fileID
	 * @param number
	 * @param data
	 */
	public Chunk(String fileID, int number, byte[] data) {
		this.data = data.clone();
		this.id = new ChunkID(fileID, number);
	}

	/**
	 * Constructor for chunk
	 * 
	 * @param id
	 * @param data
	 */
	public Chunk(ChunkID id, byte[] data) {
		this.id = id;
		this.data = data;
	}

	/**
	 * 
	 * @return the ChunkID of said chunk
	 */
	public ChunkID getId() {
		return id;
	}

	/**
	 * sets the field id
	 * 
	 * @param id
	 */
	public void setId(ChunkID id) {
		this.id = id;
	}

	/**
	 * 
	 * @return the "body" of the chunk
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * sets the chunk data (aka the body of the chunk)
	 * 
	 * @param data
	 */
	public void setData(byte[] data) {
		this.data = data;
	}

	/**
	 * 
	 * @return the max size that each chunk data may have
	 */
	public static int getChunkSize() {
		return CHUNK_SIZE;
	}

	/**
	 * increases the Representation degree in ChunkID
	 */
	public void increaseRepDegree() {
		this.id.increaseRepDegree();
	}

	/**
	 * 
	 * @return the desired Representation Degree
	 */
	public int getDesiredRepDegree() {
		return this.id.getDesiredRepDegree();
	}

	/**
	 * 
	 * @return the current Representation Degree
	 */
	public int getActualRepDegree() {
		return this.id.getActualRepDegree();
	}

	/**
	 * sets the Desired Representation Degree of id
	 * 
	 * @param i
	 */
	public void setDesiredRepDegree(int replicationDeg) {
		this.id.setDesiredRepDegree(replicationDeg);
	}

	/**
	 * sets the Actual Representation Degree of id
	 * 
	 * @param i
	 */
	public void setActualRepDegree(int i) {
		this.id.setActualRepDegree(i);

	}

	/**
	 * "converts" the object into a string
	 */
	public String toString() {
		if (data != null)
			return "ID: " + this.id + "Length Of Data: " + this.data.length;

		return "ID: " + this.id + "Data is null";
	}

}
