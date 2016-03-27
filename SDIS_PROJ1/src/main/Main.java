package main;

import java.io.IOException;
import java.util.Random;

import file.FileID;
import messages.FileHandler;
import messages.Message;
import messages.PutChunkMsg;

public class Main {

	public static void main(String arg[]) throws IOException, ClassNotFoundException {

		byte[] b = new byte[20];
		new Random().nextBytes(b);
		byte toFind = "a".getBytes()[0];
		b[5] = toFind;
		String teste = new String(b);
		// System.out.println(teste);
		// System.out.println(teste.indexOf(toFind));
		String fileName = "C:\\Users\\Filipe\\git\\feup-sdis\\SDIS_PROJ1\\src\\B1.tmp";
		FileID fileID = new FileID(fileName);

		FileHandler split = new FileHandler();
		split.changeFileToSplit(fileName);
		fileID.setDesiredRepDegree(5);
		byte[] chunk;
		int currentPos = 0;
		int chunkNumber = 0;
		try {
			do {
				// Get chunk
				chunk = split.splitFile();
				currentPos = chunk.length;
				if (currentPos <= 0)
					break;
				// update Chunk Number
				chunkNumber++;
				// Send putChunk msg
				System.out.println(currentPos + " " + chunkNumber);
				Message msg = new PutChunkMsg();
				String[] args = new String[5];
				args[0] = "1.0";
				args[1] = "ServeID";
				args[2] = fileID.getID();
				args[3] = Integer.toString(chunkNumber);
				args[4] = Integer.toString(5);
				msg.createMessage(chunk, args);
				// System.out.println(msg.getMessageToSend().indexOf(Message.EOL
				// + Message.EOL));
				System.out.println("Header: " + msg.getMessageToSend());
				byte[] receivedMessage = msg.getMessageToSend().getBytes();
				byte[] body = chunk;
				byte[] message = new byte[receivedMessage.length + body.length];
				System.in.read();
				/*for (int i = 0; i < receivedMessage.length; i++) {
					if (receivedMessage[i] == '\r' && receivedMessage[i + 1] == '\n')
						if (receivedMessage[i + 2] == '\r' && receivedMessage[i + 3] == '\n') {
							Arrays.copyOf(receivedMessage, i);
							// header = new
							// String(Arrays.copyOf(receivedMessage, i));
							// System.out.println(i + " " + header.charAt(i -
							// 1));
							/// System.out.println(i + 4 + " " +
							/// Byte.toString(body[0]));
							// System.out.println(Byte.toString(receivedMessage[i
							/// + 4]));
							break;
						}
				}*/
				// TODO why is this fucking the console?? 
				System.arraycopy(receivedMessage, 0, message, 0, receivedMessage.length);
				System.arraycopy(body, 0, message, receivedMessage.length, body.length);
				System.out.print("MESSAGE: " + new String(message));

			} while (currentPos > 0);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
