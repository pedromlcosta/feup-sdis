package protocol;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Iterator;

import messages.DeleteMsg;
import messages.FileHandler;
import messages.Message;
import service.Peer;
import channels.MCReceiver;
import chunk.ChunkID;
import extra.Extra;

public class ReclaimProtocol extends Thread {

	private int reclaimSpace;
	private int amountReclaimed;
	private static Peer peer = Peer.getInstance();

	public ReclaimProtocol(int reclaimSpace) {
		this.reclaimSpace = reclaimSpace;
		this.amountReclaimed = 0;
	}
	
	public void run(){
		
		String dirPath = "";

		try {
			dirPath = Extra.createDirectory(FileHandler.BACKUP_FOLDER_NAME);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Iterator<ChunkID> it = Peer.getInstance().getStored().iterator();
		
		while(this.amountReclaimed < this.reclaimSpace && it.hasNext()){
			ChunkID chunk = it.next();
			File file = new File(dirPath + File.separator + chunk.getFileID() + "_" + chunk.getChunkNumber());
			
			// create message
			Message msg = new DeleteMsg();

			String[] args = { "1.0",  peer.getServerID(), chunk.getFileID(), Integer.toString(chunk.getChunkNumber())};
			msg.createMessage(null, args);

			MCReceiver mc = peer.getControlChannel();
			DatagramPacket msgPacket = mc.createDatagramPacket(msg.getMessageBytes());
			mc.writePacket(msgPacket);
			
			file.delete();
			it.remove();
			peer.removeChunkPeers(chunk);
				
			this.amountReclaimed += file.length();
		}
	}
}
