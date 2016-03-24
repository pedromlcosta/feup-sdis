package messages;

public class GetChunkMsg extends Message{

	String ChunkNo;
	
	public GetChunkMsg(){
		type = MESSAGE_TYPE.GETCHUNK;
	}

	public GetChunkMsg(String[] messageFields) {
		if(messageFields.length < 5){
			System.out.println("Failed creating GetChunk message. Not enough fields");
			return;
		}
		version = messageFields[1];
		senderID = messageFields[2];
		fileId = messageFields[3];
		chunkNo = Integer.parseInt(messageFields[4]);
	}
}
