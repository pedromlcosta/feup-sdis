package main;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import chunk.Chunk;
import chunk.ChunkID;
import file.FileID;

public class Main {

	public static void main(String args[]) throws IOException, ClassNotFoundException {
		// System.out.println(Extra.SHA256("HI"));
		// FileID f = new FileID("");
		//
		// Server teste = new Server(); try { teste.setSocket(new
		// MulticastSocket(4445));
		// teste.joinMulticasGroup(InetAddress.getByName("224.0.0.0")); byte[]
		// testeA = (new String("OLA")).getBytes(); DatagramPacket packet = new
		// DatagramPacket(testeA, testeA.length,
		// InetAddress.getByName("224.0.0.0"), 4445);
		// System.out.println(packet.toString()); teste.writePacket(packet);
		//
		// teste.readPacket(packet); byte[] answer = packet.getData();
		// System.out.println(new String(answer));
		//
		// } catch (IOException e) { // TODO Auto-generated catch block
		// e.printStackTrace();
		//
		// }

		// Message testeMsg = new Message();
		// System.out.println(Character.toString(testeMsg.getEOL().toCharArray()[0]));
		// System.out.println("\u0044");
		// testeMsg.parseMessage("PUTCHUNK <Version> <SenderId> <FileId>
		// <ChunkNo> <ReplicationDeg> <CRLF><CRLF>sasdasdas");
		// String s[] = new String[4];
		// s[0] = "Version";
		// s[1] = "SenderId";
		// s[2] = "FileId";
		// s[3] = "ChunkNo_ReplicationDeg";
		// testeMsg.createMessage(MESSAGE_TYPE.PUTCHUNK, s, new byte[5]);
		// System.out.println("MSG: " + testeMsg.getMessageToSend());
		FileID file = new FileID("C:\\Users\\Filipe\\git\\feup-sdis\\SDIS_PROJ1\\src\\B1.tmp");
		System.out.println(file.getID());
		ChunkID chunkID = new ChunkID(file.getID(), 10, 20, 30);
		Chunk chunk = new Chunk(chunkID, new byte[64000]);
		FileOutputStream fileOut = new FileOutputStream("employee.ser");
		ObjectOutputStream out = new ObjectOutputStream(fileOut);
		// FileInputStream fileOut = new FileInputStream("employee.ser");
		// ObjectInputStream out = new ObjectInputStream(fileOut);
		// Chunk Answer = (Chunk) out.readObject();
		out.writeObject(chunk);
		out.close();
		fileOut.close();
		// System.out.println(Answer);
		/*
		 * try {
		 * 
		 * BackupProtocol backup = new BackupProtocol(
		 * "C:\\Users\\Filipe\\git\\feup-sdis\\SDIS_PROJ1\\src\\B1.tmp", 0,
		 * "1.0", Peer.getInstance()); MDBReceiver md = new MDBReceiver(false,
		 * 1, InetAddress.getByName(args[1]), 4446);
		 * backup.getPeer().setServerID(1); new Thread(md).start();
		 * backup.getPeer().setDataChannel(md); backup.start(); //
		 * System.out.println("END");
		 * 
		 * } catch (UnknownHostException e) { e.printStackTrace(); }
		 */
		System.out.println("end");
	}
}
