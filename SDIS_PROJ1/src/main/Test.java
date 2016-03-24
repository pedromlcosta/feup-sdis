package main;

import messages.Message;
import messages.Message.MESSAGE_TYPE;

public class Test {
	
	public static void main(String[] args){
		Message msg = new Message();
		// createMessage
		String[] messageArgs2 = new String[5];
		messageArgs2[0] = "1.0";
		messageArgs2[1] = "2";
		messageArgs2[2] = "456";
		messageArgs2[3] = "544";
		messageArgs2[4] = "3";
		msg.createMessage(MESSAGE_TYPE.PUTCHUNK, messageArgs2, "derp".getBytes());
		System.out.println("Msg to send: " + msg.getMessageToSend());
		

		String receivedMessage = new String(msg.getMessageBytes());
		System.out.println("Msg received: " + msg.getMessageToSend());
		System.out.println("Parsing done... Args are:");
		String[] receivedArgs = msg.parseMessage(receivedMessage);
		String test = "";
		for (int i=0; i < receivedArgs.length; i++){
				test += "Arg " + Integer.toString(i+1) + " " + receivedArgs[i] + "\n" ;
		}
		System.out.println(test);
		
	}
	
	
}
