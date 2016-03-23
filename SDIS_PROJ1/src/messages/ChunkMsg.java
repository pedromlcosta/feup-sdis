package messages;


public class ChunkMsg extends Message{

String ChunkNo;
	
	public ChunkMsg(){
		type = MESSAGE_TYPE.CHUNK;
	}
	
	public ChunkMsg(String[] messageFields){
		
	}
}
