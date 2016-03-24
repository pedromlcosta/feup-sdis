package messages;

import java.util.regex.Pattern;


public class ChunkMsg extends Message{

String ChunkNo;
	
	public ChunkMsg(){
		type = MESSAGE_TYPE.CHUNK;
	}
	
	public ChunkMsg(String[] messageFields){
		
	}
	
	public synchronized String[] parseMessage(String Message) {
		Pattern pattern = Pattern.compile(PATTERN);
		String[] match = pattern.split(Message, -2);
		for (String a : match)
			System.out.println("Print: " + a);
		return match;
	}
}
