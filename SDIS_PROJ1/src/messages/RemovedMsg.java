package messages;


public class RemovedMsg extends Message{
String ChunkNo;
	
	public RemovedMsg(){
		type = MESSAGE_TYPE.REMOVED;
	}

	public RemovedMsg(String[] messageFields) {
		if(messageFields.length < 5){
			System.out.println("Failed creating Stored message. Not enough fields");
			return;
		}
		version = messageFields[1];
		senderID = messageFields[2];
		fileId = messageFields[3];
		chunkNo = Integer.parseInt(messageFields[4]);
	}
}
