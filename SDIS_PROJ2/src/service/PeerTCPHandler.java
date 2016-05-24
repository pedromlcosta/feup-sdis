package service;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.net.ssl.SSLSocket;

public class PeerTCPHandler {

	static SSLSocket remoteSocket;
	private ByteArrayOutputStream os;
	DataInputStream in;
	DataOutputStream out;
	
	PeerTCPHandler(){
		
	}
	
public void run() {
		
		
		//System.out.println(remoteSocket.getSession());
		//System.out.println("Here");
		
		try {
			
			
			while (!remoteSocket.isClosed()) {
				// RECEIVE CLIENT REQUEST
				
				/*
				int bytesRead = in.read(messageByte);
				String message = new String(messageByte, 0, bytesRead);
				
				// SEND SERVER REPLY
				byte[] response = processClientRequest(message, bytesRead);
				*/
				out.write(new byte[2]);
				
			}
			System.out.println("Socket was closed.");
			
		} catch (IOException e) {
			// Close stuff
			try {
				in.close();
				out.close();
				remoteSocket.close();
			} catch (IOException e1) {
				System.out.println("Failed at closing streams. They are already closed, maybe socket was closed?");
			}
			e.printStackTrace();
			System.out.println("Socket was closed.");
			// DO SOCKET CLOSED STUFF HERE.
		} 
		
	}
	
}
