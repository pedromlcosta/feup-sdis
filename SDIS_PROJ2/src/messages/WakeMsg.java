package messages;

public class WakeMsg extends Message {

	private static int N_ARGS = 3;
	String ChunkNo;

	/**
	 * Constructor that just fills the type of Message
	 */
	public WakeMsg() {
		type = MESSAGE_TYPE.WAKEUP;
	}

	public WakeMsg(String[] messageFields, byte[] data) {
		if (messageFields.length < (N_ARGS)) {
			System.out.println("Failed creating WakeUp message. Not enough fields");
			return;
		}
		version = messageFields[1];
		senderID = messageFields[2];
		fileId = messageFields[3];
		body = null;
	}

	// WAKEUP <Version> <SenderId> <FileId> <CRLF><CRLF>
	/**
	 * creates a message with the data and the args but also validates the
	 * created message
	 */
	public boolean createMessage(byte[] data, String... args) {

		// Does not have a body
		if (data != null) {
			System.out.println("Mensagem nao valida, There is no body in WakeMsg");
			return false;
		}

		// Creates the message header
		createHeader(args, N_ARGS, getWakeup());
		// Regex that validates the message
		validateRegex = VALIDATE_MESSAGE_TYPE + MORE_THAN_1_SPACE + VALIDATE_VERSION + MORE_THAN_1_SPACE + MIDDLE_ARGS + MORE_THAN_1_SPACE + MIDDLE_ARGS + ZERO_OR_MORE_SPACES + MSG_END_WITHOUT_BODY;

		return createMessageAux();

	}

	public static int getnArgs() {
		return N_ARGS;
	}

}
