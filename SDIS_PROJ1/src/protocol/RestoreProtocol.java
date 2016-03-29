package protocol;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.HashMap;

import messages.FileHandler;
import messages.GetChunkMsg;
import messages.Message;
import channels.MDRReceiver;
import chunk.Chunk;
import chunk.ChunkID;
import extra.Extra;
import file.FileID;
import service.Peer;

public class RestoreProtocol extends Thread {

	private static Peer peer = Peer.getInstance();
	private MDRReceiver receiverChannel = peer.getRestoreChannel();
	private FileHandler fileHandler = new FileHandler();
	private String fileName;
	private static ArrayList<Chunk> fileChunks = new ArrayList<Chunk>();
	private static volatile HashMap<ChunkID, Boolean> receivedChunks;

	public RestoreProtocol(String fileName) {
		this.fileName = fileName;
	}

	public void run() {


		try {
			peer.createPeerFolder();
		} catch (IOException e2) {
			System.out.println("Folder already exists?");
		}
		
		String dirPath = "";
		try {
			dirPath = Extra.createDirectory(Integer.toString(peer.getServerID()) + File.separator + FileHandler.RESTORE_FOLDER_NAME);
			System.out.println(dirPath);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		FileID file = peer.getFilesSent().get(fileName);
		
		if (file == null) {
			System.out.println("There wasn't a file " + fileName + " backed up by this peer.");
			System.out.println(peer.getFilesSent());
			return;
		}
		
		String fileID = file.getID();

		// Check if file is already being restored at the moment or not
		// and flag it as being restored if not
		if (receiverChannel.startRestore(fileID)) {
			System.out.println("This file is already being restored");
			return;
		}

		// Send GETCHUNK -> Wait for CHUNK -> Write it to the file
		if (!fileHandler.createFile(dirPath + File.separator + fileName)) {
			System.out.println("File with this name already exists, couldn't restore");
			//return;
		}
		Message msg = new GetChunkMsg();

		for (int i = 1; i <= file.getnChunks(); i++) {
			String[] args = { "1.0",  Integer.toString(peer.getServerID()), file.getID(), Integer.toString(i) };

			if (msg.createMessage(null, args) == false) {
				System.out.println("Wasn't able to create getchunk message");
				return;
			}
			DatagramPacket packet = peer.getControlChannel().createDatagramPacket(msg.getMessageBytes());
			peer.getControlChannel().writePacket(packet);

			try {
				Chunk chunk = waitForChunk(fileName);
				fileHandler.writeToFile(chunk.getData());
			} catch (InterruptedException | IOException e) {
				System.out.println("Thead sleep interrupted? Anyway, waitforchunk threw exception");
				//e.printStackTrace();
			}catch(Exception e){
				System.out.println("waitForchunk exception??");
				e.printStackTrace();
			}

		}

		// File Restore over, remove FileID from chunks being received
		// TODO put function on MDR~
		receiverChannel.finishRestore(fileID);

	}
	

	public Chunk waitForChunk(String fileID) throws InterruptedException, Exception {


		HashMap<String, ArrayList<Chunk> > restoreChunks = receiverChannel.getRestoreChunksReceived();
		
		restoreChunks.get(fileID);
		
		Chunk chunk = null;

		
		
		boolean derp = true;
		while (derp) {
			
			if (restoreChunks.get(fileID)!= null && !restoreChunks.get(fileID).isEmpty()) {
				chunk = restoreChunks.get(fileID).remove(0);
				System.out.println("Entered here, there was something!");
				if (chunk != null)
					break; // Finally got a valid chunk
			}
			Thread.sleep(100);
			
		}
		
		System.out.println("got out");
		
		return chunk;
	}

}
