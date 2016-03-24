package messages;

public class DeleteMsg extends Message{
	
	public DeleteMsg(){
		type = MESSAGE_TYPE.DELETE;
	}

	public DeleteMsg(String[] messageFields) {
		if(messageFields.length < 5){
			System.out.println("Failed creating Stored message. Not enough fields");
			return;
		}
		version = messageFields[1];
		senderID = messageFields[2];
		fileId = messageFields[3];
	}

}
