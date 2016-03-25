package main;

import java.io.File;
import java.io.IOException;

import extra.Extra;

public class Main {
	private static final long INITIAL_WAITING_TIME = 1;

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

		// try {
		//
		// BackupProtocol backup = new BackupProtocol("derp.txt", 4, "1.0",
		// Peer.getInstance());
		// backup.getPeer().setServerID(1);
		//
		// MCReceiver mc = new MCReceiver(false, 1,
		// InetAddress.getByName(args[0]), 4445);
		// MDBReceiver md = new MDBReceiver(false, 1,
		// InetAddress.getByName(args[1]), 4446);
		// new Thread(mc).start();
		// backup.getPeer().setControlChannel(mc);
		// backup.getPeer().setDataChannel(md);
		// //
		// backup.backupFile("C:\\Users\\Filipe\\git\\feup-sdis\\SDIS_PROJ1\\src\\B1.tmp",
		// // 1, 1);
		// System.out.println("END");
		//
		// } catch (UnknownHostException e) {
		// e.printStackTrace();
		// }
		// long waitTime = TimeUnit.SECONDS.toNanos(INITIAL_WAITING_TIME);
		//
		// long elapsedTime;
		// for (int i = 0; i < 3; i++) {
		// long startTime = System.nanoTime();
		// do {
		// } while ((elapsedTime = System.nanoTime() - startTime) < waitTime);
		// System.out.println(TimeUnit.NANOSECONDS.toSeconds(elapsedTime));
		// }

	System.out.println(Extra.createDirectory("backup"));
	}
}
