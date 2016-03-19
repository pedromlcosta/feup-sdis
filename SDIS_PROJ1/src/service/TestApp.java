package service;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class TestApp {

	public static void main(String[] args) throws IOException {

		// Check if arguments are valid
		boolean valid = validArgs(args);
		
		try {
			String hostName = args[0];
			String remoteName = args[1];

			Registry registry = LocateRegistry.getRegistry(hostName);
			Invocation stub = (Invocation) registry.lookup(remoteName);

			String protocol = args[2].toLowerCase();
			String response = null;

			if(protocol.equals("testX")){
				response = stub.testX(args[3]);
			}else if(protocol.equals("testY")){
				response = stub.testY(args[3]);
			}


			System.out.println("response: " + response);

		} catch (Exception e) {
			System.err.println("Client exception: " + e.toString());
			e.printStackTrace();
		}

			
	}
	
	static boolean validArgs(String[] args){
		
		return true;
	}
	
}
