package service;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class TestApp {

	public static void main(String[] args) throws IOException {

		String accessPoint;
		String subProtocol;
		String filePath;
		int reclaimSpace;
		int desiredRepDeg;
		
		// Check if arguments are valid
		boolean valid = validArgs(args);
		
		if (valid){
			try {
				accessPoint = args[0];
				subProtocol = args[1].toLowerCase();
				filePath = args[2];
				desiredRepDeg = Integer.parseInt(args[3]);
				
				Registry registry = LocateRegistry.getRegistry("localhost");
				Invocation stub = (Invocation) registry.lookup("123");

				String response = null;

				if(subProtocol.equals("reclaim")){
					reclaimSpace = Integer.parseInt(args[2]);
					response = stub.backup(args[3]);
				}else if(subProtocol.equals("backup")){
					
					response = stub.backup(args[3]);
				}else if(subProtocol.equals("restore")){
					response = stub.restore(args[3]);
				}


				System.out.println("response: " + response);

			} catch (Exception e) {
				System.err.println("Client exception: " + e.toString());
				e.printStackTrace();
			}
		}else{
			
			System.out.println("Args not valid");
			
			return;
		}
			
	}
	
	static boolean validArgs(String[] args){
		
		System.out.println("Validating args...");
		
		return true;
	}
	
}
