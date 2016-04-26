package messages;

public class RemovedMsg extends Message {
	String ChunkNo;
	private static final int N_ARGS = 4;

	/**
	 * Constructor that just fills the type of Message
	 */
	public RemovedMsg() {
		type = MESSAGE_TYPE.REMOVED;
	}

	/**
	 * Constructor for the Delete Msg that receives the messageFields and the
	 * messageFields: Version SenderID FileID ChunkNo body (Chunk data)
	 * 
	 * @param messageFields
	 * @param data
	 *            -> there shouldn't be any data in a delete message
	 */
	public RemovedMsg(String[] messageFields, byte[] data) {
		// type+args
		if (messageFields.length < (N_ARGS)) {
			System.out.println("Failed creating Remove message. Not enough fields");
			return;
		}
		version = messageFields[1];
		senderID = messageFields[2];
		fileId = messageFields[3];
		chunkNo = Integer.parseInt(messageFields[4]);
		body = null;
	}

	// REMOVED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
	/**
	 * creates a message with the data and the args but also validates the
	 * created message
	 */
	public boolean createMessage(byte[] data, String... args) {
		// Does not have a body
		if (data != null) {
			System.out.println("Mensagem nao valida");
			return false;
		}
		// Creates the message header
		createHeader(args, N_ARGS, getRemoved());

		// Regex that validates the message
		validateRegex = VALIDATE_MESSAGE_TYPE + MORE_THAN_1_SPACE + VALIDATE_VERSION + MORE_THAN_1_SPACE + MIDDLE_ARGS + MORE_THAN_1_SPACE + MIDDLE_ARGS + MORE_THAN_1_SPACE + CHUNK_NUMBER
				+ MSG_END_WITHOUT_BODY;

		return createMessageAux();
	}
}
