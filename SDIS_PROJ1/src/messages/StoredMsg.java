package messages;


public class StoredMsg extends Message{
String ChunkNo;
	
	public StoredMsg(){
		type = MESSAGE_TYPE.STORED;
	}

	public StoredMsg(String[] messageFields) {
		// TODO Auto-generated constructor stub
	}

}
