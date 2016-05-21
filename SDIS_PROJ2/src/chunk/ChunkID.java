package chunk;

import java.io.Serializable;

public class ChunkID implements Serializable, Comparable<ChunkID> {
	private static final long serialVersionUID = 1L;
	private String fileID;
	private int chunkNumber;
	private int desiredRepDegree;
	private int actualRepDegree;

	/**
	 * Constructor for ChunkID
	 * 
	 * @param id
	 * @param number
	 */
	public ChunkID(String id, int number) {
		this.fileID = id;
		this.chunkNumber = number;
	}

	/**
	 * Constructor for ChunkID
	 * 
	 * @param fileID
	 * @param chunkNumber
	 * @param desiredRepDegree
	 * @param actualRepDegree
	 */
	public ChunkID(String fileID, int chunkNumber, int desiredRepDegree, int actualRepDegree) {
		this.fileID = fileID;
		this.chunkNumber = chunkNumber;
		this.desiredRepDegree = desiredRepDegree;
		this.actualRepDegree = actualRepDegree;
	}

	/**
	 * 
	 * @return the chunk Number
	 */
	public int getChunkNumber() {
		return chunkNumber;
	}

	/**
	 * sets the field chunkNumber
	 * 
	 * @param chunkNumber
	 */
	public void setChunkNumber(int chunkNumber) {
		this.chunkNumber = chunkNumber;
	}

	/**
	 * hashCode for a ChunkID
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + chunkNumber;
		result = prime * result + ((fileID == null) ? 0 : fileID.hashCode());
		return result;
	}

	/**
	 * How to tell if two ChunkIDs are the same
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ChunkID))
			return false;

		ChunkID chunkObj = (ChunkID) obj;

		if (this.getChunkNumber() != chunkObj.getChunkNumber() || !this.getFileID().equals(chunkObj.getFileID()))
			return false;

		return true;
	}

	/**
	 * 
	 * @return the string which identifies the file to whom the chunk belongs
	 */
	public String getFileID() {
		return fileID;
	}

	/**
	 * sets the fieldID field
	 * 
	 * @param fileID
	 */
	public void setFileID(String fileID) {
		this.fileID = fileID;
	}

	/**
	 * 
	 * @return the desired representation degree
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
	 * @return the actual representation degree
	 */
	public int getActualRepDegree() {
		return actualRepDegree;
	}

	/**
	 * sets the actual representation degree
	 * 
	 * @param desiredRepDegree
	 */
	public void setActualRepDegree(int actualRepDegree) {
		this.actualRepDegree = actualRepDegree;
	}

	/**
	 * increases by 1 the actual representation degree
	 */
	public void increaseRepDegree() {
		actualRepDegree++;

	}

	/**
	 * decreases by 1 the actual representation degree
	 */
	public void decreaseRepDegree() {
		actualRepDegree--;

	}

	/**
	 * "Converts" the object into a string
	 */
	public String toString() {
		return "\nFileID: " + this.fileID + " \nChunkNumber: " + this.chunkNumber + "\nDesired Degree: " + this.desiredRepDegree + "\nActual Degree: " + this.actualRepDegree + "\n";
	}

	/**
	 * implementation of the function comparteTo determine if a chunkID object
	 * is equal/bigger/smaller
	 */
	@Override
	public int compareTo(ChunkID chunk) {

		Integer spareRepDegree = this.actualRepDegree - this.desiredRepDegree;
		Integer chunkSpareRepDegree = chunk.actualRepDegree - chunk.desiredRepDegree;

		return -spareRepDegree.compareTo(chunkSpareRepDegree);

	}

}
