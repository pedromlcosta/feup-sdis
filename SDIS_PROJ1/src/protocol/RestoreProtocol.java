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

	public RestoreProtocol(String fileName) {
		this.fileName = fileName;
	}

	public void run() {

		// Create Peer folder, if not yet existing
		try {
			peer.createPeerFolder();
		} catch (IOException e2) {
			System.out.println("Folder already exists?");
		}

		// Create Restore folder, if not yet existing
		String dirPath = "";
		try {
			dirPath = Extra.createDirectory(Integer.toString(peer.getServerID()) + File.separator + FileHandler.RESTORE_FOLDER_NAME);
			System.out.println(dirPath);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		FileID file = peer.getFilesSent().get(fileName);

		// Check if it was backed up by this peer
		if (file == null) {
			System.out.println("There wasn't a file " + fileName + " backed up by this peer.");
			System.out.println(peer.getFilesSent());
			return;
		}

		String fileID = file.getID();

		// Check if file is already being restored at the moment or not
		// and flag it as being restored if not   -> START EXPECTING CHUNKS FOR THIS fileID
		if (receiverChannel.startRestore(fileID)) {
			System.out.println("This file is already being restored");
			return;
		}

		
		if (!fileHandler.createFile(dirPath + File.separator + fileName)) {
			System.out.println("File with this name already exists, couldn't restore");
			//return;
		}
		Message msg = new GetChunkMsg();

		
		
		// CHUNK MAIN CYCLE:  Send GETCHUNK -> Wait for CHUNK -> Write it to the file
		for (int i = 1; i <= file.getnChunks(); i++) {
			String[] args = { "1.0",  Integer.toString(peer.getServerID()), file.getID(), Integer.toString(i) };

			// Create message
			if (msg.createMessage(null, args) == false) {
				System.out.println("Wasn't able to create getchunk message");
				return;
			}
			DatagramPacket packet = peer.getControlChannel().createDatagramPacket(msg.getMessageBytes());
			// Send message
			peer.getControlChannel().writePacket(packet);

			try {
				// Wait for message and write it
				Chunk chunk = waitForChunk(fileID);
				fileHandler.writeToFile(chunk.getData());
			} catch (InterruptedException e) {
				System.out.println("Thead sleep interrupted.");
				//e.printStackTrace();
			}catch( IOException e){
				System.out.println("File writing exception");
				e.printStackTrace();
			}catch(Exception e){
				System.out.println("Non IO nor Interruption exception.");
				e.printStackTrace();
			}

		}
		
		

		// File Restore over, remove FileID from chunks being received 
		try {
			fileHandler.closeOutputStream(); // ask someone if they know a better option than doing manually?
		} catch (IOException e) {
			e.printStackTrace();
		}
		receiverChannel.finishRestore(fileID);

	}


	public Chunk waitForChunk(String fileID) throws InterruptedException {

		// This get will never return null, we previously filled the hashmap 
		// with a fileID key associated to an empty chunk array!
		
		ArrayList<Chunk> restoreChunks = receiverChannel.getRestoreChunksReceived().get(fileID);
		
		if(restoreChunks == null){
			System.out.println("Well, I be damned..." + fileID);
		}
			
		Chunk chunk = null;

		while (true) {
			
			if (restoreChunks != null && !restoreChunks.isEmpty()) {
				chunk = receiverChannel.getRestoreChunksReceived().get(fileID).remove(0);
				System.out.println("Just got the chunk, with size: " + chunk.getData().length);
				if (chunk != null)
					break; // Finally got a valid chunk
			}
			Thread.sleep(100);
			
		}
		return chunk;
	}

}
