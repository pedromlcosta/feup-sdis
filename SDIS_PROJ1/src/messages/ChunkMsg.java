package messages;

import messages.Message.MESSAGE_TYPE;

public class ChunkMsg extends Message{

String ChunkNo;
	
	public ChunkMsg(){
		type = MESSAGE_TYPE.CHUNK;
	}
	
	public ChunkMsg(String[] messageFields){
		
	}
}
