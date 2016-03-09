package chunk;

import file.FileID;

public class Chunk {
	private static final int CHUNK_SIZE = 64000;
	private ChunkID id;
	private int desiredRepDegree;
	private int actualRepDegree;
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
	
	public void increaseRepDegree(){
		this.actualRepDegree++;
	}
	
	public void decreaseRepDegree(){
		this.actualRepDegree--;
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
