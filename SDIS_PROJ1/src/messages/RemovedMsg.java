package messages;

public class RemovedMsg extends Message {
	String ChunkNo;
	private static final int N_ARGS = 4;

	public RemovedMsg() {
		type = MESSAGE_TYPE.REMOVED;
	}

	public RemovedMsg(String[] messageFields) {
		// type+args
		if (messageFields.length < (N_ARGS + 1)) {
			System.out.println("Failed creating Remove message. Not enough fields");
			return;
		}
		version = messageFields[1];
		senderID = messageFields[2];
		fileId = messageFields[3];
		chunkNo = Integer.parseInt(messageFields[4]);
	}

	// REMOVED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
	public boolean createMessage(byte[] data, String... args) {

		createHeader(args, N_ARGS, getRemoved());

		validateRegex = VALIDATE_MESSAGE_TYPE + MORE_THAN_1_SPACE + VALIDATE_VERSION + MORE_THAN_1_SPACE + MIDDLE_ARGS + MORE_THAN_1_SPACE + MIDDLE_ARGS + MORE_THAN_1_SPACE + CHUNK_NUMBER + MSG_END;

		return createMessageAux(data);
	}
}
