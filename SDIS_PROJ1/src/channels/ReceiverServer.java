package channels;

import java.io.*;
import java.net.*;

import messages.ChunkMsg;
import messages.DeleteMsg;
import messages.GetChunkMsg;
import messages.Message;
import messages.PutChunkMsg;
import messages.StoredMsg;
import service.Processor;

public class ReceiverServer extends Thread {

	private MulticastSocket socket = null;
	private boolean quitFlag = false;
	private int serverID;
	private InetAddress addr;
	private int port;
	private byte[] buf = new byte[256];

	public ReceiverServer() {

	}

	public ReceiverServer(boolean quitFlag, int serverID, InetAddress addr, int port) {
		this.quitFlag = quitFlag;
		this.serverID = serverID;
		this.addr = addr;
		this.port = port;
		try {
			this.socket = new MulticastSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {

		while (!quitFlag) {
			System.out.println("Started Running the thread");
			DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);
			try {
				socket.receive(receivePacket);
			} catch (SocketTimeoutException e) {
				System.out.println("Error TimeOut");
			} catch (IOException e) {
				e.printStackTrace();
			}

			byte[] receivedMessage = receivePacket.getData();
			String receivedString = new String(receivedMessage);
			System.out.println(receivedString.length() + "  " + receivedString.isEmpty() + "   L:" + receivedString);
			if (receivedString.length() > 0) {
				receivedString = receivedString.substring(0, receivePacket.getLength());

				System.out.println("Server Received: " + receivedString);

				// Add message to the queue to get processed
				Message msg = new Message();
				String[] messageFields = msg.parseMessage(receivedString);
				// TODO Check if it is valid

				switch (messageFields[0]) {
				case "PUTCHUNK":
					msg = new PutChunkMsg(messageFields);
					break;
				case "STORED":
					msg = new StoredMsg(messageFields);
					break;
				case "GETCHUNK":
					msg = new GetChunkMsg(messageFields);
					break;
				case "CHUNK":
					msg = new ChunkMsg(messageFields);
					break;
				case "DELETE":
					msg = new DeleteMsg(messageFields);
					break;
				}

				// Processor processingThread = new Processor(msg);
				// processingThread.start();

			}
		}
	}

	public DatagramPacket createDatagramPacket(byte[] buffer) {
		return new DatagramPacket(buffer, buffer.length, this.getAddr(), this.getPort());
	}

	public void open() throws IOException {
		this.socket = new MulticastSocket(port);
	}

	public void close() {
		this.socket.close();
	}

	public void joinMulticastGroup(InetAddress mcastaddr) {
		try {
			if (this.socket != null)
				this.socket.joinGroup(mcastaddr);
		} catch (IOException e) {
			System.out.println("Error in Join Group");
			e.printStackTrace();
		}
	}

	public void leaveMulticastGroup(InetAddress mcastaddr) {
		try {
			if (this.socket != null)
				this.socket.leaveGroup(mcastaddr);
		} catch (IOException e) {
			System.out.println("Error in Leave Group");
			e.printStackTrace();
		}
	}

	public void joinMulticastGroup() {
		try {
			if (this.socket != null)
				this.socket.joinGroup(this.getAddr());
		} catch (IOException e) {
			System.out.println("Error in Join Group");
			e.printStackTrace();
		}
	}

	public void leaveMulticastGroup() {
		try {
			if (this.socket != null)
				this.socket.leaveGroup(this.getAddr());
		} catch (IOException e) {
			System.out.println("Error in Leave Group");
			e.printStackTrace();
		}
	}

	public DatagramPacket createDatagramPacket(byte[] buffer, InetAddress addr, int port) {
		return new DatagramPacket(buffer, buffer.length, addr, port);
	}

	public void writePacket(DatagramPacket p) {
		try {
			this.socket.send(p);
			System.out.println("Packer sent");
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error writePacket");
		}
	}

	public void readPacket(DatagramPacket p) {
		try {
			this.socket.receive(p);
		} catch (SocketTimeoutException e) {
			System.out.println("Error TimeOut");
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error readPacket");
		}
	}

	public MulticastSocket getSocket() {
		return socket;
	}

	public int getServerID() {
		return serverID;
	}

	public void setServerID(int serverID) {
		this.serverID = serverID;
	}

	public void setSocket(MulticastSocket socket) {
		this.socket = socket;
	}

	public boolean isQuitFlag() {
		return quitFlag;
	}

	public void setQuitFlag(boolean quitFlag) {
		this.quitFlag = quitFlag;
	}

	public InetAddress getAddr() {
		return addr;
	}

	public void setAddr(InetAddress addr) {
		this.addr = addr;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
}
