package protocol;

import java.util.ArrayList;

import chunk.Chunk;
import file.FileID;
import service.Peer;

public class RestoreProtocol {

	private static Peer peer = Peer.getInstance();
	private static ArrayList<Chunk> fileChunks = new ArrayList<Chunk>();
	private static FileID fileToRestore = null;
	
	public RestoreProtocol(){
	}
	
	public static void startRestore(String filePath) {
		fileToRestore = peer.getFilesSent().get(filePath);
		
		//Create GETCHUNK message
		
		//Send GETCHUNK message
		
	}

	
	
}
