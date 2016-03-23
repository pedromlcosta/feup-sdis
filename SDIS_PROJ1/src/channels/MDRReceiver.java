package channels;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;

import chunk.Chunk;
import file.FileID;
import service.Peer;

public class MDRReceiver extends ReceiverServer {
	private Peer user;
	private HashMap<FileID, ArrayList<Chunk> > chunksBeingReceived;

	public MDRReceiver(String[] args) throws NumberFormatException, IOException {
		super();
	}

	public MDRReceiver(boolean quitFlag, int serverID, InetAddress addr, int port) {
		super(quitFlag, serverID, addr, port);
	}

	public HashMap<FileID, ArrayList<Chunk> > getChunksBeingReceived() {
		return chunksBeingReceived;
	}

	public void setChunksBeingReceived(HashMap<FileID, ArrayList<Chunk> > chunksBeingReceived) {
		this.chunksBeingReceived = chunksBeingReceived;
	}
}
