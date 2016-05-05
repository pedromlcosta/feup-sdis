package channels;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class UDPConnection {
	private DatagramSocket socket = null;
	private InetAddress addr;
	private int port;
	private DatagramPacket packet;
	private byte[] buf;

	public UDPConnection(InetAddress addr, int port) throws SocketException {
		super();
		this.addr = addr;
		this.port = port;
		socket = new DatagramSocket();
	}

	// TODO CHECK IF OK
	public byte[] receive() throws IOException {
		buf = new byte[256];
		packet = new DatagramPacket(buf, buf.length);
		socket.receive(packet);
		buf[packet.getLength()] = '\0';
		return buf;
	}

	// TODO CHECK IF OK
	public void send(String message) throws IOException {
	
		buf = new byte[256];
		buf = message.getBytes();
		packet = new DatagramPacket(buf, buf.length, addr, port);
		socket.send(packet);
	}

	public DatagramSocket getSocket() {
		return socket;
	}

	public void setSocket(DatagramSocket socket) {
		this.socket = socket;
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

	public DatagramPacket getPacket() {
		return packet;
	}

	public void setPacket(DatagramPacket packet) {
		this.packet = packet;
	}

	public byte[] getBuf() {
		return buf;
	}

	public void setBuf(byte[] buf) {
		this.buf = buf;
	}

}