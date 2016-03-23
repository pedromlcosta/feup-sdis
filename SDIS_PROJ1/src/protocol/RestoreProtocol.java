package protocol;

import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.HashMap;

import messages.Message;
import messages.Message.MESSAGE_TYPE;
import chunk.Chunk;
import chunk.ChunkID;
import file.FileID;
import service.Peer;

public class RestoreProtocol extends Thread {

	private static Peer peer = Peer.getInstance();
	private String filePath;
	private static ArrayList<Chunk> fileChunks = new ArrayList<Chunk>();
	private static ArrayList<FileID> filesToRestore =  new ArrayList<FileID>();
	private static volatile HashMap<ChunkID, Boolean> receivedChunks;
	
	
	public void run(){
		
	}
	
	public RestoreProtocol(String filePath){
		this.filePath = filePath;
	}
	
	public static void startRestore(String filePath) {
		
		FileID f = peer.getFilesSent().get(filePath);
		
		if(f == null){
			System.out.println("There wasn't a file" + filePath + " backed up by this peer.");
			return;
		}
		
		filesToRestore.add(f);
		
		//Create and send GETCHUNK messages
		Message msg = new Message();
		for(int i=0; i < f.getnChunks(); i++){
			String[] args= {"1.0", Integer.toString(peer.getServerID()), f.getID(), Integer.toString(i)};
			
			if(msg.createMessage(MESSAGE_TYPE.GETCHUNK, args, null) == false){
				System.out.println("Wasn't able to create getchunk message");
				return;
			}
			DatagramPacket packet = peer.getControlChannel().createDatagramPacket(msg.getMessageBytes());
			peer.getControlChannel().writePacket(packet);
		}
		
		// Is now awaiting the chunk messages
		
	}

}
