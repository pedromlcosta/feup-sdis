package messages;

import java.util.regex.Pattern;


public class ChunkMsg extends Message{

String ChunkNo;
	
	public ChunkMsg(){
		type = MESSAGE_TYPE.CHUNK;
	}
	
	public ChunkMsg(String[] messageFields){
		if(messageFields.length < 5){
			System.out.println("Failed creating Stored message. Not enough fields");
			return;
		}
		version = messageFields[1];
		senderID = messageFields[2];
		fileId = messageFields[3];
		chunkNo = Integer.parseInt(messageFields[4]);
		body = messageFields[5].getBytes();
	}
	
}
