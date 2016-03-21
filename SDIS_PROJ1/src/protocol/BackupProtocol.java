package protocol;

import chunk.Chunk;
import file.FileID;
import messages.Message;
import messages.Message.MESSAGE_TYPE;
import service.Peer;

public class BackupProtocol extends Thread {

	// PUTCHUNK message -> MDB channel
	// STORED message -> MC channel random delay of 0 to 400 ms before sending
	// message
	// A peer must never store the chunks of its own files.

	private Peer peer;
	private int serverID;

	public BackupProtocol(int serverID) {
		this.serverID = serverID;
	}

	public void putchunkCreate(FileID file, byte[] chunkData, int chunkNumber, int wantedRepDegree) {

		int nMessagesSent = 0;
		Chunk chunkToSend = new Chunk(file.getID(), chunkNumber, chunkData);
		chunkToSend.setDesiredRepDegree(wantedRepDegree);
		chunkToSend.setActualRepDegree(0);
		// createMessage
		do {
			// send Message

			// wait for asnwers

		} while (nMessagesSent <= 5 && chunkToSend.getActualRepDegree() != chunkToSend.getDesiredRepDegree());
	}

	public void putchunkReceive(String putchunkMSG) {
		Message msg = new Message();
		String args[] = new String[4];
		String receivedArgs[] = msg.parseMessage(putchunkMSG);

		if (!msg.validateMsg(putchunkMSG, 5) || Integer.parseInt(receivedArgs[1]) == getServerID())
			return;

		// Version
		args[0] = receivedArgs[0];
		// SenderID
		args[1] = Integer.toString(getServerID());
		// FileID
		args[2] = receivedArgs[2];
		// Chunk No
		args[3] = receivedArgs[3];
		byte msgData[] = msg.getMessageData();

		Chunk chunk = new Chunk(args[2], Integer.parseInt(args[2]), msgData);
		if (!peer.getStored().containsKey(chunk.getId())) {
			chunk.setDesiredRepDegree(Integer.parseInt(receivedArgs[4]));
			chunk.setActualRepDegree(1);
			peer.addChunk(chunk.getId(), chunk);
		} else {
			peer.getStored().get(chunk.getId()).increaseRepDegree();
		}

		msg.createMessage(MESSAGE_TYPE.STORED, args, null);

	}

	public Peer getPeer() {
		return peer;
	}

	public void setPeer(Peer peer) {
		this.peer = peer;
	}

	public int getServerID() {
		return serverID;
	}

	public void setServerID(int serverID) {
		this.serverID = serverID;
	}

}
