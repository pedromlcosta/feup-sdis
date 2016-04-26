package messages;

public class ChunkMsg extends Message {

	private static final int N_ARGS = 4;
	@SuppressWarnings("unused")
	private String ChunkNo;

	/**
	 * Constructor that just fills the type of Message
	 */
	public ChunkMsg() {
		type = MESSAGE_TYPE.CHUNK;
	}

	/**
	 * Constructor for the Chunk Msg that receives the messageFields and the
	 * messageFields: Version SenderID FileID ChunkNo body (Chunk data)
	 * 
	 * @param messageFields
	 * @param data
	 */
	public ChunkMsg(String[] messageFields, byte[] data) {
		// message args+type+body
		if (messageFields.length < (N_ARGS + 1)) {
			System.out.println("Failed creating Chunk message. Not enough fields");
			return;
		}
		version = messageFields[1];
		senderID = messageFields[2];
		fileId = messageFields[3];
		chunkNo = Integer.parseInt(messageFields[4]);
		body = data;
	}

	// CHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF><Body>
	/**
	 * creates a message with the data and the args but also validates the
	 * created message
	 */
	public boolean createMessage(byte[] data, String... args) {
		//Data must not be null,sending a chunk, we must have the its body
		if (data == null) {
			System.out.println("Missing body");
			return false;
		}
		this.body = data;
		// Creates the message header
		createHeader(args, N_ARGS, getChunk());
		// Regex that validates the message
		validateRegex = VALIDATE_MESSAGE_TYPE + MORE_THAN_1_SPACE + VALIDATE_VERSION + MORE_THAN_1_SPACE + MIDDLE_ARGS + MORE_THAN_1_SPACE + MIDDLE_ARGS + MORE_THAN_1_SPACE + CHUNK_NUMBER
				+ MSG_END_WITH_BODY;
		return createMessageAux();

	}
}
