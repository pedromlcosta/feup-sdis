package channels;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.Arrays;

import service.Peer;
import service.Processor;

public class ReceiverServer extends Thread {

	private MulticastSocket socket = null;
	private boolean quitFlag = false;
	private int serverID;
	private InetAddress addr;
	private int port;
	private byte[] buf = new byte[256];
	private Peer user;

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
			} catch (IOException e) {
				e.printStackTrace();
			}

			byte[] receivedMessage = receivePacket.getData();
			byte[] body = null;
			String header = null;
			for (int i = 0; i < receivedMessage.length; i++) {
				if (receivedMessage[i] == '\r' && receivedMessage[i + 1] == '\n')
					if (receivedMessage[i + 2] == '\r' && receivedMessage[i + 3] == '\n') {
						header = new String(Arrays.copyOf(receivedMessage, i));
						body = Arrays.copyOfRange(receivedMessage, i, receivedMessage.length);
						break;
					}
			}
			// System.out.println(receivedString.length() + " " +
			// receivedString.isEmpty() + " L:" + receivedString);
			if (header.length() > 0) {
				header = header.substring(0, receivePacket.getLength());

				System.out.println("Server Received: " + header);

				// TODO Check if ReceivedString is valid!!!!!!!!!!
				// TODO funcao auxiliar que tira string do packet?

				Processor processingThread = new Processor(header, body);
				processingThread.start();

				header = "";
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
			System.out.println("Packet sent");
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

	public void setAddr(InetAddress addr) throws Exception{
		this.addr = addr;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) throws Exception{
		this.port = port;
	}

	public byte[] getBuf() {
		return buf;
	}

	public void setBuf(byte[] buf) {
		this.buf = buf;
	}

	public Peer getUser() {
		return user;
	}

	public void setUser(Peer user) {
		this.user = user;
	}

}
