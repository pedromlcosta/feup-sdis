package main;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import channels.MCReceiver;
import channels.MDBReceiver;
import protocol.BackupProtocol;
import service.Peer;

public class Main {
	public static void main(String args[]) throws IOException {
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

		try {
			
			BackupProtocol backup = new BackupProtocol("derp.txt", 4, "1.0");
			backup.setPeer(Peer.getInstance());
			backup.getPeer().setServerID(1);

			MCReceiver mc = new MCReceiver(false, 1, InetAddress.getByName(args[0]), 4445);
			MDBReceiver md = new MDBReceiver(false, 1, InetAddress.getByName(args[1]), 4446);
			new Thread(mc).start();
			backup.getPeer().setControlChannel(mc);
			backup.getPeer().setDataChannel(md);
			//backup.backupFile("C:\\Users\\Filipe\\git\\feup-sdis\\SDIS_PROJ1\\src\\B1.tmp", 1, 1);
			System.out.println("END");
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

	}
}
