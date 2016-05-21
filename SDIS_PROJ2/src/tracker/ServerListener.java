package tracker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

import javax.net.ssl.SSLSocket;

public class ServerListener extends Thread {

	BufferedReader in;
	PrintWriter out;
	private SSLSocket remoteSocket;

	public void run() {
		
		
		//System.out.println(remoteSocket.getSession());
		//System.out.println("Here");
		
		try {
			
			
			while (!remoteSocket.isClosed()) {
				// RECEIVE CLIENT REQUEST
				
				String receivedString = in.readLine();
				System.out.println("Server Received: " + receivedString);

				// SEND SERVER REPLY
				String response = processClientRequest(receivedString);
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

		in = new BufferedReader(new InputStreamReader(
				remoteSocket.getInputStream()));
		out = new PrintWriter(remoteSocket.getOutputStream());
	}

	public Socket getRemoteSocket() {
		return remoteSocket;
	}

	public void setRemoteSocket(SSLSocket remoteSocket) {
		this.remoteSocket = remoteSocket;
	}

	public static String processClientRequest(String request) {

		String response = null;
		String[] tokens = request.split(" ");

		/*
		 * if(tokens[0].equals("LOOKUP")){ if (tokens.length != 2){ response =
		 * "ERROR: Command has invalid arguments"; }else{ response =
		 * lookup(tokens[1]); } }
		 */

		response = "Response";
		return response;

	}
}
