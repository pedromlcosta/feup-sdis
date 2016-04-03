INSTRUCTIONS

COMPILING THE APP

	To compile the app, you may do it in one of two ways: 1. automatically with eclipse, or 2. manually through the command line in Windows (Linux should be simillar).

	1. To compile with eclipse, simply create a Java project and dump the contents of our app's source folder into the source folder of the project that was just created. So, inside the new project's source folder you should have the following packages: channels, chunk, extra, file, main, messages, protocol, service. 

	Eclipse, by default, automatically builds the project. If this option isn't active, you can press Ctrl+B and it will build it. You can check that the project was built by assessing the presence of the bin folder, inside the project's workspace.


	2. Open a command line where you have the source folder (not inside the source folder).
		Then execute the following commands:

		1. mkdir bin
		2. javac -d bin -cp src src/service/Peer.java
		2. javac -d bin -cp src src/service/TestApp.java


RUNNING THE APP

	Our TestApp uses RMI to contact with the peers, so the first step is to start an RMI registry.
	The next steps are to run the TestApp and any necessary peers. Remembers, peers should have different IDs from the ones already running! The instructions are as follows:

	1. Go to the folder where the compiled packages are (bin, if you followed the directions given), open the command line and invoke "start rmiregistry". A window with a new rmiregistry will open. This is essential for both the TestApp and Peer to work, so don't close it.

	2. To run a peer, while still inside the compilation folder, on a command line window run the following command: java service.Peer <peer_ap> <MC_addr> <MC_port> <MDB_addr> <MDB_port> <MDR_addr> <MDR_port>
	Pay attention so that you don't use reserved addresses (example: 224.0.0.0 or 224.0.0.1) and also use only ports that aren't being used already. In our case, the peer_ap will also be the remote name of the RMI, and needs to be a number. The peer_ap must also be different from the id of any other peers already running!

	3. To run the TestApp, while still inside the compilation folder, on a command line window, run the following command: java service.TestApp <peer_ap> <sub_protocol> <opnd_1> <opnd_2>.
	The <peer_ap> argument must be the ID of a peer that is already running. The <sub_protocol> identifies the protocol, either "Backup", "Restore", "Delete" or "Reclaim".


	USAGE:
	
		BACKUP   uses <opnd_1> as the file name (may include the relative path of the file) to backup and       <opnd_2> as the desired rep. degree for that file.
		
		RESTORE   uses only <opnd_1>, as the name/relative path of the file to backup. Our implementation requires for this <opnd_1> of the restore to be the same <opnd_1> used when the file was backed up, so that files with the same name, but on different paths can be successfully backed up and restored without interference.

		DELETE   uses only <opnd_1> as the name/relative path of the file which it wants the chunks to be deleted on other peers.

		RECLAIM   uses only <opnd_1> as the ammount of space to reclaim.
		
		
		Example: if to backup, you do...     java service.Peer BACKUP folder1/folder2/image.jpg 1
				 then to restore, you do...  java service.Peer RESTORE folder1/folder2/image.jpg

	