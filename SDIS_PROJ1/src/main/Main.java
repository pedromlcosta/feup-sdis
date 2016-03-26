package main;

import java.io.IOException;

import chunk.Chunk;
import chunk.ChunkID;
import file.FileID;
import messages.DeleteMsg;
import messages.Message;

public class Main {

	public static void main(String args[]) throws IOException, ClassNotFoundException {

		FileID file = new FileID("C:\\Users\\Filipe\\git\\feup-sdis\\SDIS_PROJ1\\src\\B1.tmp");
		ChunkID chunkID = new ChunkID(file.getID(), 10, 20, 30);
		Chunk chunk = new Chunk(chunkID, new byte[64000]);

		Message msg = new DeleteMsg();
		byte[] b = "olaesou_ojaosa_dsasd_asdas".getBytes();
		System.out.println(new String(b));
		msg.createMessage(b, "1.0", "HORSHIT", file.getID());
		System.out.println(msg.getMessageToSend());
		msg.parseMessage(msg.getMessageToSend());

	}
}
