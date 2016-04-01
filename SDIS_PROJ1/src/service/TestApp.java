package service;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import extra.Extra;

public class TestApp {

	public static void main(String[] args) throws IOException {

		String remoteName;
		String subProtocol;
		String filePath;

		// Check if arguments are valid
		boolean valid = validArgs(args);

		if (valid) {
			try {
				remoteName = args[0];
				subProtocol = args[1].toLowerCase();
				filePath = args[2];


				Registry registry = LocateRegistry.getRegistry("localhost");
				Invocation stub = (Invocation) registry.lookup(remoteName);

				String response = null;

				switch (subProtocol) {
				case "backup":

					response = stub.backup(filePath, Integer.parseInt(args[3]));
					break;
				case "restore":
					response = stub.restore(filePath);
					break;
				case "delete":
					response = stub.delete(filePath);
					break;
				case "reclaim":
					response = stub.reclaim(Integer.parseInt(args[2]));
					break;
				}

				System.out.println("response: " + response);

			} catch (Exception e) {
				System.err.println("Client exception: " + e.toString());
				// e.printStackTrace();
				System.out.println("No host with that remoteName exists");
			}
		} else {

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

	static boolean validArgs(String[] args) {

		System.out.println("Validating args...");

		if (args.length == 0) {
			System.out.println("No args found.");
			return false;
		}

		if (args.length < 2) { // Min number of args: 3 Max number or args: 4
			System.out.println("Incorrect number of args.");
			System.out.println("Correct usage: java TestApp <peer_ap> <sub_protocol> <opnd_1> <opnd_2>. ");
			return false;
		} else {
			// Verify peer_ap first
			if (args[0] == null || !Extra.isNumeric(args[0])) {
				System.out.println("<peer_ap> must be a numeric value.");
				System.out.println("Correct usage: java TestApp <peer_ap> <sub_protocol> <opnd_1> <opnd_2>. ");
				return false;
			}

			String protocol = args[1].toLowerCase();

			if (protocol.equals("backup")) {
				if (args.length != 4) {
					System.out.println("Incorrect number of args.");
					System.out.println("Correct usage: java TestApp <peer_ap> BACKUP <filePath> <repDegree>. ");
					return false;
				}
				// if(args[2].notValidFile) ??

				if (!Extra.isNumeric(args[3])) {
					System.out.println("Backup expects <opnd_2> to be a numeric value.");
					System.out.println("Correct usage: java TestApp <peer_ap> BACKUP <filePath> <repDegree>. ");
					return false;
				} else if (Integer.parseInt(args[3]) > 9 || Integer.parseInt(args[3]) < 1) {
					System.out.println("Backup expects <opnd_2> to be a value between 1 and 9.");
					System.out.println("Correct usage: java TestApp <peer_ap> BACKUP <filePath> <repDegree>. ");
					return false;
				}
			} else if (protocol.equals("restore")) {
				if (args.length != 3) {
					System.out.println("Incorrect number of args.");
					System.out.println("Correct usage: java TestApp <peer_ap> RESTORE <filePath>. ");
					return false;
				}

				// if(args[2].notValidFile) ??
			} else if (protocol.equals("delete")) {
				if (args.length != 3) {
					System.out.println("Incorrect number of args.");
					System.out.println("Correct usage: java TestApp <peer_ap> DELETE <filePath>. ");
					return false;
				}

				// if(args[2].notValidFile) ??
			} else if (protocol.equals("reclaim")) {
				if (args.length != 3) {
					System.out.println("Incorrect number of args.");
					System.out.println("Correct usage: java TestApp <peer_ap> RECLAIM <space>. ");
					return false;
				}

				if (!Extra.isNumeric(args[2])) {
					System.out.println("Reclaim expects <opnd_1> to be a numeric value.");
					System.out.println("Correct usage: java TestApp <peer_ap> RECLAIM <space>. ");
					return false;
				}

			} else {
				System.out.println("<protocol> must be one of the following: Backup, restore, delete or reclaim.");
			}
		}

		return true;
	}

}
