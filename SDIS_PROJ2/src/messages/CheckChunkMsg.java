package messages;

public class CheckChunkMsg extends Message {

	private static int N_ARGS = 3;
	String ChunkNo;

	/**
	 * Constructor that just fills the type of Message
	 */
	public CheckChunkMsg() {
		setType(MESSAGE_TYPE.CHECK_CHUNK_NUMBER);
	}

	public CheckChunkMsg(String[] messageFields, byte[] data) {
		if (messageFields.length < (N_ARGS)) {
			System.out.println("Failed creating WakeUp message. Not enough fields");
			return;
		}
		setVersion(messageFields[1]);
		setSenderID(messageFields[2]);
		setFileId(messageFields[3]);
		setChunkNo(Integer.parseInt(messageFields[4]));
		setBody(null);
	}

	// Think:
	// Se um Peer receber o que tem que fazer? -> Peer vê se tem um chunk deste
	// FileID?
	// Se for o tracker faz o que? Tracker vê se o ficheiro foi deleted da rede?
	// se sim
	// CHECKCHUNK <Version> <SenderId> <FileId> <CRLF><CRLF>
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
		createHeader(args, N_ARGS, getCheckChunkNumber());
		// Regex that validates the message
		setValidateRegex(VALIDATE_MESSAGE_TYPE + MORE_THAN_1_SPACE + VALIDATE_VERSION + MORE_THAN_1_SPACE + MIDDLE_ARGS + MORE_THAN_1_SPACE + MIDDLE_ARGS + ZERO_OR_MORE_SPACES + MSG_END_WITHOUT_BODY);

		return createMessageAux();

	}

	public static int getnArgs() {
		return N_ARGS;
	}

}
