package messages;

public class ChunkMsg extends Message {

	private static final int N_ARGS = 4;
	private String ChunkNo;

	public ChunkMsg() {
		type = MESSAGE_TYPE.CHUNK;
	}

	public ChunkMsg(String[] messageFields) {
		// message args+type+body
		if (messageFields.length < (N_ARGS + 2)) {
			System.out.println("Failed creating Stored message. Not enough fields");
			return;
		}
		version = messageFields[1];
		senderID = messageFields[2];
		fileId = messageFields[3];
		chunkNo = Integer.parseInt(messageFields[4]);
		body = messageFields[5].getBytes();
	}

	// CHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF><Body>
	public boolean createMessage(byte[] data, String... args) {

		createHeader(args, N_ARGS, getChunk());
		validateRegex = VALIDATE_MESSAGE_TYPE + MORE_THAN_1_SPACE + VALIDATE_VERSION + MORE_THAN_1_SPACE + MIDDLE_ARGS + MORE_THAN_1_SPACE + MIDDLE_ARGS + MORE_THAN_1_SPACE + CHUNK_NUMBER + MSG_END;
		return createMessageAux(data);

	}
}
