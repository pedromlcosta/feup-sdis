package service;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class TestApp {

	public static void main(String[] args) throws IOException {

		String remoteName;
		String subProtocol;
		String filePath;
		int reclaimSpace;
		int desiredRepDeg;
		
		// Check if arguments are valid
		boolean valid = validArgs(args);
		
		if (valid){
			try {
				remoteName = args[0];
				subProtocol = args[1].toLowerCase();
				filePath = args[2];
				desiredRepDeg = Integer.parseInt(args[3]);
				
				Registry registry = LocateRegistry.getRegistry("localhost");
				Invocation stub = (Invocation) registry.lookup(remoteName);

				String response = null;

				switch(subProtocol){
					case "backup":
						System.out.println("Here");
						response = stub.backup(filePath, desiredRepDeg);
						System.out.println("there");
						break;
					case "restore":
						response = stub.restore(filePath);
						break;
					case "delete":
						response = stub.delete(filePath);
						break;
					case "reclaim":
						reclaimSpace = Integer.parseInt(args[2]);
						response = stub.reclaim(reclaimSpace);
						break;
				}
				
				System.out.println("response: " + response);

			} catch (Exception e) {
				System.err.println("Client exception: " + e.toString());
				//e.printStackTrace();
				System.out.println("No host with that remoteName exists");
			}
		}else{
			
			System.out.println("Args not valid");
			
			return;
		}
			
	}
	
	// Restrictions:
	// java TestApp peer_ap ... -> peer_ap must be a number
	// Options:
	// BACKUP file repDeg
	// RESTORE file
	// DELETE FILE
	//
	
	static boolean validArgs(String[] args){
		
		System.out.println("Validating args...");
		
		return true;
	}
	
}
