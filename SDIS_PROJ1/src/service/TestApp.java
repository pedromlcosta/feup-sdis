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
				/*
				remoteName = args[0];
				subProtocol = args[1].toLowerCase();
				filePath = args[2];
				desiredRepDeg = Integer.parseInt(args[3]);
				
				Registry registry = LocateRegistry.getRegistry("localhost");
				Invocation stub = (Invocation) registry.lookup(remoteName);

				String response = null;

				switch(subProtocol){
					case "backup":
						response = stub.backup(filePath, desiredRepDeg);
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
				*/
			} catch (Exception e) {
				System.err.println("Client exception: " + e.toString());
				//e.printStackTrace();
				System.out.println("No host with that remoteName exists");
			}
		}else{
			
			System.out.println("Args not valid. Correct usage: java TestApp <peer_ap> <sub_protocol> <opnd_1> <opnd_2>. ");
			System.out.println("<opnd_2> must be empty for the restore, delete and reclaim protocols.");
			return;
		}
			
	}
	
	// Restrictions:
	// java TestApp peer_ap ... -> peer_ap must be a number
	// Options:
	// BACKUP file repDeg
	// RESTORE file
	// DELETE file
	// RECLAIM space
	
	static boolean validArgs(String[] args){
		
		System.out.println("Validating args...");
		
		if(args.length == 0){
			System.out.println("No args found.");
			return false;
		}

		if(args.length < 3){ // Min number of args: 3  Max number or args: 4
			System.out.println("Incorrect number of args.");
		}else{
			// Verify peer_ap first
			if (args[0] == null || !isNumeric(args[0])){
				System.out.println("<peer_ap> must be a numeric value.");
				return false;
			} 
			
			String protocol = args[1].toLowerCase();

			if (protocol == "backup"){
				if(args.length != 4){
					System.out.println("Incorrect number of args.");
					return false;
				}
				//if(args[2].notValidFile) ??
				
				
				if(!isNumeric(args[3])){
					System.out.println("Backup expects <opnd_2> to be a numeric value.");
					return false;
				}else if(Integer.parseInt(args[3])>9 || Integer.parseInt(args[3]) <1){
					System.out.println("Backup expects <opnd_2> to be a value between 1 and 9.");
					return false;
				}
			}else if (protocol == "restore"){
				if(args.length != 3){
					System.out.println("Incorrect number of args.");
					return false;
				}
				
				//if(args[2].notValidFile) ??
			}else if (protocol == "delete"){
				if(args.length != 3){
					System.out.println("Incorrect number of args.");
					return false;
				}
				
				//if(args[2].notValidFile) ??
			}else if (protocol == "reclaim"){
				if(args.length != 3){
					System.out.println("Incorrect number of args.");
					return false;
				}
				
				if(!isNumeric(args[2])){
					System.out.println("Reclaim expects <opnd_1> to be a numeric value.");
					return false;
				}
				
			}else{
				System.out.println("<protocol> must be one of the following: Backup, restore, delete or reclaim.");
			}
		}

		
		
		return true;
	}
	
	public static boolean isNumeric(String str)  
	{  
	  try  
	  {  
	    double i = Integer.parseInt(str);  
	  }  
	  catch(NumberFormatException nfe)  
	  {  
	    return false;  
	  }  
	  return true;  
	}
	
}
