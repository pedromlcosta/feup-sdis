package messages;

public class DeleteMsg extends Message {

	private static final int N_ARGS = 3;

	/**
	 * Constructor that just fills the type of Message
	 */
	public DeleteMsg() {
		type = MESSAGE_TYPE.DELETE;
	}

	/**
	 * Constructor for the Delete Msg that receives the messageFields and the
	 * messageFields: Version SenderID FileID ChunkNo body (Chunk data)
	 * 
	 * @param messageFields
	 * @param data
	 *            -> there shouldn't be any data in a delete message
	 */
	public DeleteMsg(String[] messageFields, byte[] data) {
		// args+body
		if (messageFields.length < (N_ARGS)) {
			System.out.println("Failed creating Stored message. Not enough fields");
			return;
		}
		version = messageFields[1];
		senderID = messageFields[2];
		fileId = messageFields[3];
		body = null;
	}

	// DELETE <Version> <SenderId> <FileId> <CRLF><CRLF>
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
		createHeader(args, N_ARGS, getDelete());
		// Regex that validates the message
		validateRegex = VALIDATE_MESSAGE_TYPE + MORE_THAN_1_SPACE + VALIDATE_VERSION + MORE_THAN_1_SPACE + MIDDLE_ARGS + MORE_THAN_1_SPACE + MIDDLE_ARGS + MSG_END_WITHOUT_BODY;
		return createMessageAux();

	}

	public static int getnArgs() {
		return N_ARGS;
	}
	
}
