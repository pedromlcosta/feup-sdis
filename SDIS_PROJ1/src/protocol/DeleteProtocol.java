package protocol;

import java.net.DatagramPacket;

import channels.MCReceiver;
import messages.Message;
import messages.Message.MESSAGE_TYPE;
import service.Peer;
import extra.Extra;
import file.FileID;

public class DeleteProtocol extends Thread {
	
	private static Peer peer = Peer.getInstance();
	private static FileID file;
	private String filePath;
	private static int MAX_SENT = 5;
	
	public DeleteProtocol(String filePath){
		this.filePath = filePath;
	}
	
	public void run(){
		
		file = peer.getFilesSent().get(filePath);
		if(file == null){
			System.out.println(filePath + " not found");
			return;
		}
		
		//create message
		Message msg = new Message();
		int nMessagesSent = 0;
		
		msg.createHeader(MESSAGE_TYPE.DELETE, "1.0",Integer.toString(peer.getServerID()),Extra.SHA256(file.getID()));
		
		MCReceiver mc = peer.getControlChannel();
		DatagramPacket msgPacket = mc.createDatagramPacket(msg.getMessageBytes());
		while(nMessagesSent < MAX_SENT){
			mc.writePacket(msgPacket);
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
