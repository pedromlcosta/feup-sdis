package protocol;

import java.net.DatagramPacket;

import channels.MCReceiver;
import messages.Message;
import messages.Message.MESSAGE_TYPE;
import service.Peer;
import file.FileID;

public class DeleteProtocol {
	
	private static Peer peer = Peer.getInstance();
	private static FileID file;
	private static int MAX_SENT = 5;
	
	public void startDelete(String filePath){
		
		file = peer.getFilesSent().get(filePath);
		if(file == null){
			System.out.println(filePath + " not found");
			return;
		}
		
		//create message
		Message msg = new Message();
		int nMessagesSent = 0;
		
		msg.createHeader(MESSAGE_TYPE.DELETE, "1.0",Integer.toString(peer.getServerID()),file.getID());
		msg.addEOL();
		msg.addEOL();

		
		MCReceiver mc = peer.getControlChannel();
		DatagramPacket msgPacket = mc.createDatagramPacket(msg.getMessageBytes());
		while(nMessagesSent < MAX_SENT){
			mc.writePacket(msgPacket);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
