package protocol;

import java.net.DatagramPacket;
import java.util.ArrayList;

import messages.Message;
import messages.Message.MESSAGE_TYPE;
import chunk.Chunk;
import file.FileID;
import service.Peer;

public class RestoreProtocol extends Thread {

	private static Peer peer = Peer.getInstance();
	private static ArrayList<Chunk> fileChunks = new ArrayList<Chunk>();
	private static ArrayList<FileID> filesToRestore =  new ArrayList<FileID>();
	
	public void run(){
		
	}
	
	public RestoreProtocol(){
	}
	
	public static void startRestore(String filePath) {
		FileID f = peer.getFilesSent().get(filePath);
		filesToRestore.add(f);
		
		//Create and send GETCHUNK messages
		Message msg = new Message();
		for(int i=0; i < f.getnChunks(); i++){
			String[] args= {"1.0", Integer.toString(peer.getServerID()), f.getID(), Integer.toString(i)};
			msg.createMessage(MESSAGE_TYPE.GETCHUNK, args, null);
			
			DatagramPacket packet = peer.getControlChannel().createDatagramPacket(msg.getMessageBytes());
			peer.getControlChannel().writePacket(packet);
		}
		
	}

}
