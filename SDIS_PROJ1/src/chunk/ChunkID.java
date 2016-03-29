package chunk;

import java.io.Serializable;

public class ChunkID implements Serializable, Comparable<ChunkID> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String fileID;
	private int chunkNumber;
	private int desiredRepDegree;
	private int actualRepDegree;

	public ChunkID(String id, int number) {
		this.fileID = id;
		this.chunkNumber = number;
	}

	public ChunkID(String fileID, int chunkNumber, int desiredRepDegree, int actualRepDegree) {
		this.fileID = fileID;
		this.chunkNumber = chunkNumber;
		this.desiredRepDegree = desiredRepDegree;
		this.actualRepDegree = actualRepDegree;
	}

	 
	public int getChunkNumber() {
		return chunkNumber;
	}

	public void setChunkNumber(int chunkNumber) {
		this.chunkNumber = chunkNumber;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + chunkNumber;
		result = prime * result + ((fileID == null) ? 0 : fileID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ChunkID))
			return false;

		ChunkID chunkObj = (ChunkID) obj;

		if (this.getChunkNumber() != chunkObj.getChunkNumber() || !this.getFileID().equals(chunkObj.getFileID()))
			return false;

		return true;
	}

	public String getFileID() {
		return fileID;
	}

	public void setFileID(String fileID) {
		this.fileID = fileID;
	}

	public int getDesiredRepDegree() {
		return desiredRepDegree;
	}

	public void setDesiredRepDegree(int desiredRepDegree) {
		this.desiredRepDegree = desiredRepDegree;
	}

	public int getActualRepDegree() {
		return actualRepDegree;
	}

	public void setActualRepDegree(int actualRepDegree) {
		this.actualRepDegree = actualRepDegree;
	}

	public void increaseRepDegree() {
		actualRepDegree++;

	}
	
	public void decreaseRepDegree() {
		actualRepDegree--;

	}

	public String toString() {
		return "\nFileID: " + this.fileID + " \nChunkNumber: " + this.chunkNumber + "\nDesired Degree: " + this.desiredRepDegree
				+ "\nActual Degree: " + this.actualRepDegree + "\n";
	}

	@Override
	public int compareTo(ChunkID chunk) {
		
		Integer spareRepDegree = this.actualRepDegree - this.desiredRepDegree;
		Integer chunkSpareRepDegree = chunk.actualRepDegree - chunk.desiredRepDegree;
		
		return -spareRepDegree.compareTo(chunkSpareRepDegree);
		
	}

}
