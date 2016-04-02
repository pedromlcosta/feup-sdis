package protocol;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import channels.MDRReceiver;
import chunk.Chunk;
import extra.Extra;
import file.FileID;
import messages.FileHandler;
import messages.GetChunkMsg;
import messages.Message;
import service.Peer;

public class RestoreProtocol extends Thread {

	private final int RESTORE_TIMEOUT = 4000;
	private static Peer peer = Peer.getInstance();
	private MDRReceiver receiverChannel = peer.getRestoreChannel();
	private FileHandler fileHandler = new FileHandler();
	private String filePath;

	public RestoreProtocol(String filePath) {
		this.filePath = filePath;
	}

	public void run() {

		// Create Peer folder, if not yet existing
		try {
			peer.createPeerFolder();
		} catch (IOException e2) {
			System.out.println("Folder already exists?");
		}
		System.out.println("got here");
		// Create Restore folder, if not yet existing
		String dirPath = "";
		try {
			dirPath = Extra
					.createDirectory(Integer.toString(peer.getServerID())
							+ File.separator + FileHandler.RESTORE_FOLDER_NAME);
			System.out.println(dirPath);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// TODO get first FIle or last???
		ArrayList<FileID> fileSentVersions = peer.getFilesSent().get(filePath);
		if (fileSentVersions == null) {
			System.out.println("File has not yet been backedup");
			return;
		}
		FileID file = fileSentVersions.get(fileSentVersions.size() - 1);

		System.out.println("got here2");

		// Check if it was backed up by this peer
		if (file == null) {
			System.out.println("There wasn't a file " + filePath
					+ " backed up by this peer.");
			System.out.println(peer.getFilesSent());
			return;
		}

		String fileID = file.getID();
		String originalName = file.getFileName();

		// Check if file is already being restored at the moment or not
		// and flag it as being restored if not -> START EXPECTING CHUNKS FOR
		// THIS fileID
		if (receiverChannel.startRestore(fileID)) {
			System.out.println("This file is already being restored");
			return;
		}

		// createFile returns whether there was already a file with the name or
		// not
		
		
		
		
		String totalPath = "";
		String filePathFxd = filePath.replace("/", File.separator);
		
		System.out.println("filePath after replace is: " +  filePathFxd);
		
		int index = filePathFxd.lastIndexOf(File.separator);

		if(index == -1){
			totalPath = originalName;
		}else{
			System.out.println(index);
			totalPath = filePathFxd.
			    substring(0,index) + "-" + originalName;
		}
		
		System.out.println(totalPath);
		
		totalPath.replace("../", "");
		totalPath.replace("//", "");
		totalPath.replace("/", "_");
		
		System.out.println(totalPath);
		
		System.out.println("Creating file: " + dirPath + File.separator + totalPath );
		
		if(!fileHandler.createFile(dirPath + File.separator + totalPath)){
			System.out.println("File with this name already existed. Restoring over previous version.");
		}

		Message msg = new GetChunkMsg();

		// CHUNK MAIN CYCLE: Send GETCHUNK -> Wait for CHUNK -> Write it to the
		// file
		for (int i = 1; i <= file.getnChunks(); i++) {
			String[] args = { "1.0", Integer.toString(peer.getServerID()),
					file.getID(), Integer.toString(i) };

			// Create message
			if (msg.createMessage(null, args) == false) {
				System.out.println("Wasn't able to create getchunk message");
				return;
			}
			DatagramPacket packet = peer.getControlChannel()
					.createDatagramPacket(msg.getMessageBytes());
			// Send message
			peer.getControlChannel().writePacket(packet);

			try {
				// Wait for message and write it
				Chunk chunk = waitForChunk(fileID, i);

				if (chunk == null) {
					System.out.println("Timeout: couldn't obtain Chunk nr. "
							+ i + " after 4 seconds, restore failed");
					
					receiverChannel.finishRestore(fileID);
					
					return;
				}

				if (i != file.getnChunks())
					fileHandler.writeToFile(chunk.getData());
				// if it is the
				// last chunk,
				// write only
				// the part
				// needed?
				else {
					System.out.println("File Size: " + file.getFileSize()
							+ " and this chunk:" + (int) file.getFileSize()
							% 64000);
					fileHandler.writeToFile(chunk.getData(),
							(int) file.getFileSize() % 64000);
				}
			} catch (InterruptedException e) {
				System.out.println("Thead sleep interrupted.");
				// e.printStackTrace();
			} catch (IOException e) {
				System.out.println("File writing exception");
				e.printStackTrace();
			} catch (Exception e) {
				System.out
						.println("Non IO nor Interruption exception HAHAHAHHAEHHEHEHEHUHEUHEUHUEwesuck.");
				e.printStackTrace();
			}

		}

		// File Restore over, remove FileID from chunks being received
		try {
			fileHandler.closeOutputStream(); // ask someone if they know a
												// better option than doing
												// manually?
		} catch (IOException e) {
			e.printStackTrace();
		}
		receiverChannel.finishRestore(fileID);
	}

	public synchronized Chunk waitForChunk(String fileID, int expectedChunkNr)
			throws InterruptedException {

		// This get will never return null, we previously filled the hashmap
		// with a fileID key associated to an empty chunk array!

		ArrayList<Chunk> restoreChunks = receiverChannel
				.getRestoreChunksReceived().get(fileID);

		if (restoreChunks == null) {
			System.out.println("Well, I be damned..." + fileID);
		}

		Chunk chunk = null;

		long startTime = System.currentTimeMillis();

		while (true) {

			if (restoreChunks != null && !restoreChunks.isEmpty()) {
				chunk = receiverChannel.getRestoreChunksReceived().get(fileID)
						.remove(0);
				System.out.println("Wait for chunk obtained chunk with nr: "
						+ chunk.getId().getChunkNumber());

				if (chunk != null)
					if (chunk.getId().getChunkNumber() == expectedChunkNr)
						break; // Finally got a valid and expected chunk, else
								// would be discarded
			}

			// If timeout, returns null, else, wait 50 more milliseconds
			if ((System.currentTimeMillis() - startTime) > RESTORE_TIMEOUT)
				return null;
			else
				Thread.sleep(50);

		}
		return chunk;
	}

}
