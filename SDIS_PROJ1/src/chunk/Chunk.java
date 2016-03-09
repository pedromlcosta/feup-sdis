package chunk;

import file.FileID;

public class Chunk {
	private static final int CHUNK_SIZE = 64000;
	private ChunkID id;
	private byte data[];

	public Chunk(FileID id, int number, byte[] data) {
		this.data = data;
		this.id = new ChunkID(id, number);
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

}
