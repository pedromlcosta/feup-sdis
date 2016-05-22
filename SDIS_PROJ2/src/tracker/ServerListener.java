package tracker;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import javax.net.ssl.SSLSocket;

public class ServerListener extends Thread {

	private DataInputStream in;
	private PrintWriter out;
	private byte[] messageByte;
	private static int MAX_MESSAGE_LENGTH = 64000;
	private static String EOL = "\r\n";
	private SSLSocket remoteSocket;

	public void run() {
		
		
		//System.out.println(remoteSocket.getSession());
		//System.out.println("Here");
		
		try {
			
			
			while (!remoteSocket.isClosed()) {
				// RECEIVE CLIENT REQUEST
				
				int bytesRead = in.read(messageByte);
				String message = new String(messageByte, 0, bytesRead);
				System.out.println("Server Received: " + message);
				
				// SEND SERVER REPLY
				String response = processClientRequest(message);
				out.println(response);
				out.flush();

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

	public ServerListener(SSLSocket remoteSocket) throws IOException {
		this.remoteSocket = remoteSocket;

		in = new DataInputStream(remoteSocket.getInputStream());
		out = new PrintWriter(remoteSocket.getOutputStream());
		
		messageByte = new byte[MAX_MESSAGE_LENGTH];
	}

	public Socket getRemoteSocket() {
		return remoteSocket;
	}

	public void setRemoteSocket(SSLSocket remoteSocket) {
		this.remoteSocket = remoteSocket;
	}

	public static String processClientRequest(String request) {

		String response = null;
		
		int index = request.indexOf(EOL+EOL);
		if(index == -1){
			response = "NULL"+ " " + "ERROR" + EOL + EOL + "No header especified";
			return response;
			
		}
		String header = request.substring(0,index);
		String body = request.substring(index+1);
		String[] tokens = header.split(" ");
		
		Tracker tracker = Tracker.getInstance();
		
		if(tokens[0] != null){
			switch(tokens[0]){
			case "STORE":
				response = tokens[0] + " ";
				if(tracker.store(tokens[1],body))
					response += "SUCCESS" + EOL + EOL;
				else
					response += "ERROR" + EOL + EOL;
				break;
			default:
				response = tokens[0] + " " + "ERROR" + EOL + EOL + "Cannot recognize request";
			}
		}
		else
			response = "NULL"+ " " + "ERROR" + EOL + EOL + "Header Format incorrect";
		
		return response;
	}
}
