package chunk;

public class ChunkID {
	private String fileID;
	private int chunkNumber;
	private int desiredRepDegree;
	private int actualRepDegree;

	public ChunkID(String id, int number) {
		this.fileID = id;
		this.chunkNumber = number;
	}

	public int getChunkNumber() {
		return chunkNumber;
	}

	public void setChunkNumber(int chunkNumber) {
		this.chunkNumber = chunkNumber;
	}

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

}
