package main;

import channels.Peer;
import file.FileID;

public class Main {
	public static void main(String args[]) {
		// System.out.println(Extra.SHA256("HI"));
		// FileID f = new FileID("");
		Peer p = new Peer();
		p.start();
	}
}
