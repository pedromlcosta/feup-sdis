package messages;

public class StoredMsg extends Message {
	private static final int N_ARGS = 4;
	String ChunkNo;

	public StoredMsg() {
		type = MESSAGE_TYPE.STORED;
	}

	public StoredMsg(String[] messageFields, byte[] data) {
		// type+args
		if (messageFields.length < (N_ARGS)) {
			System.out.println("Failed creating Stored message. Not enough fields");
			return;
		}
		version = messageFields[1];
		senderID = messageFields[2];
		fileId = messageFields[3];
		chunkNo = Integer.parseInt(messageFields[4]);
		body = null;
	}

	// STORED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
	public boolean createMessage(byte[] data, String... args) {

		// Does not have a body
		if (data != null) {
			System.out.println("Mensagem nao valida");
			return false;
		}
		createHeader(args, N_ARGS, getStored());
		validateRegex = VALIDATE_MESSAGE_TYPE + MORE_THAN_1_SPACE + VALIDATE_VERSION + MORE_THAN_1_SPACE + MIDDLE_ARGS + MORE_THAN_1_SPACE + MIDDLE_ARGS + MORE_THAN_1_SPACE + CHUNK_NUMBER
				+ MSG_END_WITHOUT_BODY;
		
		return createMessageAux();

	}
}
