package channels;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;

import chunk.Chunk;
import messages.Message;
import service.Peer;
import service.Processor;

public class MulticastServer extends Thread {

	private static final int HEADER_SIZE = 512;
	private MulticastSocket socket = null;
	private boolean quitFlag = false;
	private int serverID;
	private InetAddress addr;
	private int port;
	private byte[] buf = new byte[Chunk.getChunkSize() + 512];
	private Peer user;

	/**
	 * Default constructor for the receiver server
	 */
	public MulticastServer() {

	}

	/**
	 * 
	 * @param quitFlag flag for the infinite run cycle that receives the messages
	 * @param serverID Identifier of the peer this receiver belongs to
	 * @param addr Multicast IP address of this receiver
	 * @param port Multicast Port of this receiver
	 */
	public MulticastServer(boolean quitFlag,  InetAddress addr, int port) {
		this.quitFlag = quitFlag;
		this.addr = addr;
		this.port = port;
		try {
			this.socket = new MulticastSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Thread run method. Enters an infinite loop receiving messages. Each channel subclass
	 * will run this method for their own channels. When it receives a message, it preprocesses it
	 * and dispatches it to the Processor class.
	 */
	@Override
	public void run() {
		System.out.println("Started Running the thread");

		while (!quitFlag) {

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
						// packet.getLength otherwise he would always use the
						// max size of the buf
						body = Arrays.copyOfRange(receivedMessage, i + 4, receivePacket.getLength());
						break;
					}
			}
			if (header != null && header.length() > 0) {
				header = header.substring(0, header.length());

				String[] headerArgs = Message.parseHeader(header);

				// TODO ignore messages sent by server
				if (Integer.parseInt(headerArgs[2]) == Peer.getInstance().getServerID() || !headerArgs[1].equals(Peer.getCurrentVersion())) {
					// System.out.println("same server");
				} else {
					System.out.print("Server Received:");
					for (String arg : headerArgs)
						System.out.print(", " + arg);
					System.out.println("\n");
					new Processor(headerArgs, body).start();
				}
				header = "";
				body = null;
				buf = new byte[Chunk.getChunkSize() + HEADER_SIZE];
			}
		}
	}

	/**
	 * Initializes server socket with the value that is currently on the port data member
	 * @throws IOException
	 */
	public void createSocket() throws IOException {
		this.socket = new MulticastSocket(port);
	}

	/**
	 * Creates a datagram packet with the info given on the buffer array and addr/port of this server
	 * @param buffer bytes to put on the packet
	 * @return DatagramPacket created
	 */
	public DatagramPacket createDatagramPacket(byte[] buffer) {
		return new DatagramPacket(buffer, buffer.length, this.getAddr(), this.getPort());
	}

	/**
	 * Creates a datagram packet with the info given on the buffer array and addr/port of the arguments
	 * 
	 * @param buffer bytes to put on the packet
	 * @param port to associate with packet
	 * @param addr to associate with packet
	 * @return DatagramPacket created
	 */
	public static DatagramPacket createDatagramPacket(byte[] buffer, int port, InetAddress addr) {
		return new DatagramPacket(buffer, buffer.length, addr, port);
	}

	public DatagramPacket createDatagramPacket(byte[] buffer, InetAddress addr, int port) {
		return new DatagramPacket(buffer, buffer.length, addr, port);
	}
	
	/**
	 * Initializes multicast socket with the value on the port data member
	 * @throws IOException
	 */
	public void open() throws IOException {
		this.socket = new MulticastSocket(port);
	}

	/**
	 * Closes the socket
	 */
	public void close() {
		this.socket.close();
	}

	/**
	 * Joins multicast group associated with mcastaddr
	 * @param mcastaddr address of the multicast group
	 */
	public void joinMulticastGroup(InetAddress mcastaddr) {
		try {
			if (this.socket != null)
				this.socket.joinGroup(mcastaddr);
		} catch (IOException e) {
			System.out.println("Error in Join Group");
			e.printStackTrace();
		}
	}

	/**
	 * Leaves multicast group associated with mcastaddr
	 * @param mcastaddr address of the multicast group
	 */
	public void leaveMulticastGroup(InetAddress mcastaddr) {
		try {
			if (this.socket != null)
				this.socket.leaveGroup(mcastaddr);
		} catch (IOException e) {
			System.out.println("Error in Leave Group");
			e.printStackTrace();
		}
	}

	/**
	 * Joins multicast group associated with the address on the "addr" member data of this class
	 */
	public void joinMulticastGroup() {
		System.out.println("Joining group: " + this.getAddr().toString());
		try {
			if (this.socket != null) {
				this.socket.joinGroup(this.getAddr());
			} else
				System.out.println("SOCKET IS NULL");
		} catch (IOException e) {
			System.out.println("Error in Join Group");
			e.printStackTrace();
		}
	}

	/**
	 * Leaves multicast group associated with the address on the "addr" member data of this class
	 */
	public void leaveMulticastGroup() {
		try {
			if (this.socket != null)
				this.socket.leaveGroup(this.getAddr());
		} catch (IOException e) {
			System.out.println("Error in Leave Group");
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param p packet reference, of the packet to send
	 */
	public void writePacket(DatagramPacket p) {
		try {
			this.socket.send(p);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error writePacket");
		}
	}
	
	/**
	 * 
	 * @param p packet reference of the packet which will receive the information
	 */
	public void readPacket(DatagramPacket p) {
		try {
			this.socket.receive(p);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error readPacket");
		}
	}

	/**
	 * Getter for this data member
	 */
	public MulticastSocket getSocket() {
		return socket;
	}

	/**
	 * Getter for this data member
	 */
	public int getServerID() {
		return serverID;
	}
	
	/**
	 * Setter for this data member
	 */
	public void setServerID(int serverID) {
		this.serverID = serverID;
	}

	/**
	 * Setter for this data member
	 */
	public void setSocket(MulticastSocket socket) {
		this.socket = socket;
	}

	/**
	 * Getter for this data member
	 */
	public boolean isQuitFlag() {
		return quitFlag;
	}

	/**
	 * Setter for this data member
	 */
	public void setQuitFlag(boolean quitFlag) {
		this.quitFlag = quitFlag;
	}

	/**
	 * Getter for this data member
	 */
	public InetAddress getAddr() {
		return addr;
	}

	/**
	 * Setter for this data member
	 */
	public void setAddr(InetAddress addr) throws Exception {
		this.addr = addr;
	}

	/**
	 * Getter for this data member
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Setter for this data member
	 */
	public void setPort(int port) throws Exception {
		this.port = port;
	}

	/**
	 * Getter for this data member
	 */
	public byte[] getBuf() {
		return buf;
	}

	/**
	 * Setter for this data member
	 */
	public void setBuf(byte[] buf) {
		this.buf = buf;
	}

	/**
	 * Getter for this data member
	 */
	public Peer getUser() {
		return user;
	}

	/**
	 * Setter for this data member
	 */
	public void setUser(Peer user) {
		this.user = user;
	}

}
