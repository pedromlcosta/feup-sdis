package chunk;

public class ChunkID {
	private String fileID;
	private int chunkNumber;

	public ChunkID(String id, int number) {
		this.fileID = id;
		this.chunkNumber = number;
	}

	public String getFile() {
		return fileID;
	}

	public void setFile(String file) {
		this.fileID = file;
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

		if (this.getChunkNumber() != chunkObj.getChunkNumber() || !this.getFile().equals(chunkObj.getFile()))
			return false;

		return true;
	}
}
