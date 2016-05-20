package tracker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

public class ServerListener extends Thread {

	BufferedReader in;
	PrintWriter out;
	private Socket remoteSocket;

	public void run() {
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
		} catch (IOException e) {
			// Close stuff
			try {
				in.close();
				out.close();
				remoteSocket.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			e.printStackTrace();
		}
	}

	public ServerListener(Socket remoteSocket) throws IOException {
		this.remoteSocket = remoteSocket;

		in = new BufferedReader(new InputStreamReader(
				remoteSocket.getInputStream()));
		out = new PrintWriter(remoteSocket.getOutputStream());
	}

	public Socket getRemoteSocket() {
		return remoteSocket;
	}

	public void setRemoteSocket(Socket remoteSocket) {
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
