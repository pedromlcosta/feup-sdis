package messages;

public class GetChunkMsg extends Message{

	String ChunkNo;
	
	public GetChunkMsg(){
		type = MESSAGE_TYPE.GETCHUNK;
	}

	public GetChunkMsg(String[] messageFields) {
		// TODO Auto-generated constructor stub
	}
}
