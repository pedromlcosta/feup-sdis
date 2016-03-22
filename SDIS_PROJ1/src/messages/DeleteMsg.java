package messages;

import messages.Message.MESSAGE_TYPE;

public class DeleteMsg extends Message{
	
	public DeleteMsg(){
		type = MESSAGE_TYPE.DELETE;
	}

	public DeleteMsg(String[] messageFields) {
		// TODO Auto-generated constructor stub
	}

}
