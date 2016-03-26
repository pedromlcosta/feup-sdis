package messages;

public class DeleteMsg extends Message {

	private static final int N_ARGS = 3;

	public DeleteMsg() {
		type = MESSAGE_TYPE.DELETE;
	}

	public DeleteMsg(String[] messageFields) {
		// args+body
		if (messageFields.length < (N_ARGS + 1)) {
			System.out.println("Failed creating Stored message. Not enough fields");
			return;
		}
		version = messageFields[1];
		senderID = messageFields[2];
		fileId = messageFields[3];
	}

	// DELETE <Version> <SenderId> <FileId> <CRLF><CRLF>
	public boolean createMessage(byte[] data, String... args) {

		createHeader(args, N_ARGS, getDelete());
		validateRegex = VALIDATE_MESSAGE_TYPE + MORE_THAN_1_SPACE + VALIDATE_VERSION + MORE_THAN_1_SPACE + MIDDLE_ARGS + MORE_THAN_1_SPACE + MIDDLE_ARGS + MSG_END;
		return createMessageAux(data);

	}
}