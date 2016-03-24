package chunk;

public class Chunk {
	private static final int CHUNK_SIZE = 64000;
	private ChunkID id;
	private byte data[];

	public Chunk(String id, int number, byte[] data) {
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

}
