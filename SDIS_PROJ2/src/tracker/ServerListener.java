package tracker;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;

import javax.net.ssl.SSLSocket;

public class ServerListener extends Thread {

	private Tracker tracker;
	private DataInputStream in;
	private DataOutputStream out;
	private ByteArrayOutputStream os;
	private byte[] messageByte;
	private static int MAX_MESSAGE_LENGTH = 64000;
	private static String EOL = "\r\n";
	private SSLSocket remoteSocket;
	
	// TEST STUFF
	BufferedReader input;
	PrintWriter output;
	
	public void run() {
		
		
		//System.out.println(remoteSocket.getSession());
		//System.out.println("Here");
		
		try {
			
			
			while (!remoteSocket.isClosed() || !remoteSocket.isOutputShutdown() || remoteSocket.isInputShutdown()) {
				// RECEIVE CLIENT REQUEST
				
				int bytesRead = in.read(messageByte);
				String message = new String(messageByte, 0, bytesRead);
				
				// SEND SERVER REPLY
				byte[] response = processClientRequest(message, bytesRead);
				out.write(response);
			}
			System.out.println("Socket with peer" + "1" +" was closed.");
			
		} catch (IOException e) {
			// Close stuff
			try {
				//in.close();
				//out.close();
				remoteSocket.close();
			} catch (IOException e1) {
				System.out.println("Failed at closing streams. They are already closed, maybe socket was closed?");
			}
			//e.printStackTrace();
			System.out.println("Socket was closed.");
			return;
			// DO SOCKET CLOSED STUFF HERE.
		} 
		
	}

	public ServerListener(SSLSocket remoteSocket, Tracker tracker) throws IOException {
		
		this.tracker = tracker;
		this.remoteSocket = remoteSocket;

		in = new DataInputStream(remoteSocket.getInputStream());
		out = new DataOutputStream(remoteSocket.getOutputStream());
		os = new ByteArrayOutputStream();
		
		messageByte = new byte[MAX_MESSAGE_LENGTH];
	}

	public Socket getRemoteSocket() {
		return remoteSocket;
	}

	public void setRemoteSocket(SSLSocket remoteSocket) {
		this.remoteSocket = remoteSocket;
	}

	public byte[] processClientRequest(String request, int length) {

		byte[] response = null;
		
		int index = request.indexOf(endHeader());
		if(index == -1){
			response = ("NULL"+ " " + "ERROR" + endHeader() + "No header especified").getBytes();
			return response;
			
		}
		String header = request.substring(0,index);
		int interval = endHeader().getBytes().length;
		byte[] body = Arrays.copyOfRange(messageByte,index+interval,length);
		String[] tokens = header.split(" ");
		
		Tracker tracker = Tracker.getInstance();
		
		try{
			if(tokens[0] != null){
				switch(tokens[0]){
				case "STORE":
					os.write((tokens[0] + " ").getBytes());
					if(tracker.store(tokens[1],body))
						os.write(("SUCCESS" + endHeader()).getBytes());
					else
						os.write(("ERROR" + endHeader()).getBytes());
					response = os.toByteArray();
					os.reset();
					break;
				case "DATAREQUEST":
					response = tracker.getPeerData(tokens[1]);
					if(response != null){
						os.write((tokens[0] + " " + "SUCCESS" + endHeader()).getBytes());
						os.write(response);
						response = os.toByteArray();
						os.reset();
					}
					else
						response = (tokens[0] + " " + "ERROR" + endHeader() + "Cannot find a peerData with given id").getBytes();
					break;
				case "KEYREQUEST":
					response = tracker.getKey();
					if(response != null){
						os.write((tokens[0] + " " + "SUCCESS" + endHeader()).getBytes());
						os.write(response);
						response = os.toByteArray();
					}
					else
						response = (tokens[0] + " " + "ERROR" + endHeader() + "Cannot retrieve a key").getBytes();
					break;
				default:
					response = (tokens[0] + " " + "ERROR" + endHeader() + "Cannot recognize request").getBytes();
				}
			}
			else
				response = ("NULL"+ " " + "ERROR" + endHeader() + "Header Format incorrect").getBytes();
		}
		catch(IOException e){
			e.printStackTrace();
		}
		return response;
	}
	
	private String endHeader(){
		
		return EOL + EOL;
	}
}
