package messages;


public class PutChunkMsg extends Message{
	
String ChunkNo;
String ReplicationDeg;
	
	public PutChunkMsg(){
		type = MESSAGE_TYPE.PUTCHUNK;
	}

	public PutChunkMsg(String[] messageFields) {
		if(messageFields.length < 5){
			System.out.println("Failed creating Stored message. Not enough fields");
			return;
		}
		version = messageFields[1];
		senderID = messageFields[2];
		fileId = messageFields[3]; // process fileID back to normal SHA here?
		chunkNo = Integer.parseInt(messageFields[4]);
		replicationDeg = Integer.parseInt(messageFields[5]);
		body = messageFields[6].getBytes();
	}
}
