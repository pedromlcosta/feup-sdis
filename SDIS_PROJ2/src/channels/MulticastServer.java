package channels;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidParameterSpecException;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import chunk.Chunk;
import messages.Message;
import service.Peer;
import service.Processor;

public class MulticastServer extends Thread {

	private static final int HEADER_SIZE = 512;
	private static final String CIPHER_TYPE = "AES";

	private int dataSize = Chunk.getChunkSize() * 2;
	private MulticastSocket socket = null;
	private boolean quitFlag = false;
	private int serverID;
	private InetAddress addr;
	private int port;
	private byte[] buf = new byte[dataSize + 512];
	protected Peer user;

	/**
	 * Default constructor for the receiver server
	 */
	public MulticastServer() {

	}

	/**
	 * 
	 * @param quitFlag
	 *            flag for the infinite run cycle that receives the messages
	 * @param addr
	 *            Multicast IP address of this receiver
	 * @param port
	 *            Multicast Port of this receiver
	 */
	public MulticastServer(boolean quitFlag, InetAddress addr, int port) {
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
	 * 
	 * @param quitFlag
	 *            flag for the infinite run cycle that receives the messages
	 * @param serverID
	 *            Identifier of the peer this receiver belongs to
	 * @param addr
	 *            Multicast IP address of this receiver
	 * @param port
	 *            Multicast Port of this receiver
	 */
	public MulticastServer(boolean quitFlag, int serverID, InetAddress addr, int port) {
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

	/**
	 * Thread run method. Enters an infinite loop receiving messages. Each
	 * channel subclass will run this method for their own channels. When it
	 * receives a message, it preprocesses it and dispatches it to the Processor
	 * class.
	 */
	@Override
	public void run() {
		System.out.println("Started Running the thread");

		while (!quitFlag) {

			DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);
			// try {
			// socket.receive(receivePacket);
			//
			// } catch (IOException e) {
			// e.printStackTrace();
			// }

			receivePacket = readPacket(receivePacket);

			byte[] receivedMessage = receivePacket.getData();
			byte[] body = null;
			String header = null;

			//System.out.println("Received message: " + new String(receivedMessage));

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
				System.out.println("headerArgs: " + headerArgs);
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
				buf = new byte[dataSize + HEADER_SIZE];
			}
		}
	}

	/**
	 * Initializes server socket with the value that is currently on the port
	 * data member
	 * 
	 * @throws IOException
	 */
	public void createSocket() throws IOException {
		this.socket = new MulticastSocket(port);
	}

	/**
	 * Creates a datagram packet with the info given on the buffer array and
	 * addr/port of this server
	 * 
	 * @param buffer
	 *            bytes to put on the packet
	 * @return DatagramPacket created
	 */
	public DatagramPacket createDatagramPacket(byte[] buffer) {
		return new DatagramPacket(buffer, buffer.length, this.getAddr(), this.getPort());
	}

	/**
	 * Creates a datagram packet with the info given on the buffer array and
	 * addr/port of the arguments
	 * 
	 * @param buffer
	 *            bytes to put on the packet
	 * @param port
	 *            to associate with packet
	 * @param addr
	 *            to associate with packet
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
	 * 
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
	 * 
	 * @param mcastaddr
	 *            address of the multicast group
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
	 * 
	 * @param mcastaddr
	 *            address of the multicast group
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
	 * Joins multicast group associated with the address on the "addr" member
	 * data of this class
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
	 * Leaves multicast group associated with the address on the "addr" member
	 * data of this class
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
	 * @param p
	 *            packet reference, of the packet to send
	 */
	public void writePacket(DatagramPacket p) {
		try {
			/*
			//System.out.println(Base64.getEncoder().encodeToString(user.getEncryptionKey().getEncoded()));
			
			Cipher cipher = Cipher.getInstance(CIPHER_TYPE);
			
			cipher.init(Cipher.ENCRYPT_MODE, user.getEncryptionKey());
			
			System.out.println("Original length: " + p.getLength());
			//System.out.println("Data straight to string: " + new String(p.getData()));
			//String data = Base64.encodeBase64String(p.getData());
			//data = new String(p.getData());
			//System.out.println("Data to string with encode: " + data);
			//byte[] dataUTF = data.getBytes("UTF-8");
			//System.out.println("Before encrypt + encode: " + new String(p.getData()));
			
			
			byte[] encryptedData = cipher.doFinal(p.getData());
			System.out.println("Length after encryption: " + encryptedData.length);
			byte[] encodedData = Base64.getEncoder().encode(encryptedData);
			//System.out.println("After encrypt and before encode: " + new String(encryptedData));
			//System.out.println("After encrypt + encode: " + new String(encodedData));
			
			
			p.setData(encodedData);
			p.setLength(encodedData.length);
			
			System.out.println("Sending packet with length: " + p.getLength() + " and data length: " + encodedData.length);
			*/
			this.socket.send(p);
		} catch (Exception e) {

		} /*  catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException e) {
			e.printStackTrace();
			System.out.println("Error writePacket");
			} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			}   */

	}

	/**
	 * 
	 * @param p
	 *            packet reference of the packet which will receive the
	 *            information
	 */
	public DatagramPacket readPacket(DatagramPacket p) {
		try {

			this.socket.receive(p);
			/*
			Cipher cipher = Cipher.getInstance(CIPHER_TYPE);
			
			//byte[] iv = cipher.getParameters().getParameterSpec(IvParameterSpec.class).getIV();
			
			//IvParameterSpec ivSpec = new IvParameterSpec(iv);
			  
			cipher.init(Cipher.DECRYPT_MODE, user.getEncryptionKey());
			
			
			byte[] data = new byte[p.getLength()];
			System.arraycopy(p.getData(), 0, data, 0, p.getLength());
			
			System.out.println("Received packet with length: " + p.getLength() + " and encodedData with length: " + data.length);
			
			//System.out.println("Before decode and decrypt: " + new String(data));
			byte[] decodedData = Base64.getDecoder().decode(data);
			//System.out.println("After decode and before decrypt: " + new String(decodedData));
			System.out.println("Decoded data has length: " + decodedData.length);
			byte[] decryptedData = cipher.doFinal(decodedData);
			System.out.println("Successfully decrypted.");
			System.out.println("");
			//System.out.println("After decode + decrypt: " + new String(decryptedData));
			
			p.setData(decryptedData);
			*/

			return p;
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error readPacket");
		} /* catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			}  */

		return null;
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
