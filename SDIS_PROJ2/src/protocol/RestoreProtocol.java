package protocol;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.ArrayList;

import channels.MDRReceiver;
import chunk.Chunk;
import data.FileID;
import extra.Extra;
import extra.FileHandler;
import messages.GetChunkMsg;
import messages.Message;

public class RestoreProtocol extends Protocol {

	private final int RESTORE_TIMEOUT = 4000;
	private MDRReceiver receiverChannel = peer.getRestoreChannel();
	private FileHandler fileHandler = new FileHandler();

	/**
	 * Restore Protocol thread constructor.
	 * 
	 * @param filePath
	 *            relative path of file to restore
	 */
	public RestoreProtocol(String filePath) {
		this.filePath = filePath;
	}

	/**
	 * Run method associated with the thread that runs this protocol It will
	 * restore the file represented by filePath chunk by chunk It times out if
	 * it doesn't receive a chunk for 4 seconds
	 */
	public void run() {

		// Create Peer folder, if not yet existing
		try {
			peer.createPeerFolder();
		} catch (IOException e2) {

		}

		// Create Restore folder, if not yet existing
		String dirPath = "";
		try {
			dirPath = Extra.createDirectory(Integer.toString(peer.getServerID()) + File.separator + FileHandler.RESTORE_FOLDER_NAME);

		} catch (IOException e1) {
			e1.printStackTrace();
		}

		ArrayList<FileID> fileSentVersions = peer.getFilesSent().get(filePath);
		if (fileSentVersions == null) {
			System.out.println("File has not yet been backed up");
			return;
		}
		FileID file = fileSentVersions.get(fileSentVersions.size() - 1);

		// Check if it was backed up by this peer
		if (file == null) {
			System.out.println("There wasn't a file " + filePath + " backed up by this peer.");
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

		String totalPath = "";
		String filePathFxd = filePath.replace("/", File.separator);

		int index = filePathFxd.lastIndexOf(File.separator);

		if (index == -1) {
			totalPath = originalName;
		} else {
			totalPath = filePathFxd.substring(0, index) + "-" + originalName;
		}

		totalPath.replace("../", "");
		totalPath.replace("//", "");
		totalPath.replace("/", "_");

		System.out.println("Creating file: " + dirPath + File.separator + totalPath);

		if (!fileHandler.createFile(dirPath + File.separator + totalPath)) {
			System.out.println("File with this name already existed. Restoring over previous version.");
		}

		Message msg = new GetChunkMsg();

		// CHUNK MAIN CYCLE: Send GETCHUNK -> Wait for CHUNK -> Write it to the
		// file
		for (int i = 1; i <= file.getnChunks(); i++) {
			String[] args = { "1.0", Integer.toString(peer.getServerID()), file.getID(), Integer.toString(i) };

			msg = new GetChunkMsg();
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
				Chunk chunk = waitForChunk(fileID, i);

				if (chunk == null || chunk.getData() == null || chunk.getData().length == 0) {
					System.out.println("Timeout: couldn't obtain Chunk nr. " + i + " after 4 seconds, restore failed");

					receiverChannel.finishRestore(fileID);
					return;
				}

				if (i != file.getnChunks())
					fileHandler.writeToFile(chunk.getData());
				// else, last chunk!
				else {
					fileHandler.writeToFile(chunk.getData(), (int) file.getFileSize() % 64000);
				}
			} catch (InterruptedException e) {
				System.out.println("Thead sleep interrupted.");
				// e.printStackTrace();
			} catch (IOException e) {
				System.out.println("File writing exception");
				e.printStackTrace();
			} catch (Exception e) {
				System.out.println("Couldn't write chunk to file.");
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

	/**
	 * Waits 4 seconds for a certain chunk of a certain file
	 * 
	 * @param fileID
	 *            identifier of the file the chunk we expect belongs to
	 * @param expectedChunkNr
	 *            number of the chunk we expect
	 * @return the Chunk that was awaited
	 * @throws InterruptedException
	 */
	public synchronized Chunk waitForChunk(String fileID, int expectedChunkNr) throws InterruptedException {

		// This get will never return null, we previously filled the hashmap
		// with a fileID key associated to an empty chunk array!

		ArrayList<Chunk> restoreChunks = receiverChannel.getRestoreChunksReceived().get(fileID);

		if (restoreChunks == null) {
		}

		Chunk chunk = null;

		long startTime = System.currentTimeMillis();

		while (true) {

			if (restoreChunks != null && !restoreChunks.isEmpty()) {
				chunk = receiverChannel.getRestoreChunksReceived().get(fileID).remove(0);
				System.out.println("Wait for chunk with nr: " + chunk.getId().getChunkNumber());

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
