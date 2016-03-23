package protocol;

import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.HashMap;

import messages.Message;
import messages.Message.MESSAGE_TYPE;
import channels.MDRReceiver;
import chunk.Chunk;
import chunk.ChunkID;
import file.FileID;
import service.Peer;

public class RestoreProtocol extends Thread {

	private static Peer peer = Peer.getInstance();
	private MDRReceiver receiverChannel = peer.getRestoreChannel();
	private String filePath;
	private static ArrayList<Chunk> fileChunks = new ArrayList<Chunk>();
	private static volatile HashMap<ChunkID, Boolean> receivedChunks;
	
	
	public RestoreProtocol(String filePath){
		this.filePath = filePath;
	}
	
	public void run() {
		
		FileID file = peer.getFilesSent().get(filePath);
		
		if(file == null){
			System.out.println("There wasn't a file" + filePath + " backed up by this peer.");
			return;
		}
		
		//Create and send GETCHUNK messages
		Message msg = new Message();
		for(int i=0; i < file.getnChunks(); i++){
			String[] args= {"1.0", Integer.toString(peer.getServerID()), file.getID(), Integer.toString(i)};
			
			if(msg.createMessage(MESSAGE_TYPE.GETCHUNK, args, null) == false){
				System.out.println("Wasn't able to create getchunk message");
				return;
			}
			DatagramPacket packet = peer.getControlChannel().createDatagramPacket(msg.getMessageBytes());
			peer.getControlChannel().writePacket(packet);
			
			try {
				Chunk chunk = waitForChunk(file);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			// Do stuff with the chunk
		}
		
	}
	
	public Chunk waitForChunk(FileID file) throws InterruptedException{
		
		ArrayList<Chunk> fileChunks = receiverChannel.getChunksBeingReceived().get(file);
		Chunk chunk = null;		
		
		while(true){
			
			if(!fileChunks.isEmpty()){
				chunk = fileChunks.remove(0);
				if(chunk != null)
					break;    // Finally got a valid chunk
			}

			Thread.sleep(100);
		}
		
		return chunk;
		
	}

}
