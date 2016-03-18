package main;

import java.lang.reflect.Array;

import messages.Message;
import messages.Message.MESSAGE_TYPE;

public class Main {
	public static void main(String args[]) {
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

		Message testeMsg = new Message();
		// System.out.println(Character.toString(testeMsg.getEOL().toCharArray()[0]));
		// System.out.println("\u0044");
		// testeMsg.parseMessage("PUTCHUNK <Version> <SenderId> <FileId>
		// <ChunkNo> <ReplicationDeg> <CRLF><CRLF>sasdasdas");
		String s[] = new String[4];
		s[0] = "Version";
		s[1] = "SenderId";
		s[2] = "FileId";
		s[3] = "ChunkNo_ReplicationDeg";
		testeMsg.createMessage(MESSAGE_TYPE.PUTCHUNK, s, new byte[5]);
		System.out.println("MSG: " + testeMsg.getMessageToSend());

	}
}
