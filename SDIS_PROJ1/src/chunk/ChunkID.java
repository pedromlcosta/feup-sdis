package chunk;

import file.FileID;

public class ChunkID {
	private FileID file;
	private int chunkNumber;

	public ChunkID(FileID id, int number) {
		this.file = id;
		this.chunkNumber = number;
	}

	public FileID getFile() {
		return file;
	}

	public void setFile(FileID file) {
		this.file = file;
	}

	public int getChunkNumber() {
		return chunkNumber;
	}

	public void setChunkNumber(int chunkNumber) {
		this.chunkNumber = chunkNumber;
	}
}
