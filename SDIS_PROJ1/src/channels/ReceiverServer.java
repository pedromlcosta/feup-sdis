package channels;

import java.io.*;
import java.net.*;

public class ReceiverServer extends Thread {

	private MulticastSocket socket = null;
	private boolean quitFlag = false;
	private int serverID;
	private InetAddress addr;
	private int port;
	

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

	public void joinMulticasGroup() {
		try {
			if (this.socket != null)
				this.socket.joinGroup(this.getAddr());
		} catch (IOException e) {
			System.out.println("Error in Join Group");
			e.printStackTrace();
		}
	}

	public void leaveMulticasGroup() {
		try {
			if (this.socket != null)
				this.socket.leaveGroup(this.getAddr());
		} catch (IOException e) {
			System.out.println("Error in Leave Group");
			e.printStackTrace();
		}
	}

	public DatagramPacket createDatagramPacket(byte[] buffer) {
		return new DatagramPacket(buffer, buffer.length, this.getAddr(), this.getPort());
	}

	public void joinMulticasGroup(InetAddress mcastaddr) {
		try {
			if (this.socket != null)
				this.socket.joinGroup(mcastaddr);
		} catch (IOException e) {
			System.out.println("Error in Join Group");
			e.printStackTrace();
		}
	}

	public void leaveMulticasGroup(InetAddress mcastaddr) {
		try {
			if (this.socket != null)
				this.socket.leaveGroup(mcastaddr);
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
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error writePacket");
		}
	}

	public void readPacket(DatagramPacket p) {
		try {
			this.socket.receive(p);
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
