package main;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

import channels.*;

public class Main {
	public static void main(String args[]) {
		// System.out.println(Extra.SHA256("HI"));
		// FileID f = new FileID("");
		Server teste = new Server();
		try {
			teste.setSocket(new MulticastSocket(4445));
			teste.joinMulticasGroup(InetAddress.getByName("224.0.0.0"));
			byte[] testeA = (new String("OLA")).getBytes();
			DatagramPacket packet = new DatagramPacket(testeA, testeA.length, InetAddress.getByName("224.0.0.0"), 4445);
			System.out.println(packet.toString());
			teste.writePacket(packet);

			teste.readPacket(packet);
			byte[] answer = packet.getData();
			System.out.println(new String(answer));

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
	}
}
