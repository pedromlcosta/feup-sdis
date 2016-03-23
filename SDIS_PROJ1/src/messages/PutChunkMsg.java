package messages;


public class PutChunkMsg extends Message{
	
String ChunkNo;
String ReplicationDeg;
	
	public PutChunkMsg(){
		type = MESSAGE_TYPE.PUTCHUNK;
	}

	public PutChunkMsg(String[] messageFields) {
	}
}
