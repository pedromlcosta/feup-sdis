package protocol;

import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.Random;

import chunk.Chunk;
import chunk.ChunkID;
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

	// most 5 PUTCHUNK messages per chunk.
	public void putchunkCreate(FileID file, byte[] chunkData, int chunkNumber, int wantedRepDegree, int version) {
		Message msg = new Message();
		int nMessagesSent = 0;
		// Create Chunk
		Chunk chunkToSend = new Chunk(file.getID(), chunkNumber, chunkData);
		chunkToSend.setDesiredRepDegree(wantedRepDegree);
		chunkToSend.setActualRepDegree(0);
		ChunkID chunkToSendID = chunkToSend.getId();

		// createMessage
		String[] args = new String[4];
		args[0] = Integer.toString(version);
		args[1] = Integer.toString(getServerID());
		args[2] = file.getID();
		args[3] = Integer.toString(chunkNumber);
		args[4] = Integer.toString(wantedRepDegree);
		msg.createMessage(MESSAGE_TYPE.PUTCHUNK, args, chunkData);

		// Send Mensage
		DatagramPacket msgPacket = peer.getDataChannel().createDatagramPacket(msg.getMessageBytes());
		DatagramPacket answerPacket = peer.getDataChannel().createDatagramPacket(new byte[0]);

		// check if the pair chunkID,ServersWhoReplied exists
		// TODO when should we clean ArrayList to avoid having "false positives"
		if (!peer.getAnsweredCommand().containsKey(chunkToSendID)) {
			peer.getAnsweredCommand().put(chunkToSendID, new ArrayList<Integer>());
		} else
			peer.getAnsweredCommand().replace(chunkToSendID, new ArrayList<Integer>());
		do {
			
			// send Message
			peer.getDataChannel().writePacket(msgPacket);
			nMessagesSent++;
			// wait for asnwers
			// TODO check caso de mensagem ser roubad por outro thread (do mesmo
			// tipo ou não)

			peer.getControlChannel().readPacket(answerPacket);
			String answer = new String(answerPacket.getData());
			String[] replayArgs = msg.parseMessage(answer);

			if (replayArgs[0].equals(Message.getStored())) {
				// FileID
				String answerFileID = replayArgs[2];
				// ServerID
				int answerServerID = Integer.parseInt(replayArgs[1]);
				// Chunk No
				int answerChunkNumber = Integer.parseInt(replayArgs[3]);

				if (answerFileID.equals(file.getID()) && answerChunkNumber == chunkNumber) {
					if (!peer.getAnsweredCommand().get(chunkToSendID).contains(answerServerID)) {
						chunkToSend.increaseRepDegree();
						peer.getAnsweredCommand().get(chunkToSendID).add(answerServerID);
					}
				}
			}

		} while (nMessagesSent <= 5 && chunkToSend.getActualRepDegree() != chunkToSend.getDesiredRepDegree());
	}

	// 0 and 400 ms. delay for the stored msg
	public void putchunkReceive(String putchunkMSG) {
		Message msg = new Message();
		String args[] = new String[4];
		String receivedArgs[] = msg.parseMessage(putchunkMSG);

		if (!msg.validateMsg(putchunkMSG, 5) || Integer.parseInt(receivedArgs[1]) == getServerID())
			return;

		// Version - same version??
		args[0] = receivedArgs[0];
		// SenderID
		args[1] = Integer.toString(getServerID());
		// FileID
		args[2] = receivedArgs[2];
		// Chunk No
		args[3] = receivedArgs[3];
		byte msgData[] = msg.getMessageData();

		Chunk chunk = new Chunk(args[2], Integer.parseInt(args[2]), msgData);

		// TODO Check if condition makes sense
		if (!peer.getStored().containsKey(chunk.getId())) {
			chunk.setDesiredRepDegree(Integer.parseInt(receivedArgs[4]));
			chunk.setActualRepDegree(1);
			peer.addChunk(chunk.getId(), chunk);
		} else {
			peer.getStored().get(chunk.getId()).increaseRepDegree();
		}

		msg.createMessage(MESSAGE_TYPE.STORED, args, null);
		DatagramPacket packet = peer.getControlChannel().createDatagramPacket(msg.getMessageBytes());
		Random randomDelayGenerator = new Random();
		int delay = randomDelayGenerator.nextInt(401);
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		peer.getControlChannel().writePacket(packet);

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
